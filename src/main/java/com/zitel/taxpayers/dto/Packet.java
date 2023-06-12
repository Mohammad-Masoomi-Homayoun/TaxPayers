package com.zitel.taxpayers.dto;

import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class Packet {

    @Builder.Default
    private String uid = UUID.randomUUID().toString();
    private String packetType;
    @Builder.Default
    private boolean retry = false;
    private Object data;
    @Builder.Default
    private String encryptionKeyId = "";
    @Builder.Default
    private String symmetricKey = "";
    @Builder.Default
    private String iv = "";
    @Builder.Default
    private String fiscalId = "";
    @Builder.Default
    private String dataSignature = "";

}
