package com.ironvault.payments.application;

import com.ironvault.payments.domain.enums.PaymentStatus;
import com.ironvault.payments.domain.model.Payment;
import com.ironvault.payments.domain.model.PaymentGatewayResult;
import com.ironvault.payments.domain.port.in.webhook.HandleMercadoPagoWebhookUseCase;
import com.ironvault.payments.domain.port.out.PaymentGatewayPort;
import com.ironvault.payments.domain.port.out.PaymentRepositoryPort;
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

    public HandleMercadoPagoWebhookService(PaymentGatewayPort paymentGatewayPort,
                                           PaymentRepositoryPort paymentRepositoryPort,
                                           WebhookEventRepositoryPort webhookEventRepositoryPort) {
        this.paymentGatewayPort = paymentGatewayPort;
        this.paymentRepositoryPort = paymentRepositoryPort;
        this.webhookEventRepositoryPort = webhookEventRepositoryPort;
    }

    @Override
    public boolean handle(String eventId, String externalId) {
        if (webhookEventRepositoryPort.existsById(eventId)) {
            log.warn("Duplicate webhook event ignored. eventId={}", eventId);
            return false;
        }

        Payment payment = paymentRepositoryPort.findByExternalId(eventId)
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

        webhookEventRepositoryPort.save(eventId, externalId, Instant.now());

        log.info("Payment updated via MercadoPago webhook. externalId={} status={}", externalId,
                paymentGatewayResult.getStatus());
        return true;
    }
}
