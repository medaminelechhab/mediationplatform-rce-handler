package com.MyProject.mediationplatformrcehandler.model.database;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.MyProject.mediationplatformrcehandler.utils.Constants;

@Document(collection = Constants.ENT_ECO_NEW)
@Builder(toBuilder = true)
@Getter
@Setter
public class EntEcoNewData extends AuditMetadata {

    @Id
    private String id;
    private String idEnt;
    private String ca;
    private String caExp;
    private String effectif;
    private String dateBilan;
    private String durationExercice;
    private String modificationDate;
}
