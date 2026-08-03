package com.ironvault.payments.application;

import com.ironvault.payments.domain.model.Payment;
import com.ironvault.payments.domain.port.in.payment.GetAllPaymentsUseCase;
import com.ironvault.payments.domain.port.out.PaymentRepositoryPort;
import com.ironvault.payments.domain.query.PageQuery;
import com.ironvault.payments.domain.query.PageResult;
import com.ironvault.payments.domain.query.PaymentFilter;
import org.springframework.stereotype.Service;

@Service
public class GetAllPaymentsService implements GetAllPaymentsUseCase {

    private final PaymentRepositoryPort paymentRepositoryPort;

    public GetAllPaymentsService(PaymentRepositoryPort paymentRepositoryPort) {
        this.paymentRepositoryPort = paymentRepositoryPort;
    }

    @Override
    public PageResult<Payment> getAllWithFilters(PaymentFilter filter, PageQuery pageQuery) {

        return paymentRepositoryPort.findAllWithFilters(
                filter.getMerchantId(),
                filter.getStatus(),
                filter.getCurrency(),
                filter.getMinAmount(),
                filter.getMaxAmount(),
                pageQuery
        );
    }
}
