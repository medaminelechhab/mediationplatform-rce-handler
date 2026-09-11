package com.MyProject.mediationplatformrcehandler.model.customerlinks;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@SuperBuilder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Establishment extends Account{


    @JsonProperty("SMB_ACC_Headquarter_Account__c")
    private Boolean headQuarterAccount;

    @JsonProperty("SMB_ACC_NIC__c")
    private String nic;

    @JsonProperty("SIRET_ID__c")
    private String siret;
    @JsonProperty("SMB_ACC_Group_SIREN_ID__c")
    private String groupSirenId;

    @JsonProperty("Decision_Degree__c")
    private String decisionDegree;

    @JsonProperty("Decision_Degree_Label__c")
    private String decisionDegreeLabel;
    @JsonProperty("ParentId")
    private String parentAccount;

}
