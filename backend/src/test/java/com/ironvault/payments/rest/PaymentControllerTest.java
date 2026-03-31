package com.ironvault.payments.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ironvault.payments.adapter.in.dto.PaymentRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should create payment successfully")
    void shouldCreatePayment() throws Exception {

        PaymentRequest request = new PaymentRequest(
                BigDecimal.valueOf(150),
                "BRL",
                "PIX",
                "test-description"
        );

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(150))
                .andExpect(jsonPath("$.currency").value("BRL"))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.paymentMethod").value("PIX"));
    }

    @Test
    @DisplayName("Should return same payment when using same idempotency key")
    void shouldReturnSamePaymentWithSameIdempotencyKey() throws Exception {

        PaymentRequest request = new PaymentRequest(
                BigDecimal.valueOf(150),
                "BRL",
                "PIX",
                "test-description"
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
                "test-description");
        PaymentRequest req2 = new PaymentRequest(BigDecimal.valueOf(200),
                "BRL",
                "PIX",
                "test-description");

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
                "test-description"
        );

        // SUA API NÃO VALIDA → então espera 201
        mockMvc.perform(post("/api/payments")
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
                "test-description"
        );

        mockMvc.perform(post("/api/payments")
                        .header("X-Correlation-Id", "REQ-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(header().exists("x-Correlation-Id")); // lowercase (Spring normaliza)
    }
}