package dev.nbcsparta.assignment.nbccheckoutsystem.infrastructure.portone.client;

import dev.nbcsparta.assignment.nbccheckoutsystem.infrastructure.portone.dto.PortOneCancelRequest;
import dev.nbcsparta.assignment.nbccheckoutsystem.infrastructure.portone.dto.PortOnePaymentResponse;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.exception.RefundFailedException;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.exception.UnexpectedPaymentFailException;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.port.PaymentGateway;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.port.PaymentGatewayResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
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
        PortOnePaymentResponse response;
        try {
            response = portOneRestClient.get()
                    .uri(uriBuilder -> {
                        uriBuilder.path("/payments/{paymentId}");
                        if (storeId != null && !storeId.isBlank()) {
                            uriBuilder.queryParam("storeId", storeId);
                        }
                        return uriBuilder.build(paymentId);
                    })
                    .retrieve()
                    .body(PortOnePaymentResponse.class);
        } catch (Exception e) {
            log.error("Payment confirmation unknown error for portOnePaymentId {}: {}", paymentId, e.getMessage(), e);
            throw new UnexpectedPaymentFailException();
        }

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

        try {
            portOneRestClient.post()
                    .uri("/payments/{paymentId}/cancel", paymentId)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.error("CRITICAL: DB refund succeeded but PG cancellation failed. Manual refund required! PaymentId: {}, Error: {}", paymentId, e.getMessage(), e);
            throw new RefundFailedException();
        }
    }
}

