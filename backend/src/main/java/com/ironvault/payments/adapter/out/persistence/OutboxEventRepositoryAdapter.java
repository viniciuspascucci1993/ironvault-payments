package com.ironvault.payments.adapter.out.persistence;

import com.ironvault.payments.adapter.out.entity.outbox.OutboxEventEntity;
import com.ironvault.payments.domain.enums.OutboxEventStatus;
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
    public OutboxEventEntity save(OutboxEventEntity event) {
        return jpaRepository.save(event);
    }

    @Override
    public List<OutboxEventEntity> findPendingEvents() {
        return jpaRepository.findTop10ByStatusOrderByCreatedAtAsc(OutboxEventStatus.PENDING);
    }
}
