package com.ironvault.payments.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ironvault.payments.adapter.in.dto.MockGatewayWebhookRequest;
import com.ironvault.payments.application.signature.WebhookSignatureService;
import com.ironvault.payments.domain.port.in.webhook.ProcessWebhookUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Payments Webhook", description = "Operações de pagamento Webhook")
public class PaymentWebhookController {

    private final WebhookSignatureService webhookSignatureService;
    private final ProcessWebhookUseCase webhookProcessingService;
    private final ObjectMapper objectMapper;
    private final Validator validator;


    public PaymentWebhookController(WebhookSignatureService webhookSignatureService,
                                    ProcessWebhookUseCase webhookProcessingService,
                                    ObjectMapper objectMapper,
                                    Validator validator) {
        this.webhookSignatureService = webhookSignatureService;
        this.webhookProcessingService = webhookProcessingService;
        this.objectMapper = objectMapper;
        this.validator = validator;
    }

    @PostMapping("/mock")
    @Operation(
            summary = "Receber webhook mock",
            description = "Endpoint para receber notificações de pagamento do gateway mock"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Webhook recebido e processamento iniciado"),
            @ApiResponse(responseCode = "400", description = "Payload inválido"),
            @ApiResponse(responseCode = "401", description = "Assinatura inválida")
    })
    public ResponseEntity<Map<String, Object>> handleMockWebhook(
            @Parameter(description = "Assinatura HMAC do payload", required = true)
            @RequestHeader("X-Webhook-Signature") String signature,

            @Parameter(description = "Timestamp do envio em Unix epoch", required = true)
            @RequestHeader("X-Webhook-Timestamp") String timestamp,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Payload do webhook em bytes",
                    required = true
            )
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

        webhookProcessingService.process(
                request.getEventId(),
                request.getExternalId(),
                request.getStatus(),
                request.getGatewayCode(),
                request.getGatewayMessage(),
                request.getFailureReason()
        );

        return ResponseEntity.accepted().body(Map.of(
                "received", true,
                "eventId", request.getEventId()
        ));
    }
}
