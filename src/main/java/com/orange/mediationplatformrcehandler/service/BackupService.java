package com.MyProject.mediationplatformrcehandler.service;

import com.mongodb.client.FindIterable;
import com.MyProject.mediationplatform.common.error.ApiCallException;
import com.MyProject.mediationplatform.common.model.Token;
import com.MyProject.mediationplatformrcehandler.model.efiles.EFilesErrorResponse;
import com.MyProject.mediationplatformrcehandler.network.JWTAuthenticator;
import com.MyProject.mediationplatformrcehandler.network.JWTClientConfigurer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.MyProject.mediationplatformrcehandler.service.utils.Utils;
import com.MyProject.mediationplatformrcehandler.utils.Constants;


import com.MyProject.mediationplatformrcehandler.service.referential.RefRCEService;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
@Setter
public class BackupService {

  private final JWTClientConfigurer clientConfiguration;
  private final JWTAuthenticator jwtAuthenticator;

  @Value("${clinksplatform.tmp-storage-dir}")
  private String temporaryDir;

  @Value("#{'${backup.collections}'.replaceAll('\\s+', '').split(',')}")
  private List<String> collectionsToBackup;

  public void startBackupProcess() throws Exception {
    if(temporaryDir==null || temporaryDir.isEmpty()){
      throw new IllegalArgumentException("Beckup dir must be set in clinksplatform.tmp-storage-dir");
    }

    final String BACKUP_DIR = temporaryDir + "backup/";

    //Create Dir if not exists
    Files.createDirectories(Paths.get(BACKUP_DIR));

    final String BACKUP_FILE = temporaryDir + "rce_db_export_" + LocalDateTime.now().format(Constants.DF_YYYYMMDD) + ".tar";

    //Exporting DB collections
    exportAllCollectionsToCsv(BACKUP_DIR);

    //Compressing to archive
    compressToTar(BACKUP_DIR, BACKUP_FILE, true);

    //Sending to eFiles
    sendCsvFileToEFiles(Files.readAllBytes(Paths.get(BACKUP_FILE)), new File(BACKUP_FILE).getName());

    //Cleaning dirs
    Utils.cleanDir(temporaryDir);
  }

  private final MongoTemplate mongoTemplate;

  public void exportAllCollectionsToCsv(String outputDirectory) {
      Set<String> collectionNames = mongoTemplate.getCollectionNames();

      collectionNames.parallelStream().forEach(e -> {
        if(collectionsToBackup.contains(e)) exportCollectionToCsv(e, outputDirectory);
      });
  }

  private void exportCollectionToCsv(String collectionName, String outputDirectory) {
      FindIterable<Document> documents = mongoTemplate.getCollection(collectionName).find();

      String outputFile = outputDirectory + collectionName + ".csv";

      try (FileWriter writer = new FileWriter(outputFile)) {
          // Write header
          Document firstDocument = documents.first();
          if (firstDocument != null) {
              writer.append(String.join(",", firstDocument.keySet())).append("\n");

              // Write data
              Iterator<Document> iterator = documents.iterator();
              while (iterator.hasNext()) {
                  Document document = iterator.next();
                  writer.append(convertToCsvRow(document)).append("\n");
              }

              log.info("Exported {} to {}", collectionName, outputFile);
          }
      } catch (IOException e) {
          log.error("Exception occured {}", e.getMessage());
      }
    }
    private String convertToCsvRow(Map<String, Object> document) {
        return document.values().stream()
                .map(value -> value != null ? value.toString() : "")
                .collect(Collectors.joining(","));
    }

  public static void compressToTar(String sourceDirectory, String tarFilePath, boolean cleanFiles) throws IOException {
    try (TarArchiveOutputStream tarOut = new TarArchiveOutputStream(new FileOutputStream(tarFilePath))) {
        // Set up the compression method (optional)
        // tarOut.setMethod(TarConstants.LZMA);

        addFilesToTar(tarOut, sourceDirectory, "");
    }

    if(cleanFiles)
      Utils.cleanDir(sourceDirectory);
  }

  private static File addFilesToTar(TarArchiveOutputStream tarOut, String path, String base) throws IOException {
      File file = new File(path);
      String entryName = base + file.getName();

      TarArchiveEntry tarEntry = new TarArchiveEntry(file, entryName);
      
      if (file.isFile()) {

        tarEntry.setSize(file.length());
        tarOut.putArchiveEntry(tarEntry);

        try (FileInputStream in = new FileInputStream(file)) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) != -1) {
                tarOut.write(buffer, 0, len);
            }
        }
        tarOut.closeArchiveEntry();
      } else if (file.isDirectory()) {

        tarEntry.setSize(0); // Directories have size 0
        tarOut.putArchiveEntry(tarEntry);
        tarOut.closeArchiveEntry();

        for (File child : file.listFiles()) {
            addFilesToTar(tarOut, child.getAbsolutePath(), entryName + "/");
        }
      }
      log.info("Compressed file generated in {}", file.getPath());
      return file;
  }

  /**
   * @param fileContent CSV file to send to EFiles
   */
  public String sendCsvFileToEFiles(byte[] fileContent, String fileName) {
    log.info("Attempting to send backup file to EFiles");
    return jwtAuthenticator
        .authenticate()
        .flatMap(
            token -> {
              log.info("jwt token : {}", token);
              return clientConfiguration
                  .buildWebClient()
                  .post()
                  .uri(clientConfiguration.buildUri("/files", Collections.emptyMap()))
                  .headers(getAuthJWTHeaders(token, fileName))
                  .body(BodyInserters.fromValue(fileContent))
                  .retrieve()
                  .bodyToMono(String.class)
                  .doOnError(ApiCallException.class, e -> {
                        log.error("Error occurred while sending csv file to EFiles - [{}]", e.getMessage());
                        Mono.just(EFilesErrorResponse.mapError(e));
                      });
            })
        .block();
  }

  /**
   * @param token ODI
   * @return httpHeaders with values
   */
  private Consumer<HttpHeaders> getAuthJWTHeaders(Token token, String fileName) {
    return httpHeaders -> {
      httpHeaders.setBearerAuth(token.getAccessToken());
      httpHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM.toString());
      httpHeaders.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON.toString());
      httpHeaders.add(HttpHeaders.CACHE_CONTROL, "no-cache");
      httpHeaders.add("retention", "3");
      httpHeaders.add("tags", "backup");
      httpHeaders.add("filename", fileName);
    };
  }

}