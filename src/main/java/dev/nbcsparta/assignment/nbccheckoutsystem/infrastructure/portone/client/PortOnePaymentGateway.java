package dev.nbcsparta.assignment.nbccheckoutsystem.infrastructure.portone.client;

import dev.nbcsparta.assignment.nbccheckoutsystem.infrastructure.portone.dto.PortOneCancelRequest;
import dev.nbcsparta.assignment.nbccheckoutsystem.infrastructure.portone.dto.PortOnePaymentResponse;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.port.PaymentGateway;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.port.PaymentGatewayResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class PortOnePaymentGateway implements PaymentGateway {

    private final RestClient portOneRestClient;
    private final String storeId;

    public PortOnePaymentGateway(
            RestClient portOneRestClient,
            @Value("${portone.store-id}") String storeId) {
        this.portOneRestClient = portOneRestClient;
        this.storeId = storeId;
    }

    @Override
    public PaymentGatewayResponse getPayment(String paymentId) {
        PortOnePaymentResponse response = portOneRestClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path("/payments/{paymentId}");
                    if (storeId != null && !storeId.isBlank()) {
                        uriBuilder.queryParam("storeId", storeId);
                    }
                    return uriBuilder.build(paymentId);
                })
                .retrieve()
                .body(PortOnePaymentResponse.class);

        if (response == null) {
            throw new IllegalStateException("Failed to fetch payment info from PortOne: " + paymentId);
        }

        return new PaymentGatewayResponse(
                response.id(),
                response.status(),
                response.amount().total()
        );
    }

    @Override
    public void cancelPayment(String paymentId, String reason) {
        PortOneCancelRequest request = new PortOneCancelRequest(reason, storeId);

        portOneRestClient.post()
                .uri("/payments/{paymentId}/cancel", paymentId)
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }
}

