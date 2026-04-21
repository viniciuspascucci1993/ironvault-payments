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

    private final OutboxEventRepositoryPort outboxEventRepository;
    private final ProcessPaymentAsyncService processPaymentAsyncService;

    public OutboxDispatcherService(OutboxEventRepositoryPort outboxEventRepository,
                                   ProcessPaymentAsyncService processPaymentAsyncService) {
        this.outboxEventRepository = outboxEventRepository;
        this.processPaymentAsyncService = processPaymentAsyncService;
    }

    @Scheduled(fixedDelay = 500)
    public void dispatch() {

        var pendingEvents = outboxEventRepository.findPendingEvents();
        if (pendingEvents.isEmpty()) return;

        for (var event : pendingEvents) {
            log.info("Dispatching outbox event. paymentId={}", event.getPaymentId());
            processPaymentAsyncService.processPayment(event.getPaymentId(), event);
        }
    }

}