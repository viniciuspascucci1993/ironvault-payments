package com.ironvault.payments.domain.query;

import com.ironvault.payments.domain.enums.PaymentStatus;

import java.math.BigDecimal;
import java.util.UUID;

public class PaymentFilter {

    private final UUID merchantId;
    private final PaymentStatus status;
    private final String currency;
    private final BigDecimal minAmount;
    private final BigDecimal maxAmount;

    public PaymentFilter(UUID merchantId,
                        PaymentStatus status,
                         String currency,
                         BigDecimal minAmount,
                         BigDecimal maxAmount) {
        this.merchantId = merchantId;
        this.status = status;
        this.currency = currency;
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
    }

    public UUID getMerchantId() {
        return merchantId;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getMinAmount() {
        return minAmount;
    }

    public BigDecimal getMaxAmount() {
        return maxAmount;
    }
}
