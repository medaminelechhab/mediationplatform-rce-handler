package com.MyProject.mediationplatformrcehandler.utils;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Log4j2
@ActiveProfiles({"test", "db", "account", "rce", "clinks", "tx"})
class TarUtilsTest {

    @Value("${clinksplatform.tmp-storage-dir}")
    private String tmpDirectory;

    @BeforeEach
    void createTmpDirectory() {
        File tmpDir = new File(tmpDirectory);
        if (!tmpDir.exists()) {
            tmpDir.mkdirs();
        }
        log.info("Working directory is {}", tmpDirectory);
    }

    @AfterEach
    void cleanTmpDirectory() throws IOException {
        Files.list(Path.of(tmpDirectory)).forEach((p) -> {
            try {
                Files.deleteIfExists(p);
                log.info("files deleted");
            } catch (Exception e) {
                log.error("cannot clean directory {}", e.getMessage());
            }
        });
    }

    @Test
    @DisplayName("when launch job , must call tasklet to get file and extracted")
    void unTarFile() throws Exception { // todo rename
        File file = new File("src/test/resources/rce/data-rce.tgz");

        List<File> files = TarUtils.unTar(file, new File(tmpDirectory));
        assertEquals(11, files.size());
    }
}
