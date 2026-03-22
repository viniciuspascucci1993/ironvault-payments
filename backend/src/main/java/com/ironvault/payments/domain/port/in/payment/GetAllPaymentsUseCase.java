package com.ironvault.payments.domain.port.in.payment;

import com.ironvault.payments.domain.enums.PaymentStatus;
import com.ironvault.payments.domain.model.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface GetAllPaymentsUseCase {

    Page<Payment> getAllWithFilters(
            String status,
            String currency,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            Pageable pageable
    );
}
