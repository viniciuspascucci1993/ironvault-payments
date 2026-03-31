package com.ironvault.payments.domain.port.in.payment;


import java.math.BigDecimal;

public class CreatePaymentCommand {

    private BigDecimal amount;
    private String currency;
    private String paymentMethod;
    private String description;

    public CreatePaymentCommand(BigDecimal amount, String currency, String paymentMethod, String description) {
        this.amount = amount;
        this.currency = currency;
        this.paymentMethod = paymentMethod;
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
