package com.ironvault.payments.integration;

import com.ironvault.payments.domain.enums.PaymentStatus;

import com.ironvault.payments.domain.model.Payment;
import com.ironvault.payments.domain.port.in.payment.CreatePaymentCommand;
import com.ironvault.payments.domain.port.in.payment.CreatePaymentUseCase;
import com.ironvault.payments.domain.port.in.payment.UpdatePaymentStatusCommand;
import com.ironvault.payments.domain.port.in.payment.UpdatePaymentStatusUseCase;
import com.ironvault.payments.domain.port.out.PaymentIdempotencyRepositoryPort;
import com.ironvault.payments.domain.port.out.PaymentRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Callable;

import org.springframework.test.context.ActiveProfiles;


@SpringBootTest
@ActiveProfiles("test")
public class CreatePaymentIntegrationTest {

    @Autowired
    private CreatePaymentUseCase createPaymentUseCase;

    @Autowired
    private PaymentIdempotencyRepositoryPort paymentIdempotencyRepositoryPort;

    @Autowired
    private UpdatePaymentStatusUseCase updatePaymentStatusUseCase;

    @Autowired
    private PaymentRepositoryPort paymentRepositoryPort;

    @Test
    @DisplayName("Should return same payment when using the same idempotency key")
    void shouldReturnSamePaymentForSameIdempotencyKey() {

        String key = "ironvault-test-123";

        var cmd = new CreatePaymentCommand(BigDecimal.valueOf(150),
                "BRL",
                "PIX",
                "test-description");

        var first = createPaymentUseCase.create(cmd, key);
        var second = createPaymentUseCase.create(cmd, key);

        assertThat(first.getId()).isEqualTo(second.getId());

        var persisted = paymentIdempotencyRepositoryPort.findByKey(key);
        assertThat(persisted).isPresent();
        assertThat(persisted.get().getPaymentId()).isEqualTo(first.getId().toString());
    }

    @Test
    @DisplayName("Should fail when idempotency key is not provided")
    void shouldFailWhenIdempotencyKeyIsMissing() {

        var cmd = new CreatePaymentCommand(BigDecimal.valueOf(150),
                "BRL",
                "PIX",
                "test-description");

        assertThatThrownBy(() ->
                createPaymentUseCase.create(cmd, null)
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Idempotency-Key header is required");
    }

    @Test
    @DisplayName("Should handle concurrent requests safely using the same idempotency key")
    void shouldHandleConcurrentRequestsSafely() throws ExecutionException, InterruptedException {

        String key = "ironvault-concurrent-1";

        var cmd = new CreatePaymentCommand(BigDecimal.valueOf(150),
                "BRL",
                "PIX",
                "test-description");

        var executor = Executors.newFixedThreadPool(2);
        try {
            Callable<Object> task = () -> {
                try {
                    return createPaymentUseCase.create(cmd, key);
                } catch (IllegalStateException ex) {
                    return ex;
                }
            };

            Future<Object> future1 = executor.submit(task);
            Future<Object> future2 = executor.submit(task);

            Object result1 = future1.get();
            Object result2 = future2.get();

            if (result1 instanceof IllegalStateException || result2 instanceof IllegalStateException) {
                IllegalStateException exception = (IllegalStateException) (result1 instanceof IllegalStateException ? result1 : result2);
                assertThat(exception.getMessage()).contains("Request is still being processed");
            } else {
                var p1 = (com.ironvault.payments.domain.model.Payment) result1;
                var p2 = (com.ironvault.payments.domain.model.Payment) result2;
                assertThat(p1.getId()).isEqualTo(p2.getId());
            }
        } finally {
            executor.shutdownNow();
        }

    }

    @Test
    @DisplayName("Should throw conflict when same idempotency key is used with different payload")
    void shouldThrowConflictWhenSameKeyWithDifferentPayload() {

        String key = "ironvault-test-456";

        var cmd1 = new CreatePaymentCommand(BigDecimal.valueOf(200),
                "BRL",
                "PIX",
                "test-description");
        var cmd2 = new CreatePaymentCommand(BigDecimal.valueOf(300),
                "BRL",
                "PIX",
                "test-description");

        createPaymentUseCase.create(cmd1, key);

        assertThatThrownBy(() ->
                createPaymentUseCase.create(cmd2, key)
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Should throw conflict when same key is reused with different description")
    void shouldThrowConflictWhenDescriptionChanges() {
        String key = "ironvault-test-789";

        var cmd1 = new CreatePaymentCommand(BigDecimal.valueOf(200),
                "BRL",
                "PIX",
                "description-1");
        var cmd2 = new CreatePaymentCommand(BigDecimal.valueOf(200),
                "BRL",
                "PIX",
                "description-2");

        createPaymentUseCase.create(cmd1, key);

        assertThatThrownBy(() ->
                createPaymentUseCase.create(cmd2, key)
        ).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("different payload");
    }

    @Test
    @DisplayName("Should enforce valid payment status transitions")
    void shouldEnforceValidPaymentStatusTransitions() {
        String key = "ironvault-transition-test-1";

        var cmd = new CreatePaymentCommand(BigDecimal.valueOf(250),
                "BRL",
                "PIX",
                "transition-test");

        var created = createPaymentUseCase.create(cmd, key);

        assertThatThrownBy(() ->
                updatePaymentStatusUseCase.updateStatus(
                        new UpdatePaymentStatusCommand(created.getId(), PaymentStatus.CREATED, null)
                )
        ).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid status transition");

        var approved = updatePaymentStatusUseCase.updateStatus(
                new UpdatePaymentStatusCommand(created.getId(), PaymentStatus.APPROVED, null)
        );
        assertThat(approved.getStatus()).isEqualTo(PaymentStatus.APPROVED);

        assertThatThrownBy(() ->
                updatePaymentStatusUseCase.updateStatus(
                        new UpdatePaymentStatusCommand(created.getId(), PaymentStatus.PROCESSING, null)
                )
        ).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid status transition");

        var processingPayment = createPaymentUseCase.create(
                new CreatePaymentCommand(BigDecimal.valueOf(251), "BRL", "PIX", "transition-failure-test"),
                "ironvault-transition-test-2"
        );
        var failed = updatePaymentStatusUseCase.updateStatus(
                new UpdatePaymentStatusCommand(processingPayment.getId(), PaymentStatus.FAILED, "Gateway timeout")
        );
        assertThat(failed.getStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(failed.getFailureReason()).isEqualTo("Gateway timeout");
    }

    @Test
    @DisplayName("Should process payment asynchronously and mark failed on technical error")
    void shouldProcessPaymentAsyncFailurePath() {
        String key = "ironvault-async-failed-1";

        var cmd = new CreatePaymentCommand(new BigDecimal("14.00"),
                "BRL",
                "PIX",
                "async-failed");

        var created = createPaymentUseCase.create(cmd, key);

        assertThat(created.getStatus()).isEqualTo(PaymentStatus.PROCESSING);

        var finalized = awaitFinalStatus(created.getId());
        assertThat(finalized.getStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(finalized.getFailureReason()).contains("Mock gateway timeout");
    }

    private Payment awaitFinalStatus(UUID paymentId) {
        Instant deadline = Instant.now().plus(Duration.ofSeconds(3));

        while (Instant.now().isBefore(deadline)) {
            var current = paymentRepositoryPort.findById(paymentId).orElseThrow();
            if (current.getStatus() != PaymentStatus.PROCESSING && current.getStatus() != PaymentStatus.CREATED) {
                return current;
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        return paymentRepositoryPort.findById(paymentId).orElseThrow();
    }
}
