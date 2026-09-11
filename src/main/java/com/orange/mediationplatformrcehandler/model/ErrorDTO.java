package com.MyProject.mediationplatformrcehandler.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class ErrorDTO {

    private String rceId;
    private String clinksId;
    private AccountType type;
    private String errorCode;
    private String errorMessage;


    public enum AccountType {
        GROUPE,
        ETABLISSEMENT,
        ENTREPRISE
    }

}
