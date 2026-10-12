package com.ironvault.payments.adapter.out.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
@Slf4j
public class StoreClient {

    private final RestTemplate restTemplate;

    @Value("${app.store.url:}")
    private String storeUrl;

    @Value("${app.auth.internal-api-key}")
    private String internalApiKey;

    public StoreClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void notifyOrderPaid(String paymentId) {
        if (storeUrl == null || storeUrl.isBlank()) {
            log.warn("app.store.url not configured, skipping store notification. paymentId={}", paymentId);
            return;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Internal-Key", internalApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> request =
                new HttpEntity<>(Map.of("paymentId", paymentId), headers);

        try {
            restTemplate.postForEntity(storeUrl + "/api/internal/orders/paid", request, Void.class);
            log.info("Store notified: order paid. paymentId={}", paymentId);
        } catch (HttpClientErrorException.NotFound ex) {
            // Pagamento que nao veio da loja: nada a fazer
            log.debug("No store order for paymentId={}", paymentId);
        } catch (RestClientException ex) {
            log.error("Failed to notify store. paymentId={} error={}", paymentId, ex.getMessage());
        }
     }
}
