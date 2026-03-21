package com.ironvault.payments.domain.port.in;

import com.ironvault.payments.domain.model.Payment;
import java.math.BigDecimal;

public interface CreatePaymentUseCase {

    Payment create(Command command);

    record Command(BigDecimal amount, String currency) {
    }
}
