package com.ironvault.payments.adapter.out.entity.webhook;

import com.ironvault.payments.domain.enums.WebhookDeliveryStatus;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "webhook_delivery_attempts")
public class WebhookDeliveryAttemptEntity {

    @Id
    private UUID id;

    private String eventId;
    private String externalId;

    private int attemptNumber;

    @Enumerated(EnumType.STRING)
    private WebhookDeliveryStatus status;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    private Instant attemptedAt;

    public WebhookDeliveryAttemptEntity() { }

    public WebhookDeliveryAttemptEntity(String eventId,
                                        String externalId,
                                        int attemptNumber,
                                        WebhookDeliveryStatus status,
                                        String errorMessage,
                                        Instant attemptedAt) {
        this.id = UUID.randomUUID();
        this.eventId = eventId;
        this.externalId = externalId;
        this.attemptNumber = attemptNumber;
        this.status = status;
        this.errorMessage = errorMessage;
        this.attemptedAt = attemptedAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public int getAttemptNumber() {
        return attemptNumber;
    }

    public void setAttemptNumber(int attemptNumber) {
        this.attemptNumber = attemptNumber;
    }

    public WebhookDeliveryStatus getStatus() {
        return status;
    }

    public void setStatus(WebhookDeliveryStatus status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Instant getAttemptedAt() {
        return attemptedAt;
    }

    public void setAttemptedAt(Instant attemptedAt) {
        this.attemptedAt = attemptedAt;
    }
}
