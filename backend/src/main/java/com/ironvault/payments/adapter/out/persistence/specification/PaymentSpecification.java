package com.ironvault.payments.adapter.out.persistence.specification;

import com.ironvault.payments.adapter.out.entity.PaymentEntity;
import com.ironvault.payments.domain.enums.PaymentStatus;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.UUID;

public class PaymentSpecification {

    public static Specification<PaymentEntity> withFilters(
            UUID merchantId,
            PaymentStatus status,
            String currency,
            BigDecimal minAmount,
            BigDecimal maxAmount
    ) {

        return (root, query, cb) -> {

            var predicates = cb.conjunction();

            if (merchantId != null) {
                predicates = cb.and(predicates,
                        cb.equal(root.get("merchantId"), merchantId));
            }

            if (status != null) {
                predicates = cb.and(predicates,
                        cb.equal(root.get("status"), status));
            }

            if (currency != null) {
                predicates = cb.and(predicates,
                        cb.equal(root.get("currency"), currency));
            }

            if (minAmount != null) {
                predicates = cb.and(predicates,
                        cb.greaterThanOrEqualTo(root.get("amount"), minAmount));
            }

            if (maxAmount != null) {
                predicates = cb.and(predicates,
                        cb.lessThanOrEqualTo(root.get("amount"), maxAmount));
            }

            return predicates;
        };
    }
}
