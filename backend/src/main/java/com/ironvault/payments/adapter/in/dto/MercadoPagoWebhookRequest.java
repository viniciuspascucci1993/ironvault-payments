package com.ironvault.payments.adapter.in.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MercadoPagoWebhookRequest {

    private Long id;

    @JsonProperty("live_mode")
    private boolean liveMode;

    private String type;

    @JsonProperty("date_created")
    private String dateCreated;

    private String action;

    private MercadoPagoWebhookData data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MercadoPagoWebhookData {
        private String id;
    }
}
