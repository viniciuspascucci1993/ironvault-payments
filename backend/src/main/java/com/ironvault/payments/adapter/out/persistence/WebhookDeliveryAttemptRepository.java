package com.ironvault.payments.adapter.out.persistence;

import com.ironvault.payments.adapter.out.entity.webhook.WebhookDeliveryAttemptEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WebhookDeliveryAttemptRepository extends JpaRepository<WebhookDeliveryAttemptEntity, UUID> { }
