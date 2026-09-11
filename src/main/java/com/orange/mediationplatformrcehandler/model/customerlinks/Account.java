package com.MyProject.mediationplatformrcehandler.model.customerlinks;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Account{

    @JsonProperty("id")
    private String id;
    @JsonProperty("attributes")
    private Attributes attributes;
    @JsonProperty("Name")
    private String name;
    @JsonProperty("RecordTypeId")
    private String accountRecordType;
    @JsonProperty("RCE_ID__c")
    private String rceID;
    @JsonProperty("RCE_Account_Type__c")
    private String rceAccountType;
    @JsonProperty("SMB_ACC_Siren_ID__c")
    private String sirenID;

    @JsonProperty("CurrencyIsoCode")
    private String accountCurrency;
    @JsonProperty("AccountSource")
    private String accountSource;
    @JsonProperty("SMB_Inactive__c")
    private Boolean inactive;
    @JsonProperty("SMB_ACC_COD_EDG__c")
    private String codeEDG;
    @JsonProperty("SMB_ACC_LIB_EDG__c")
    private String labelEDG;
    @JsonProperty("BillingStreet")
    private String street;
    @JsonProperty("BillingCity")
    private String city;
    @JsonProperty("BillingPostalCode")
    private String postalCode;
    @JsonProperty("BillingCountry")
    private String country;
    @JsonProperty("SMB_ACC_Date_Det__c")
    private LocalDate detachmentDate;
    @JsonProperty("OwnerId")
    private String accountOwner;


}
