package com.ironvault.payments.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ironvault.payments.domain.enums.PaymentStatus;
import com.ironvault.payments.domain.model.Payment;
import com.ironvault.payments.domain.model.PaymentIdempotency;
import com.ironvault.payments.domain.port.in.payment.CreatePaymentCommand;
import com.ironvault.payments.domain.port.in.payment.CreatePaymentUseCase;
import com.ironvault.payments.domain.port.out.PaymentIdempotencyRepositoryPort;
import com.ironvault.payments.domain.port.out.PaymentRepositoryPort;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class CreatePaymentService implements CreatePaymentUseCase {

    private final PaymentRepositoryPort paymentRepositoryPort;
    private final PaymentIdempotencyRepositoryPort idempotencyRepository;
    private final ObjectMapper objectMapper;

    public CreatePaymentService(PaymentRepositoryPort paymentRepositoryPort,
                                PaymentIdempotencyRepositoryPort idempotencyRepository,
                                ObjectMapper objectMapper) {
        this.paymentRepositoryPort = paymentRepositoryPort;
        this.idempotencyRepository = idempotencyRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public Payment create(CreatePaymentCommand command, String idempotencyKey) {

        String requestHash = generateHash(command);
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {

            var existing = idempotencyRepository.findByKey(idempotencyKey);

            if (existing.isPresent()) {

                if (!existing.get().getRequestHash().equals(requestHash)) {
                    throw new IllegalStateException(
                            "Idempotency key already used with different payload"
                    );
                }

                return deserialize(existing.get().getResponse());
            }
        }

        // processamento normal
        Payment payment = new Payment(
                UUID.randomUUID(),
                command.getAmount(),
                command.getCurrency(),
                PaymentStatus.CREATED,
                Instant.now()
        );

        Payment saved = paymentRepositoryPort.save(payment);

        // 🔥 só salva se tiver key
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {

            PaymentIdempotency record = new PaymentIdempotency(
                    idempotencyKey,
                    requestHash,
                    serialize(saved),
                    Instant.now()
            );

            idempotencyRepository.save(record);
        }

        return saved;
    }

    private String serialize(Payment payment) {
        try {
            return objectMapper.writeValueAsString(payment);
        } catch (Exception e) {
            throw new RuntimeException("Error serializing payment", e);
        }
    }

    private Payment deserialize(String data) {
        try {
            return objectMapper.readValue(data, Payment.class);
        } catch (Exception e) {
            throw new RuntimeException("Error deserializing payment", e);
        }
    }

    private String generateHash(CreatePaymentCommand command) {
        return command.getAmount() + "|" + command.getCurrency();
    }
}
