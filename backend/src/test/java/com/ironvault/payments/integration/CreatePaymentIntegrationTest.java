package com.ironvault.payments.integration;

import com.ironvault.payments.domain.port.in.payment.CreatePaymentCommand;
import com.ironvault.payments.domain.port.in.payment.CreatePaymentUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
public class CreatePaymentIntegrationTest {

    @Autowired
    private CreatePaymentUseCase createPaymentUseCase;

    @Test
    @DisplayName("Should return same payment when using the same idempotency key")
    void shouldReturnSamePaymentForSameIdempotencyKey() {

        String key = "ironvault-test-123";

        var cmd = new CreatePaymentCommand(BigDecimal.valueOf(150),
                "BRL",
                "test-description",
                "customer-123");

        var first = createPaymentUseCase.create(cmd, key);
        var second = createPaymentUseCase.create(cmd, key);

        assertThat(first.getId()).isEqualTo(second.getId());
    }

    @Test
    @DisplayName("Should create different payments when idempotency key is not provided")
    void shouldCreateDifferentPaymentsWithoutIdempotencyKey() {

        var cmd = new CreatePaymentCommand(BigDecimal.valueOf(150),
                "BRL",
                "test-description",
                "customer-123");

        var first = createPaymentUseCase.create(cmd, null);
        var second = createPaymentUseCase.create(cmd, null);

        assertThat(first.getId()).isNotEqualTo(second.getId());
    }

    @Test
    @DisplayName("Should handle concurrent requests safely using the same idempotency key")
    void shouldHandleConcurrentRequestsSafely() throws ExecutionException, InterruptedException {

        String key = "ironvault-concurrent-1";

        var cmd = new CreatePaymentCommand(BigDecimal.valueOf(150),
                "BRL",
                "test-description",
                "customer-123");

        var executor = java.util.concurrent.Executors.newFixedThreadPool(2);

        var future1 = executor.submit(() -> createPaymentUseCase.create(cmd, key));
        var future2 = executor.submit(() -> createPaymentUseCase.create(cmd, key));

        var p1 = future1.get();
        var p2 = future2.get();

        assertThat(p1.getId()).isEqualTo(p2.getId());

    }

    @Test
    @DisplayName("Should throw conflict when same idempotency key is used with different payload")
    void shouldThrowConflictWhenSameKeyWithDifferentPayload() {

        String key = "ironvault-test-456";

        var cmd1 = new CreatePaymentCommand(BigDecimal.valueOf(200),
                "BRL",
                "test-description",
                "customer-123");
        var cmd2 = new CreatePaymentCommand(BigDecimal.valueOf(300),
                "BRL",
                "test-description",
                "customer-123");

        createPaymentUseCase.create(cmd1, key);

        assertThatThrownBy(() ->
                createPaymentUseCase.create(cmd2, key)
        ).isInstanceOf(IllegalStateException.class);

    }
}
