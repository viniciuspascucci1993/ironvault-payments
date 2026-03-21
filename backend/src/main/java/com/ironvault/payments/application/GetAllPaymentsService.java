package com.ironvault.payments.application;

import com.ironvault.payments.domain.model.Payment;
import com.ironvault.payments.domain.port.in.payment.GetAllPaymentsUseCase;
import com.ironvault.payments.domain.port.out.PaymentRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetAllPaymentsService implements GetAllPaymentsUseCase {

    private final PaymentRepositoryPort paymentRepositoryPort;

    public GetAllPaymentsService(PaymentRepositoryPort paymentRepositoryPort) {
        this.paymentRepositoryPort = paymentRepositoryPort;
    }

    @Override
    public List<Payment> getAll() {
        return paymentRepositoryPort.findAll();
    }
}
