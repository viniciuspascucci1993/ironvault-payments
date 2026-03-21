package com.ironvault.payments.adapter.in.web;

import com.ironvault.payments.domain.model.Payment;
import com.ironvault.payments.domain.port.in.CreatePaymentUseCase;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import org.springframework.http.HttpStatus;
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
    public Payment create(@Valid @RequestBody CreatePaymentRequest request) {
        return createPaymentUseCase.create(new CreatePaymentUseCase.Command(request.amount(), request.currency()));
    }

    public record CreatePaymentRequest(
            @DecimalMin(value = "0.01") BigDecimal amount,
            @NotBlank String currency
    ) {
    }
}
