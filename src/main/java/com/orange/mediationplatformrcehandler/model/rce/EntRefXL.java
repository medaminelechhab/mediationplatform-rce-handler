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
public class EntRefXL {

    @CsvBindByPosition(position = 0)
    private String id;
    @CsvBindByPosition(position = 1)
    private String dReferencement;
    @CsvBindByPosition(position = 2)
    private String motifReferencement;
    @CsvBindByPosition(position = 3)
    private String dDReferencement;
    @CsvBindByPosition(position = 4)
    private String motifDeReferencement;
    @CsvBindByPosition(position = 5)
    private String codPaysSiege;
    @CsvBindByPosition(position = 6)
    private String enCoursImmat;
    @CsvBindByPosition(position = 7)
    private String modificationDate;
    @CsvBindByPosition(position = 8)
    private String statusInsee;
    @CsvBindByPosition(position = 9)
    private String siren;
    @CsvBindByPosition(position = 10)
    private String nomrs;
    @CsvBindByPosition(position = 11)
    private String sigle;
    @CsvBindByPosition(position = 12)
    private String civility;
    @CsvBindByPosition(position = 13)
    private String familyName;
    @CsvBindByPosition(position = 14)
    private String firstName;
    @CsvBindByPosition(position = 15)
    private String indPersphys;
    @CsvBindByPosition(position = 16)
    private String codeCatj;
    @CsvBindByPosition(position = 17)
    private String codNaf31;
    @CsvBindByPosition(position = 18)
    private String codNaf;
    @CsvBindByPosition(position = 19)
    private String dcren;
    @CsvBindByPosition(position = 20)
    private String dreacten;
    @CsvBindByPosition(position = 21)
    private String monthCreationEnt;
    @CsvBindByPosition(position = 22)
    private String yearCreationEnt;
    @CsvBindByPosition(position = 23)
    private String tefen;
    @CsvBindByPosition(position = 24)
    private String efencent;
    @CsvBindByPosition(position = 25)
    private String defen;
    @CsvBindByPosition(position = 26)
    private String recme;
    @CsvBindByPosition(position = 27)
    private String monoAct;
    @CsvBindByPosition(position = 28)
    private String regimp;
    @CsvBindByPosition(position = 29)
    private String monoreg;
    @CsvBindByPosition(position = 30)
    private String category;
    @CsvBindByPosition(position = 31)
    private String rna;
    @CsvBindByPosition(position = 32)
    private String tca;
    @CsvBindByPosition(position = 33)
    private String mailAddress;
    @CsvBindByPosition(position = 34)
    private String proden;
    @CsvBindByPosition(position = 35)
    private String ess;
    @CsvBindByPosition(position = 36)
    private String dateEss;
    @CsvBindByPosition(position = 37)
    private String aprm;

}
