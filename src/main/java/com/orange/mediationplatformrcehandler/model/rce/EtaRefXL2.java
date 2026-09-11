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
public class EtaRefXL2 {

    @CsvBindByPosition(position = 0)
    private String idENT;
    @CsvBindByPosition(position = 1)
    private String idETA;
    @CsvBindByPosition(position = 2)
    private String dReferencement;
    @CsvBindByPosition(position = 3)
    private String motifReferencement;
    @CsvBindByPosition(position = 4)
    private String dDReferencement;
    @CsvBindByPosition(position = 5)
    private String motifDeReferencement;
    @CsvBindByPosition(position = 6)
    private String modificationDate;
    @CsvBindByPosition(position = 7)
    private String statutINSEE;
    @CsvBindByPosition(position = 8)
    private String siren;
    @CsvBindByPosition(position = 9)
    private String nic;
    @CsvBindByPosition(position = 10)
    private String siege;
    @CsvBindByPosition(position = 11)
    private String enseigne;
    @CsvBindByPosition(position = 12)
    private String numStreet;
    @CsvBindByPosition(position = 13)
    private String indrep;
    @CsvBindByPosition(position = 14)
    private String typeStreet;
    @CsvBindByPosition(position = 15)
    private String labelStreet;
    @CsvBindByPosition(position = 16)
    private String postalCode;
    @CsvBindByPosition(position = 17)
    private String cedex;
    @CsvBindByPosition(position = 18)
    private String streetCode;
    @CsvBindByPosition(position = 19)
    private String libcom;
    @CsvBindByPosition(position = 20)
    private String countryCode;
    @CsvBindByPosition(position = 21)
    private String rpet;
    @CsvBindByPosition(position = 22)
    private String depet;
    @CsvBindByPosition(position = 23)
    private String comet;
    @CsvBindByPosition(position = 24)
    private String arronet;
    @CsvBindByPosition(position = 25)
    private String ctonet;
    @CsvBindByPosition(position = 26)
    private String du;
    @CsvBindByPosition(position = 27)
    private String tu;
    @CsvBindByPosition(position = 28)
    private String uu;
    @CsvBindByPosition(position = 29)
    private String epci;
    @CsvBindByPosition(position = 30)
    private String tdc;
    @CsvBindByPosition(position = 31)
    private String zemet_xl2;
    @CsvBindByPosition(position = 32)
    private String l1Normalisee;
    @CsvBindByPosition(position = 33)
    private String l2Normalisee;
    @CsvBindByPosition(position = 34)
    private String l3Normalisee;
    @CsvBindByPosition(position = 35)
    private String l4Normalisee;
    @CsvBindByPosition(position = 36)
    private String l5Normalisee;
    @CsvBindByPosition(position = 37)
    private String l6Normalisee;
    @CsvBindByPosition(position = 38)
    private String l7Normalisee;
    @CsvBindByPosition(position = 39)
    private String codeNAF31;
    @CsvBindByPosition(position = 40)
    private String codeNaf;
    @CsvBindByPosition(position = 41)
    private String tefet;
    @CsvBindByPosition(position = 42)
    private String efetcent;
    @CsvBindByPosition(position = 43)
    private String defet;
    @CsvBindByPosition(position = 44)
    private String originCode;
    @CsvBindByPosition(position = 45)
    private String dcret;
    @CsvBindByPosition(position = 46)
    private String activNat;
    @CsvBindByPosition(position = 47)
    private String dreatet;
    @CsvBindByPosition(position = 48)
    private String lieuAct;
    @CsvBindByPosition(position = 49)
    private String saisonNat;

    @CsvBindByPosition(position = 50)
    private String AUXILT;
    @CsvBindByPosition(position = 51)
    private String IND_PUBLIPO;
    @CsvBindByPosition(position = 52)
    private String DIFFCOM;
    @CsvBindByPosition(position = 53)
    private String AMINTRET;
    @CsvBindByPosition(position = 54)
    private String ACTISURF;
    @CsvBindByPosition(position = 55)
    private String MODET;
    @CsvBindByPosition(position = 56)
    private String PRODET;
    @CsvBindByPosition(position = 57)
    private String PRODPART;
    @CsvBindByPosition(position = 58)
    private String SIRETPS;
    @CsvBindByPosition(position = 59)
    private String TEL;
    @CsvBindByPosition(position = 60)
    private String  EVE;

}
