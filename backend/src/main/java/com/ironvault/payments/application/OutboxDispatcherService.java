package com.ironvault.payments.application;

import com.ironvault.payments.adapter.out.persistence.OutboxEventRepository;
import com.ironvault.payments.domain.enums.OutboxEventStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@Slf4j
public class OutboxDispatcherService {

    private final OutboxEventRepository outboxEventRepository;
    private final ProcessPaymentAsyncService processPaymentAsyncService;

    public OutboxDispatcherService(OutboxEventRepository outboxEventRepository,
                                   ProcessPaymentAsyncService processPaymentAsyncService) {
        this.outboxEventRepository = outboxEventRepository;
        this.processPaymentAsyncService = processPaymentAsyncService;
    }

    @Scheduled(fixedDelay = 500)
    public void dispatch() {

        var pendingEvents = outboxEventRepository.findTop10ByStatusOrderByCreatedAtAsc(
                OutboxEventStatus.PENDING);

        if (pendingEvents.isEmpty()) {
            return;
        }

        log.info("Outbox dispatcher found {} pending event(s)", pendingEvents.size());

        for (var event : pendingEvents) {
            try {

                event.setStatus(OutboxEventStatus.PROCESSED);
                event.setProcessedAt(Instant.now());
                outboxEventRepository.save(event);

                processPaymentAsyncService.processPayment(event.getPaymentId());

                log.info("Outbox event dispatched. paymentId={}", event.getPaymentId());

            } catch (Exception ex) {
                event.setStatus(OutboxEventStatus.FAILED);
                event.setProcessedAt(Instant.now());
                outboxEventRepository.save(event);

                log.error("Outbox dispatch failed. paymentId={} reason={}",
                        event.getPaymentId(), ex.getMessage());
            }
        }

    }
}
