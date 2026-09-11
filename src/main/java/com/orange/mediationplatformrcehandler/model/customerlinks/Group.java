package com.MyProject.mediationplatformrcehandler.model.customerlinks;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

import org.springframework.data.annotation.Transient;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@SuperBuilder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Group extends Account {

    @JsonProperty("SMB_ACC_SEGNAT_id__c")
    private String codeSegNat;

    @JsonProperty("SMB_ACC_SEGNAT_lib__c")
    private String rceSegmentationNational;

    @JsonProperty("SMB_ACC_SEGLOC_id__c")
    private String codeSegLoc;

    @JsonProperty("SMB_ACC_SEGLOC_lib__c")
    private String rceSegmentationLocal;


    @JsonProperty("SMB_ACC_leg_str__c")
    private String legalStructure;
    @JsonProperty("SMB_ACC_NAF31__c")
    private String apen31;
    @JsonProperty("SMB_ACC_NAF__c")
    private String naf;
    @JsonProperty("SMB_ACC_NIC__c")
    private String nic;

    @JsonProperty("SMB_ACC_Group_SIREN_ID__c")
    private String groupSirenId;

    @JsonProperty("SMB_ACC_MAC_SNAT__c")
    private String nationalMacroSectorization;
    @JsonProperty("SMB_ACC_MIC_SNAT__c")
    private String nationalMicroSectorization;

    @JsonProperty("SMB_ACC_MIC_SLOC__c")
    private String localMicroSectorization;

    //Unused in CL Creation & Update
    @Transient
    private LocalDate suppressionDate;
}
