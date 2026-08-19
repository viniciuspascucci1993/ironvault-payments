package com.ironvault.payments.domain.model;

import com.ironvault.payments.domain.enums.DeclineReason;
import com.ironvault.payments.domain.enums.PaymentStatus;

import java.math.BigDecimal;

public class PaymentGatewayResult {

    private final String externalId;
    private final PaymentStatus status;
    private final String gatewayCode;
    private final String gatewayMessage;
    private final DeclineReason declineReason;
    private final String failureReason;
    private final String pixQrCode;
    private final String pixCopyPaste;
    private final BigDecimal netAmount;

    public PaymentGatewayResult(String externalId,
                                PaymentStatus status,
                                String gatewayCode,
                                String gatewayMessage,
                                DeclineReason declineReason,
                                String failureReason,
                                String pixQrCode,
                                String pixCopyPaste,
                                BigDecimal netAmount) {
        this.externalId = externalId;
        this.status = status;
        this.gatewayCode = gatewayCode;
        this.gatewayMessage = gatewayMessage;
        this.declineReason = declineReason;
        this.failureReason = failureReason;
        this.pixQrCode = pixQrCode;
        this.pixCopyPaste = pixCopyPaste;
        this.netAmount = netAmount;
    }

    public String getExternalId() {
        return externalId;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public String getGatewayCode() {
        return gatewayCode;
    }

    public String getGatewayMessage() {
        return gatewayMessage;
    }

    public DeclineReason getDeclineReason() {
        return declineReason;
    }

    public String getPixQrCode() {
        return pixQrCode;
    }

    public String getPixCopyPaste() {
        return pixCopyPaste;
    }

    public BigDecimal getNetAmount() {
        return netAmount;
    }
}
