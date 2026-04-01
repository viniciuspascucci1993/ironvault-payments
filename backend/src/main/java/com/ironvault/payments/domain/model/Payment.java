package com.ironvault.payments.domain.model;

import com.ironvault.payments.domain.enums.DeclineReason;
import com.ironvault.payments.domain.enums.PaymentMethod;
import com.ironvault.payments.domain.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class Payment {

    private UUID id;
    private BigDecimal amount;
    private String currency;


    private PaymentStatus status;
    private PaymentMethod paymentMethod;

    private String description;
    private String externalId; // id do gateway de pagamento

    private String gatewayCode;
    private String gatewayMessage;
    private DeclineReason declineReason;

    private String failureReason;


    private Instant createdAt;
    private Instant updatedAt;

    public Payment(UUID id, BigDecimal amount, String currency,
                   PaymentStatus status,
                   PaymentMethod paymentMethod,
                   String description,
                   String externalId,
                   String gatewayCode,
                   String gatewayMessage,
                   DeclineReason declineReason,
                   String failureReason,
                   Instant createdAt,
                   Instant updatedAt) {
        this.id = id;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.paymentMethod = paymentMethod;
        this.description = description;
        this.externalId = externalId;
        this.gatewayCode = gatewayCode;
        this.gatewayMessage = gatewayMessage;
        this.declineReason = declineReason;
        this.failureReason = failureReason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
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

    public DeclineReason getDeclineReason() {
        return declineReason;
    }

    public void setDeclineReason(DeclineReason declineReason) {
        this.declineReason = declineReason;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
