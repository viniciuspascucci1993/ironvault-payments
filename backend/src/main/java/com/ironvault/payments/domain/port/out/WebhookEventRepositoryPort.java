package com.ironvault.payments.domain.port.out;

import java.time.Instant;

public interface WebhookEventRepositoryPort {

    boolean existsById(String eventId);
    void save(String eventId, String externalId, Instant receivedAt);
}
