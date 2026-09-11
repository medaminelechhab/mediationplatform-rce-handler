package com.MyProject.mediationplatformrcehandler.model.database;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.MyProject.mediationplatformrcehandler.utils.Constants;

@Document(collection = Constants.ENT_ME_ORG)
@Builder(toBuilder = true)
@Getter
@Setter
public class EntMeOrgData extends AuditMetadata {

    @Id
    private String id;
    private String idEnt;
    private String codeSegmentCCial;
    private String dattachement;
    private String motifattachement;
    private String dDetachement;
    private String motifDetachement;
    private String codeEDSVente;
    private String aliasEDSVente;
    private String modificationDate;
}
