package com.ironvault.payments.adapter.out.persistence;

import com.ironvault.payments.adapter.out.entity.webhook.PaymentWebhookEventEntity;
import com.ironvault.payments.domain.port.out.WebhookEventRepositoryPort;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class WebhookEventRepositoryAdapter implements WebhookEventRepositoryPort {

    private final PaymentWebhookEventJpaRepository jpaRepository;

    public WebhookEventRepositoryAdapter(PaymentWebhookEventJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }


    @Override
    public boolean existsById(String eventId) {
        return jpaRepository.existsById(eventId);
    }

    @Override
    public void save(String eventId, String externalId, Instant receivedAt) {
        jpaRepository.save(new PaymentWebhookEventEntity(eventId, externalId, receivedAt));
    }
}
