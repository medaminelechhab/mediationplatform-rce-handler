package com.MyProject.mediationplatformrcehandler.model.database;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.MyProject.mediationplatformrcehandler.utils.Constants;

@Document(collection = Constants.GPE_CCIAL_ORG)
@Builder(toBuilder = true)
@Getter
@Setter
public class GroupCCIALOrgData extends AuditMetadata {

    @Id
    private String id;
    private String idGroup;
    private String nameGroup;
    private String codeEDSVente;
    private String aliasEDSVente;
    private String idEntMm;
    private String modificationDate;
    private String creationDate;
    private String deletionDate;
}
