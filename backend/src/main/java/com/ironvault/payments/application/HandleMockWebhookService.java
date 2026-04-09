package com.ironvault.payments.application;

import com.ironvault.payments.application.mapper.GatewayDeclineReasonMapper;
import com.ironvault.payments.domain.enums.PaymentStatus;
import com.ironvault.payments.domain.model.Payment;
import com.ironvault.payments.domain.port.out.PaymentRepositoryPort;
import com.ironvault.payments.domain.port.out.WebhookEventRepositoryPort;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class HandleMockWebhookService {

    private final PaymentRepositoryPort paymentRepositoryPort;
    private final WebhookEventRepositoryPort webhookEventRepository;
    private final GatewayDeclineReasonMapper declineReasonMapper;

    public HandleMockWebhookService(PaymentRepositoryPort paymentRepositoryPort,
                                    WebhookEventRepositoryPort webhookEventRepository,
                                    GatewayDeclineReasonMapper declineReasonMapper) {
        this.paymentRepositoryPort = paymentRepositoryPort;
        this.webhookEventRepository = webhookEventRepository;
        this.declineReasonMapper = declineReasonMapper;
    }

    public boolean handle(String eventId,
                          String externalId,
                          String status,
                          String gatewayCode,
                          String gatewayMessage,
                          String failureReason) {

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
        payment.setGatewayCode(gatewayCode);
        payment.setGatewayMessage(gatewayMessage);
        payment.setDeclineReason(declineReasonMapper.map(gatewayCode, gatewayMessage));
        payment.setFailureReason(normalizeReason(failureReason));
        payment.setUpdatedAt(Instant.now());
        paymentRepositoryPort.save(payment);

        webhookEventRepository.save(eventId, externalId, Instant.now());

        return true;
    }

    private PaymentStatus parseStatus(String status) {
        try {
            return PaymentStatus.valueOf(status.toUpperCase());
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }
    }

    private String normalizeReason(String reason) {
        if (reason == null) return null;
        String normalized = reason.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}