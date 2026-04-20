package com.ironvault.payments.adapter.out.entity;

import com.ironvault.payments.domain.enums.TransactionStatus;
import com.ironvault.payments.domain.enums.TransactionType;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transactions")
public class TransactionEntity {

    @Id
    private UUID id;

    @Column(name = "payment_id", nullable = false)
    private UUID paymentId;

    private String externalId;

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    private BigDecimal amount;
    private String currency;
    private String gatewayCode;
    private String gatewayMessage;
    private Instant createdAt;
    private Instant updatedAt;

    public TransactionEntity() { }

    public TransactionEntity(UUID id, UUID paymentId,
                             String externalId,
                             TransactionType type,
                             TransactionStatus status,
                             BigDecimal amount,
                             String currency,
                             String gatewayCode,
                             String gatewayMessage,
                             Instant createdAt,
                             Instant updatedAt) {
        this.id = id;
        this.paymentId = paymentId;
        this.externalId = externalId;
        this.type = type;
        this.status = status;
        this.amount = amount;
        this.currency = currency;
        this.gatewayCode = gatewayCode;
        this.gatewayMessage = gatewayMessage;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(UUID paymentId) {
        this.paymentId = paymentId;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getGatewayCode() {
        return gatewayCode;
    }

    public void setGatewayCode(String gatewayCode) {
        this.gatewayCode = gatewayCode;
    }

    public String getGatewayMessage() {
        return gatewayMessage;
    }

    public void setGatewayMessage(String gatewayMessage) {
        this.gatewayMessage = gatewayMessage;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
