package com.ironvault.payments.application;

import com.ironvault.payments.domain.model.Payment;
import com.ironvault.payments.domain.port.in.CreatePaymentUseCase;
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
    public Payment create(Command command) {
        Payment payment = new Payment(
                UUID.randomUUID(),
                command.amount(),
                command.currency(),
                "CREATED",
                Instant.now()
        );
        return paymentRepositoryPort.save(payment);
    }
}
