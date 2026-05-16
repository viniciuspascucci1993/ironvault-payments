package com.ironvault.payments.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ironvault.payments.domain.enums.*;
import com.ironvault.payments.domain.exception.IdempotencyKeyConflictException;
import com.ironvault.payments.domain.model.OutboxEvent;
import com.ironvault.payments.domain.model.Payment;
import com.ironvault.payments.domain.model.PaymentIdempotency;
import com.ironvault.payments.domain.model.Transaction;
import com.ironvault.payments.domain.port.in.command.CreatePaymentCommand;
import com.ironvault.payments.domain.port.in.payment.CreatePaymentUseCase;
import com.ironvault.payments.domain.port.out.OutboxEventRepositoryPort;
import com.ironvault.payments.domain.port.out.PaymentIdempotencyRepositoryPort;
import com.ironvault.payments.domain.port.out.PaymentRepositoryPort;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import com.ironvault.payments.domain.port.out.TransactionRepositoryPort;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CreatePaymentService implements CreatePaymentUseCase {

    private static final String PENDING_PAYMENT_ID = "00000000-0000-0000-0000-000000000000";

    private final PaymentRepositoryPort paymentRepositoryPort;
    private final PaymentIdempotencyRepositoryPort idempotencyRepository;
    private final OutboxEventRepositoryPort outboxEventRepository;
    private final TransactionRepositoryPort transactionRepositoryPort;
    private final ObjectMapper objectMapper;

    public CreatePaymentService(PaymentRepositoryPort paymentRepositoryPort,
                                PaymentIdempotencyRepositoryPort idempotencyRepository,
                                OutboxEventRepositoryPort outboxEventRepository,
                                TransactionRepositoryPort transactionRepositoryPort,
                                ObjectMapper objectMapper) {
        this.paymentRepositoryPort = paymentRepositoryPort;
        this.idempotencyRepository = idempotencyRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.transactionRepositoryPort = transactionRepositoryPort;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public Payment create(CreatePaymentCommand command, String idempotencyKey) {

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("Idempotency-Key header is required");
        }

        String requestHash = generateHash(command);

        // 🥇 1. CHECK ANTES (resolve fluxo sequencial)
        if (!idempotencyKey.isBlank()) {

            var existing = idempotencyRepository.findByKey(idempotencyKey);

            if (existing.isPresent()) {

                if (!existing.get().getRequestHash().equals(requestHash)) {
                    throw new IllegalStateException(
                            "Idempotency key already used with different payload"
                    );
                }

                if (existing.get().getResponse() == null) {
                    return resolvePendingOrInFlight(existing.get());
                }

                return deserialize(existing.get().getResponse());
            }
        }

        // 🥈 2. RESERVA (resolve concorrência)
        if (!idempotencyKey.isBlank()) {

            try {
                PaymentIdempotency reservation = new PaymentIdempotency(
                        idempotencyKey,
                        requestHash,
                        null,
                        PENDING_PAYMENT_ID,
                        Instant.now()
                );

                idempotencyRepository.save(reservation);

            } catch (IdempotencyKeyConflictException ex) {
                var existing = idempotencyRepository.findByKey(idempotencyKey)
                        .orElseThrow(() -> new IllegalStateException(
                                "Unable to reserve idempotency key due to persistence constraint"
                        ));

                if (!existing.getRequestHash().equals(requestHash)) {
                    throw new IllegalStateException(
                            "Idempotency key already used with different payload"
                    );
                }

                if (existing.getResponse() == null) {
                    return resolvePendingOrInFlight(existing);
                }

                return deserialize(existing.getResponse());
            }
        }

        // processamento normal
        Payment payment = new Payment(
                UUID.randomUUID(),
                command.getAmount(),
                command.getCurrency(),
                PaymentStatus.PROCESSING,
                PaymentMethod.valueOf(command.getPaymentMethod()),
                command.getDescription(),
                command.getPayerEmail(),
                null,  // externalId
                null,  // gatewayCode
                null,  // gatewayMessage
                null,  // declineReason
                null,  // pixQrCode
                null,  // pixCopyPaste
                null,  // failureReason
                Instant.now(),
                Instant.now()
        );

        Payment processingPayment = paymentRepositoryPort.save(payment);

        // 🏁 4. UPDATE DA IDEMPOTÊNCIA
        if (!idempotencyKey.isBlank()) {

            var existing = idempotencyRepository.findByKey(idempotencyKey)
                    .orElseThrow();

            existing.setResponse(serialize(processingPayment));
            existing.setPaymentId(processingPayment.getId().toString());

            idempotencyRepository.save(existing);
        }

        transactionRepositoryPort.save(new Transaction(
                UUID.randomUUID(),
                processingPayment.getId(),
                null,
                TransactionType.CHARGE,
                TransactionStatus.PENDING,
                processingPayment.getAmount(),
                processingPayment.getCurrency(),
                null,
                null,
                Instant.now(),
                Instant.now()
        ));

        outboxEventRepository.save(new OutboxEvent(
                processingPayment.getId(),
                OutboxEventStatus.PENDING,
                Instant.now()
        ));

        log.info("Payment created and outbox event saved. paymentId={}", processingPayment.getId());
        log.info("POST /payments - Creating payment amount={} currency={}",
                command.getAmount(),
                command.getCurrency());

        return processingPayment;
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
        try {
            Map<String, String> payload = new LinkedHashMap<>();
            payload.put("amount", normalizeAmount(command.getAmount()));
            payload.put("currency", normalizeTextUpper(command.getCurrency()));
            payload.put("paymentMethod", normalizeTextUpper(command.getPaymentMethod()));
            payload.put("description", normalizeText(command.getDescription()));
            payload.put("payerEmail", normalizeText(command.getPayerEmail()));

            String canonicalPayload = objectMapper.writeValueAsString(payload);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(canonicalPayload.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashBytes);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to generate request hash", ex);
        }
    }

    private Payment resolvePendingOrInFlight(PaymentIdempotency existing) {
        if (existing.getPaymentId() == null || PENDING_PAYMENT_ID.equals(existing.getPaymentId())) {
            throw new IllegalStateException(
                    "Request is still being processed for this idempotency key"
            );
        }

        try {
            UUID paymentId = UUID.fromString(existing.getPaymentId());
            Payment payment = paymentRepositoryPort.findById(paymentId)
                    .orElseThrow(() -> new IllegalStateException(
                            "Idempotency record exists but payment was not found"
                    ));

            existing.setResponse(serialize(payment));
            existing.setPaymentId(payment.getId().toString());
            idempotencyRepository.save(existing);

            return payment;
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException(
                    "Invalid payment reference stored for this idempotency key"
            );
        }
    }

    private String normalizeAmount(BigDecimal amount) {
        if (amount == null) {
            return "null";
        }
        return amount.stripTrailingZeros().toPlainString();
    }

    private String normalizeTextUpper(String value) {
        return normalizeText(value).toUpperCase(Locale.ROOT);
    }

    private String normalizeText(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
