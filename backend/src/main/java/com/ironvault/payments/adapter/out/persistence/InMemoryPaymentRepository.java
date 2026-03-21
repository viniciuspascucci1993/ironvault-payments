package com.ironvault.payments.adapter.out.persistence;

import com.ironvault.payments.domain.model.Payment;
import com.ironvault.payments.domain.port.out.PaymentRepositoryPort;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryPaymentRepository implements PaymentRepositoryPort {

    private final Map<UUID, Payment> storage = new ConcurrentHashMap<>();

    @Override
    public Payment save(Payment payment) {
        storage.put(payment.id(), payment);
        return payment;
    }
}
