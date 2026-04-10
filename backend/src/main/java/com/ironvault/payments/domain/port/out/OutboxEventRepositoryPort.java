package com.ironvault.payments.domain.port.out;

import com.ironvault.payments.domain.model.OutboxEvent;
import java.util.List;

public interface OutboxEventRepositoryPort {

    OutboxEvent save(OutboxEvent event);
    List<OutboxEvent> findPendingEvents();
}
