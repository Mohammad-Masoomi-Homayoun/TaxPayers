package com.zitel.taxpayers.model;

import com.zitel.taxpayers.model.parts.BillBody;
import com.zitel.taxpayers.model.parts.BillExtension;
import com.zitel.taxpayers.model.parts.BillHeader;
import com.zitel.taxpayers.model.parts.BillPayment;
import lombok.Data;

import java.util.List;

@Data
public class Bill {

    private BillHeader header;
    private List<BillBody> body;
    private List<BillPayment> payments;
    private List<BillExtension> extension;
}
