package com.ironvault.payments.domain.model;

import com.ironvault.payments.domain.enums.PaymentStatus;

public class PaymentGatewayResult {

    private final String externalId;
    private final PaymentStatus status;
    private final String failureReason;

    public PaymentGatewayResult(String externalId, PaymentStatus status, String failureReason) {
        this.externalId = externalId;
        this.status = status;
        this.failureReason = failureReason;
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
}
