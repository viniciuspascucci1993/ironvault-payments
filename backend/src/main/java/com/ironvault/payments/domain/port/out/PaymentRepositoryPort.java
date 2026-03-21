package com.ironvault.payments.domain.port.out;

import com.ironvault.payments.domain.model.Payment;

public interface PaymentRepositoryPort {

    Payment save(Payment payment);
}
