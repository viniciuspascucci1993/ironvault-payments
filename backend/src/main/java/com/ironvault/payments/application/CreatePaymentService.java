package com.ironvault.payments.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ironvault.payments.domain.enums.PaymentMethod;
import com.ironvault.payments.domain.enums.PaymentStatus;
import com.ironvault.payments.domain.model.Payment;
import com.ironvault.payments.domain.model.PaymentIdempotency;
import com.ironvault.payments.domain.port.in.payment.CreatePaymentCommand;
import com.ironvault.payments.domain.port.in.payment.CreatePaymentUseCase;
import com.ironvault.payments.domain.port.out.PaymentIdempotencyRepositoryPort;
import com.ironvault.payments.domain.port.out.PaymentRepositoryPort;
import java.time.Instant;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
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

        // 🥇 1. CHECK ANTES (resolve fluxo sequencial)
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {

            var existing = idempotencyRepository.findByKey(idempotencyKey);

            if (existing.isPresent()) {

                if (!existing.get().getRequestHash().equals(requestHash)) {
                    throw new IllegalStateException(
                            "Idempotency key already used with different payload"
                    );
                }

                if (existing.get().getResponse() == null) {
                    throw new IllegalStateException(
                            "Request is still being processed for this idempotency key"
                    );
                }

                return deserialize(existing.get().getResponse());
            }
        }

        // 🥈 2. RESERVA (resolve concorrência)
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {

            try {
                PaymentIdempotency reservation = new PaymentIdempotency(
                        idempotencyKey,
                        requestHash,
                        null,
                        null,
                        Instant.now()
                );

                idempotencyRepository.save(reservation);

            } catch (DataIntegrityViolationException ex) {

                // outro thread já reservou
                var existing = idempotencyRepository.findByKey(idempotencyKey)
                        .orElseThrow();

                if (!existing.getRequestHash().equals(requestHash)) {
                    throw new IllegalStateException(
                            "Idempotency key already used with different payload"
                    );
                }

                if (existing.getResponse() == null) {
                    throw new IllegalStateException(
                            "Request is still being processed for this idempotency key"
                    );
                }

                return deserialize(existing.getResponse());
            }
        }

        // processamento normal
        Payment payment = new Payment(
                UUID.randomUUID(),
                command.getAmount(),
                command.getCurrency(),
                PaymentStatus.CREATED,
                PaymentMethod.valueOf(command.getPaymentMethod()),
                command.getDescription(),
                null,
                null,
                Instant.now(),
                Instant.now()
        );

        Payment saved = paymentRepositoryPort.save(payment);

        // 🏁 4. UPDATE DA IDEMPOTÊNCIA
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {

            var existing = idempotencyRepository.findByKey(idempotencyKey)
                    .orElseThrow();

            existing.setResponse(serialize(saved));
            existing.setPaymentId(saved.getId().toString());

            idempotencyRepository.save(existing);
        }

        log.info("Creating payment amount={} currency={}", command.getAmount(), command.getCurrency());
        log.info("POST /payments - Creating payment amount={} currency={}",
                command.getAmount(),
                command.getCurrency());

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
