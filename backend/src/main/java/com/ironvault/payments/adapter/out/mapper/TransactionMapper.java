package com.ironvault.payments.adapter.out.mapper;

import com.ironvault.payments.adapter.out.entity.TransactionEntity;
import com.ironvault.payments.domain.model.Transaction;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    Transaction toDomain(TransactionEntity entity);
    TransactionEntity toEntity(Transaction domain);
}
