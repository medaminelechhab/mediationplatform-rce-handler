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
public class EntMeOrg {

    private String id;
    @CsvBindByPosition(position = 0)
    private String idEnt;
    @CsvBindByPosition(position = 1)
    private String codeSegmentCCial;
    @CsvBindByPosition(position = 2)
    private String dattachement;
    @CsvBindByPosition(position = 3)
    private String motifattachement;
    @CsvBindByPosition(position = 4)
    private String dDetachement;
    @CsvBindByPosition(position = 5)
    private String motifDetachement;
    @CsvBindByPosition(position = 6)
    private String codeEDSVente;
    @CsvBindByPosition(position = 7)
    private String aliasEDSVente;
    @CsvBindByPosition(position = 8)
    private String modificationDate;
}
