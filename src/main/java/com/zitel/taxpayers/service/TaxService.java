package com.zitel.taxpayers.service;

import com.zitel.taxpayers.clients.WebUtil;
import com.zitel.taxpayers.crypto.CryptoUtils;
import com.zitel.taxpayers.crypto.Normalizer;
import com.zitel.taxpayers.crypto.TaxKeyGenerator;
import com.zitel.taxpayers.crypto.TokenManager;
import com.zitel.taxpayers.dto.AsyncTaxRequest;
import com.zitel.taxpayers.dto.Packet;
import com.zitel.taxpayers.dto.SyncTaxRequest;
import com.zitel.taxpayers.dto.TaxRequest;
import com.zitel.taxpayers.model.Bill;
import ir.gov.tax.tpis.sdk.content.api.DefaultTaxApiClient;
import ir.gov.tax.tpis.sdk.content.api.TaxApi;
import ir.gov.tax.tpis.sdk.content.dto.InvoiceDto;
import ir.gov.tax.tpis.sdk.transfer.api.ObjectTransferApiImpl;
import ir.gov.tax.tpis.sdk.transfer.api.TransferApi;
import ir.gov.tax.tpis.sdk.transfer.config.ApiConfig;
import ir.gov.tax.tpis.sdk.transfer.dto.AsyncResponseModel;
import ir.gov.tax.tpis.sdk.transfer.impl.signatory.InMemorySignatory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.*;

import static javax.crypto.Cipher.PRIVATE_KEY;

@Service
public class TaxService {

    @Value("${api.tax:https://tp.tax.gov.ir/req/api/self-tsp}")
    private String url;

    private final TokenManager tokenManager;
    private final Normalizer normalizer;
    private final TaxKeyGenerator taxKeyGenerator;

    public TaxService(TokenManager tokenManager, Normalizer normalizer, TaxKeyGenerator taxKeyGenerator) {
        this.tokenManager = tokenManager;
        this.normalizer = normalizer;
        this.taxKeyGenerator = taxKeyGenerator;
    }

    public String registerBill() throws Exception {

        Map<String, Object> headers = WebUtil.getNewHeaders();
        headers.put("Authorization", tokenManager.getToken());
        TaxRequest registerRequest = makeRegisterBillRequest(headers);
        String registerResponse = WebUtil.sendRequest(url + "/async/normal-enqueue", registerRequest, headers);
        return extractReference(registerResponse);
    }

    public String inquiryBillByReferenceNumber(String referenceNumber) throws Exception {
        Map<String, Object> headers = WebUtil.getNewHeaders();
        headers.put("Authorization", tokenManager.getToken());
        TaxRequest inquiryRequest = makeInquiryRequest(headers, referenceNumber);
        return WebUtil.sendRequest(url + "/sync/INQUIRY_BY_REFERENCE_NUMBER", inquiryRequest, headers);
    }

    public TaxRequest makeInquiryRequest(Map<String, Object> headers, String referenceNumber) throws Exception {

        Map<String, List<String>> data = new HashMap<>();
        data.put("referenceNumber", Arrays.asList(referenceNumber));

        Packet packet = Packet.builder()
                .packetType("INQUIRY_BY_REFERENCE_NUMBER")
                .data(data)
                .build();

        String signature = normalizer.normalizeAndSign(headers, packet);

        return new SyncTaxRequest(packet, signature);
    }

    public AsyncTaxRequest makeRegisterBillRequest(Map<String, Object> headers) throws Exception {

        // 2- sign with private key -> into dataSignature
        String dataSignature = normalizer.normalizeAndSignBill(getBody());
        // 3- generate random symmetric key with len: 256 bit
        SecretKey randomSymmetricKey = CryptoUtils.getAESKey(256);
        // 4- generating iv -> IV (Hex)
        byte[] ivBytes = CryptoUtils.getRandomNonce(32);
        String iv = CryptoUtils.bytesToHex(ivBytes);
//        String iv = Base64.getEncoder().encodeToString(ivBytes);
        // 5- XOR bill with generated random symmetric key 256/256/256
        byte[] xoredBill = CryptoUtils.xorBody(getBody().getBytes() ,randomSymmetricKey.getEncoded());
        // 6- encrypt bill AES/GCM -> data (Base64)
        byte[] dataByte = CryptoUtils.encrypt(xoredBill, randomSymmetricKey, ivBytes);
        String data = Base64.getEncoder().encodeToString(dataByte);

        // GET_SERVER_INFORMATION and extract public key -> symmetricKey (Base64)
        String randomSymmetricKeyStr = new String(randomSymmetricKey.getEncoded());
        String randomSymmetricKeyHexStr = CryptoUtils.bytesToHex(randomSymmetricKey.getEncoded());
        String randomSymmetricKeyBase64Str = Base64.getEncoder().encodeToString(randomSymmetricKey.getEncoded());
        String symmetricKey = CryptoUtils.encrypt(randomSymmetricKeyBase64Str, taxKeyGenerator.getPublicKey());

        List<Packet> packets = new ArrayList<>();
        Packet packet = Packet.builder()
                .packetType("INVOICE.V01")
                .data(data)
                .encryptionKeyId(TaxKeyGenerator.PUBLIC_KEY_ID)
                .symmetricKey(symmetricKey)
                .iv(iv)
                .dataSignature(dataSignature)
                .build();
        packets.add(packet);

        headers.remove("Authorization");
        headers.put("Authorization", tokenManager.getToken());

        String signature = normalizer.normalizeAndSign(headers, packets);

        return new AsyncTaxRequest(packets, signature);
    }

