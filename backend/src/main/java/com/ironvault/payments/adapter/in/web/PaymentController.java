package com.ironvault.payments.adapter.in.web;

import com.ironvault.payments.adapter.in.dto.CreatePaymentRequest;
import com.ironvault.payments.adapter.in.dto.CreatePaymentResponse;
import com.ironvault.payments.domain.model.Payment;
import com.ironvault.payments.domain.port.in.payment.CreatePaymentCommand;
import com.ironvault.payments.domain.port.in.payment.CreatePaymentUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final CreatePaymentUseCase createPaymentUseCase;

    public PaymentController(CreatePaymentUseCase createPaymentUseCase) {
        this.createPaymentUseCase = createPaymentUseCase;
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
}
