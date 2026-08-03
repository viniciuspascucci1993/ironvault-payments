package com.ironvault.payments.domain.model;

import com.ironvault.payments.domain.enums.DeclineReason;
import com.ironvault.payments.domain.enums.PaymentMethod;
import com.ironvault.payments.domain.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class Payment {

    private UUID id;
    private UUID merchantId;
    private BigDecimal amount;
    private String currency;


    private PaymentStatus status;
    private PaymentMethod paymentMethod;

    private String description;
    private String payerEmail;
    private String externalId; // id do gateway de pagamento

    private String gatewayCode;
    private String gatewayMessage;
    private DeclineReason declineReason;

    private String pixQrCode;
    private String pixCopyPaste;

    private String failureReason;


    private Instant createdAt;
    private Instant updatedAt;

    public Payment() {}

    public Payment(UUID id, UUID merchantId, BigDecimal amount, String currency,
                   PaymentStatus status,
                   PaymentMethod paymentMethod,
                   String description,
                   String payerEmail,
                   String externalId,
                   String gatewayCode,
                   String gatewayMessage,
                   DeclineReason declineReason,
                   String pixQrCode,
                   String pixCopyPaste,
                   String failureReason,
                   Instant createdAt,
                   Instant updatedAt) {
        this.id = id;
        this.merchantId = merchantId;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.paymentMethod = paymentMethod;
        this.description = description;
        this.payerEmail = payerEmail;
        this.externalId = externalId;
        this.gatewayCode = gatewayCode;
        this.gatewayMessage = gatewayMessage;
        this.declineReason = declineReason;
        this.pixQrCode = pixQrCode;
        this.pixCopyPaste = pixCopyPaste;
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

    public UUID getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(UUID merchantId) {
        this.merchantId = merchantId;
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

    public String getPayerEmail() {
        return payerEmail;
    }

    public void setPayerEmail(String payerEmail) {
        this.payerEmail = payerEmail;
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

    public String getPixQrCode() {
        return pixQrCode;
    }

    public void setPixQrCode(String pixQrCode) {
        this.pixQrCode = pixQrCode;
    }

    public String getPixCopyPaste() {
        return pixCopyPaste;
    }

    public void setPixCopyPaste(String pixCopyPaste) {
        this.pixCopyPaste = pixCopyPaste;
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
