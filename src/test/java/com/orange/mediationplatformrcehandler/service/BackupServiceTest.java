package com.MyProject.mediationplatformrcehandler.service;

import static com.MyProject.mediationplatform.common.network.CacheProvider.COMPONENT.EFILES;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.MyProject.mediationplatform.common.model.Token;
import com.MyProject.mediationplatform.common.network.CacheProvider;
import com.MyProject.mediationplatform.common.network.NetworkConfigurer;
import com.MyProject.mediationplatform.common.network.RetryPolicy;
import com.MyProject.mediationplatformrcehandler.model.rce.RefRCE;
import com.MyProject.mediationplatformrcehandler.network.JWTAuthenticator;
import com.MyProject.mediationplatformrcehandler.network.JWTClientConfigurer;
import com.MyProject.mediationplatformrcehandler.network.JWTParameters;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

import com.MyProject.mediationplatformrcehandler.service.referential.RefRCEService;
import com.MyProject.mediationplatformrcehandler.service.utils.Utils;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import org.bson.Document;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;
import reactor.netty.tcp.TcpClient;

@SpringBootTest
@ActiveProfiles({"test", "db", "account", "rce", "clinks", "tx"})
class BackupServiceTest {

  @InjectMocks 
  private BackupService backupService;
  
  private static MockWebServer mockBackEnd;
  @Mock
  private JWTParameters jwtParameters;
  @Mock
  private MongoTemplate mongoTemplate;
  @Mock
  private JWTAuthenticator authenticator;
  @Mock
  private NetworkConfigurer networkConfigurer;
  @Spy
  private RetryPolicy retryPolicy;
  @Mock
  RefRCEService refRCEService;
  @Mock
  JWTParameters mockParameters;
  private CacheProvider cacheProvider;
  private Token tokenJWT;
  
  @Value("${clinksplatform.tmp-storage-dir}")
  private String tmpDirectory;
  @Value("#{'${backup.collections}'.replaceAll('\\s+', '').split(',')}")
  private List<String> collectionsToBackup;

  RefRCE refRCE;

  @BeforeAll
  static void setUp() throws IOException {
    mockBackEnd = new MockWebServer();
    mockBackEnd.start();
  }

  @BeforeEach
  void initialize() {
    String baseUrl = String.format("http://localhost:%s", mockBackEnd.getPort());
     mockParameters = jwtParameters;
    lenient().when(mockParameters.getBaseUrl()).thenReturn(baseUrl);
    cacheProvider = new CacheProvider();
    JWTClientConfigurer jwtClientConfigurer = new JWTClientConfigurer(mockParameters, authenticator, networkConfigurer, retryPolicy, cacheProvider);
    ReflectionTestUtils.setField(jwtClientConfigurer, "component", CacheProvider.COMPONENT.EFILES);
    backupService = new BackupService(jwtClientConfigurer, authenticator, mongoTemplate);

    tokenJWT = new Token(
        "FAKE_TOKEN",
        "type",
        "scope",
        1800,
        LocalDateTime.now()
            ,"appName"
    );

    refRCE = RefRCE.builder()
        .referenceName("TEST_REFERENCE_NAME")
        .code("TEST_CODE")
        .label("TEST_LABEL")
        .modificationDate("20231201")
        .build();

    createTmpDirectory();
  }

  @AfterEach
  void cleanTmpDirectory() throws IOException {
      Utils.cleanDir(tmpDirectory);
  }

  @AfterAll
  static void tearDown() throws IOException {
    mockBackEnd.shutdown();
  }

