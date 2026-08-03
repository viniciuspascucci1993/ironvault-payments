package com.ironvault.payments.domain.port.in.payment;

import com.ironvault.payments.domain.enums.TransactionStatus;
import com.ironvault.payments.domain.enums.TransactionType;
import com.ironvault.payments.domain.model.Transaction;
import com.ironvault.payments.domain.query.PageQuery;
import com.ironvault.payments.domain.query.PageResult;

import java.util.UUID;

public interface GetAllTransactionsUseCase {

    PageResult<Transaction> getAll(UUID merchantId, TransactionType type, TransactionStatus status, PageQuery pageQuery);
}
