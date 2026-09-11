package com.MyProject.mediationplatformrcehandler.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FilePrefix {

    GPE_CCIAL_ORG("GPE-CCIAL-ORG_"),
    GPE_CCIAL_COMP("GPE-CCIAL-COMP_"),
    GPE_CCIAL_CONSTIT("GPE-CCIAL-CONSTIT_"),
    ENT_ME_ORG("ENT-ME-ORG_"),
    ENT_ME_COMP("ENT-ME-COMP_"),
    ENT_REFERENCE_NEW("ENT-REF-XL2_"),
    REF_RCE("REF-RCE_"),
    ENT_SCORING_NOTEORT("ENT-SCORING-NOTEORT_"),
    ETA_REFERENCE_NEW("ETA-REF-XL2_"),
    ENT_ECO_NEW("ENT-ECO-NEW_"),
    ETA_ME_ORG_NEW("ETA-ME-ORG-NEW_");

    private final String filePrefix;
}
