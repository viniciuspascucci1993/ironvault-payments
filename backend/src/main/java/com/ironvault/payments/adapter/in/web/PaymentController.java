package com.ironvault.payments.adapter.in.web;

import com.ironvault.payments.adapter.in.dto.PaymentRequest;
import com.ironvault.payments.adapter.in.dto.PaymentResponse;
import com.ironvault.payments.adapter.in.mapper.PaymentResponseMapper;
import com.ironvault.payments.adapter.in.dto.UpdatePaymentStatusRequest;
import com.ironvault.payments.domain.port.in.payment.UpdatePaymentStatusCommand;
import com.ironvault.payments.domain.port.in.payment.UpdatePaymentStatusUseCase;
import com.ironvault.payments.domain.enums.PaymentStatus;
import com.ironvault.payments.domain.model.Payment;
import com.ironvault.payments.domain.port.in.payment.CreatePaymentCommand;
import com.ironvault.payments.domain.port.in.payment.CreatePaymentUseCase;
import com.ironvault.payments.domain.port.in.payment.GetAllPaymentsUseCase;
import com.ironvault.payments.domain.port.in.payment.GetPaymentByIdUseCase;
import com.ironvault.payments.domain.query.PageQuery;
import com.ironvault.payments.domain.query.PageResult;
import com.ironvault.payments.domain.query.PaymentFilter;
import com.ironvault.payments.utils.IdempotencyKeyValidator;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final CreatePaymentUseCase createPaymentUseCase;
    private final GetAllPaymentsUseCase getAllPaymentsUseCase;
    private final GetPaymentByIdUseCase getPaymentByIdUseCase;
    private final UpdatePaymentStatusUseCase updatePaymentStatusUseCase;

    private final PaymentResponseMapper mapper;

    public PaymentController(CreatePaymentUseCase createPaymentUseCase,
                             GetAllPaymentsUseCase getAllPaymentsUseCase,
                             GetPaymentByIdUseCase getPaymentByIdUseCase,
                             UpdatePaymentStatusUseCase updatePaymentStatusUseCase,
                             PaymentResponseMapper mapper) {
        this.createPaymentUseCase = createPaymentUseCase;
        this.getAllPaymentsUseCase = getAllPaymentsUseCase;
        this.getPaymentByIdUseCase = getPaymentByIdUseCase;
        this.updatePaymentStatusUseCase = updatePaymentStatusUseCase;
        this.mapper = mapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<PaymentResponse> create(
            @RequestHeader(value = "Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody PaymentRequest request) {

        // Validate idempotencyKey
        IdempotencyKeyValidator.validateIdempotencyKey(idempotencyKey);

        Payment payment = createPaymentUseCase.create(
                new CreatePaymentCommand(request.getAmount(), request.getCurrency(), request.getPaymentMethod(),
                        request.getDescription(), request.getPayerEmail()),
                idempotencyKey
        );

        return ResponseEntity.status(201).body(mapper.toResponse(payment));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<PaymentResponse> updateStatus(
            @PathVariable("id") UUID id,
            @Valid @RequestBody UpdatePaymentStatusRequest request) {

        PaymentStatus targetStatus;
        try {
            targetStatus = PaymentStatus.valueOf(request.getStatus().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid status: " + request.getStatus());
        }

        Payment updated = updatePaymentStatusUseCase.updateStatus(
                new UpdatePaymentStatusCommand(
                        id,
                        targetStatus,
                        request.getFailureReason()
                )
        );

        return ResponseEntity.ok(mapper.toResponse(updated));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getById(@PathVariable("id")UUID id) {

        Payment payment = getPaymentByIdUseCase.getById(id);
        return ResponseEntity.ok(mapper.toResponse(payment));
    }

    @GetMapping
    public ResponseEntity<PageResult<PaymentResponse>> getAll(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String currency,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PaymentStatus paymentStatus = null;

        if (status != null) {
            try {
                paymentStatus = PaymentStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("Invalid status: " + status);
            }
        }

        PaymentFilter filter = new PaymentFilter(
                paymentStatus,
                currency,
                minAmount,
                maxAmount
        );

        if (currency != null && currency.length() != 3) {
            throw new IllegalArgumentException(
                    "Currency must be a 3-letter code (e.g. BRL, USD)"
            );
        }

        if (minAmount != null && maxAmount != null && minAmount.compareTo(maxAmount) > 0) {
            throw new IllegalArgumentException(
                    "minAmount cannot be greater than maxAmount"
            );
        }

        PageQuery pageQuery = new PageQuery(page, size);

        PageResult<PaymentResponse> response =
                getAllPaymentsUseCase
                        .getAllWithFilters(filter, pageQuery)
                        .map(mapper::toResponse);

        return ResponseEntity.ok(response);
    }

}
