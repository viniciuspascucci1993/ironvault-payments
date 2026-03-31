package com.ironvault.payments.adapter.out.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "idempotency")
public class PaymentIdempotencyEntity {

    @Id
    @Column(unique = true, nullable = false)
    private String idempotencyKey;

    private String requestHash;

    @Column(columnDefinition = "TEXT")
    private String response;

    @Column(name = "payment_id", nullable = false)
    private String paymentId;

    private Instant createdAt;

    public PaymentIdempotencyEntity() {}

    public PaymentIdempotencyEntity(String idempotencyKey,
                                    String requestHash, String response, String paymentId, Instant createdAt) {
        this.idempotencyKey = idempotencyKey;
        this.requestHash = requestHash;
        this.response = response;
        this.paymentId = paymentId;
        this.createdAt = createdAt;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public String getRequestHash() {
        return requestHash;
    }

    public void setRequestHash(String requestHash) {
        this.requestHash = requestHash;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
