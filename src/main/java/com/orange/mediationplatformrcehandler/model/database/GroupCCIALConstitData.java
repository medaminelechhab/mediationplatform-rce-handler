package com.MyProject.mediationplatformrcehandler.model.database;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.MyProject.mediationplatformrcehandler.utils.Constants;

@Document(collection = Constants.GPE_CCIAL_CONSTIT)
@Builder(toBuilder = true)
@Getter
@Setter
public class GroupCCIALConstitData extends AuditMetadata {

    @Id
    private String id;
    private String idENT;
    private String idGroup;
    private String modificationDate;
}
