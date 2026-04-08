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
    private String paymentMethod;
    private String description;
    private String externalId;
    private String gatewayCode;
    private String gatewayMessage;
    private String declineReason;
    private String pixQrCode;
    private String pixCopyPaste;
    private String payerEmail;
    private String failureReason;
    private Instant createdAt;
    private Instant updatedAt;
}
