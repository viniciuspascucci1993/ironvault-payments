package com.ironvault.payments.application;

import com.ironvault.payments.application.mapper.GatewayDeclineReasonMapper;
import com.ironvault.payments.domain.enums.PaymentStatus;
import com.ironvault.payments.domain.enums.TransactionStatus;
import com.ironvault.payments.domain.model.Payment;
import com.ironvault.payments.domain.port.in.webhook.HandleWebhookUseCase;
import com.ironvault.payments.domain.port.out.PaymentRepositoryPort;
import com.ironvault.payments.domain.port.out.TransactionRepositoryPort;
import com.ironvault.payments.domain.port.out.WebhookEventRepositoryPort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;

import static com.ironvault.payments.domain.enums.PaymentStatus.APPROVED;
import static com.ironvault.payments.domain.enums.PaymentStatus.REJECTED;

@Service
public class HandleMockWebhookService implements HandleWebhookUseCase {

    private final PaymentRepositoryPort paymentRepositoryPort;
    private final WebhookEventRepositoryPort webhookEventRepository;
    private final GatewayDeclineReasonMapper declineReasonMapper;
    private final TransactionRepositoryPort transactionRepositoryPort;

    public HandleMockWebhookService(PaymentRepositoryPort paymentRepositoryPort,
                                    WebhookEventRepositoryPort webhookEventRepository,
                                    GatewayDeclineReasonMapper declineReasonMapper,
                                    TransactionRepositoryPort transactionRepositoryPort) {
        this.paymentRepositoryPort = paymentRepositoryPort;
        this.webhookEventRepository = webhookEventRepository;
        this.declineReasonMapper = declineReasonMapper;
        this.transactionRepositoryPort = transactionRepositoryPort;
    }

    @Override
    @Transactional
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
        if (targetStatus != APPROVED
                && targetStatus != REJECTED
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

        transactionRepositoryPort.findByPaymentId(payment.getId())
                .stream()
                .findFirst()
                .ifPresent(transaction -> {
                    transaction.setStatus(mapToTransactionStatus(targetStatus));
                    transaction.setExternalId(externalId);
                    transaction.setGatewayCode(gatewayCode);
                    transaction.setGatewayMessage(gatewayMessage);
                    transaction.setUpdatedAt(Instant.now());
                    transactionRepositoryPort.save(transaction);
                });

        webhookEventRepository.save(eventId, externalId, Instant.now());

        return true;
    }

    private TransactionStatus mapToTransactionStatus(PaymentStatus paymentStatus) {
        return switch (paymentStatus) {
            case APPROVED -> TransactionStatus.CAPTURED;
            case REJECTED -> TransactionStatus.CANCELLED;
            case FAILED -> TransactionStatus.FAILED;
            default -> TransactionStatus.PENDING;
        };
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