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
public class GroupCCIALOrg {

    @CsvBindByPosition(position = 0)
    private String idGroup;
    @CsvBindByPosition(position = 1)
    private String nameGroup;
    @CsvBindByPosition(position = 2)
    private String codeEDSVente;
    @CsvBindByPosition(position = 3)
    private String aliasEDSVente;
    @CsvBindByPosition(position = 4)
    private String idEntMm;
    @CsvBindByPosition(position = 5)
    private String modificationDate;
    @CsvBindByPosition(position = 6)
    private String creationDate;
    @CsvBindByPosition(position = 7)
    private String deletionDate;
}
