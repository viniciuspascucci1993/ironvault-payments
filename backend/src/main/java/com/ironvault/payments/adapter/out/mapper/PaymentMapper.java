package com.ironvault.payments.adapter.out.mapper;

import com.ironvault.payments.adapter.out.entity.PaymentEntity;
import com.ironvault.payments.domain.model.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(target = "status", source = "status")
    @Mapping(target = "paymentMethod", source = "paymentMethod")
    PaymentEntity toEntity(Payment payment);

    @Mapping(target = "status", source = "status")
    @Mapping(target = "paymentMethod", source = "paymentMethod")
    Payment toDomain(PaymentEntity paymentEntity);
}
