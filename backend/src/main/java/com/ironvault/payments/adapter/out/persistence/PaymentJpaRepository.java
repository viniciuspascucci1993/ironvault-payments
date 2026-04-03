package com.ironvault.payments.adapter.out.persistence;

import com.ironvault.payments.adapter.out.entity.PaymentEntity;
import com.ironvault.payments.domain.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface PaymentJpaRepository extends JpaRepository<PaymentEntity, UUID>, JpaSpecificationExecutor<PaymentEntity> {

    Page<PaymentEntity> findByStatus(PaymentStatus status, Pageable pageable);

    Optional<PaymentEntity> findByExternalId(String externalId);
}
