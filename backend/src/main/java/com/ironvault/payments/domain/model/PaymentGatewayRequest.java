package com.ironvault.payments.domain.model;

import java.math.BigDecimal;

public class PaymentGatewayRequest {

    private final Payment payment;
    private final String sellerAccessToken;
    private final BigDecimal applicationFee;

    public PaymentGatewayRequest(Payment payment, String sellerAccessToken, BigDecimal applicationFee) {
        this.payment = payment;
        this.sellerAccessToken = sellerAccessToken;
        this.applicationFee = applicationFee;
    }

    public Payment getPayment() {
        return payment;
    }

    public String getSellerAccessToken() {
        return sellerAccessToken;
    }

    public BigDecimal getApplicationFee() {
        return applicationFee;
    }
}
