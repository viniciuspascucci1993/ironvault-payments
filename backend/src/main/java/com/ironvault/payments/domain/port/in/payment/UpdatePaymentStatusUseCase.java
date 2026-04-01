package com.ironvault.payments.domain.port.in.payment;

import com.ironvault.payments.domain.model.Payment;

public interface UpdatePaymentStatusUseCase {

    Payment updateStatus(UpdatePaymentStatusCommand command);
}
