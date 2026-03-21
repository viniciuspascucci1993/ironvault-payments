package com.ironvault.payments.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record Payment(
        UUID id,
        BigDecimal amount,
        String currency,
        String status,
        Instant createdAt
) {
}
