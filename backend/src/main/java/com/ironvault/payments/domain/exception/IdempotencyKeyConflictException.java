package com.ironvault.payments.domain.exception;

public class IdempotencyKeyConflictException extends RuntimeException {

    public IdempotencyKeyConflictException(String idempotencyKey) {
        super("Idempotency key already in use: " + idempotencyKey);
    }
}
