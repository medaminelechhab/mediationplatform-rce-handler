package com.MyProject.mediationplatformrcehandler.service.utils;

import com.opencsv.CSVReader;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.MyProject.mediationplatformrcehandler.model.rce.EntRefXL;
import com.MyProject.mediationplatformrcehandler.model.rce.EtaRefXL2;
import com.MyProject.mediationplatformrcehandler.model.rce.GroupCCIALOrg;
import com.MyProject.mediationplatformrcehandler.utils.Constants;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Slf4j
public class FileReader {

    private static final char DELIMITER = '|';
    public static final int SEVEN_DAYS = 7;

    private FileReader() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static <T> List<T> parseFile(String filePath, Class<T> clazz) throws IOException {
        try (java.io.FileReader fileReader = new java.io.FileReader(filePath)) {
            //Calculating size of file
            File file = new File(filePath); 
            double fileSizeInMB = (double) file.length() / (1024 * 1024);
            log.info("File : " + file  + " is of size: " + new DecimalFormat("#0." + "0".repeat(2)).format(fileSizeInMB) + " MB");

            //Parsing file
            CsvToBean<T> csvToBean = new CsvToBeanBuilder<T>(fileReader)
                    .withType(clazz)
                    .withSeparator(DELIMITER)
                    .withIgnoreQuotations(true)
                    .withSkipLines(0)
                    .build();
            return csvToBean.parse();
        }
    }

    public static <T> List<?> parseBigFile(String filePath, Class<T> clazz) throws IOException {
        try (BufferedReader reader = new BufferedReader(new java.io.FileReader(filePath))) {
            //Calculating size of file
            File file = new File(filePath);
            double fileSizeInMB = (double) file.length() / (1024 * 1024);
            log.info("File : " + file  + " is of size: " + new DecimalFormat("#0." + "0".repeat(2)).format(fileSizeInMB) + " MB");

            //Parsing file
            CsvToBean<?> csvToBean = new CsvToBeanBuilder<>(reader)
                    .withType(clazz)
                    .withSeparator(DELIMITER)
                    .withIgnoreQuotations(true)
                    .withIgnoreLeadingWhiteSpace(true)
                    .withSkipLines(0)
                    .build();

            // Iterate over the beans one by one
            Iterator<?> iterator = csvToBean.iterator();

            var list = new ArrayList<>();
            while (iterator.hasNext()) {
                var record = iterator.next();
                // Process each record as it's read, minimizing memory usage
                list.add(record);
            }

            return list;
        }
    }

    public static <T> List<T> parseFile(String filePath, FilesManagement.TypeManagement typeManagement, Class<T> clazz) throws IOException {
        return filterByMode(parseFile(filePath, clazz), typeManagement);
    }

    public static <T> List<T> filterByMode(List<T> list, FilesManagement.TypeManagement typeManagement) {
        return list.stream()
                .filter(obj -> applyFilter(typeManagement, obj))
                .toList();
    }

    private static <T> boolean applyFilter(FilesManagement.TypeManagement typeManagement, T obj) {
        String modificationDate;

        if (obj instanceof GroupCCIALOrg group) {
            modificationDate = group.getModificationDate();
        } else if (obj instanceof EntRefXL ent) {
            modificationDate = ent.getModificationDate();
        } else if (obj instanceof EtaRefXL2 eta) {
            modificationDate = eta.getModificationDate();
        } else {
            throw new IllegalArgumentException("Unexpected business object, must be of any type in : GroupCCIALOrg|EntReferenceNew|EtaReferenceNew");
        }

        return switch (typeManagement) {
            case DAILY -> LocalDate.parse(modificationDate, Constants.DF_YYYYMMDD).isEqual(LocalDate.now());
            case SUNDAY -> LocalDate.parse(modificationDate, Constants.DF_YYYYMMDD).isAfter(LocalDate.now().minusDays(SEVEN_DAYS));
        };
    }
}
