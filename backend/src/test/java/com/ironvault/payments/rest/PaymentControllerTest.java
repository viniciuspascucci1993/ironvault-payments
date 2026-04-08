package com.ironvault.payments.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ironvault.payments.adapter.in.dto.PaymentRequest;
import com.ironvault.payments.domain.enums.PaymentStatus;
import com.ironvault.payments.domain.model.PaymentGatewayResult;
import com.ironvault.payments.domain.port.out.PaymentGatewayPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentGatewayPort paymentGatewayPort;

    @BeforeEach
    void setUp() {
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
    @DisplayName("Should create payment successfully")
    void shouldCreatePayment() throws Exception {

        PaymentRequest request = new PaymentRequest(
                BigDecimal.valueOf(150),
                "BRL",
                "PIX",
                "test-description",
                "teste@gmail.com"
        );

        mockMvc.perform(post("/api/payments")
                        .header("Idempotency-Key", "ironvault-" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(150))
                .andExpect(jsonPath("$.currency").value("BRL"))
                .andExpect(jsonPath("$.status").value("PROCESSING"))
                .andExpect(jsonPath("$.paymentMethod").value("PIX"));
    }

    @Test
    @DisplayName("Should return 409 for invalid status transition")
    void shouldReturnConflictForInvalidStatusTransition() throws Exception {
        PaymentRequest request = new PaymentRequest(
                BigDecimal.valueOf(100),
                "BRL",
                "PIX",
                "test-description",
                "teste@gmail.com"
        );

        var createResult = mockMvc.perform(post("/api/payments")
                        .header("Idempotency-Key", "ironvault-" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String paymentId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id")
                .asText();

        String approveBody = """
                {
                  "status": "APPROVED"
                }
                """;

        String invalidTransitionBody = """
                {
                  "status": "PROCESSING"
                }
                """;

        mockMvc.perform(patch("/api/payments/{id}/status", paymentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(approveBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        mockMvc.perform(patch("/api/payments/{id}/status", paymentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidTransitionBody))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Should persist failure reason when status becomes FAILED")
    void shouldPersistFailureReasonWhenFailed() throws Exception {
        PaymentRequest request = new PaymentRequest(
                BigDecimal.valueOf(100),
                "BRL",
                "PIX",
                "test-description",
                "teste@gmail.com"
        );

        var createResult = mockMvc.perform(post("/api/payments")
                        .header("Idempotency-Key", "ironvault-" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String paymentId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id")
                .asText();

        String processingBody = """
                {
                  "status": "PROCESSING"
                }
                """;

        String failedBody = """
                {
                  "status": "FAILED",
                  "failureReason": "Gateway timeout"
                }
                """;

        mockMvc.perform(patch("/api/payments/{id}/status", paymentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(processingBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PROCESSING"));

        mockMvc.perform(patch("/api/payments/{id}/status", paymentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(failedBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.failureReason").value("Gateway timeout"));
    }

    @Test
    @DisplayName("Should return same payment when using same idempotency key")
    void shouldReturnSamePaymentWithSameIdempotencyKey() throws Exception {

        PaymentRequest request = new PaymentRequest(
                BigDecimal.valueOf(150),
                "BRL",
                "PIX",
                "test-description",
                "teste@gmail.com"
        );

        String key = "ironvault-" + UUID.randomUUID();

        var first = mockMvc.perform(post("/api/payments")
                        .header("Idempotency-Key", key)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        var second = mockMvc.perform(post("/api/payments")
                        .header("Idempotency-Key", key)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String response1 = first.getResponse().getContentAsString();
        String response2 = second.getResponse().getContentAsString();

        assertThat(response1).isEqualTo(response2);
    }

    @Test
    @DisplayName("Should return 409 when same idempotency key with different payload")
    void shouldReturnConflict() throws Exception {

        PaymentRequest req1 = new PaymentRequest(BigDecimal.valueOf(100),
                "BRL",
                "PIX",
                "test-description",
                "teste@gmail.com");
        PaymentRequest req2 = new PaymentRequest(BigDecimal.valueOf(200),
                "BRL",
                "PIX",
                "test-description",
                "teste@gmail.com");

        String key = "ironvault-123e4567-e89b-12d3-a456-426614174000";

        mockMvc.perform(post("/api/payments")
                        .header("Idempotency-Key", key)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/payments")
                        .header("Idempotency-Key", key)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req2)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Should return 400 when payment method is invalid")
    void shouldReturnBadRequest() throws Exception {

        PaymentRequest request = new PaymentRequest(
                BigDecimal.valueOf(100),
                "BRL",
                "INVALID_METHOD",
                "test-description",
                "teste@gmail.com"
        );

        // SUA API NÃO VALIDA → então espera 201
        mockMvc.perform(post("/api/payments")
                        .header("Idempotency-Key", "ironvault-" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return correlation id in response header")
    void shouldReturnCorrelationId() throws Exception {

        PaymentRequest request = new PaymentRequest(
                BigDecimal.valueOf(100),
                "BRL",
                "PIX",
                "test-description",
                "teste@gmail.com"
        );

        mockMvc.perform(post("/api/payments")
                        .header("X-Correlation-Id", "REQ-123")
                        .header("Idempotency-Key", "ironvault-" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(header().exists("x-Correlation-Id")); // lowercase (Spring normaliza)
    }
}