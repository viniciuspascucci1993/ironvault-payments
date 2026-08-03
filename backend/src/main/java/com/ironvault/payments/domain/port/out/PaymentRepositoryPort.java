package com.ironvault.payments.domain.port.out;

import com.ironvault.payments.domain.enums.PaymentStatus;
import com.ironvault.payments.domain.model.Payment;
import com.ironvault.payments.domain.query.PageQuery;
import com.ironvault.payments.domain.query.PageResult;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepositoryPort {

    Payment save(Payment payment);

    Optional<Payment> findById(UUID id);


    PageResult<Payment> findAllWithFilters(
            UUID merchantId,
            PaymentStatus status,
            String currency,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            PageQuery pageable);

    Optional<Payment> findByExternalId(String externalId);
}
