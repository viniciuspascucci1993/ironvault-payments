package com.ironvault.payments.domain.port.in.payment;

import com.ironvault.payments.domain.model.Payment;

import java.util.List;

public interface GetAllPaymentsUseCase {

    List<Payment> getAll();
}
