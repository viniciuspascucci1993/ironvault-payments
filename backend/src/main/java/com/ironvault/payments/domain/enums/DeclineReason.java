package com.ironvault.payments.domain.enums;

public enum DeclineReason {

    INSUFFICIENT_FUNDS,
    SUSPECTED_FRAUD,
    CARD_EXPIRED,
    LIMIT_EXCEEDED,
    PROCESSOR_UNAVAILABLE,
    UNKNOWN
}
