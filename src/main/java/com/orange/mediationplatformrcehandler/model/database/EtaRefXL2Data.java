package com.MyProject.mediationplatformrcehandler.model.database;

import com.opencsv.bean.CsvBindByPosition;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.MyProject.mediationplatformrcehandler.utils.Constants;

@Document(collection = Constants.ETA_REFERENCE_NEW)
@Builder(toBuilder = true)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EtaRefXL2Data extends AuditMetadata {

    private String idENT;

    private String idETA;

    private String dReferencement;
    
    private String motifReferencement;
    
    private String dDReferencement;
    
    private String motifDeReferencement;
    
    private String modificationDate;
    
    private String statutINSEE;
    
    private String siren;

    private String nic;

    private String siege;
    
    private String enseigne;
    
    private String numStreet;
    
    private String indrep;
    
    private String typeStreet;
    
    private String labelStreet;

    private String postalCode;

    private String cedex;
    
    private String streetCode;

    private String libcom;

    private String countryCode;

    private String rpet;
    
    private String depet;
    
    private String comet;
    
    private String arronet;
    
    private String ctonet;
    
    private String du;
    
    private String tu;
    
    private String uu;
    
    private String epci;
    
    private String tdc;
    
    private String zemet_xl2;
    
    private String l1Normalisee;
    
    private String l2Normalisee;
    
    private String l3Normalisee;
    
    private String l4Normalisee;
    
    private String l5Normalisee;
    
    private String l6Normalisee;
    
    private String l7Normalisee;
    
    private String codeNAF31;
    
    private String codeNaf;
    
    private String tefet;
    
    private String efetcent;
    
    private String defet;
    
    private String originCode;
    
    private String dcret;
    
    private String activNat;
    
    private String dreatet;
    
    private String lieuAct;
    
    private String saisonNat;

    
    private String AUXILT;
    
    private String IND_PUBLIPO;
    
    private String DIFFCOM;
    
    private String AMINTRET;
    
    private String ACTISURF;
    
    private String MODET;
    
    private String PRODET;
    
    private String PRODPART;
    
    private String SIRETPS;
    
    private String TEL;
    
    private String  EVE;
}
