package com.MyProject.mediationplatformrcehandler.model.rce;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.opencsv.bean.CsvBindByPosition;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"referenceName", "code"})
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class RefRCE {

    @CsvBindByPosition(position = 0)
    private String referenceName;
    @CsvBindByPosition(position = 1)
    private String code;
    @CsvBindByPosition(position = 2)
    private String label;
    @CsvBindByPosition(position = 3)
    private String modificationDate;
    @CsvBindByPosition(position = 4)
    private String deletionDate;
}
