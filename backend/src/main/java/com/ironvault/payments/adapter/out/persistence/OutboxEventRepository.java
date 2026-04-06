package com.ironvault.payments.adapter.out.persistence;

import com.ironvault.payments.adapter.out.entity.outbox.OutboxEventEntity;
import com.ironvault.payments.domain.enums.OutboxEventStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxEventEntity, UUID> {

    List<OutboxEventEntity> findTop10ByStatusOrderByCreatedAtAsc(OutboxEventStatus status);
}
