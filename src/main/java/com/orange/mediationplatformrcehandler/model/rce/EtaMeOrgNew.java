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
public class EtaMeOrgNew {

    @CsvBindByPosition(position = 0)
    private String id;
    @CsvBindByPosition(position = 1)
    private String codeDGDEC;
    @CsvBindByPosition(position = 2)
    private String codeAGGEO;
    @CsvBindByPosition(position = 3)
    private String codeEDSVente;
    @CsvBindByPosition(position = 4)
    private String aliasEDSVente;
    @CsvBindByPosition(position = 5)
    private String dattachment;
    @CsvBindByPosition(position = 6)
    private String motifAttachement;
    @CsvBindByPosition(position = 7)
    private String dDattachment;
    @CsvBindByPosition(position = 8)
    private String motifDetachment;
    @CsvBindByPosition(position = 9)
    private String modificationDate;
    @CsvBindByPosition(position = 10)
    private String idETA;
}
