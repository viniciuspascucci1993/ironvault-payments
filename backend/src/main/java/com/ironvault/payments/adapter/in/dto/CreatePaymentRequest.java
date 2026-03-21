package com.ironvault.payments.adapter.in.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreatePaymentRequest {

    @NotNull
    private BigDecimal amount;

    @NotBlank
    private String currency;
}
