package com.ironvault.payments.adapter.out.entity;

import com.ironvault.payments.domain.enums.DeclineReason;
import com.ironvault.payments.domain.enums.PaymentMethod;
import com.ironvault.payments.domain.enums.PaymentStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payments")
public class PaymentEntity {

    @Id
    private UUID id;
    private BigDecimal amount;
    private String currency;

    @Enumerated(EnumType.STRING)
    private PaymentStatus  status;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    private String description;
    private String externalId;

    private String gatewayCode;
    private String gatewayMessage;
    @Enumerated(EnumType.STRING)
    private DeclineReason declineReason;

    @Column(name = "pix_qr_code")
    private String pixQrCode;

    @Column(name = "pix_copy_paste")
    private String pixCopyPaste;

    private String failureReason;

    private Instant createdAt;
    private Instant updatedAt;


    public PaymentEntity() { }

    public PaymentEntity(UUID id, BigDecimal amount,
                         String currency,
                         PaymentStatus status,
                         PaymentMethod paymentMethod,
                         String description,
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
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.paymentMethod = paymentMethod;
        this.description = description;
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
