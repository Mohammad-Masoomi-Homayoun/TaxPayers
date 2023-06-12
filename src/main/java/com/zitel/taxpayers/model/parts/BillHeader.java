package com.zitel.taxpayers.model.parts;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class BillHeader {

    private String taxid;
    private String indatim;
    @JsonProperty("Indati2m")
    private String indati2m;
    private String inty;
    private String inno;
    private Object irtaxid;
    private String inp;
    private String ins;
    private String tins;
    private String tob;
    private String bid;
    private String tinb;
    private String sbc;
    private String bpc;
    private String bbc;
    private String ft;
    private String bpn;
    private String scln;
    private String scc;
    private String crn;
    private String billid;
    private String tprdis;
    private String tdis;
    private String tadis;
    private String tvam;
    private String todam;
    private String tbill;
    private String setm;
    private String cap;
    private String insp;
    private String tvop;
    private String tax17;
}
