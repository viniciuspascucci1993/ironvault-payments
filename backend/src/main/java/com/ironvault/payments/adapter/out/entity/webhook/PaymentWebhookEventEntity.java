package com.ironvault.payments.adapter.out.entity.webhook;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "payment_webhook_events")
public class PaymentWebhookEventEntity {

    @Id
    private String eventId;
    private String externalId;
    private Instant processedAt;

    public PaymentWebhookEventEntity() {
    }

    public PaymentWebhookEventEntity(String eventId, String externalId, Instant processedAt) {
        this.eventId = eventId;
        this.externalId = externalId;
        this.processedAt = processedAt;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(Instant processedAt) {
        this.processedAt = processedAt;
    }
}
