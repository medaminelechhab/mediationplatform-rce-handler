package com.MyProject.mediationplatformrcehandler.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AccountType {

    GROUPE("Groupe"),
    ENTREPRISE("Entreprise"),
    ETABLISSEMENT("Etablissement");
    private final String type;
}
