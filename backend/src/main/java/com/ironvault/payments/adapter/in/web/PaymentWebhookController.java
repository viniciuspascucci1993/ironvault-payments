package com.ironvault.payments.adapter.in.web;

import com.ironvault.payments.adapter.in.dto.MockGatewayWebhookRequest;
import com.ironvault.payments.application.HandleMockWebhookService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments/webhooks")
public class PaymentWebhookController {

    private final HandleMockWebhookService handleMockWebhookService;

    public PaymentWebhookController(HandleMockWebhookService handleMockWebhookService) {
        this.handleMockWebhookService = handleMockWebhookService;
    }

    @PostMapping("/mock")
    public ResponseEntity<Map<String, Object>> handleMockWebhook(
            @RequestHeader("X-Webhook-Signature") String signature,
            @Valid @RequestBody MockGatewayWebhookRequest request) {

        boolean processed = handleMockWebhookService.handle(
                signature,
                request.getEventId(),
                request.getExternalId(),
                request.getStatus(),
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
