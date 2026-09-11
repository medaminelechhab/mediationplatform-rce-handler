package com.MyProject.mediationplatformrcehandler.model.database;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.MyProject.mediationplatformrcehandler.utils.Constants;

@Document(collection = Constants.ETA_ME_ORG_NEW)
@Builder(toBuilder = true)
@Getter
@Setter
public class EtaMeOrgNewData extends AuditMetadata {

    @Id
    private String id;
    private String idEts;
    private String codeDGDEC;
    private String codeAGGEO;
    private String codeEDSVente;
    private String aliasEDSVente;
    private String dattachment;
    private String motifAttachement;
    private String dDattachment;
    private String motifDetachment;
    private String modificationDate;
}
