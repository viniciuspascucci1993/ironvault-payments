package com.ironvault.payments.adapter.out.persistence.specification;

import com.ironvault.payments.adapter.out.entity.TransactionEntity;
import com.ironvault.payments.domain.enums.TransactionStatus;
import com.ironvault.payments.domain.enums.TransactionType;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class TransactionSpecification {

    public static Specification<TransactionEntity> withFilters(
            UUID merchantId,
            TransactionType type,
            TransactionStatus status
    ) {

        return (root, query, cb) -> {

            var predicates = cb.conjunction();

            if (merchantId != null) {
                predicates = cb.and(predicates,
                        cb.equal(root.get("merchantId"), merchantId));
            }

            if (type != null) {
                predicates = cb.and(predicates,
                        cb.equal(root.get("type"), type));
            }

            if (status != null) {
                predicates = cb.and(predicates,
                        cb.equal(root.get("status"), status));
            }

            return predicates;
        };
    }
}
