package com.MyProject.mediationplatformrcehandler.service.utils;

import com.MyProject.mediationplatformrcehandler.model.rce.GroupCCIALConstit;
import com.MyProject.mediationplatformrcehandler.model.rce.GroupCCIALOrg;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.List;

@SpringBootTest
@ActiveProfiles({"test", "db", "account", "rce", "clinks", "tx"})
class FileReaderTest {

    @Test
    @DisplayName("Verify that we cannot instantiate an utility class")
    void givenFileReaderUtility_WhenConstructorCalledExceptionThrown() throws NoSuchMethodException {

        Constructor<FileReader> constructor = FileReader.class.getDeclaredConstructor();
        Assertions.assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);

        Assertions.assertThrows(Exception.class, constructor::newInstance);
    }


    @Test
    @DisplayName("Verify if a given csv file is parsed successfully")
    void givenAppropriateFile_WhenParseIsFiredObjectsReturned() throws IOException {

        List<GroupCCIALOrg> groupCCIALOrgs = FileReader.parseFile("src/test/resources/rce/full/GPE-CCIAL-ORG.txt", GroupCCIALOrg.class);
        Assertions.assertFalse(groupCCIALOrgs.isEmpty());
    }

    @Test
    @DisplayName("Verify the case when file not found")
    void givenUndefinedFile_WhenParseIsFiredExceptionThrown() {

        Throwable exception = Assertions.assertThrows(FileNotFoundException.class, () ->
                FileReader.parseFile("ENT-ECO-NEW_20230822.txt", GroupCCIALOrg.class));
        MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsString("ENT-ECO-NEW_20230822.txt"));
    }

    @Test
    @DisplayName("Verify the case when parse and filter called with unsupported class")
    void givenAppropriateFile_WhenParseWithFilterIsFiredExceptionThrown() {

        Throwable exception = Assertions.assertThrows(IllegalArgumentException.class, () ->
                FileReader.parseFile("src/test/resources/rce/full/GPE-CCIAL-ORG.txt", FilesManagement.TypeManagement.SUNDAY, GroupCCIALConstit.class));
        MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsString("Unexpected business object"));
    }
}
