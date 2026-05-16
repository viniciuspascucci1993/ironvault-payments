package com.ironvault.payments.adapter.out.persistence;

import com.ironvault.payments.adapter.out.entity.outbox.OutboxEventEntity;
import com.ironvault.payments.domain.enums.OutboxEventStatus;
import com.ironvault.payments.domain.model.OutboxEvent;
import com.ironvault.payments.domain.port.out.OutboxEventRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OutboxEventRepositoryAdapter implements OutboxEventRepositoryPort {

    private final OutboxEventRepository jpaRepository;

    public OutboxEventRepositoryAdapter(OutboxEventRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public OutboxEvent save(OutboxEvent event) {
        OutboxEventEntity entity = toEntity(event);
        OutboxEventEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public List<OutboxEvent> findPendingEvents() {
        return jpaRepository
                .findTop10ByStatusOrderByCreatedAtAsc(OutboxEventStatus.PENDING)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private OutboxEventEntity toEntity(OutboxEvent event) {
        OutboxEventEntity entity = new OutboxEventEntity();
        entity.setId(event.getId());
        entity.setPaymentId(event.getPaymentId());
        entity.setStatus(event.getStatus());
        entity.setCreatedAt(event.getCreatedAt());
        entity.setProcessedAt(event.getProcessedAt());
        return entity;
    }

    private OutboxEvent toDomain(OutboxEventEntity entity) {
        OutboxEvent event = new OutboxEvent();
        event.setId(entity.getId());
        event.setPaymentId(entity.getPaymentId());
        event.setStatus(entity.getStatus());
        event.setCreatedAt(entity.getCreatedAt());
        event.setProcessedAt(entity.getProcessedAt());
        return event;
    }
}
