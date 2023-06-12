package com.zitel.taxpayers.dto;

public class TaxRequest {

    String signature;

    public TaxRequest(String signature) {
        this.signature = signature;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}
