package com.ironvault.payments.adapter.in.mapper;

import com.ironvault.payments.adapter.in.dto.PaymentResponse;
import com.ironvault.payments.domain.model.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentResponseMapper {

    @Mapping(target = "status", expression = "java(mapEnum(payment.getStatus()))")
    PaymentResponse toResponse(Payment payment);

    default String mapEnum(Enum<?> e) {
        return e != null ? e.name() : null;
    }
}
