package com.ironvault.payments.adapter.out.persistence;

import com.ironvault.payments.adapter.out.entity.TransactionEntity;
import com.ironvault.payments.domain.enums.TransactionStatus;
import com.ironvault.payments.domain.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface TransactionJpaRepository extends JpaRepository<TransactionEntity, UUID>,
        JpaSpecificationExecutor<TransactionEntity> {

    List<TransactionEntity> findByPaymentId(UUID paymentId);
}
