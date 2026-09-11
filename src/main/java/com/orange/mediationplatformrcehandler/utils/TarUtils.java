package com.MyProject.mediationplatformrcehandler.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

@Slf4j
public class TarUtils {

    /**
     * @param inputFile file to untaring
     * @param outputDir output directory
     * @return List<File>
     * @throws IOException
     * @throws ArchiveException
     */
    public static List<File> unTar(final File inputFile, final File outputDir) throws IOException, ArchiveException {

        log.info(String.format("Untaring %s to dir %s.", inputFile.getAbsolutePath(), outputDir.getAbsolutePath()));

        final List<File> untaredFiles = new LinkedList<>();
        final TarArchiveInputStream debInputStream = new TarArchiveInputStream(
                new GzipCompressorInputStream(new FileInputStream(inputFile)));
        TarArchiveEntry entry = null;
        while ((entry = (TarArchiveEntry) debInputStream.getNextEntry()) != null) {
            final File outputFile = new File(outputDir, entry.getName());
            if (entry.isDirectory()) {
                log.info(String.format("Attempting to write output directory %s.", outputFile.getAbsolutePath()));
                if (!outputFile.exists()) {
                    log.info(String.format("Attempting to create output directory %s.", outputFile.getAbsolutePath()));
                    if (!outputFile.mkdirs()) {
                        throw new IllegalStateException(String.format("Couldn't create directory %s.", outputFile.getAbsolutePath()));
                    }
                }
            } else {
                log.info(String.format("Creating output file %s.", outputFile.getAbsolutePath()));
                final OutputStream outputFileStream = new FileOutputStream(outputFile);
                IOUtils.copy(debInputStream, outputFileStream);
                outputFileStream.close();
            }
            untaredFiles.add(outputFile);
        }
        debInputStream.close();

        return untaredFiles;
    }
}