  @Test
  @DisplayName("When start backup process, must be OK")
  @Order(1)
  void startBackupProcessOK() throws Exception {
    // Given
    mockBackEnd.enqueue(new MockResponse().setResponseCode(200));

    when(networkConfigurer.tcp(anyBoolean(), anyBoolean())).thenReturn(TcpClient.newConnection());
    when(authenticator.authenticate()).thenReturn(Mono.just(tokenJWT));
    cacheProvider.setToken(EFILES, tokenJWT);

     Document document1 = new Document("key1", "value1");
     backupService.setCollectionsToBackup(collectionsToBackup);
     backupService.setTemporaryDir(tmpDirectory);
     MongoCollection<Document> mockedCollection = mock(MongoCollection.class);
 
     when(mongoTemplate.getCollectionNames()).thenReturn(new HashSet<>(collectionsToBackup));
     when(mongoTemplate.getCollection(anyString())).thenReturn(mockedCollection);
     FindIterable<Document> mockedFindIterable = mock(FindIterable.class);
     
     when(mockedCollection.find()).thenReturn(mockedFindIterable);
 
     MongoCursor<Document> mongoCursor = mock(MongoCursor.class);
     when(mongoCursor.hasNext()).thenReturn(true, true, false); // Simulate hasNext() behavior
     when(mongoCursor.next()).thenReturn(document1);
     when(mockedFindIterable.iterator()).thenReturn(mongoCursor);
     when(mockedFindIterable.first()).thenReturn(document1);
 
 
     // When
     backupService.startBackupProcess();
 
     RecordedRequest request = mockBackEnd.takeRequest();

     
     assertEquals("POST", request.getMethod());
     assertNotNull(request.getPath());
     assertTrue(request.getPath().contains("/files"));
     verify(authenticator, times(1)).authenticate();

  }

  @Test
  @DisplayName("When start backup process, must build csv file with referencesRCE")
  @Disabled
  void exportAllCollectionsToCsv() throws Exception {
    // Given
    Document document1 = new Document("key1", "value1");
    backupService.setCollectionsToBackup(collectionsToBackup);
    MongoCollection<Document> mockedCollection = mock(MongoCollection.class);

    when(mongoTemplate.getCollectionNames()).thenReturn(new HashSet<>(collectionsToBackup));
    when(mongoTemplate.getCollection(anyString())).thenReturn(mockedCollection);
    FindIterable<Document> mockedFindIterable = mock(FindIterable.class);
    
    when(mockedCollection.find()).thenReturn(mockedFindIterable);

    MongoCursor<Document> mongoCursor = mock(MongoCursor.class);
    when(mongoCursor.hasNext()).thenReturn(true, true, false); // Simulate hasNext() behavior
    when(mongoCursor.next()).thenReturn(document1);
    when(mongoCursor.next()).thenReturn(document1);
    when(mongoCursor.next()).thenReturn(document1);
    when(mockedFindIterable.iterator()).thenReturn(mongoCursor);
    when(mockedFindIterable.first()).thenReturn(document1);
    when(mockedFindIterable.first()).thenReturn(document1);


    // When
    backupService.exportAllCollectionsToCsv(tmpDirectory);

    for (var col : collectionsToBackup) {
      verify(mongoTemplate).getCollection(col);
      try (BufferedReader reader = new BufferedReader(new FileReader(tmpDirectory+ col + ".csv"))) {
        // Skip the first line (header)
        reader.readLine();
        // Then
        assertNotNull(reader.readLine());
      }    
    }
  }

  @Test
  @DisplayName("Verify e-files call  to send csv file content ")
  @Order(3)
  void sendCsvFileToEFiles() throws Exception {
    // Given
    mockBackEnd.enqueue(new MockResponse().setResponseCode(200));

    when(networkConfigurer.tcp(anyBoolean(), anyBoolean())).thenReturn(TcpClient.newConnection());
    when(authenticator.authenticate()).thenReturn(Mono.just(tokenJWT));
    cacheProvider.setToken(EFILES, tokenJWT);

    // When
    backupService.sendCsvFileToEFiles(new byte[0], "dummy");
    RecordedRequest request = mockBackEnd.takeRequest();

    // Then
    assertEquals("POST", request.getMethod());
    assertNotNull(request.getPath());
    assertTrue(request.getPath().contains("/files"));
    verify(authenticator, times(1)).authenticate();
  }

  void createTmpDirectory() {
    File tmpDir = new File(tmpDirectory);
    if (!tmpDir.exists()){
      tmpDir.mkdirs();
    }
  }
}