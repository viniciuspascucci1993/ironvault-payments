package com.ironvault.payments.domain.port.in.payment;

import com.ironvault.payments.domain.model.Payment;
import com.ironvault.payments.domain.port.in.command.UpdatePaymentStatusCommand;

public interface UpdatePaymentStatusUseCase {

    Payment updateStatus(UpdatePaymentStatusCommand command);
}
