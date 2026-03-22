package com.ironvault.payments.utils;

import java.util.regex.Pattern;

public class IdempotencyKeyValidator {

    private static final Pattern PATTERN =
            Pattern.compile("^ironvault-[a-zA-Z0-9\\-]{8,}$");

    public static void validateIdempotencyKey(String key) {

        if (key == null || key.isBlank()) return;

        if (!PATTERN.matcher(key).matches()) {
            throw new IllegalArgumentException(
                    "Invalid idempotency key format. Expected: ironvault-{random}"
            );
        }
        }
}
