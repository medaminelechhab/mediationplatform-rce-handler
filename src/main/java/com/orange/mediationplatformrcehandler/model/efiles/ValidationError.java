package com.MyProject.mediationplatformrcehandler.model.efiles;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@SuperBuilder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ValidationError {

    @JsonProperty("message")
    private String message;

    @JsonProperty("description")
    private String description;

    @JsonProperty("rejectedValue")
    private Object rejectedValue;
}
