package com.MyProject.mediationplatformrcehandler.model.database;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import com.MyProject.mediationplatformrcehandler.utils.Constants;

@Document(collection = Constants.REF_RCE)
@CompoundIndex(def = "{'referenceName': 1, 'code': 1}", unique = true)
@Builder(toBuilder = true)
@Getter
@Setter
public class RefRCEData extends AuditMetadata {

    @Id
    private String id;
    private String referenceName;
    private String code;
    private String label;
    private String modificationDate;
    private String deletionDate;
}