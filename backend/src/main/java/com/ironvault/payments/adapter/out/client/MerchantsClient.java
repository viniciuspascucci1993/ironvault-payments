package com.ironvault.payments.adapter.out.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
public class MerchantsClient {

    private final RestTemplate restTemplate;

    @Value("${app.merchants.url}")
    private String merchantsUrl;

    @Value("${app.auth.internal-api-key}")
    private String internalApiKey;

    public MerchantsClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getMerchantAccessToken(UUID merchantId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Internal-Key", internalApiKey);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        var response = restTemplate.exchange(
                merchantsUrl + "/api/internal/merchants/" + merchantId + "/credentials",
                org.springframework.http.HttpMethod.GET,
                request,
                Map.class
        );

        Map<?, ?> body = response.getBody();
        if (body == null || body.get("accessToken") == null) {
            throw new IllegalStateException("Merchant não possui credenciais Mercado Pago conectadas: " + merchantId);
        }

        return (String) body.get("accessToken");
    }
}
