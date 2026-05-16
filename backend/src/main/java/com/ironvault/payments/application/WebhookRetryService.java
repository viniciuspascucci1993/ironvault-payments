package com.ironvault.payments.application;

import com.ironvault.payments.application.exception.TechnicalException;
import com.ironvault.payments.domain.enums.WebhookDeliveryStatus;
import com.ironvault.payments.domain.port.in.webhook.HandleWebhookUseCase;
import com.ironvault.payments.domain.port.out.WebhookDeliveryAttemptRepositoryPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.support.RetrySynchronizationManager;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Objects;

@Service
@Slf4j
public class WebhookRetryService {

    private final HandleWebhookUseCase handleWebhookUseCase;
    private final WebhookDeliveryAttemptRepositoryPort attemptRepository;

    public WebhookRetryService(HandleWebhookUseCase handleWebhookUseCase,
                               WebhookDeliveryAttemptRepositoryPort attemptRepository) {
        this.handleWebhookUseCase = handleWebhookUseCase;
        this.attemptRepository = attemptRepository;
    }

    @Retryable(
            retryFor = TechnicalException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 2.0)
    )
    public void processWithRetry(String eventId, String externalId, String status,
                                 String gatewayCode, String gatewayMessage, String failureReason) {

        int attemptNumber = RetrySynchronizationManager.getContext().getRetryCount() + 1;

        try {
            boolean processed = handleWebhookUseCase.handle(
                    eventId, externalId, status, gatewayCode, gatewayMessage, failureReason
            );

            if (processed) {
                saveAttempt(eventId, externalId, attemptNumber,
                        WebhookDeliveryStatus.SUCCESS, null);
                log.info("Webhook processed successfully. eventId={} attempt={}", eventId, attemptNumber);
            } else {
                saveAttempt(eventId, externalId, attemptNumber,
                        WebhookDeliveryStatus.FAILED, "Event already processed (duplicate)");
                log.warn("Duplicate webhook event ignored. eventId={}", eventId);
            }

        } catch (IllegalArgumentException ex) {
            saveAttempt(eventId, externalId, attemptNumber,
                    WebhookDeliveryStatus.FAILED, ex.getMessage());
            log.error("Business error processing webhook. eventId={} reason={}", eventId, ex.getMessage());

        } catch (Exception ex) {
            saveAttempt(eventId, externalId, attemptNumber,
                    WebhookDeliveryStatus.FAILED, ex.getMessage());
            log.warn("Technical error processing webhook, will retry. eventId={} attempt={} error={}",
                    eventId, attemptNumber, ex.getMessage());
            throw new TechnicalException("Technical failure processing webhook: " + eventId, ex);
        }
    }

    @Recover
    public void recover(TechnicalException ex, String eventId, String externalId, String status,
                        String gatewayCode, String gatewayMessage, String failureReason) {

        int attemptNumber = Objects.requireNonNull(RetrySynchronizationManager.getContext()).getRetryCount() + 1;

        saveAttempt(eventId, externalId, attemptNumber,
                WebhookDeliveryStatus.EXHAUSTED, ex.getMessage());
        log.error("Webhook processing exhausted all retries. eventId={} externalId={} error={}",
                eventId, externalId, ex.getMessage());
    }

    private void saveAttempt(String eventId, String externalId, int attemptNumber,
                             WebhookDeliveryStatus status, String errorMessage) {
        attemptRepository.save(eventId, externalId, attemptNumber, status, errorMessage, Instant.now());
    }
}
