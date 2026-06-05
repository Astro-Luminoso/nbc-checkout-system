package dev.nbcsparta.assignment.nbccheckoutsystem.infrastructure.portone.webhook.controller;

import dev.nbcsparta.assignment.nbccheckoutsystem.infrastructure.portone.webhook.config.PortOneWebhookVerifier;
import dev.nbcsparta.assignment.nbccheckoutsystem.infrastructure.portone.webhook.config.WebhookHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
public class WebhookController {

    private final PortOneWebhookVerifier webhookVerifier;
    private final WebhookHandler webhookHandler;

    @PostMapping("/portone")
    public ResponseEntity<String> receiveWebhook(
            @RequestHeader("webhook-id") String webhookId,
            @RequestHeader("webhook-signature") String webhookSignature,
            @RequestHeader("webhook-timestamp") String webhookTimestamp,
            @RequestBody String rawBody) {

        log.info("Received PortOne webhook request. webhook-id={}", webhookId);

        // 1. 서명 검증
        boolean verified = webhookVerifier.verify(webhookId, webhookSignature, webhookTimestamp, rawBody);
        if (!verified) {
            log.warn("PortOne Webhook Signature verification failed for id: {}", webhookId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Signature verification failed");
        }

        // 2. 웹훅 이벤트 처리
        try {
            webhookHandler.handle(webhookId, rawBody);
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            log.error("Failed to process webhook event: {}", webhookId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Processing failed: " + e.getMessage());
        }
    }
}
