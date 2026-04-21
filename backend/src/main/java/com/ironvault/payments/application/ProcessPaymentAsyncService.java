package com.ironvault.payments.application;

import com.ironvault.payments.application.mapper.GatewayDeclineReasonMapper;
import com.ironvault.payments.domain.enums.PaymentStatus;
import com.ironvault.payments.domain.enums.TransactionStatus;
import com.ironvault.payments.domain.port.out.PaymentGatewayPort;
import com.ironvault.payments.domain.port.out.PaymentRepositoryPort;
import com.ironvault.payments.domain.port.out.TransactionRepositoryPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

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

    public ProcessPaymentAsyncService(PaymentRepositoryPort paymentRepositoryPort,
                                      PaymentGatewayPort paymentGatewayPort,
                                      GatewayDeclineReasonMapper declineReasonMapper,
                                      TransactionRepositoryPort transactionRepositoryPort) {
        this.paymentRepositoryPort = paymentRepositoryPort;
        this.paymentGatewayPort = paymentGatewayPort;
        this.declineReasonMapper = declineReasonMapper;
        this.transactionRepositoryPort = transactionRepositoryPort;
    }

    @Async
    public void processPayment(UUID paymentId) {
        var payment = paymentRepositoryPort.findById(paymentId)
                .orElse(null);

        if (payment == null) {
            log.warn("Async processing skipped. Payment not found paymentId={}", paymentId);
            return;
        }

        try {
            var gatewayResult = paymentGatewayPort.process(payment);

            if (gatewayResult == null) {
                throw new RuntimeException("Gateway returned null response");
            }

            payment.setExternalId(gatewayResult.getExternalId());
            payment.setStatus(gatewayResult.getStatus());
            payment.setGatewayCode(gatewayResult.getGatewayCode());
            payment.setGatewayMessage(gatewayResult.getGatewayMessage());
            payment.setDeclineReason(gatewayResult.getDeclineReason());
            payment.setFailureReason(gatewayResult.getFailureReason());
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

            log.info("Async gateway processing finished paymentId={} status={} externalId={}",
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

            log.error("Async gateway processing failed paymentId={} reason={}",
                    payment.getId(),
                    ex.getMessage());
        }
    }

    private TransactionStatus mapToTransactionStatus(PaymentStatus paymentStatus) {
        return switch (paymentStatus) {
            case APPROVED -> TransactionStatus.CAPTURED;
            case FAILED -> TransactionStatus.FAILED;
            default -> TransactionStatus.PENDING;
        };
    }
}
