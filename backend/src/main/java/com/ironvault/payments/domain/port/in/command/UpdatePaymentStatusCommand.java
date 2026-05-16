package com.ironvault.payments.domain.port.in.command;

import com.ironvault.payments.domain.enums.PaymentStatus;

import java.util.UUID;

public class UpdatePaymentStatusCommand {

    private final UUID paymentId;
    private final PaymentStatus targetStatus;
    private final String failureReason;

    public UpdatePaymentStatusCommand(UUID paymentId, PaymentStatus targetStatus, String failureReason) {
        this.paymentId = paymentId;
        this.targetStatus = targetStatus;
        this.failureReason = failureReason;
    }

    public UUID getPaymentId() {
        return paymentId;
    }

    public PaymentStatus getTargetStatus() {
        return targetStatus;
    }

    public String getFailureReason() {
        return failureReason;
    }
}
