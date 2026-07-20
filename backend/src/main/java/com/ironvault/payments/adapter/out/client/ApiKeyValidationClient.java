package com.ironvault.payments.adapter.out.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
@Slf4j
public class ApiKeyValidationClient {

    private final WebClient webClient;
    private final String internalApiKey;

    public ApiKeyValidationClient(
            @Value("${app.auth.url}") String authUrl,
            @Value("${app.auth.internal-api-key}") String internalApiKey) {
        this.webClient = WebClient.builder().baseUrl(authUrl).build();
        this.internalApiKey = internalApiKey;
    }

    public boolean validate(String apiKey) {
        try {
            var response = webClient.get()
                    .uri("/api/keys/validate?key=" + apiKey)
                    .header("X-Internal-Key", internalApiKey)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            return Boolean.TRUE.equals(response != null ? response.get("valid") : false);
        } catch (Exception ex) {
            log.error("Error validating API key: {}", ex.getMessage());
            return false;
        }
    }
}
