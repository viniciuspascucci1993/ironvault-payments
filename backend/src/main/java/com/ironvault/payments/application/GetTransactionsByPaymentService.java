package com.ironvault.payments.application;

import com.ironvault.payments.domain.model.Transaction;
import com.ironvault.payments.domain.port.in.payment.GetPaymentByIdUseCase;
import com.ironvault.payments.domain.port.in.payment.GetTransactionsByPaymentUseCase;
import com.ironvault.payments.domain.port.out.TransactionRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class GetTransactionsByPaymentService implements GetTransactionsByPaymentUseCase {

    private final TransactionRepositoryPort transactionRepositoryPort;
    private final GetPaymentByIdUseCase getPaymentByIdUseCase;

    public GetTransactionsByPaymentService(TransactionRepositoryPort transactionRepositoryPort,
                                           GetPaymentByIdUseCase getPaymentByIdUseCase) {
        this.transactionRepositoryPort = transactionRepositoryPort;
        this.getPaymentByIdUseCase = getPaymentByIdUseCase;
    }

    @Override
    public List<Transaction> getByPaymentId(UUID paymentId, UUID requesterMerchantId, boolean isAdmin) {
        getPaymentByIdUseCase.getById(paymentId, requesterMerchantId, isAdmin);
        return transactionRepositoryPort.findByPaymentId(paymentId);
    }
}
