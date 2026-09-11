package com.MyProject.mediationplatformrcehandler.model.database;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.MyProject.mediationplatformrcehandler.utils.Constants;

@Document(collection = Constants.ENT_ME_COMP)
@Builder(toBuilder = true)
@Getter
@Setter
public class EntMeCompData extends AuditMetadata {

    @Id
    private String id;
    private String idEnt;
    private String codeMacroSec;
    private String codeMicroSec;
    private String codeMicroSecLoc;
    private String codeSegemntationDCE;
    private String codeSegemntationDCELoc;
    private String modificationDate;
}
