package com.zitel.taxpayers.controller;

import com.zitel.taxpayers.model.Bill;
import com.zitel.taxpayers.service.TaxService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/taxes")
public class TaxController {

    private final TaxService taxService;

    public TaxController(TaxService taxService) {
        this.taxService = taxService;
    }

    @GetMapping("/register")
    public String registerBill() throws Exception {

        String referenceNumber = taxService.registerBill();
        return taxService.inquiryBillByReferenceNumber(referenceNumber);
    }

    @PostMapping("/register")
    public String registerBill(@RequestBody Bill bill) throws Exception {

        String referenceNumber = taxService.registerBill(bill);
        return null;
    }

    @GetMapping("/inquiry/{referenceNumber}")
    public String inquiryBillByReferenceNumber(@PathVariable("referenceNumber") String referenceNumber) throws Exception {

        return taxService.inquiryBillByReferenceNumber(referenceNumber);
    }

}
