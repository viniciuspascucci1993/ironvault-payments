package com.ironvault.payments.domain.port.in.webhook;

public interface HandleWebhookUseCase {

    boolean handle(String eventId,
                   String externalId,
                   String status,
                   String gatewayCode,
                   String gatewayMessage,
                   String failureReason);
}
