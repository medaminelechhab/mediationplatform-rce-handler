package com.MyProject.mediationplatformrcehandler.utils;

import java.time.format.DateTimeFormatter;

public class Constants {

    public static final DateTimeFormatter DF_YYYYMMDD = DateTimeFormatter.ofPattern("yyyyMMdd");
    public static final DateTimeFormatter DF_MIDNIGHT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'00:00:00");

    public static final String GROUP = "Groupe";
    public static final String ENTREPRISE = "Entreprise";
    public static final String ETABLISSEMENT = "Etablissement";
    public static final String RCE_FRANCE = "RCE_France";

    public enum Sectorisation {
        MICRO_SECTORISATION,
        MACRO_SECTORISATION
    }


    public static final String RCE_FILE_EXTENTION = ".tgz";
    public static final String GPE_CCIAL_ORG="gpe_ccial_org";
    public static final String GPE_CCIAL_COMP="gpe_ccial_comp";
    public static final String GPE_CCIAL_CONSTIT="gpe_ccial_constit";
    public static final String ENT_ME_ORG="ent_me_org";
    public static final String ENT_ME_COMP="ent_me_comp";
    public static final String ENT_REFERENCE_NEW="ent_ref_xl2";
    public static final String REF_RCE="ref_rce";
    public static final String ENT_SCORING_NOTEORT="ent_scoring_noteort";
    public static final String ETA_REFERENCE_NEW="eta_ref_xl2";
    public static final String ENT_ECO_NEW="ent_eco_new";
    public static final String ETA_ME_ORG_NEW="eta_me_org_new";

}
