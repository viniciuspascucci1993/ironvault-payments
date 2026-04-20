package com.ironvault.payments.adapter.out.persistence;

import com.ironvault.payments.adapter.out.entity.TransactionEntity;
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

    public TransactionRepositoryAdapter(TransactionJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Transaction save(Transaction transaction) {
        return toDomain(jpaRepository.save(toEntity(transaction)));
    }

    @Override
    public List<Transaction> findByPaymentId(UUID paymentId) {
        return jpaRepository.findByPaymentId(paymentId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public PageResult<Transaction> findAll(TransactionType type, TransactionStatus status, PageQuery pageQuery) {
        var pageable = PageRequest.of(pageQuery.getPage(), pageQuery.getSize());
        Page<TransactionEntity> page;

        if (type != null && status != null) {
            page = jpaRepository.findByTypeAndStatus(type, status, pageable);
        } else if (type != null) {
            page = jpaRepository.findByType(type, pageable);
        } else if (status != null) {
            page = jpaRepository.findByStatus(status, pageable);
        } else {
            page = jpaRepository.findAll(pageable);
        }

        return new PageResult<>(
                page.getContent().stream().map(this::toDomain).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    private TransactionEntity toEntity(Transaction transaction) {
        TransactionEntity e = new TransactionEntity();
        e.setId(transaction.getId());
        e.setPaymentId(transaction.getPaymentId());
        e.setExternalId(transaction.getExternalId());
        e.setType(transaction.getType());
        e.setStatus(transaction.getStatus());
        e.setAmount(transaction.getAmount());
        e.setCurrency(transaction.getCurrency());
        e.setGatewayCode(transaction.getGatewayCode());
        e.setGatewayMessage(transaction.getGatewayMessage());
        e.setCreatedAt(transaction.getCreatedAt());
        e.setUpdatedAt(transaction.getUpdatedAt());

        return e;
    }

    private Transaction toDomain(TransactionEntity e) {
        return new Transaction(
                e.getId(),
                e.getPaymentId(),
                e.getExternalId(),
                e.getType(),
                e.getStatus(),
                e.getAmount(),
                e.getCurrency(),
                e.getGatewayCode(),
                e.getGatewayMessage(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }
}
