package com.zitel.taxpayers.dto;

public class SyncTaxRequest extends TaxRequest {

    Packet packet;

    public SyncTaxRequest(Packet packet, String signature) {
        super(signature);
        this.packet = packet;
    }

    public Packet getPacket() {
        return packet;
    }

    public void setPacket(Packet packet) {
        this.packet = packet;
    }
}
