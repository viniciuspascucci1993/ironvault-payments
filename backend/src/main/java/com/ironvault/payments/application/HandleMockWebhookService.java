package com.ironvault.payments.application;

import com.ironvault.payments.adapter.out.entity.PaymentWebhookEventEntity;
import com.ironvault.payments.adapter.out.persistence.PaymentWebhookEventJpaRepository;
import com.ironvault.payments.domain.enums.PaymentStatus;
import com.ironvault.payments.domain.model.Payment;
import com.ironvault.payments.domain.port.out.PaymentRepositoryPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class HandleMockWebhookService {

        private final PaymentRepositoryPort paymentRepositoryPort;
        private final PaymentWebhookEventJpaRepository webhookEventRepository;
        private final String webhookSecret;

        public HandleMockWebhookService(PaymentRepositoryPort paymentRepositoryPort,
                                        PaymentWebhookEventJpaRepository webhookEventRepository,
                                        @Value("${app.webhook.mock-secret:ironvault-webhook-secret}") String webhookSecret) {
            this.paymentRepositoryPort = paymentRepositoryPort;
            this.webhookEventRepository = webhookEventRepository;
            this.webhookSecret = webhookSecret;
        }

        public boolean handle(String signature,
                              String eventId,
                              String externalId,
                              String status,
                              String failureReason) {

            validateSignature(signature);

            if (webhookEventRepository.existsById(eventId)) {
                return false;
            }

            Payment payment = paymentRepositoryPort.findByExternalId(externalId)
                    .orElseThrow(() -> new IllegalArgumentException("Payment not found for externalId: " + externalId));

            PaymentStatus targetStatus = parseStatus(status);
            if (targetStatus != PaymentStatus.APPROVED
                    && targetStatus != PaymentStatus.REJECTED
                    && targetStatus != PaymentStatus.FAILED) {
                throw new IllegalArgumentException("Unsupported webhook status: " + status);
            }

            payment.setStatus(targetStatus);
            payment.setFailureReason(normalizeReason(failureReason));
            payment.setUpdatedAt(Instant.now());
            paymentRepositoryPort.save(payment);

            webhookEventRepository.save(
                    new PaymentWebhookEventEntity(eventId, externalId, Instant.now())
            );

            return true;
        }

        private void validateSignature(String signature) {
            if (signature == null || signature.isBlank() || !webhookSecret.equals(signature)) {
                throw new IllegalArgumentException("Invalid webhook signature");
            }
        }

        private PaymentStatus parseStatus(String status) {
            try {
                return PaymentStatus.valueOf(status.toUpperCase());
            } catch (Exception ex) {
                throw new IllegalArgumentException("Invalid status: " + status);
            }
        }

        private String normalizeReason(String reason) {
            if (reason == null) {
                return null;
            }
            String normalized = reason.trim();
            return normalized.isEmpty() ? null : normalized;
        }
}
