package com.ironvault.payments.domain.model;

import com.ironvault.payments.domain.enums.OutboxEventStatus;

import java.time.Instant;
import java.util.UUID;

public class OutboxEvent {

    private UUID id;
    private UUID paymentId;
    private OutboxEventStatus status;
    private Instant createdAt;
    private Instant processedAt;

    public OutboxEvent() {}

    public OutboxEvent(UUID paymentId, OutboxEventStatus status, Instant createdAt) {
        this.id = UUID.randomUUID();
        this.paymentId = paymentId;
        this.status = status;
        this.createdAt = createdAt;
        this.processedAt = null;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getPaymentId() { return paymentId; }
    public void setPaymentId(UUID paymentId) { this.paymentId = paymentId; }
    public OutboxEventStatus getStatus() { return status; }
    public void setStatus(OutboxEventStatus status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getProcessedAt() { return processedAt; }
    public void setProcessedAt(Instant processedAt) { this.processedAt = processedAt; }
}
