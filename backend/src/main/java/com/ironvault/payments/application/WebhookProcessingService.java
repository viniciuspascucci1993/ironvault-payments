package com.ironvault.payments.application;

import com.ironvault.payments.domain.port.in.webhook.ProcessWebhookUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class WebhookProcessingService implements ProcessWebhookUseCase {

    private final WebhookRetryService webhookRetryService;

    public WebhookProcessingService(WebhookRetryService webhookRetryService) {
        this.webhookRetryService = webhookRetryService;
    }

    @Async
    @Override
    public void process(String eventId, String externalId, String status,
                        String gatewayCode, String gatewayMessage, String failureReason) {
        webhookRetryService.processWithRetry(
                eventId, externalId, status, gatewayCode, gatewayMessage, failureReason
        );
    }
}
