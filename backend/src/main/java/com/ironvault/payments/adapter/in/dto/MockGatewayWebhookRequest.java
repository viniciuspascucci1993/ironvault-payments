package com.ironvault.payments.adapter.in.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MockGatewayWebhookRequest {


    @NotBlank(message = "eventId is required")
    private String eventId;
    @NotBlank(message = "externalId is required")
    private String externalId;
    @NotBlank(message = "status is required")
    private String status;
    private String failureReason;
}
