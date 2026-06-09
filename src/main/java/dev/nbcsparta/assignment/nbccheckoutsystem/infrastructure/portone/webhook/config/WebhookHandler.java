package dev.nbcsparta.assignment.nbccheckoutsystem.infrastructure.portone.webhook.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.nbcsparta.assignment.nbccheckoutsystem.infrastructure.portone.webhook.entity.WebhookEvent;
import dev.nbcsparta.assignment.nbccheckoutsystem.infrastructure.portone.webhook.exception.InvalidWebhookException;
import dev.nbcsparta.assignment.nbccheckoutsystem.infrastructure.portone.webhook.service.WebhookEventService;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.domain.Payment;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.exception.PaymentNotFoundException;
import dev.nbcsparta.assignment.nbccheckoutsystem.facade.PaymentFacade;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebhookHandler {

    private final ObjectMapper objectMapper;
    private final WebhookEventService webhookEventService;
    private final PaymentRepository paymentRepository;
    private final PaymentFacade paymentFacade;

    public void handle(String webhookId, String rawBody) {
        log.info("Handling webhook event: webhookId={}", webhookId);

        // 1. 중복 수신 검증 (Idempotent Webhook Event Log)
        if (webhookEventService.isDuplicate(webhookId)) {
            log.info("Duplicate webhook event received. Skipping processing. webhookId={}", webhookId);
            return;
        }

        WebhookPayload payload;
        try {
            payload = objectMapper.readValue(rawBody, WebhookPayload.class);
        } catch (Exception e) {
            log.error("Failed to parse webhook body: {}", rawBody, e);
            throw new InvalidWebhookException("Invalid webhook payload format");
        }

        if (payload == null || payload.type() == null) {
            log.error("Invalid webhook payload: {}", rawBody);
            throw new InvalidWebhookException("Webhook event type is missing");
        }

        // 2. 수신 이벤트 기록 저장
        WebhookEvent event = webhookEventService.saveEvent(webhookId, payload.type(), rawBody);

        // 3. 처리 대상이 아닌 이벤트인 경우 (예: Transaction.Ready 등) 처리 건너뛰기
        if (!"Transaction.Paid".equals(payload.type())) {
            log.info("Non-target webhook event type received ({}). Skipping processing. webhookId={}", payload.type(), webhookId);
            webhookEventService.markIgnored(event, "Non-target event type: " + payload.type());
            return;
        }

        if (payload.data() == null || payload.data().paymentId() == null) {
            log.error("Webhook payload data or paymentId is missing. webhookId={}", webhookId);
            webhookEventService.markFailed(event, "paymentId is missing in webhook data");
            throw new InvalidWebhookException("paymentId is missing in webhook data");
        }

        String portOnePaymentId = payload.data().paymentId();

        // 4. 결제 비즈니스 로직 수행
        try {
            // DB에서 해당 portOnePaymentId로 Payment와 Order를 조회
            Payment payment = paymentRepository.findByPortOnePaymentIdWithOrder(portOnePaymentId)
                    .orElseThrow(PaymentNotFoundException::new);

            // 결제 상태 검증 및 상태 갱신 (소유권 검증은 생략하므로 checkOwnership = false)
            paymentFacade.processConfirmation(payment, portOnePaymentId, null, false);

            webhookEventService.markProcessed(event);
            log.info("Webhook event successfully processed. webhookId={}, portOnePaymentId={}", webhookId, portOnePaymentId);
        } catch (Exception e) {
            log.error("Error processing payment validation for webhookId={}, portOnePaymentId={}", webhookId, portOnePaymentId, e);
            webhookEventService.markFailed(event, e.getMessage());
            throw e;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record WebhookPayload(String type, WebhookData data) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record WebhookData(String paymentId) {}
}
