package com.ironvault.payments.domain.port.in.payment;

import com.ironvault.payments.domain.model.Payment;
import com.ironvault.payments.domain.query.PageQuery;
import com.ironvault.payments.domain.query.PageResult;
import com.ironvault.payments.domain.query.PaymentFilter;

public interface GetAllPaymentsUseCase {

    PageResult<Payment> getAllWithFilters(PaymentFilter filter, PageQuery pageQuery);
}
