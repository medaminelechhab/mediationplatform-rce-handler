package com.MyProject.mediationplatformrcehandler.model.customerlinks;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@SuperBuilder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Entreprise extends Account{

    @JsonProperty("SMB_ACC_SEGNAT_lib__c")
    private String rceSegmentationNational;
    @JsonProperty("SMB_ACC_SEGLOC_id__c")
    private String codeSegLoc;
    @JsonProperty("SMB_ACC_SEGLOC_lib__c")
    private String rceSegmentationLocal;
    @JsonProperty("SMB_ACC_SEGNAT_id__c")
    private String nationalCodeSegment;
    @JsonProperty("SMB_ACC_leg_str__c")
    private String legalStructure;
    @JsonProperty("SMB_ACC_NAF31__c")
    private String apen31;
    @JsonProperty("SMB_ACC_NAF__c")
    private String naf;
    @JsonProperty("Score_Note__c")
    private String scoreNote;

    @JsonProperty("Scoring_Label__c")
    private String scoreLabel;

    @JsonProperty("ParentId")
    private String parentAccount;
    @JsonProperty("SMB_ACC_Group_SIREN_ID__c")
    private String groupSirenID;
    @JsonProperty("SMB_ACC_MAC_SNAT__c")
    private String nationalMacroSectorization;
    @JsonProperty("SMB_ACC_MIC_SNAT__c")
    private String nationalMicroSectorization;

    @JsonProperty("SMB_ACC_MIC_SLOC__c")
    private String localMicroSectorization;



}
