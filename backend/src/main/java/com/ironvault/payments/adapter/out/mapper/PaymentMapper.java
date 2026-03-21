package com.ironvault.payments.adapter.out.mapper;

import com.ironvault.payments.adapter.out.entity.PaymentEntity;
import com.ironvault.payments.domain.model.Payment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    PaymentEntity toEntity(Payment payment);
    Payment toDomain(PaymentEntity paymentEntity);
}
