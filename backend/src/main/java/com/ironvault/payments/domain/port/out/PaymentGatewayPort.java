package com.ironvault.payments.domain.port.out;

import com.ironvault.payments.domain.model.Payment;
import com.ironvault.payments.domain.model.PaymentGatewayResult;

public interface PaymentGatewayPort {

    PaymentGatewayResult process(Payment payment);
}
