package com.MyProject.mediationplatformrcehandler.service.utils;

import com.opencsv.CSVWriter;
import com.MyProject.mediationplatformrcehandler.model.ErrorDTO;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@Slf4j
public class ErrorHandler {

    private static final List<ErrorDTO> list = new CopyOnWriteArrayList<>();

    public void reset() {
        list.clear();
    }

    public void add(ErrorDTO error) {
        Objects.requireNonNull(error);
        list.add(error);
    }

    public List<ErrorDTO> getErrors() {
        return list;
    }

    public File createCSV(String filePath, String filename) {
        log.info("Creating file {} in {}", filename, filePath);

        try {
            File file = new File(filePath.concat(filename));
            FileWriter fileWriter = new FileWriter(file);
            CSVWriter csvWriter = new CSVWriter(fileWriter,
                    ',',
                    CSVWriter.NO_QUOTE_CHARACTER,
                    CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                    CSVWriter.DEFAULT_LINE_END);

            // Write the CSV header line
            String[] header = {"RCE ID", "Code erreur", "Message erreur"};
            csvWriter.writeNext(header);

            // Write the CSV data lines
            for (ErrorDTO error : list) {
                String[] data = {
                        error.getRceId().concat("_").concat(error.getType().name()),
                        error.getErrorCode(),
                        error.getErrorMessage()
                };
                csvWriter.writeNext(data);
            }

            csvWriter.close();
            log.info("Created !");
            return file;
        } catch (Exception e) {
            log.error("Exception occured", e.getMessage());
        }
        return null;
    }
}
