package com.ironvault.payments.adapter.out.persistence;

import com.ironvault.payments.adapter.out.entity.PaymentWebhookEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentWebhookEventJpaRepository  extends JpaRepository<PaymentWebhookEventEntity, String> { }
