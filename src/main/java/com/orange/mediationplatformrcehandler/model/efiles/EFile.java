package com.MyProject.mediationplatformrcehandler.model.efiles;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@SuperBuilder
@JsonIgnoreProperties(ignoreUnknown = true)
public class EFile {

    @JsonProperty("operationId")
    private String operationId;

    @JsonProperty("streamId")
    private long streamId;

    @JsonProperty("expirationDate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private OffsetDateTime expirationDate;

    @JsonProperty("creationDate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private OffsetDateTime creationDate;

    @JsonProperty("fileName")
    private String fileName;

    @JsonProperty("fileSize")
    private long fileSize;

    @JsonProperty("sender")
    private String sender;

    @JsonProperty("tags")
    private List<String> tags;
    @JsonProperty("recipients")
    private List<String> recipients;

    @JsonProperty("criticality")
    private String criticality;

}
