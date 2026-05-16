package com.ironvault.payments.adapter.in.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdatePaymentStatusRequest {

    @NotBlank(message = "status is required")
    private String status;
    private String failureReason;
}
