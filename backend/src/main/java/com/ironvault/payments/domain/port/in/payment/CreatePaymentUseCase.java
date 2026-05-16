package com.ironvault.payments.domain.port.in.payment;

import com.ironvault.payments.domain.model.Payment;
import com.ironvault.payments.domain.port.in.command.CreatePaymentCommand;

public interface CreatePaymentUseCase {

    Payment create(CreatePaymentCommand createPaymentCommand, String idempotencyKey);

}
