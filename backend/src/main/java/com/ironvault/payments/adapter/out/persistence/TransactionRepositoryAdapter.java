package com.ironvault.payments.adapter.out.persistence;

import com.ironvault.payments.adapter.out.entity.TransactionEntity;
import com.ironvault.payments.adapter.out.mapper.TransactionMapper;
import com.ironvault.payments.adapter.out.persistence.specification.TransactionSpecification;
import com.ironvault.payments.domain.enums.TransactionStatus;
import com.ironvault.payments.domain.enums.TransactionType;
import com.ironvault.payments.domain.model.Transaction;
import com.ironvault.payments.domain.port.out.TransactionRepositoryPort;
import com.ironvault.payments.domain.query.PageQuery;
import com.ironvault.payments.domain.query.PageResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class TransactionRepositoryAdapter implements TransactionRepositoryPort {

    private final TransactionJpaRepository jpaRepository;
    private final TransactionMapper mapper;

    public TransactionRepositoryAdapter(TransactionJpaRepository jpaRepository, TransactionMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Transaction save(Transaction transaction) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(transaction)));
    }

    @Override
    public List<Transaction> findByPaymentId(UUID paymentId) {
        return jpaRepository.findByPaymentId(paymentId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public PageResult<Transaction> findAll(UUID merchantId, TransactionType type, TransactionStatus status, PageQuery pageQuery) {
        var spec = TransactionSpecification.withFilters(merchantId, type, status);
        var pageable = PageRequest.of(pageQuery.getPage(), pageQuery.getSize());

        Page<TransactionEntity> page = jpaRepository.findAll(spec, pageable);

        return new PageResult<>(
                page.getContent().stream().map(mapper::toDomain).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
