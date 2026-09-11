package com.MyProject.mediationplatformrcehandler.model.database;

import com.opencsv.bean.CsvBindByPosition;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.MyProject.mediationplatformrcehandler.utils.Constants;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = Constants.ENT_REFERENCE_NEW)
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class EntRefXl2Data extends AuditMetadata {

    @Field("idEnt")
    private String id;

    private String dReferencement;

    private String motifReferencement;

    private String dDReferencement;
    
    private String motifDeReferencement;
    
    private String codPaysSiege;
    
    private String enCoursImmat;
    
    private String modificationDate;
    
    private String statusInsee;
    
    private String siren;
    
    private String nomrs;
    
    private String sigle;
    
    private String civility;
    
    private String familyName;
    
    private String firstName;
    
    private String indPersphys;
    
    private String codeCatj;
    
    private String codNaf31;
    
    private String codNaf;
    
    private String dcren;
    
    private String dreacten;
    
    private String monthCreationEnt;
    
    private String yearCreationEnt;
    
    private String tefen;
    
    private String efencent;
    
    private String defen;
    
    private String recme;
    
    private String monoAct;
    
    private String regimp;
    
    private String monoreg;
    
    private String category;
    
    private String rna;
    
    private String tca;
    
    private String mailAddress;
    
    private String proden;
    
    private String ess;
    
    private String dateEss;
    
    private String aprm;
}