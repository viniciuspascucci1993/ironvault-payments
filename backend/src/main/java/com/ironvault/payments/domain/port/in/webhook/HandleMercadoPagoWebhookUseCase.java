package com.ironvault.payments.domain.port.in.webhook;

public interface HandleMercadoPagoWebhookUseCase {
    boolean handle(String eventId, String externalId);
}
