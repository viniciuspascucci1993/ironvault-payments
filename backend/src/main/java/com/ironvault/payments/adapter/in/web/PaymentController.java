package com.ironvault.payments.adapter.in.web;

import com.ironvault.payments.adapter.in.dto.CreatePaymentRequest;
import com.ironvault.payments.adapter.in.dto.CreatePaymentResponse;
import com.ironvault.payments.domain.enums.PaymentStatus;
import com.ironvault.payments.domain.model.Payment;
import com.ironvault.payments.domain.port.in.payment.CreatePaymentCommand;
import com.ironvault.payments.domain.port.in.payment.CreatePaymentUseCase;
import com.ironvault.payments.domain.port.in.payment.GetAllPaymentsUseCase;
import com.ironvault.payments.domain.port.in.payment.GetPaymentByIdUseCase;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final CreatePaymentUseCase createPaymentUseCase;
    private final GetAllPaymentsUseCase getAllPaymentsUseCase;
    private final GetPaymentByIdUseCase getPaymentByIdUseCase;

    public PaymentController(CreatePaymentUseCase createPaymentUseCase,
                             GetAllPaymentsUseCase getAllPaymentsUseCase,
                             GetPaymentByIdUseCase getPaymentByIdUseCase) {
        this.createPaymentUseCase = createPaymentUseCase;
        this.getAllPaymentsUseCase = getAllPaymentsUseCase;
        this.getPaymentByIdUseCase = getPaymentByIdUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<CreatePaymentResponse> create(@Valid @RequestBody CreatePaymentRequest request) {

        Payment payment = createPaymentUseCase.create(
                new CreatePaymentCommand(
                        request.getAmount(),
                        request.getCurrency()
                )
        );

        CreatePaymentResponse response = new CreatePaymentResponse(
                payment.getId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getStatus().name(),
                payment.getCreatedAt()
        );

        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CreatePaymentResponse> getById(@PathVariable("id")UUID id) {

        Payment payment = getPaymentByIdUseCase.getById(id);

        CreatePaymentResponse response = new CreatePaymentResponse(
                payment.getId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getStatus().name(),
                payment.getCreatedAt()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<CreatePaymentResponse>> getAll(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String currency,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            Pageable pageable) {

        Page<CreatePaymentResponse> response =
                getAllPaymentsUseCase.getAllWithFilters(
                        status,
                        currency,
                        minAmount,
                        maxAmount,
                        pageable
                ).map(p -> new CreatePaymentResponse(
                        p.getId(),
                        p.getAmount(),
                        p.getCurrency(),
                        p.getStatus().name(),
                        p.getCreatedAt()
                ));

        return ResponseEntity.ok(response);
    }

}
