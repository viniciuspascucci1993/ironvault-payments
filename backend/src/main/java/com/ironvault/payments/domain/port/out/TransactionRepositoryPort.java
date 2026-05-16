package com.ironvault.payments.domain.port.out;

import com.ironvault.payments.domain.enums.TransactionStatus;
import com.ironvault.payments.domain.enums.TransactionType;
import com.ironvault.payments.domain.model.Transaction;
import com.ironvault.payments.domain.query.PageQuery;
import com.ironvault.payments.domain.query.PageResult;

import java.util.List;
import java.util.UUID;

public interface TransactionRepositoryPort {

    Transaction save(Transaction transaction);
    List<Transaction> findByPaymentId(UUID paymentId);
    PageResult<Transaction> findAll(TransactionType type, TransactionStatus status, PageQuery pageQuery);
}
