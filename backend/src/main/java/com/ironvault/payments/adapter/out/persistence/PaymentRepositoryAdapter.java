package com.ironvault.payments.adapter.out.persistence;

import com.ironvault.payments.adapter.out.entity.PaymentEntity;
import com.ironvault.payments.adapter.out.mapper.PaymentMapper;
import com.ironvault.payments.adapter.out.persistence.specification.PaymentSpecification;
import com.ironvault.payments.domain.enums.PaymentStatus;
import com.ironvault.payments.domain.model.Payment;
import com.ironvault.payments.domain.port.out.PaymentRepositoryPort;
import com.ironvault.payments.domain.query.PageQuery;
import com.ironvault.payments.domain.query.PageResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

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


    @Override
    public Optional<Payment> findById(UUID id) {
        return paymentJpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public PageResult<Payment> findAllWithFilters(PaymentStatus status,
                                                  String currency,
                                                  BigDecimal minAmount,
                                                  BigDecimal maxAmount,
                                                  PageQuery pageQuery) {
        var spec = PaymentSpecification.withFilters(status, currency, minAmount, maxAmount);
        var pageable = PageRequest.of(pageQuery.getPage(), pageQuery.getSize());
        Page<Payment> page = paymentJpaRepository.findAll(spec, pageable)
                .map(mapper::toDomain);

        return new PageResult<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    @Override
    public Optional<Payment> findByExternalId(String externalId) {
        return paymentJpaRepository.findByExternalId(externalId)
                .map(mapper::toDomain);
    }

}
