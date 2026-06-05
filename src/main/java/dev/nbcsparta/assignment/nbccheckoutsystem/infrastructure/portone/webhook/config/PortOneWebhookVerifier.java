package dev.nbcsparta.assignment.nbccheckoutsystem.infrastructure.portone.webhook.config;

import io.portone.sdk.server.errors.WebhookVerificationException;
import io.portone.sdk.server.webhook.WebhookVerifier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PortOneWebhookVerifier {

    private final WebhookVerifier verifier;

    public PortOneWebhookVerifier(@Value("${portone.webhook-secret:dummy-webhook-secret}") String webhookSecret) {
        this.verifier = new WebhookVerifier(webhookSecret);
    }

    public boolean verify(String webhookId, String webhookSignature, String webhookTimestamp, String rawBody) {
        try {
            verifier.verify(rawBody, webhookId, webhookSignature, webhookTimestamp);
            return true;
        } catch (WebhookVerificationException e) {
            log.error("PortOne Webhook Verification Failed: webhookId={}, error={}", webhookId, e.getMessage());
            return false;
        }
    }
}
