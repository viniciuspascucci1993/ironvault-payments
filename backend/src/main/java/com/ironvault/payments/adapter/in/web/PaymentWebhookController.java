package com.ironvault.payments.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ironvault.payments.adapter.in.dto.MockGatewayWebhookRequest;
import com.ironvault.payments.application.HandleMockWebhookService;
import com.ironvault.payments.application.signature.WebhookSignatureService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/payments/webhooks")
public class PaymentWebhookController {

    private final WebhookSignatureService webhookSignatureService;
    private final HandleMockWebhookService handleMockWebhookService;
    private final ObjectMapper objectMapper;


    public PaymentWebhookController(WebhookSignatureService webhookSignatureService,
                                    HandleMockWebhookService handleMockWebhookService,
                                    ObjectMapper objectMapper) {
        this.webhookSignatureService = webhookSignatureService;
        this.handleMockWebhookService = handleMockWebhookService;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/mock")
    public ResponseEntity<Map<String, Object>> handleMockWebhook(
            @RequestHeader("X-Webhook-Signature") String signature,
            @RequestHeader("X-Webhook-Timestamp") String timestamp,
            @RequestBody byte[] payload) throws IOException {

        webhookSignatureService.validate(signature, timestamp, payload);

        MockGatewayWebhookRequest request = objectMapper.readValue(payload, MockGatewayWebhookRequest.class);
        // opcional: validar DTO com Validator bean

        boolean processed = handleMockWebhookService.handle(
                signature,
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
