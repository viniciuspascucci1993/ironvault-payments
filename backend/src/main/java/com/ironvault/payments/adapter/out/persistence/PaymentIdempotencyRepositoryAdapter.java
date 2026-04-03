package com.ironvault.payments.adapter.out.persistence;

import com.ironvault.payments.adapter.out.entity.PaymentIdempotencyEntity;
import com.ironvault.payments.domain.model.PaymentIdempotency;
import com.ironvault.payments.domain.port.out.PaymentIdempotencyRepositoryPort;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class PaymentIdempotencyRepositoryAdapter implements PaymentIdempotencyRepositoryPort {

    private final PaymentIdempotencyJpaRepository repository;

    public PaymentIdempotencyRepositoryAdapter(PaymentIdempotencyJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<PaymentIdempotency> findByKey(String key) {
        return repository.findById(key)
                .map(e -> new PaymentIdempotency(
                        e.getIdempotencyKey(),
                        e.getRequestHash(),
                        e.getResponse(),
                        e.getPaymentId(),
                        e.getCreatedAt()
                ));
    }

    @Override
    public PaymentIdempotency save(PaymentIdempotency idempotency) {
        PaymentIdempotencyEntity entity = new PaymentIdempotencyEntity();

        entity.setIdempotencyKey(idempotency.getIdempotencyKey());
        entity.setRequestHash(idempotency.getRequestHash());
        entity.setResponse(idempotency.getResponse());
        entity.setPaymentId(idempotency.getPaymentId());
        entity.setCreatedAt(idempotency.getCreatedAt());

        repository.save(entity);

        return idempotency;
    }
}
