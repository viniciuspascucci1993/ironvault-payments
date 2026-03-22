package com.ironvault.payments.adapter.in.mapper;

import com.ironvault.payments.adapter.in.dto.PaymentResponse;
import com.ironvault.payments.domain.model.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentResponseMapper {

    @Mapping(target = "status", expression = "java(payment.getStatus().name())")
    PaymentResponse toResponse(Payment payment);
}
