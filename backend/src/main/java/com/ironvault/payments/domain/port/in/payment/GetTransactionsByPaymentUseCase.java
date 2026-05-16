package com.ironvault.payments.domain.port.in.payment;

import com.ironvault.payments.domain.model.Transaction;

import java.util.List;
import java.util.UUID;

public interface GetTransactionsByPaymentUseCase {

    List<Transaction> getByPaymentId(UUID paymentId);
}
