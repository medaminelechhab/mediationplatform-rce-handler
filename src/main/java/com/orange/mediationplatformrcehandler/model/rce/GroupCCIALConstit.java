package com.MyProject.mediationplatformrcehandler.model.rce;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.opencsv.bean.CsvBindByPosition;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class GroupCCIALConstit {

    @CsvBindByPosition(position = 0)
    private String idGroup;
    @CsvBindByPosition(position = 1)
    private String modificationDate;
    @CsvBindByPosition(position = 2)
    private String idENT;
}
