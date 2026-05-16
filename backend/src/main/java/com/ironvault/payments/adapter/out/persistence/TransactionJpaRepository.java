package com.ironvault.payments.adapter.out.persistence;

import com.ironvault.payments.adapter.out.entity.TransactionEntity;
import com.ironvault.payments.domain.enums.TransactionStatus;
import com.ironvault.payments.domain.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TransactionJpaRepository extends JpaRepository<TransactionEntity, UUID> {

    List<TransactionEntity> findByPaymentId(UUID paymentId);
    Page<TransactionEntity> findByTypeAndStatus(TransactionType type, TransactionStatus status, Pageable pageable);
    Page<TransactionEntity> findByType(TransactionType type, Pageable pageable);
    Page<TransactionEntity> findByStatus(TransactionStatus status, Pageable pageable);
}
