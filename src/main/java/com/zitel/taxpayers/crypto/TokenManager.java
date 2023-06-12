package com.zitel.taxpayers.crypto;

import com.zitel.taxpayers.clients.WebUtil;
import com.zitel.taxpayers.dto.Packet;
import com.zitel.taxpayers.dto.SyncTaxRequest;
import com.zitel.taxpayers.dto.TaxRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

@Component
public class TokenManager {

    private final Normalizer normalizer;
    @Value("${tax.api.token:https://tp.tax.gov.ir/req/api/self-tsp/sync/GET_TOKEN}")
    private String url;

    private String token;
    private long expireAt;

    public TokenManager(Normalizer normalizer) {
        this.normalizer = normalizer;
    }

    public String getToken() throws Exception {

        if(token == null || expireAt < System.currentTimeMillis()) {
            refreshToken();
        }
        return token;
    }

    public String getBearerToken() throws Exception {
        if(token == null || expireAt < System.currentTimeMillis()) {
            refreshToken();
        }
        return "Bearer " + this.token;
    }

    public void refreshToken() throws Exception {

        Map<String, Object> headers = WebUtil.getNewHeaders();
        TaxRequest loginRequestBody = makeLoginRequest(headers);

        String tokenResponse = WebUtil.sendRequest(url, loginRequestBody, headers);
        extractTokenAndExpiration(tokenResponse);
    }


    private SyncTaxRequest makeLoginRequest(Map<String, Object> headers) throws Exception {

        Packet packet = Packet.builder()
                .packetType("GET_TOKEN")
                .data(Map.of("username", "شناسه یکتا"))
                .build();

        String signature = normalizer.normalizeAndSign(headers, packet);

        return new SyncTaxRequest(packet, signature);
    }

    private void extractTokenAndExpiration(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONObject data =  new JSONObject(new JSONObject(jsonObject.get("result").toString()).get("data").toString());
            this.token = data.get("token").toString();
            this.expireAt = Long.parseLong(data.get("expiresIn").toString());
        }catch (JSONException err){
            System.out.println(err);
        }
    }

}
