package com.ironvault.payments.application;

import com.ironvault.payments.adapter.in.exception.TechnicalException;
import com.ironvault.payments.adapter.out.entity.webhook.WebhookDeliveryAttemptEntity;
import com.ironvault.payments.adapter.out.persistence.WebhookDeliveryAttemptRepository;
import com.ironvault.payments.domain.enums.WebhookDeliveryStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.support.RetrySynchronizationManager;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Objects;

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
