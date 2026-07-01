package com.ironvault.payments.adapter.out.gateway;

import com.ironvault.payments.domain.enums.DeclineReason;
import com.ironvault.payments.domain.enums.PaymentStatus;
import com.ironvault.payments.domain.model.Payment;
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
    public PaymentGatewayResult process(Payment payment) {
        String externalId = "mock-gw-" + UUID.randomUUID();

        if (isTechnicalFailure(payment.getAmount())) {
            throw new RuntimeException("Mock gateway timeout");
        }

        if (isApproved(payment.getAmount())) {
            return new PaymentGatewayResult(
                    externalId,
                    PaymentStatus.APPROVED,
                    "APPROVED",
                    "Payment approved by mock gateway",
                    null,
                    null,
                    null,  // pixQrCode
                    null   // pixCopyPaste
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
                null   // pixCopyPaste
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
