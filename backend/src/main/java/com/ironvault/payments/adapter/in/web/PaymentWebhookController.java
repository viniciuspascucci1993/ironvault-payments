package com.ironvault.payments.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ironvault.payments.adapter.in.dto.MockGatewayWebhookRequest;
import com.ironvault.payments.application.HandleMockWebhookService;
import com.ironvault.payments.application.signature.WebhookSignatureService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/payments/webhooks")
public class PaymentWebhookController {

    private final WebhookSignatureService webhookSignatureService;
    private final HandleMockWebhookService handleMockWebhookService;
    private final ObjectMapper objectMapper;
    private final Validator validator;


    public PaymentWebhookController(WebhookSignatureService webhookSignatureService,
                                    HandleMockWebhookService handleMockWebhookService,
                                    ObjectMapper objectMapper,
                                    Validator validator) {
        this.webhookSignatureService = webhookSignatureService;
        this.handleMockWebhookService = handleMockWebhookService;
        this.objectMapper = objectMapper;
        this.validator = validator;
    }

    @PostMapping("/mock")
    public ResponseEntity<Map<String, Object>> handleMockWebhook(
            @RequestHeader("X-Webhook-Signature") String signature,
            @RequestHeader("X-Webhook-Timestamp") String timestamp,
            @RequestBody byte[] payload) throws IOException {

        webhookSignatureService.validate(signature, timestamp, payload);

        MockGatewayWebhookRequest request = objectMapper.readValue(payload, MockGatewayWebhookRequest.class);

        Set<ConstraintViolation<MockGatewayWebhookRequest>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .collect(Collectors.joining(", "));
            throw new IllegalArgumentException("Invalid webhook payload - " + message);
        }

        boolean processed = handleMockWebhookService.handle(
                request.getEventId(),
                request.getExternalId(),
                request.getStatus(),
                request.getGatewayCode(),
                request.getGatewayMessage(),
                request.getFailureReason()
        );

        if (!processed) {
            return ResponseEntity.ok(Map.of(
                    "processed", false,
                    "message", "Event already processed"
            ));
        }

        return ResponseEntity.ok(Map.of(
                "processed", true,
                "eventId", request.getEventId()
        ));
    }
}
