package com.ironvault.payments.domain.model;

import com.ironvault.payments.domain.enums.PaymentStatus;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
public class Payment {

    private final UUID id;
    private final BigDecimal amount;
    private final String currency;
    private final PaymentStatus status;
    private final Instant createdAt;

    public Payment(UUID id,
                   BigDecimal amount,
                   String currency,
                   PaymentStatus status,
                   Instant createdAt) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        this.id = id;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.createdAt = createdAt;
    }
}
