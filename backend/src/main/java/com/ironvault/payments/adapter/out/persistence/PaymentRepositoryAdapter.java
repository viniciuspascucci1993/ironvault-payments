package com.ironvault.payments.adapter.out.persistence;

import com.ironvault.payments.adapter.out.entity.PaymentEntity;
import com.ironvault.payments.adapter.out.mapper.PaymentMapper;
import com.ironvault.payments.domain.enums.PaymentStatus;
import com.ironvault.payments.domain.model.Payment;
import com.ironvault.payments.domain.port.out.PaymentRepositoryPort;
import org.springframework.stereotype.Repository;

@Repository
public class PaymentRepositoryAdapter implements PaymentRepositoryPort {

    private final PaymentJpaRepository paymentJpaRepository;
    private final PaymentMapper mapper;

    public PaymentRepositoryAdapter(PaymentJpaRepository paymentJpaRepository,
                                    PaymentMapper mapper) {
        this.paymentJpaRepository = paymentJpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Payment save(Payment payment) {

        PaymentEntity entity = mapper.toEntity(payment);
        PaymentEntity saved = paymentJpaRepository.save(entity);

        return mapper.toDomain(saved);
    }
}
