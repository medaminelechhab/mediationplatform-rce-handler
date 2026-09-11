package com.MyProject.mediationplatformrcehandler.model.rce;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.opencsv.bean.CsvBindByPosition;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class EntScoringNoteort {

    @CsvBindByPosition(position = 0)
    private String id;
    @CsvBindByPosition(position = 1)
    private String noteScoring;
    @CsvBindByPosition(position = 2)
    private String deffJugement;
    @CsvBindByPosition(position = 3)
    private String noteORT;
    @CsvBindByPosition(position = 4)
    private String modificationDate;
}
