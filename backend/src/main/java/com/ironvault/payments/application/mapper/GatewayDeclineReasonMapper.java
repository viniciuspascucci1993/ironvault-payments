package com.ironvault.payments.application.mapper;

import com.ironvault.payments.domain.enums.DeclineReason;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class GatewayDeclineReasonMapper {

    public DeclineReason map(String gatewayCode, String gatewayMessage) {
        String code = normalize(gatewayCode);
        String message = normalize(gatewayMessage);

        if (code.contains("INSUFFICIENT") || message.contains("INSUFFICIENT")) {
            return DeclineReason.INSUFFICIENT_FUNDS;
        }
        if (code.contains("FRAUD") || message.contains("FRAUD")) {
            return DeclineReason.SUSPECTED_FRAUD;
        }
        if (code.contains("EXPIRED") || message.contains("EXPIRED")) {
            return DeclineReason.CARD_EXPIRED;
        }
        if (code.contains("LIMIT") || message.contains("LIMIT")) {
            return DeclineReason.LIMIT_EXCEEDED;
        }
        if (code.contains("TIMEOUT") || message.contains("TIMEOUT")
                || code.contains("UNAVAILABLE") || message.contains("UNAVAILABLE")) {
            return DeclineReason.PROCESSOR_UNAVAILABLE;
        }

        return DeclineReason.UNKNOWN;
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }
}
