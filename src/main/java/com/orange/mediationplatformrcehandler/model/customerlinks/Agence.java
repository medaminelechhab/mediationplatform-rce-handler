package com.MyProject.mediationplatformrcehandler.model.customerlinks;

import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class Agence {

    private String code;
    private String label;
    private String smbCode;
    private String commercialSegment;
}
