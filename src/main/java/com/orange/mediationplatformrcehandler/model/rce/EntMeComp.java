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
public class EntMeComp {

    @CsvBindByPosition(position = 0)
    private String id;
    @CsvBindByPosition(position = 1)
    private String codeMacroSec;
    @CsvBindByPosition(position = 2)
    private String codeMicroSec;
    @CsvBindByPosition(position = 3)
    private String codeMicroSecLoc;
    @CsvBindByPosition(position = 4)
    private String codeSegemntationDCE;
    @CsvBindByPosition(position = 5)
    private String codeSegemntationDCELoc;
    @CsvBindByPosition(position = 6)
    private String modificationDate;
}
