package com.ironvault.payments.application;

import com.ironvault.payments.domain.exception.PaymentAccessDeniedException;
import com.ironvault.payments.domain.exception.PaymentNotFoundException;
import com.ironvault.payments.domain.model.Payment;
import com.ironvault.payments.domain.port.in.payment.GetPaymentByIdUseCase;
import com.ironvault.payments.domain.port.out.PaymentRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GetPaymentByIdService implements GetPaymentByIdUseCase {

    private final PaymentRepositoryPort paymentRepositoryPort;

    public GetPaymentByIdService(PaymentRepositoryPort paymentRepositoryPort) {
        this.paymentRepositoryPort = paymentRepositoryPort;
    }

    @Override
    public Payment getById(UUID id, UUID requesterMerchantId, boolean isAdmin) {
        Payment payment = paymentRepositoryPort.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found"));

        if (!isAdmin && !payment.getMerchantId().equals(requesterMerchantId)) {
            throw new PaymentAccessDeniedException("You do not have access to this payment");
        }

        return payment;
    }
}
