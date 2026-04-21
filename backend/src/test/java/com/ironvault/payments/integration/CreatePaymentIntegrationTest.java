package com.ironvault.payments.integration;

import com.ironvault.payments.application.OutboxDispatcherService;
import com.ironvault.payments.domain.enums.OutboxEventStatus;
import com.ironvault.payments.domain.enums.PaymentStatus;

import com.ironvault.payments.domain.model.Payment;
import com.ironvault.payments.domain.model.PaymentGatewayResult;
import com.ironvault.payments.domain.port.in.command.CreatePaymentCommand;
import com.ironvault.payments.domain.port.in.payment.CreatePaymentUseCase;
import com.ironvault.payments.domain.port.in.command.UpdatePaymentStatusCommand;
import com.ironvault.payments.domain.port.in.payment.UpdatePaymentStatusUseCase;
import com.ironvault.payments.domain.port.out.OutboxEventRepositoryPort;
import com.ironvault.payments.domain.port.out.PaymentGatewayPort;
import com.ironvault.payments.domain.port.out.PaymentIdempotencyRepositoryPort;
import com.ironvault.payments.domain.port.out.PaymentRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Callable;

import org.springframework.boot.test.mock.mockito.MockBean;
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

    @Autowired
    private OutboxEventRepositoryPort outboxEventRepositoryPort;

    @Autowired
    private OutboxDispatcherService outboxDispatcherService;

    @MockBean
    private PaymentGatewayPort paymentGatewayPort;


    @BeforeEach
    void setUp() {

        outboxEventRepositoryPort.findPendingEvents()
                .forEach(event -> {
                    event.setStatus(OutboxEventStatus.FAILED);
                    outboxEventRepositoryPort.save(event);
                });

        // Mock comportamento padrão — aprovado
        when(paymentGatewayPort.process(any())).thenReturn(
                new PaymentGatewayResult(
                        "mock-gw-" + UUID.randomUUID(),
                        PaymentStatus.APPROVED,
                        "APPROVED",
                        "Approved",
                        null, null, null, null
                )
        );
    }

    @Test
    @DisplayName("Should return same payment when using the same idempotency key")
    void shouldReturnSamePaymentForSameIdempotencyKey() {

        String key = "ironvault-test-123";

        var cmd = new CreatePaymentCommand(BigDecimal.valueOf(150),
                "BRL",
                "PIX",
                "test-description",
                "teste@gmail.com");

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
                "test-description",
                "teste@gmail.com");

        assertThatThrownBy(() ->
                createPaymentUseCase.create(cmd, null)
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Idempotency-Key header is required");
    }

    @Test
    @DisplayName("Should handle concurrent requests safely using the same idempotency key")
    void shouldHandleConcurrentRequestsSafely() throws ExecutionException, InterruptedException {

        String key = "ironvault-concurrent-" + UUID.randomUUID();

        var cmd = new CreatePaymentCommand(BigDecimal.valueOf(150),
                "BRL",
                "PIX",
                "test-description",
                "teste@gmail.com");

        var executor = Executors.newFixedThreadPool(2);
        try {
            Callable<Object> task = () -> {
                try {
                    return createPaymentUseCase.create(cmd, key);
                } catch (IllegalStateException ex) {
                    return ex;
                } catch (Exception ex) {
                    return new IllegalStateException(
                            "Request is still being processed for this idempotency key", ex
                    );
                }
            };

            Future<Object> future1 = executor.submit(task);
            Future<Object> future2 = executor.submit(task);

            Object result1 = future1.get();
            Object result2 = future2.get();

            boolean result1IsError = result1 instanceof IllegalStateException;
            boolean result2IsError = result2 instanceof IllegalStateException;

            if (result1IsError || result2IsError) {
                IllegalStateException exception = (IllegalStateException)
                        (result1IsError ? result1 : result2);
                assertThat(exception.getMessage()).contains("Request is still being processed");
            } else {
                var p1 = (Payment) result1;
                var p2 = (Payment) result2;
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
                "test-description",
                "teste@gmail.com");
        var cmd2 = new CreatePaymentCommand(BigDecimal.valueOf(300),
                "BRL",
                "PIX",
                "test-description",
                "teste@gmail.com");

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
                "description-1",
                "teste@gmail.com");
        var cmd2 = new CreatePaymentCommand(BigDecimal.valueOf(200),
                "BRL",
                "PIX",
                "description-2",
                "teste@gmail.com");

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
                "transition-test",
                "teste@gmail.com");

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
                new CreatePaymentCommand(BigDecimal.valueOf(251), "BRL", "PIX",
                        "transition-failure-test",
                        "teste@gmail.com"),
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
    void shouldProcessPaymentAsyncFailurePath() throws InterruptedException {
        when(paymentGatewayPort.process(any()))
                .thenThrow(new RuntimeException("Mock gateway timeout"));

        String key = "ironvault-async-failed-" + UUID.randomUUID();
        var cmd = new CreatePaymentCommand(new BigDecimal("14.00"), "BRL",
                "PIX", "async-failed", "teste@gmail.com");
        var created = createPaymentUseCase.create(cmd, key);

        assertThat(created.getStatus()).isEqualTo(PaymentStatus.PROCESSING);
        outboxDispatcherService.dispatch();

        var finalized = awaitFinalStatus(created.getId());
        assertThat(finalized.getStatus()).isEqualTo(PaymentStatus.PROCESSING);
        assertThat(finalized.getFailureReason()).contains("Mock gateway timeout");
    }

    private Payment awaitFinalStatus(UUID paymentId) {
        Instant deadline = Instant.now().plus(Duration.ofSeconds(5)); // era 3

        while (Instant.now().isBefore(deadline)) {
            var current = paymentRepositoryPort.findById(paymentId).orElseThrow();
            if (current.getStatus() != PaymentStatus.PROCESSING
                    && current.getStatus() != PaymentStatus.CREATED) {
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
