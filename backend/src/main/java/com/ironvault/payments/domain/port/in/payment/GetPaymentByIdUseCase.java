package com.ironvault.payments.domain.port.in.payment;

import com.ironvault.payments.domain.model.Payment;

import java.util.UUID;

public interface GetPaymentByIdUseCase {

    Payment getById(UUID id, UUID requesterMerchantId, boolean isAdmin);
}
