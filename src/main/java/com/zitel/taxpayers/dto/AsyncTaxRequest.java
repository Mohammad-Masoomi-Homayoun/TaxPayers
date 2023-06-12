package com.zitel.taxpayers.dto;

import java.util.List;

public class AsyncTaxRequest extends TaxRequest {

    List<Packet> packets;

    public AsyncTaxRequest(List<Packet> packets, String signature) {
        super(signature);
        this.packets = packets;
    }

    public List<Packet> getPackets() {
        return packets;
    }

    public void setPackets(List<Packet> packets) {
        this.packets = packets;
    }
}
