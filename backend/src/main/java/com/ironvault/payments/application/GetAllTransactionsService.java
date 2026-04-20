package com.ironvault.payments.application;

import com.ironvault.payments.domain.enums.TransactionStatus;
import com.ironvault.payments.domain.enums.TransactionType;
import com.ironvault.payments.domain.model.Transaction;
import com.ironvault.payments.domain.port.in.payment.GetAllTransactionsUseCase;
import com.ironvault.payments.domain.port.out.TransactionRepositoryPort;
import com.ironvault.payments.domain.query.PageQuery;
import com.ironvault.payments.domain.query.PageResult;
import org.springframework.stereotype.Service;

@Service
public class GetAllTransactionsService implements GetAllTransactionsUseCase {

    private final TransactionRepositoryPort transactionRepositoryPort;

    public GetAllTransactionsService(TransactionRepositoryPort transactionRepositoryPort) {
        this.transactionRepositoryPort = transactionRepositoryPort;
    }

    @Override
    public PageResult<Transaction> getAll(TransactionType type, TransactionStatus status, PageQuery pageQuery) {
        return transactionRepositoryPort.findAll(type, status, pageQuery);
    }
}
