package com.ironvault.payments.domain.port.in.command;


import java.math.BigDecimal;

public class CreatePaymentCommand {

    private BigDecimal amount;
    private String currency;
    private String paymentMethod;
    private String description;
    private String payerEmail;

    public CreatePaymentCommand(BigDecimal amount, String currency, String paymentMethod, String description, String payerEmail) {
        this.amount = amount;
        this.currency = currency;
        this.paymentMethod = paymentMethod;
        this.description = description;
        this.payerEmail = payerEmail;
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

    public String getPayerEmail() {
        return payerEmail;
    }

    public void setPayerEmail(String payerEmail) {
        this.payerEmail = payerEmail;
    }
}
