package dev.nbcsparta.assignment.nbccheckoutsystem.infrastructure.portone.webhook.repository;

import dev.nbcsparta.assignment.nbccheckoutsystem.infrastructure.portone.webhook.entity.WebhookEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WebhookEventRepository extends JpaRepository<WebhookEvent, Long> {
    boolean existsByWebhookId(String webhookId);
}
