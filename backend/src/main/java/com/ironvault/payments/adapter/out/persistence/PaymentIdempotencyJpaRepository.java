package com.ironvault.payments.adapter.out.persistence;

import com.ironvault.payments.adapter.out.entity.PaymentIdempotencyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentIdempotencyJpaRepository extends JpaRepository<PaymentIdempotencyEntity, String> { }
