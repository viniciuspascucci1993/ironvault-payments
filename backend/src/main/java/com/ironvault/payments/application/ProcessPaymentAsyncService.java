package com.ironvault.payments.application;

import com.ironvault.payments.application.mapper.GatewayDeclineReasonMapper;
import com.ironvault.payments.domain.enums.PaymentStatus;
import com.ironvault.payments.domain.port.out.PaymentGatewayPort;
import com.ironvault.payments.domain.port.out.PaymentRepositoryPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@Slf4j
public class ProcessPaymentAsyncService {

    private final PaymentRepositoryPort paymentRepositoryPort;
    private final PaymentGatewayPort paymentGatewayPort;
    private final GatewayDeclineReasonMapper declineReasonMapper;

    public ProcessPaymentAsyncService(PaymentRepositoryPort paymentRepositoryPort,
                                      PaymentGatewayPort paymentGatewayPort,
                                      GatewayDeclineReasonMapper declineReasonMapper) {
        this.paymentRepositoryPort = paymentRepositoryPort;
        this.paymentGatewayPort = paymentGatewayPort;
        this.declineReasonMapper = declineReasonMapper;
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
            payment.setExternalId(gatewayResult.getExternalId());
            payment.setStatus(gatewayResult.getStatus());
            payment.setGatewayCode(gatewayResult.getGatewayCode());
            payment.setGatewayMessage(gatewayResult.getGatewayMessage());
            payment.setDeclineReason(gatewayResult.getDeclineReason());
            payment.setFailureReason(gatewayResult.getFailureReason());
            payment.setUpdatedAt(Instant.now());
            paymentRepositoryPort.save(payment);

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

            log.error("Async gateway processing failed paymentId={} reason={}",
                    payment.getId(),
                    ex.getMessage());
        }
    }
}
