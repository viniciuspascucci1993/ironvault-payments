package com.ironvault.payments.application;

import com.ironvault.payments.domain.enums.PaymentStatus;
import com.ironvault.payments.domain.enums.TransactionStatus;
import com.ironvault.payments.domain.model.Payment;
import com.ironvault.payments.domain.model.PaymentGatewayResult;
import com.ironvault.payments.domain.port.in.webhook.HandleMercadoPagoWebhookUseCase;
import com.ironvault.payments.domain.port.out.PaymentGatewayPort;
import com.ironvault.payments.domain.port.out.PaymentRepositoryPort;
import com.ironvault.payments.domain.port.out.TransactionRepositoryPort;
import com.ironvault.payments.domain.port.out.WebhookEventRepositoryPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@Slf4j
public class HandleMercadoPagoWebhookService implements HandleMercadoPagoWebhookUseCase {

    private final PaymentGatewayPort paymentGatewayPort;
    private final PaymentRepositoryPort paymentRepositoryPort;
    private final WebhookEventRepositoryPort webhookEventRepositoryPort;
    private final TransactionRepositoryPort transactionRepositoryPort;

    public HandleMercadoPagoWebhookService(PaymentGatewayPort paymentGatewayPort,
                                           PaymentRepositoryPort paymentRepositoryPort,
                                           WebhookEventRepositoryPort webhookEventRepositoryPort,
                                           TransactionRepositoryPort transactionRepositoryPort) {
        this.paymentGatewayPort = paymentGatewayPort;
        this.paymentRepositoryPort = paymentRepositoryPort;
        this.webhookEventRepositoryPort = webhookEventRepositoryPort;
        this.transactionRepositoryPort = transactionRepositoryPort;
    }

    @Override
    public boolean handle(String eventId, String externalId) {
        if (webhookEventRepositoryPort.existsById(eventId)) {
            log.warn("Duplicate webhook event ignored. eventId={}", eventId);
            return false;
        }

        Payment payment = paymentRepositoryPort.findByExternalId(externalId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found for externalId: " + externalId));

        PaymentGatewayResult paymentGatewayResult = paymentGatewayPort.getPaymentStatus(externalId);

        if (paymentGatewayResult.getStatus() == PaymentStatus.PROCESSING) {
            log.info("Payment still processing, skipping update. externalId={}", externalId);
            return false;
        }

        payment.setStatus(paymentGatewayResult.getStatus());
        payment.setGatewayCode(paymentGatewayResult.getGatewayCode());
        payment.setGatewayMessage(paymentGatewayResult.getGatewayMessage());
        payment.setUpdatedAt(Instant.now());
        paymentRepositoryPort.save(payment);

        transactionRepositoryPort.findByPaymentId(payment.getId())
                        .stream()
                                .findFirst()
                                        .ifPresent((tx -> {
                                            tx.setStatus(mapToTransactionStatus(paymentGatewayResult.getStatus()));
                                            tx.setGatewayCode(paymentGatewayResult.getGatewayCode());
                                            tx.setGatewayMessage(paymentGatewayResult.getGatewayMessage());
                                            tx.setUpdatedAt(Instant.now());
                                            transactionRepositoryPort.save(tx);
                                        }));


        webhookEventRepositoryPort.save(eventId, externalId, Instant.now());

        log.info("Payment updated via MercadoPago webhook. externalId={} status={}", externalId,
                paymentGatewayResult.getStatus());
        return true;
    }

    private TransactionStatus mapToTransactionStatus(PaymentStatus status) {
        return switch (status) {
            case APPROVED -> TransactionStatus.CAPTURED;
            case REJECTED -> TransactionStatus.CANCELLED;
            case FAILED -> TransactionStatus.FAILED;
            default ->  TransactionStatus.PENDING;
        };
    }
}
