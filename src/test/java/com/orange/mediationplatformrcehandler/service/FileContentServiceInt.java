package com.MyProject.mediationplatformrcehandler.service;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.MyProject.mediationplatformrcehandler.service.efiles.FileContentService;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles({"test", "db", "account", "rce", "clinks", "tx"})
class FileContentServiceInt {

    @Autowired
    private FileContentService fileContentService;

    @Test
    @Disabled
    @DisplayName("get file")
    void getFileContent() {

        var fileContent = fileContentService.getContentFile("5dd1e8df-bdc9-409f-9bf0-88bbb94091be").block();
        assertNotNull(fileContent);
    }
}
