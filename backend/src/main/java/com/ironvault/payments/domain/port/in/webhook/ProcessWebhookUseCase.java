package com.ironvault.payments.domain.port.in.webhook;

public interface ProcessWebhookUseCase {

    void process(String eventId,
                 String externalId,
                 String status,
                 String gatewayCode,
                 String gatewayMessage,
                 String failureReason);
}
