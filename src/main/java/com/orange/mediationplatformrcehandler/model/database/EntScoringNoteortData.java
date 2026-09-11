package com.MyProject.mediationplatformrcehandler.model.database;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.MyProject.mediationplatformrcehandler.utils.Constants;

@Document(collection = Constants.ENT_SCORING_NOTEORT)
@Builder(toBuilder = true)
@Getter
@Setter
public class EntScoringNoteortData extends AuditMetadata {

    @Id
    private String id;
    private String idEnt;
    private String noteScoring;
    private String deffJugement;
    private String noteORT;
    private String modificationDate;
}
