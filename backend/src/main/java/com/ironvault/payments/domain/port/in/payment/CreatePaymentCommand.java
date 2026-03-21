package com.ironvault.payments.domain.port.in.payment;

import java.math.BigDecimal;

public class CreatePaymentCommand {

    private final BigDecimal amount;
    private final String currency;

    public CreatePaymentCommand(BigDecimal amount, String currency) {
        this.amount = amount;
        this.currency = currency;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }
}