    private String extractReference(String registerResponse) {
        try {
            JSONObject jsonObject = new JSONObject(registerResponse);
            return new JSONObject(jsonObject.getJSONArray("result").get(0).toString()).get("referenceNumber").toString();
        }catch (JSONException err){
            System.out.println(err);
        }
        return "Failed";
    }

    private String getBody() {
        return "{" +
                "  \"header\": {" +
                "    \"taxid\": \"A111220E1B9155CB1F18C7\"," +
                "    \"indatim\": \"1665490063785\"," +
                "    \"Indati2m\": \"1665490063785\"," +
                "    \"inty\": \"1\"," +
                "    \"inno\": \"0000011300\"," +
                "    \"irtaxid\": null," +
                "    \"inp\": \"1\"," +
                "    \"ins\": \"1\"," +
                "    \"tins\": \"19117484910001\"," +
                "    \"tob\": \"1\"," +
                "    \"bid\": \"0\"," +
                "    \"tinb\": \"19117484910002\"," +
                "    \"sbc\": \"0\"," +
                "    \"bpc\": \"0\"," +
                "    \"bbc\": \"0\"," +
                "    \"ft\": \"0\"," +
                "    \"bpn\": \"0\"," +
                "    \"scln\": \"0\"," +
                "    \"scc\": \"0\"," +
                "    \"crn\": \"0\"," +
                "    \"billid\": \"0\"," +
                "    \"tprdis\": \"2400000\"," +
                "    \"tdis\": \"0\"," +
                "    \"tadis\": \"2400000\"," +
                "    \"tvam\": \"216000\"," +
                "    \"todam\": \"0\"," +
                "    \"tbill\": \"2616000\"," +
                "    \"setm\": \"1\"," +
                "    \"cap\": \"2616000\"," +
                "    \"insp\": \"0\"," +
                "    \"tvop\": \"216000\"," +
                "    \"tax17\": \"0\"" +
                "  }," +
                "  \"body\": [" +
                "    {" +
                "      \"sstid\": \"1254219865985\"," +
                "      \"sstt\": \"test\"," +
                "      \"am\": \"1\"," +
                "      \"mu\": \"unit\"," +
                "      \"fee\": \"2400000\"," +
                "      \"cfee\": \"0\"," +
                "      \"cut\": \"0\"," +
                "      \"exr\": \"0\"," +
                "      \"prdis\": \"2400000\"," +
                "      \"dis\": \"0\"," +
                "      \"adis\": \"2400000\"," +
                "      \"vra\": \"0.09\"," +
                "      \"vam\": \"216000\"," +
                "      \"odt\": \"0\"," +
                "      \"odr\": \"0\"," +
                "      \"odam\": \"0\"," +
                "      \"olt\": \"0\"," +
                "      \"olr\": \"0\"," +
                "      \"olam\": \"0\"," +
                "      \"consfee\": \"0\"," +
                "      \"spro\": \"0\"," +
                "      \"bros\": \"0\"," +
                "      \"tcpbs\": \"0\"," +
                "      \"cop\": \"0\"," +
                "      \"vop\": \"216000\"," +
                "      \"bsrn\": null," +
                "      \"tsstam\": \"2616000\"" +
                "    }" +
                "  ]," +
                "  \"payments\": [" +
                "    {" +
                "      \"iinn\": \"125036\"," +
                "      \"acn\": \"252544\"," +
                "      \"trmn\": \"2356566\"," +
                "      \"trn\": \"252545\"," +
                "      \"pcn\": \"6037991785693265\"," +
                "      \"pid\": \"19117484910002\"," +
                "      \"pdt\": \"1665490061447\"" +
                "    }" +
                "  ]," +
                "\"extension\": [" +
                "      {" +
                "        \"key\": null," +
                "        \"value\": null" +
                "      }" +
                "    ]" +
                "}";
    }

    public String registerBill(Bill bill) {

        return null;
    }
}
