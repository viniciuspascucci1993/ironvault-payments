package com.ironvault.payments.adapter.out.gateway;

import com.ironvault.payments.domain.enums.DeclineReason;
import com.ironvault.payments.domain.enums.PaymentStatus;
import com.ironvault.payments.domain.model.Payment;
import com.ironvault.payments.domain.model.PaymentGatewayRequest;
import com.ironvault.payments.domain.model.PaymentGatewayResult;
import com.ironvault.payments.domain.port.out.PaymentGatewayPort;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@Profile("!prod")
public class MockPaymentGatewayAdapter implements PaymentGatewayPort {
    @Override
    public PaymentGatewayResult process(PaymentGatewayRequest request) {
        String externalId = "mock-gw-" + UUID.randomUUID();

        if (isTechnicalFailure(request.getPayment().getAmount())) {
            throw new RuntimeException("Mock gateway timeout");
        }

        if (isApproved(request.getPayment().getAmount())) {
            return new PaymentGatewayResult(
                    externalId,
                    PaymentStatus.APPROVED,
                    "APPROVED",
                    "Payment approved by mock gateway",
                    null,
                    null,
                    null,  // pixQrCode
                    null,   // pixCopyPaste,
                    null
            );
        }

        return new PaymentGatewayResult(
                externalId,
                PaymentStatus.REJECTED,
                "INSUFFICIENT_FUNDS",
                "Mock gateway rejected payment",
                DeclineReason.INSUFFICIENT_FUNDS,
                "Mock gateway rejected payment",
                null,  // pixQrCode
                null,   // pixCopyPaste
                null
        );
    }

    @Override
    public PaymentGatewayResult getPaymentStatus(String externalId) {
        return new PaymentGatewayResult(
                externalId,
                PaymentStatus.APPROVED,
                "APPROVED",
                "Mock payment approved",
                null,
                null,
                null,
                null,
                null
        );
    }

    private boolean isApproved(BigDecimal amount) {
        return amount.remainder(BigDecimal.valueOf(2)).compareTo(BigDecimal.ZERO) == 0;
    }

    private boolean isTechnicalFailure(BigDecimal amount) {
        return amount.remainder(BigDecimal.valueOf(7)).compareTo(BigDecimal.ZERO) == 0;
    }
}
