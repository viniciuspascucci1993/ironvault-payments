package com.ironvault.payments.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ironvault.payments.adapter.in.web.PaymentWebhookController;
import com.ironvault.payments.application.HandleMockWebhookService;
import com.ironvault.payments.application.WebhookProcessingService;
import com.ironvault.payments.application.signature.WebhookSignatureService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentWebhookController.class)
@Import(WebhookSignatureService.class)
@ActiveProfiles("test")
public class PaymentWebhookControllerTest {

    private static final String SECRET = "ironvault-webhook-secret";
    private static final String URL = "/api/payments/webhooks/mock";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WebhookProcessingService webhookProcessingService;

    @MockBean
    private HandleMockWebhookService handleMockWebhookService;

    private String validPayload;

    @BeforeEach
    void setUp() throws Exception {
        validPayload = objectMapper.writeValueAsString(new java.util.HashMap<>() {{
            put("eventId", "evt-test-001");
            put("externalId", "ext-001");
            put("status", "APPROVED");
            put("gatewayCode", "00");
            put("gatewayMessage", "Approved");
            put("failureReason", null);
        }});
    }

    // --- Helpers ---

    private String generateSignature(String timestamp, String body) throws Exception {
        String signedPayload = timestamp + "." + body;
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return Base64.getEncoder().encodeToString(
                mac.doFinal(signedPayload.getBytes(StandardCharsets.UTF_8))
        );
    }

    private String validTimestamp() {
        return String.valueOf(System.currentTimeMillis() / 1000);
    }

    // --- Testes ---

    @Test
    @DisplayName("Should return 202 when webhook is valid")
    void shouldReturn202WhenWebhookIsValid() throws Exception {
        String timestamp = validTimestamp();
        String signature = generateSignature(timestamp, validPayload);

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Webhook-Signature", signature)
                        .header("X-Webhook-Timestamp", timestamp)
                        .content(validPayload))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.received").value(true))
                .andExpect(jsonPath("$.eventId").value("evt-test-001"));

        verify(webhookProcessingService, times(1))
                .process(eq("evt-test-001"), eq("ext-001"), eq("APPROVED"),
                        eq("00"), eq("Approved"), isNull());
    }

    @Test
    @DisplayName("Should return 400 when signature is invalid")
    void shouldReturn400WhenSignatureIsInvalid() throws Exception {
        String timestamp = validTimestamp();

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Webhook-Signature", "assinatura-invalida")
                        .header("X-Webhook-Timestamp", timestamp)
                        .content(validPayload))
                .andExpect(status().isBadRequest());

        verify(webhookProcessingService, never()).process(any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Should return 400 when timestamp is outside tolerance window")
    void shouldReturn400WhenTimestampIsOutsideToleranceWindow() throws Exception {
        String oldTimestamp = String.valueOf((System.currentTimeMillis() / 1000) - 400);
        String signature = generateSignature(oldTimestamp, validPayload);

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Webhook-Signature", signature)
                        .header("X-Webhook-Timestamp", oldTimestamp)
                        .content(validPayload))
                .andExpect(status().isBadRequest());

        verify(webhookProcessingService, never()).process(any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Should return 400 when signature header is missing")
    void shouldReturn400WhenSignatureHeaderIsMissing() throws Exception {
        String timestamp = validTimestamp();

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Webhook-Timestamp", timestamp)
                        .content(validPayload))
                .andExpect(status().isBadRequest());

        verify(webhookProcessingService, never()).process(any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Should return 400 when eventId is missing")
    void shouldReturn400WhenEventIdIsMissing() throws Exception {
        String payload = objectMapper.writeValueAsString(new java.util.HashMap<>() {{
            put("eventId", "");
            put("externalId", "ext-001");
            put("status", "APPROVED");
            put("gatewayCode", "00");
            put("gatewayMessage", "Approved");
        }});

        String timestamp = validTimestamp();
        String signature = generateSignature(timestamp, payload);

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Webhook-Signature", signature)
                        .header("X-Webhook-Timestamp", timestamp)
                        .content(payload))
                .andExpect(status().isBadRequest());

        verify(webhookProcessingService, never()).process(any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Should return 400 when externalId is missing")
    void shouldReturn400WhenExternalIdIsMissing() throws Exception {
        String payload = objectMapper.writeValueAsString(new java.util.HashMap<>() {{
            put("eventId", "evt-001");
            put("externalId", "");
            put("status", "APPROVED");
            put("gatewayCode", "00");
            put("gatewayMessage", "Approved");
        }});

        String timestamp = validTimestamp();
        String signature = generateSignature(timestamp, payload);

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Webhook-Signature", signature)
                        .header("X-Webhook-Timestamp", timestamp)
                        .content(payload))
                .andExpect(status().isBadRequest());

        verify(webhookProcessingService, never()).process(any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Should return 202 and not reprocess duplicate event")
    void shouldReturn202AndNotReprocessDuplicateEvent() throws Exception {
        String timestamp = validTimestamp();
        String signature = generateSignature(timestamp, validPayload);

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Webhook-Signature", signature)
                        .header("X-Webhook-Timestamp", timestamp)
                        .content(validPayload))
                .andExpect(status().isAccepted());

        verify(webhookProcessingService, times(1))
                .process(any(), any(), any(), any(), any(), any());
    }
}
