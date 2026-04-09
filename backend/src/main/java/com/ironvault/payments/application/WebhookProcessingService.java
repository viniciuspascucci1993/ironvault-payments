package com.ironvault.payments.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class WebhookProcessingService {

    private final WebhookRetryService webhookRetryService;

    public WebhookProcessingService(WebhookRetryService webhookRetryService) {
        this.webhookRetryService = webhookRetryService;
    }

    @Async
    public void process(String eventId, String externalId, String status,
                        String gatewayCode, String gatewayMessage, String failureReason) {
        webhookRetryService.processWithRetry(
                eventId, externalId, status, gatewayCode, gatewayMessage, failureReason
        );
    }
}
