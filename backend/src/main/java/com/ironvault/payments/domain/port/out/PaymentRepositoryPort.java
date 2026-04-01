package com.ironvault.payments.domain.port.out;

import com.ironvault.payments.domain.enums.PaymentStatus;
import com.ironvault.payments.domain.model.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepositoryPort {

    Payment save(Payment payment);

    Optional<Payment> findById(UUID id);


    Page<Payment> findAllWithFilters(
            PaymentStatus status,
            String currency,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            Pageable pageable);

    Optional<Payment> findByExternalId(String externalId);
}
