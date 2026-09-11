package com.MyProject.mediationplatformrcehandler.service;

import com.MyProject.mediationplatformrcehandler.model.ErrorDTO;
import com.MyProject.mediationplatformrcehandler.service.utils.ErrorHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static com.MyProject.mediationplatformrcehandler.model.ErrorDTO.AccountType;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles({"test", "db", "account", "rce", "clinks", "tx"})
class ErrorHandlerTest {

    @Autowired
    private ErrorHandler errorHandler;

    @BeforeEach
    void cleanList() throws IOException {
        errorHandler.reset();
    }

    @Test
    void verifyTheListOfErrors() {
        errorHandler.add(createError("rice1", AccountType.GROUPE, "code1", "this is message 1"));
        errorHandler.add(createError("rice2", AccountType.ENTREPRISE, "code2", "this is message 3"));
        errorHandler.add(createError("rice3", AccountType.ETABLISSEMENT, "code2", "this is message 3"));

        List<ErrorDTO> errors = errorHandler.getErrors();

        assertEquals(3, errors.size());
    }

    @Test
    void verifyTheCleaningOfList() {
        errorHandler.add(createError("rice1", AccountType.GROUPE, "code1", "this is message 1"));
        errorHandler.add(createError("rice2", AccountType.ENTREPRISE, "code2", "this is message 3"));
        errorHandler.add(createError("rice3", AccountType.ETABLISSEMENT, "code2", "this is message 3"));

        List<ErrorDTO> errors = errorHandler.getErrors();

        assertEquals(3, errors.size());

        errorHandler.reset();

        assertEquals(0, errors.size());
    }

    @Test
    void verifyCSVFile() throws IOException {
        // Create a sample list of Product objects
        List<ErrorDTO> errors = new ArrayList<>();
        errors.add(createError("rice1", AccountType.GROUPE, "code1", "this is message 1"));
        errors.add(createError("rice2", AccountType.ENTREPRISE, "code2", "this is message 2"));
        errors.add(createError("rice3", AccountType.ETABLISSEMENT, "code3", "this is message 3"));

        // Specify the file path for the CSV file
        String filePath = "src/test/resources/";

        errors.forEach(errorHandler::add);

        // Invoke the writeCSV method
        File csvFile = errorHandler.createCSV(filePath, "test.csv");

        // Verify the CSV file is created
        Assertions.assertTrue(csvFile.exists());

        // Verify the content of the CSV file
        List<String> lines = Files.readAllLines(Path.of(filePath.concat("test.csv")));
        Assertions.assertEquals(4, lines.size());
        Assertions.assertEquals("rice1_GROUPE,code1,this is message 1", lines.get(1));
        Assertions.assertEquals("rice2_ENTREPRISE,code2,this is message 2", lines.get(2));
        Assertions.assertEquals("rice3_ETABLISSEMENT,code3,this is message 3", lines.get(3));

        // Delete the CSV file
//        csvFile.delete();
    }

    private ErrorDTO createError(String riceId, AccountType type, String code, String message) {
        ErrorDTO error = new ErrorDTO();
        error.setRceId(riceId);
        error.setType(type);
        error.setErrorCode(code);
        error.setErrorMessage(message);
        return error;
    }

}
