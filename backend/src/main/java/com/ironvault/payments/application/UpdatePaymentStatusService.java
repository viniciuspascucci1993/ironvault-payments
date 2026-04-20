package com.ironvault.payments.application;

import com.ironvault.payments.domain.enums.PaymentStatus;
import com.ironvault.payments.domain.exception.PaymentNotFoundException;
import com.ironvault.payments.domain.model.Payment;
import com.ironvault.payments.domain.port.in.command.UpdatePaymentStatusCommand;
import com.ironvault.payments.domain.port.in.payment.UpdatePaymentStatusUseCase;
import com.ironvault.payments.domain.port.out.PaymentRepositoryPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@Slf4j
public class UpdatePaymentStatusService implements UpdatePaymentStatusUseCase {

    private final PaymentRepositoryPort paymentRepositoryPort;

    public UpdatePaymentStatusService(PaymentRepositoryPort paymentRepositoryPort) {
        this.paymentRepositoryPort = paymentRepositoryPort;
    }

    @Override
    public Payment updateStatus(UpdatePaymentStatusCommand command) {
        Payment payment = paymentRepositoryPort.findById(command.getPaymentId())
                .orElseThrow(() -> new PaymentNotFoundException(command.getPaymentId().toString()));

        PaymentStatus previousStatus = payment.getStatus();
        validateTransition(previousStatus, command.getTargetStatus());

        payment.setStatus(command.getTargetStatus());
        payment.setUpdatedAt(Instant.now());

        if (command.getTargetStatus() == PaymentStatus.REJECTED || command.getTargetStatus() == PaymentStatus.FAILED) {
            payment.setFailureReason(normalizeFailureReason(command.getFailureReason()));
        } else {
            payment.setFailureReason(null);
        }


        Payment saved = paymentRepositoryPort.save(payment);

        log.info("Updated payment status paymentId={} from={} to={}",
                payment.getId(),
                previousStatus,
                command.getTargetStatus());

        return saved;
    }

    private void validateTransition(PaymentStatus current, PaymentStatus target) {
        if (current == target) {
            return;
        }

        boolean allowed = switch (current) {
            case CREATED -> target == PaymentStatus.PROCESSING;
            case PROCESSING -> target == PaymentStatus.APPROVED
                    || target == PaymentStatus.REJECTED
                    || target == PaymentStatus.FAILED;
            case APPROVED, REJECTED, FAILED -> false;
        };

        if (!allowed) {
            throw new IllegalStateException("Invalid status transition: " + current + " -> " + target);
        }
    }

    private String normalizeFailureReason(String failureReason) {
        if (failureReason == null) {
            return null;
        }

        String normalized = failureReason.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
