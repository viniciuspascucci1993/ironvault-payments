package com.ironvault.payments.application;

import com.ironvault.payments.domain.enums.PaymentStatus;
import com.ironvault.payments.domain.model.Payment;
import com.ironvault.payments.domain.port.in.payment.GetAllPaymentsUseCase;
import com.ironvault.payments.domain.port.out.PaymentRepositoryPort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class GetAllPaymentsService implements GetAllPaymentsUseCase {

    private final PaymentRepositoryPort paymentRepositoryPort;

    public GetAllPaymentsService(PaymentRepositoryPort paymentRepositoryPort) {
        this.paymentRepositoryPort = paymentRepositoryPort;
    }

    @Override
    public Page<Payment> getAllWithFilters(String status,
                                           String currency,
                                           BigDecimal minAmount,
                                           BigDecimal maxAmount,
                                           Pageable pageable) {



        PaymentStatus paymentStatus = null;

        if (status != null) {
            try {
                paymentStatus = PaymentStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException(
                        "Invalid status: " + status + ". Allowed: CREATED, APPROVED, REJECTED"
                );
            }
        }

        if (minAmount != null && maxAmount != null && minAmount.compareTo(maxAmount) > 0) {
            throw new IllegalArgumentException("minAmount cannot be greater than maxAmount");
        }

        if (currency != null && currency.length() != 3) {
            throw new IllegalArgumentException("Currency must be a 3-letter code (e.g. BRL, USD)");
        }

        return paymentRepositoryPort.findAllWithFilters(
                paymentStatus,
                currency,
                minAmount,
                maxAmount,
                pageable
        );
    }
}
