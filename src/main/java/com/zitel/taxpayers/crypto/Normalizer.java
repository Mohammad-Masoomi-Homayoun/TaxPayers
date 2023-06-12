package com.zitel.taxpayers.crypto;

import com.zitel.taxpayers.dto.Packet;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class Normalizer {

    private final TaxKeyGenerator taxKeyGenerator;

    public Normalizer(TaxKeyGenerator taxKeyGenerator) {
        this.taxKeyGenerator = taxKeyGenerator;
    }


    public String normalizeAndSign(Map<String, Object> headers, Packet body) throws Exception {

        String normalizedBody = CryptoUtils.normalJson(body, headers);
        return CryptoUtils.getSignedText(normalizedBody, null, taxKeyGenerator.getPrivateKey());
    }

    public String normalizeAndSign(Map<String, Object> headers, List<Packet> body) throws Exception {

        String normalizedBody = CryptoUtils.normalJson(body, headers);
        return CryptoUtils.getSignedText(normalizedBody, null, taxKeyGenerator.getPrivateKey());
    }

    public String normalizeAndSignBill(String body) throws Exception {

        String normalizedBody = CryptoUtils.normalJson(body, null);
        return CryptoUtils.getSignedText(normalizedBody, null, taxKeyGenerator.getPrivateKey());
    }
}
