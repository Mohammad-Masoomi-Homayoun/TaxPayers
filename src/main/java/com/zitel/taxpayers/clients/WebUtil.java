package com.zitel.taxpayers.clients;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zitel.taxpayers.dto.TaxRequest;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WebUtil {

    private final static ObjectMapper mapper = new ObjectMapper();

    public static Map<String, Object> getNewHeaders() {
        Map<String, Object> headers = new HashMap<>();
        headers.put("requestTraceId", UUID.randomUUID().toString());
        headers.put("timestamp", System.currentTimeMillis());
        return headers;
    }

    public static String sendRequest(String url, TaxRequest taxRequest, Map<String, Object> headers) {

        HttpClient httpClient =  HttpClients.createDefault();
        try {
            HttpPost request = new HttpPost(url);
            StringEntity stringEntity = new StringEntity(mapper.writeValueAsString(taxRequest));
            stringEntity.setContentType(ContentType.APPLICATION_JSON.getMimeType());
            request.setEntity(stringEntity);
            for(String key : headers.keySet()) {
                if("Authorization".equals(key)) {
                    request.addHeader(key, "Bearer " + headers.get(key).toString());
                } else {
                    request.addHeader(key, headers.get(key).toString());
                }
            }
            HttpResponse response = httpClient.execute(request);
            System.out.println(response.getStatusLine().getStatusCode());
            HttpEntity entity = response.getEntity();
            return EntityUtils.toString(entity, "UTF-8");
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return "Failed";
    }

}
