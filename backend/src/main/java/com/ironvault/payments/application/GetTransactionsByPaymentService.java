package com.ironvault.payments.application;

import com.ironvault.payments.domain.model.Transaction;
import com.ironvault.payments.domain.port.in.payment.GetTransactionsByPaymentUseCase;
import com.ironvault.payments.domain.port.out.TransactionRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class GetTransactionsByPaymentService implements GetTransactionsByPaymentUseCase {

    private final TransactionRepositoryPort transactionRepositoryPort;

    public GetTransactionsByPaymentService(TransactionRepositoryPort transactionRepositoryPort) {
        this.transactionRepositoryPort = transactionRepositoryPort;
    }

    @Override
    public List<Transaction> getByPaymentId(UUID paymentId) {
        return transactionRepositoryPort.findByPaymentId(paymentId);
    }
}
