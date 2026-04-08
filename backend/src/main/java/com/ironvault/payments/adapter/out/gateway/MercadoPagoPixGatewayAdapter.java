package com.ironvault.payments.adapter.out.gateway;

import com.ironvault.payments.domain.enums.PaymentStatus;
import com.ironvault.payments.domain.model.Payment;
import com.ironvault.payments.domain.model.PaymentGatewayResult;
import com.ironvault.payments.domain.port.out.PaymentGatewayPort;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.payment.PaymentCreateRequest;
import com.mercadopago.client.payment.PaymentPayerRequest;
import com.mercadopago.exceptions.MPApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
@Slf4j
public class MercadoPagoPixGatewayAdapter implements PaymentGatewayPort {

    @Value("${app.mercadopago.access-token}")
    private String accessToken;

    @Value("${app.mercadopago.payer-email}")
    private String payerEmail;

    @Override
    public PaymentGatewayResult process(Payment payment) {

        try {

            MercadoPagoConfig.setAccessToken(accessToken);

            PaymentCreateRequest request = PaymentCreateRequest.builder()
                    .transactionAmount(payment.getAmount())
                    .description(payment.getDescription() != null
                            ? payment.getDescription()
                            : "IronVault Payment")
                    .paymentMethodId("pix")
                    .payer(PaymentPayerRequest.builder()
                            .email(payerEmail)
                            .build())
                    .build();

            var response = callMercadoPago(request);

            String externalId = String.valueOf(response.getId());
            String qrCode = response.getPointOfInteraction()
                    .getTransactionData().getQrCode();
            String qrCodeBase64 = response.getPointOfInteraction()
                    .getTransactionData().getQrCodeBase64();

            log.info("PIX payment created. externalId={} status={}",
                    externalId, response.getStatus());

            return new PaymentGatewayResult(
                    externalId,
                    PaymentStatus.PROCESSING,
                    response.getStatus(),
                    response.getStatusDetail(),
                    null,
                    null,
                    qrCode,
                    qrCodeBase64
            );

        } catch (MPApiException apiEx) {
            log.error("MercadoPago API error. Status: {} Response: {}",
                    apiEx.getStatusCode(),
                    apiEx.getApiResponse().getContent());
            throw new RuntimeException("MercadoPago PIX processing failed: " + apiEx.getApiResponse().getContent(), apiEx);

        } catch (Exception ex) {
            log.error("MercadoPago PIX error: {}", ex.getMessage());
            throw new RuntimeException("MercadoPago PIX processing failed: " + ex.getMessage(), ex);
        }

    }

    private com.mercadopago.resources.payment.Payment callMercadoPago(PaymentCreateRequest request)
            throws Exception {
        return new PaymentClient().create(request);
    }
}
