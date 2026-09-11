package com.MyProject.mediationplatformrcehandler.model.efiles;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.MyProject.mediationplatform.common.error.ApiCallException;
import lombok.*;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@SuperBuilder
@JsonIgnoreProperties(ignoreUnknown = true)
@Slf4j
public class EFilesErrorResponse{

    @JsonProperty("date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime date;

    @JsonProperty("code")
    private long code;

    @JsonProperty("message")
    private String message;

    @JsonProperty("description")
    private String description;

    @JsonProperty("validationErrors")
    private List<ValidationError> validationErrors;

    public static EFilesErrorResponse mapError(ApiCallException apiCallException) {
        ObjectMapper objectMapper = new ObjectMapper();
        EFilesErrorResponse errorResponse = new EFilesErrorResponse();
        try {
            errorResponse = objectMapper.readValue(apiCallException.getResponseBody(), EFilesErrorResponse.class);
        } catch (JsonProcessingException e) {
            log.info("error mapping to parse efiles error response");
        }

        return errorResponse;
    }

}
