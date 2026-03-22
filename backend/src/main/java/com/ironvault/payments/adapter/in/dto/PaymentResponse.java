package com.ironvault.payments.adapter.in.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponse {

    private UUID id;
    private BigDecimal amount;
    private String currency;
    private String status;
    private Instant createdAt;
}
