package com.zitel.taxpayers.model.parts;

import lombok.Data;

@Data
public class BillPayment {

    private String iinn;
    private String acn;
    private String trmn;
    private String trn;
    private String pcn;
    private String pid;
    private String pdt;
}
