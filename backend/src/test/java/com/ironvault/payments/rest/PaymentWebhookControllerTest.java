package com.ironvault.payments.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ironvault.payments.adapter.in.dto.MockGatewayWebhookRequest;
import com.ironvault.payments.adapter.out.persistence.PaymentIdempotencyJpaRepository;
import com.ironvault.payments.adapter.out.persistence.PaymentJpaRepository;
import com.ironvault.payments.adapter.out.persistence.PaymentWebhookEventJpaRepository;
import com.ironvault.payments.domain.enums.PaymentMethod;
import com.ironvault.payments.domain.enums.PaymentStatus;
import com.ironvault.payments.domain.model.Payment;
import com.ironvault.payments.domain.port.out.PaymentRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = "app.webhook.mock-secret=ironvault-webhook-secret")
public class PaymentWebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private PaymentRepositoryPort paymentRepositoryPort;
    @Autowired
    private PaymentJpaRepository paymentJpaRepository;
    @Autowired
    private PaymentWebhookEventJpaRepository paymentWebhookEventJpaRepository;
    @Autowired
    private PaymentIdempotencyJpaRepository paymentIdempotencyJpaRepository;

    @BeforeEach
    void cleanDatabase() {
        paymentWebhookEventJpaRepository.deleteAll();
        paymentIdempotencyJpaRepository.deleteAll();
        paymentJpaRepository.deleteAll();
    }

    @Test
    @DisplayName("Should process webhook event and update payment status")
    void shouldProcessWebhookAndUpdatePayment() throws Exception {
        String externalId = "mock-gw-test-" + UUID.randomUUID();
        String eventId = "evt-" + UUID.randomUUID();
        Payment payment = new Payment(
                UUID.randomUUID(),
                new BigDecimal("10.00"),
                "BRL",
                PaymentStatus.PROCESSING,
                PaymentMethod.PIX,
                "webhook-test",
                externalId,
                null,
                Instant.now(),
                Instant.now()
        );
        Payment saved = paymentRepositoryPort.save(payment);

        MockGatewayWebhookRequest request = new MockGatewayWebhookRequest();
        request.setEventId(eventId);
        request.setExternalId(externalId);
        request.setStatus("APPROVED");

        mockMvc.perform(post("/api/payments/webhooks/mock")
                        .header("X-Webhook-Signature", "ironvault-webhook-secret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.processed").value(true));

        Payment updated = paymentRepositoryPort.findById(saved.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(PaymentStatus.APPROVED);
    }

    @Test
    @DisplayName("Should ignore duplicate webhook event id")
    void shouldIgnoreDuplicateEventId() throws Exception {
        String externalId = "mock-gw-test-dup-" + UUID.randomUUID();
        String duplicateEventId = "evt-dup-" + UUID.randomUUID();
        Payment payment = new Payment(
                UUID.randomUUID(),
                new BigDecimal("11.00"),
                "BRL",
                PaymentStatus.PROCESSING,
                PaymentMethod.PIX,
                "webhook-dup",
                externalId,
                null,
                Instant.now(),
                Instant.now()
        );
        paymentRepositoryPort.save(payment);

        MockGatewayWebhookRequest request = new MockGatewayWebhookRequest();
        request.setEventId(duplicateEventId);
        request.setExternalId(externalId);
        request.setStatus("REJECTED");
        request.setFailureReason("Insufficient funds");

        mockMvc.perform(post("/api/payments/webhooks/mock")
                        .header("X-Webhook-Signature", "ironvault-webhook-secret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.processed").value(true));

        mockMvc.perform(post("/api/payments/webhooks/mock")
                        .header("X-Webhook-Signature", "ironvault-webhook-secret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.processed").value(false));

        Payment updated = paymentRepositoryPort.findByExternalId(externalId).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(PaymentStatus.REJECTED);
        assertThat(updated.getFailureReason()).isEqualTo("Insufficient funds");
    }

    @Test
    @DisplayName("Should reject webhook with invalid signature")
    void shouldRejectInvalidSignature() throws Exception {
        MockGatewayWebhookRequest request = new MockGatewayWebhookRequest();
        request.setEventId("evt-invalid-signature-" + UUID.randomUUID());
        request.setExternalId("mock-gw-missing-" + UUID.randomUUID());
        request.setStatus("APPROVED");

        mockMvc.perform(post("/api/payments/webhooks/mock")
                        .header("X-Webhook-Signature", "invalid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
