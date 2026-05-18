package com.ironvault.payments.adapter.out.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
@Slf4j
public class NotificationClient {

    private final WebClient webClient;
    private final String apiKey;

    public NotificationClient(
            @Value("${app.notifications.url}") String notificationsUrl,
            @Value("${app.notifications.api-key}") String apiKey) {
        this.apiKey = apiKey;
        this.webClient = WebClient.builder()
                .baseUrl(notificationsUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public void sendPixGeneratedEvent(String email, String amount, String currency, String pixCopyPaste) {
        sendEvent("PIX_GENERATED", Map.of(
                "email", email,
                "amount", amount,
                "currency", currency,
                "pixCopyPaste", pixCopyPaste
        ));
    }

    public void sendPaymentFailedEvent(String email, String amount, String reason) {
        sendEvent("PAYMENT_FAILED", Map.of(
                "email", email,
                "amount", amount,
                "reason", reason
        ));
    }

    public void sendEvent(String type, Map<String, String> payload) {
        try {
            webClient.post()
                    .uri("/api/notifications/events")
                    .header("X-API-KEY", apiKey)
                    .bodyValue(Map.of(
                            "type", type,
                            "sourceService", "ironvault-payments",
                            "payload", payload
                    ))
                    .retrieve()
                    .bodyToMono(Void.class)
                    .subscribe(
                            null,
                            error -> log.error("Failed to send {} event reason={}", type, error.getMessage())
                    );
        } catch (Exception ex) {
            log.error("Failed to send notification type={} reason={}", type, ex.getMessage());
        }
    }
}
