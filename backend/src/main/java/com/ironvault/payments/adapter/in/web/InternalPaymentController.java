package com.ironvault.payments.adapter.in.web;

import com.ironvault.payments.adapter.in.dto.PaymentResponse;
import com.ironvault.payments.adapter.in.mapper.PaymentResponseMapper;
import com.ironvault.payments.domain.model.Payment;
import com.ironvault.payments.domain.port.in.command.CreatePaymentCommand;
import com.ironvault.payments.domain.port.in.payment.CreatePaymentUseCase;
import com.ironvault.payments.domain.port.in.payment.GetPaymentByIdUseCase;
import com.ironvault.payments.utils.IdempotencyKeyValidator;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/internal/payments")
public class InternalPaymentController {

    @Value("${app.auth.internal-api-key}")
    private String internalApiKey;

    private final CreatePaymentUseCase createPaymentUseCase;
    private final GetPaymentByIdUseCase getPaymentByIdUseCase;
    private final PaymentResponseMapper mapper;

    public InternalPaymentController(CreatePaymentUseCase createPaymentUseCase,
                                     GetPaymentByIdUseCase getPaymentByIdUseCase,
                                     PaymentResponseMapper mapper) {
        this.createPaymentUseCase = createPaymentUseCase;
        this.getPaymentByIdUseCase = getPaymentByIdUseCase;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> create(
            @RequestHeader("X-Internal-Key") String internalKey,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody InternalCreateRequest request) {

        if (!internalApiKey.equals(internalKey)) {
            return ResponseEntity.status(401).build();
        }

        IdempotencyKeyValidator.validateIdempotencyKey(idempotencyKey);

        Payment payment = createPaymentUseCase.create(
                new CreatePaymentCommand(
                        request.merchantId(),
                        request.amount(),
                        request.currency(),
                        request.paymentMethod(),
                        request.description(),
                        request.payerEmail()
                ),
                idempotencyKey
        );

        return ResponseEntity.status(201).body(mapper.toResponse(payment));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getById(
            @RequestHeader("X-Internal-Key") String internalKey,
            @PathVariable("id") UUID id) {

        if (!internalApiKey.equals(internalKey)) {
            return ResponseEntity.status(401).build();
        }

        Payment payment = getPaymentByIdUseCase.getById(id, null, true);
        return ResponseEntity.ok(mapper.toResponse(payment));

    }

    public record InternalCreateRequest(
            UUID merchantId,
            BigDecimal amount,
            String currency,
            String paymentMethod,
            String description,
            String payerEmail
    ) { }
}
