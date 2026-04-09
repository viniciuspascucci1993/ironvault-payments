package com.ironvault.payments.application;

import com.ironvault.payments.domain.enums.OutboxEventStatus;
import com.ironvault.payments.domain.port.out.OutboxEventRepositoryPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@Slf4j
public class OutboxDispatcherService {

    private final OutboxEventRepositoryPort outboxEventRepository;  // ← trocou
    private final ProcessPaymentAsyncService processPaymentAsyncService;

    public OutboxDispatcherService(OutboxEventRepositoryPort outboxEventRepository,
                                   ProcessPaymentAsyncService processPaymentAsyncService) {
        this.outboxEventRepository = outboxEventRepository;
        this.processPaymentAsyncService = processPaymentAsyncService;
    }

    @Scheduled(fixedDelay = 500)
    public void dispatch() {

        var pendingEvents = outboxEventRepository.findPendingEvents();

        if (pendingEvents.isEmpty()) {
            return;
        }

        log.info("Outbox dispatcher found {} pending event(s)", pendingEvents.size());

        for (var event : pendingEvents) {
            try {
                processPaymentAsyncService.processPayment(event.getPaymentId()); // ← despacha primeiro

                event.setStatus(OutboxEventStatus.PROCESSED); // ← só marca depois que foi aceito
                event.setProcessedAt(Instant.now());
                outboxEventRepository.save(event);

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
