package com.ironvault.payments.application;

import com.ironvault.payments.adapter.out.client.MerchantsClient;
import com.ironvault.payments.adapter.out.client.NotificationClient;
import com.ironvault.payments.application.mapper.GatewayDeclineReasonMapper;
import com.ironvault.payments.domain.enums.OutboxEventStatus;
import com.ironvault.payments.domain.enums.PaymentStatus;
import com.ironvault.payments.domain.enums.TransactionStatus;
import com.ironvault.payments.domain.model.OutboxEvent;
import com.ironvault.payments.domain.model.PaymentGatewayRequest;
import com.ironvault.payments.domain.port.out.OutboxEventRepositoryPort;
import com.ironvault.payments.domain.port.out.PaymentGatewayPort;
import com.ironvault.payments.domain.port.out.PaymentRepositoryPort;
import com.ironvault.payments.domain.port.out.TransactionRepositoryPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;

import static com.ironvault.payments.domain.enums.TransactionStatus.CANCELLED;


@Service
@Slf4j
public class ProcessPaymentAsyncService {

    private final PaymentRepositoryPort paymentRepositoryPort;
    private final PaymentGatewayPort paymentGatewayPort;
    private final GatewayDeclineReasonMapper declineReasonMapper;
    private final TransactionRepositoryPort transactionRepositoryPort;
    private final OutboxEventRepositoryPort outboxEventRepository;
    private final NotificationClient notificationClient;
    private final MerchantsClient merchantsClient;

    @Value("${app.ironvault.commission-percentage}")
    private BigDecimal commissionPercentage;

    public ProcessPaymentAsyncService(PaymentRepositoryPort paymentRepositoryPort,
                                      PaymentGatewayPort paymentGatewayPort,
                                      GatewayDeclineReasonMapper declineReasonMapper,
                                      TransactionRepositoryPort transactionRepositoryPort,
                                      OutboxEventRepositoryPort outboxEventRepository,
                                      NotificationClient notificationClient,
                                      MerchantsClient merchantsClient) {
        this.paymentRepositoryPort = paymentRepositoryPort;
        this.paymentGatewayPort = paymentGatewayPort;
        this.declineReasonMapper = declineReasonMapper;
        this.transactionRepositoryPort = transactionRepositoryPort;
        this.outboxEventRepository = outboxEventRepository;
        this.notificationClient = notificationClient;
        this.merchantsClient = merchantsClient;
    }

    public void processPayment(UUID paymentId, OutboxEvent outboxEvent) {
        var payment = paymentRepositoryPort.findById(paymentId)
                .orElse(null);

        if (payment == null) {
            log.warn("Processing skipped. Payment not found paymentId={}", paymentId);
            markOutbox(outboxEvent, OutboxEventStatus.FAILED);
            return;
        }

        try {
            String sellerAccessToken = merchantsClient.getMerchantAccessToken(payment.getMerchantId());
            BigDecimal applicationFee = payment.getAmount()
                    .multiply(commissionPercentage)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            var gatewayResult = paymentGatewayPort.process(
                    new PaymentGatewayRequest(payment, sellerAccessToken, applicationFee)
            );

            if (gatewayResult == null) {
                throw new RuntimeException("Gateway returned null response");
            }

            payment.setExternalId(gatewayResult.getExternalId());
            payment.setStatus(gatewayResult.getStatus());
            payment.setGatewayCode(gatewayResult.getGatewayCode());
            payment.setGatewayMessage(gatewayResult.getGatewayMessage());
            payment.setDeclineReason(gatewayResult.getDeclineReason());
            payment.setFailureReason(gatewayResult.getFailureReason());
            payment.setApplicationFee(applicationFee);
            payment.setNetAmount(gatewayResult.getNetAmount());
            payment.setPixQrCode(gatewayResult.getPixQrCode());
            payment.setPixCopyPaste(gatewayResult.getPixCopyPaste());
            payment.setUpdatedAt(Instant.now());
            paymentRepositoryPort.save(payment);

            transactionRepositoryPort.findByPaymentId(paymentId)
                    .stream()
                    .findFirst()
                    .ifPresent(tx -> {
                        tx.setExternalId(gatewayResult.getExternalId());
                        tx.setStatus(mapToTransactionStatus(gatewayResult.getStatus()));
                        tx.setGatewayCode(gatewayResult.getGatewayCode());
                        tx.setGatewayMessage(gatewayResult.getGatewayMessage());
                        tx.setUpdatedAt(Instant.now());
                        transactionRepositoryPort.save(tx);
                    });

            markOutbox(outboxEvent, OutboxEventStatus.PROCESSED);

            if (payment.getPixCopyPaste() != null) {
                notificationClient.sendPixGeneratedEvent(
                        payment.getPayerEmail(),
                        payment.getAmount().toPlainString(),
                        payment.getCurrency(),
                        payment.getPixCopyPaste()
                );
            }

            log.info("Gateway processing finished paymentId={} status={} externalId={}",
                    payment.getId(),
                    gatewayResult.getStatus(),
                    gatewayResult.getExternalId());


        } catch (Exception ex) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setGatewayCode("TECHNICAL_ERROR");
            payment.setGatewayMessage(ex.getMessage());
            payment.setDeclineReason(declineReasonMapper.map("TECHNICAL_ERROR", ex.getMessage()));
            payment.setFailureReason(ex.getMessage());
            payment.setUpdatedAt(Instant.now());
            paymentRepositoryPort.save(payment);

            transactionRepositoryPort.findByPaymentId(paymentId)
                    .stream()
                    .findFirst()
                    .ifPresent(tx -> {
                        tx.setStatus(TransactionStatus.FAILED);
                        tx.setGatewayCode("TECHNICAL_ERROR");
                        tx.setGatewayMessage(ex.getMessage());
                        tx.setUpdatedAt(Instant.now());
                        transactionRepositoryPort.save(tx);
                    });

            markOutbox(outboxEvent, OutboxEventStatus.FAILED);

            notificationClient.sendPaymentFailedEvent(
                    payment.getPayerEmail(),
                    payment.getAmount().toPlainString(),
                    payment.getFailureReason()
            );

            log.error("Async gateway processing failed paymentId={} reason={}",
                    payment.getId(),
                    ex.getMessage());
        }
    }

    private TransactionStatus mapToTransactionStatus(PaymentStatus paymentStatus) {
        return switch (paymentStatus) {
            case APPROVED -> TransactionStatus.CAPTURED;
            case REJECTED -> CANCELLED;
            case FAILED -> TransactionStatus.FAILED;
            default -> TransactionStatus.PENDING;
        };
    }

    private void markOutbox(OutboxEvent event, OutboxEventStatus status) {
        event.setStatus(status);
        event.setProcessedAt(Instant.now());
        outboxEventRepository.save(event);
    }
}
