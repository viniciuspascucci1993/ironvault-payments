package com.ironvault.payments.domain.port.out;

import com.ironvault.payments.domain.enums.WebhookDeliveryStatus;

import java.time.Instant;

public interface WebhookDeliveryAttemptRepositoryPort {

    void save(String eventId, String externalId, int attemptNumber,
              WebhookDeliveryStatus status, String errorMessage, Instant attemptedAt);
}
