package com.ironvault.payments.domain.port.out;

import com.ironvault.payments.domain.model.PaymentIdempotency;

import java.util.Optional;

public interface PaymentIdempotencyRepositoryPort {

    Optional<PaymentIdempotency> findByKey(String key);

    PaymentIdempotency save(PaymentIdempotency idempotency);
}
