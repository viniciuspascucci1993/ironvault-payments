package com.ironvault.payments.domain.port.out;

import com.ironvault.payments.domain.model.Payment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepositoryPort {

    Payment save(Payment payment);

    List<Payment> findAll();

    Optional<Payment> findById(UUID id);
}
