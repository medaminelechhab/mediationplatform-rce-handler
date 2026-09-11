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
public class EntEcoNew {

    @CsvBindByPosition(position = 0)
    private String id;
    @CsvBindByPosition(position = 1)
    private String ca;
    @CsvBindByPosition(position = 2)
    private String caExp;
    @CsvBindByPosition(position = 3)
    private String effectif;
    @CsvBindByPosition(position = 4)
    private String dateBilan;
    @CsvBindByPosition(position = 5)
    private String durationExercice;
    @CsvBindByPosition(position = 6)
    private String modificationDate;
}
