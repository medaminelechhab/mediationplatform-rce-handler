package com.MyProject.mediationplatformrcehandler.model.database;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.MyProject.mediationplatformrcehandler.utils.Constants;

@Document(collection = Constants.GPE_CCIAL_COMP)
@Builder(toBuilder = true)
@AllArgsConstructor
@Getter
@Setter
public class GroupCCIALCompData extends AuditMetadata {

    @Id
    private String id;
    private String idGroup;
    private String sectorMacroCode;
    private String sectorMicroCode;
    private String sectorMicroCodeLoc;
    private String segmentationDCECode;
    private String segmentationDCECodeLoc;
    private String reserve;
    private String modificationDate;
}
