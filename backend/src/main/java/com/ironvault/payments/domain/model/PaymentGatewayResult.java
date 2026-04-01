package com.ironvault.payments.domain.model;

import com.ironvault.payments.domain.enums.DeclineReason;
import com.ironvault.payments.domain.enums.PaymentStatus;

public class PaymentGatewayResult {

    private final String externalId;
    private final PaymentStatus status;
    private final String gatewayCode;
    private final String gatewayMessage;
    private final DeclineReason declineReason;
    private final String failureReason;

    public PaymentGatewayResult(String externalId,
                                PaymentStatus status,
                                String gatewayCode,
                                String gatewayMessage,
                                DeclineReason declineReason,
                                String failureReason) {
        this.externalId = externalId;
        this.status = status;
        this.gatewayCode = gatewayCode;
        this.gatewayMessage = gatewayMessage;
        this.declineReason = declineReason;
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

    public String getGatewayCode() {
        return gatewayCode;
    }

    public String getGatewayMessage() {
        return gatewayMessage;
    }

    public DeclineReason getDeclineReason() {
        return declineReason;
    }
}
