package com.ironvault.payments.adapter.out.persistence;

import com.ironvault.payments.adapter.out.entity.webhook.WebhookDeliveryAttemptEntity;
import com.ironvault.payments.domain.enums.WebhookDeliveryStatus;
import com.ironvault.payments.domain.port.out.WebhookDeliveryAttemptRepositoryPort;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class WebhookDeliveryAttemptRepositoryAdapter implements WebhookDeliveryAttemptRepositoryPort {

    private final WebhookDeliveryAttemptRepository jpaRepository;

    public WebhookDeliveryAttemptRepositoryAdapter(WebhookDeliveryAttemptRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void save(String eventId, String externalId, int attemptNumber,
                     WebhookDeliveryStatus status, String errorMessage, Instant attemptedAt) {
        jpaRepository.save(new WebhookDeliveryAttemptEntity(
                eventId, externalId, attemptNumber, status, errorMessage, attemptedAt
        ));

    }
}
