package com.ironvault.payments.application.signature;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

@Service
public class WebhookSignatureService {

    private final String secret;
    private final long toleranceSeconds;

    public WebhookSignatureService(@Value("${app.webhook.mock-secret}") String secret,
                                   @Value("${app.webhook.signature-tolerance-seconds:300}") long toleranceSeconds) {
        this.secret = secret;
        this.toleranceSeconds = toleranceSeconds;
    }

    public void validate(String signatureHeader, String timestampHeader, byte[] payloadBytes) {
        if (signatureHeader == null || signatureHeader.isBlank()) {
            throw new IllegalArgumentException("Missing webhook signature");
        }
        if (timestampHeader == null || timestampHeader.isBlank()) {
            throw new IllegalArgumentException("Missing webhook timestamp");
        }

        long timestamp;
        try {
            timestamp = Long.parseLong(timestampHeader);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid webhook timestamp");
        }

        long now = Instant.now().getEpochSecond();
        if (Math.abs(now - timestamp) > toleranceSeconds) {
            throw new IllegalArgumentException("Webhook timestamp outside tolerance window");
        }

        String signedPayload = timestampHeader + "." + new String(payloadBytes, StandardCharsets.UTF_8);
        String expected = hmacSha256Base64(signedPayload, secret);

        if (!constantTimeEquals(expected, signatureHeader)) {
            throw new IllegalArgumentException("Invalid webhook signature");
        }
    }

    private String hmacSha256Base64(String data, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return Base64.getEncoder().encodeToString(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to validate webhook signature", ex);
        }
    }

    private boolean constantTimeEquals(String a, String b) {
        byte[] x = a.getBytes(StandardCharsets.UTF_8);
        byte[] y = b.getBytes(StandardCharsets.UTF_8);
        if (x.length != y.length) return false;
        int result = 0;
        for (int i = 0; i < x.length; i++) result |= x[i] ^ y[i];
        return result == 0;
    }
}
