package com.MyProject.mediationplatformrcehandler.utils;

import com.MyProject.mediationplatformrcehandler.model.efiles.EFilesErrorResponse;
import com.MyProject.mediationplatformrcehandler.model.efiles.EFile;
import com.MyProject.mediationplatformrcehandler.model.efiles.ValidationError;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

public class FileFactory {

    public static List<EFile> fileList() {

        return List.of(EFile.builder()
                .operationId("3fa85f64-5717-4562-b3fc-2c963f66afa6")
                .streamId(1452)
                .creationDate(OffsetDateTime.of(LocalDateTime.of(2023, 06, 13, 9, 42, 34,
                        147 * 1000000), ZoneOffset.UTC))
                .fileSize(25)
                .expirationDate(OffsetDateTime.of(LocalDateTime.of(2023, 06, 13, 9, 42, 34,
                        147 * 1000000), ZoneOffset.UTC))
                .fileName("ENT-ECO-NEW_20230326")
                .recipients(List.of("owner"))
                .tags(List.of("tests"))
                .criticality("PUBLIC")
                .build());
    }

    public static EFilesErrorResponse errorResponse(){

        ValidationError validationError = ValidationError.builder()
                .message("erreur validation")
                .description("erreur validation")
                .rejectedValue(null)
                .build();

        return EFilesErrorResponse.builder()
                .date(LocalDateTime.of(2023, 06, 13, 9, 42, 34,
                        147 * 1000000))
                .code(41)
                .message("erreur")
                .description("error")
                .validationErrors(List.of(validationError))
                .build();
    }
}
