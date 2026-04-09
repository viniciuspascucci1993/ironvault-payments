package com.ironvault.payments.domain.port.out;

import com.ironvault.payments.adapter.out.entity.outbox.OutboxEventEntity;

import java.util.List;

public interface OutboxEventRepositoryPort {

    OutboxEventEntity save(OutboxEventEntity event);
    List<OutboxEventEntity> findPendingEvents();
}
