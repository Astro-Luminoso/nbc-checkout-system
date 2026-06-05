package dev.nbcsparta.assignment.nbccheckoutsystem.infrastructure.portone.webhook.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "webhook_events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class WebhookEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String webhookId;

    private String eventType;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WebhookStatus status;

    private String errorMessage;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;

    public WebhookEvent(String webhookId, String eventType, String payload) {
        this.webhookId = webhookId;
        this.eventType = eventType;
        this.payload = payload;
        this.status = WebhookStatus.RECEIVED;
    }

    public void markProcessed() {
        this.status = WebhookStatus.PROCESSED;
    }

    public void markFailed(String errorMessage) {
        this.status = WebhookStatus.FAILED;
        this.errorMessage = errorMessage;
    }

    public void markIgnored(String reason) {
        this.status = WebhookStatus.IGNORED;
        this.errorMessage = reason;
    }
}
