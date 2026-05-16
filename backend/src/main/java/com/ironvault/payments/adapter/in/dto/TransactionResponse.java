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
public class TransactionResponse {

    private UUID id;
    private UUID paymentId;
    private String externalId;
    private String type;
    private String status;
    private BigDecimal amount;
    private String currency;
    private String gatewayCode;
    private String gatewayMessage;
    private Instant createdAt;
    private Instant updatedAt;
}
