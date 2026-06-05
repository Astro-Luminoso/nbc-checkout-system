package dev.nbcsparta.assignment.nbccheckoutsystem.infrastructure.portone.webhook.service;

import dev.nbcsparta.assignment.nbccheckoutsystem.infrastructure.portone.webhook.entity.WebhookEvent;
import dev.nbcsparta.assignment.nbccheckoutsystem.infrastructure.portone.webhook.repository.WebhookEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WebhookEventService {

    private final WebhookEventRepository webhookEventRepository;

    @Transactional
    public WebhookEvent saveEvent(String webhookId, String eventType, String payload) {
        WebhookEvent event = new WebhookEvent(webhookId, eventType, payload);
        return webhookEventRepository.save(event);
    }

    @Transactional(readOnly = true)
    public boolean isDuplicate(String webhookId) {
        return webhookEventRepository.existsByWebhookId(webhookId);
    }

    @Transactional
    public void markProcessed(WebhookEvent event) {
        event.markProcessed();
        webhookEventRepository.save(event);
    }

    @Transactional
    public void markFailed(WebhookEvent event, String errorMessage) {
        event.markFailed(errorMessage);
        webhookEventRepository.save(event);
    }

    @Transactional
    public void markIgnored(WebhookEvent event, String reason) {
        event.markIgnored(reason);
        webhookEventRepository.save(event);
    }
}
