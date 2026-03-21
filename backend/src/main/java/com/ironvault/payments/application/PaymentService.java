package com.ironvault.payments.application;

import com.ironvault.payments.domain.enums.PaymentStatus;
import com.ironvault.payments.domain.model.Payment;
import com.ironvault.payments.domain.port.in.payment.CreatePaymentCommand;
import com.ironvault.payments.domain.port.in.payment.CreatePaymentUseCase;
import com.ironvault.payments.domain.port.out.PaymentRepositoryPort;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class PaymentService implements CreatePaymentUseCase {

    private final PaymentRepositoryPort paymentRepositoryPort;

    public PaymentService(PaymentRepositoryPort paymentRepositoryPort) {
        this.paymentRepositoryPort = paymentRepositoryPort;
    }

    @Override
    public Payment create(CreatePaymentCommand command) {
        Payment payment = new Payment(
                UUID.randomUUID(),
                command.getAmount(),
                command.getCurrency(),
                PaymentStatus.CREATED,
                Instant.now()
        );
        return paymentRepositoryPort.save(payment);
    }
}
