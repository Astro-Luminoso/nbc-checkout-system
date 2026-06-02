package dev.nbcsparta.assignment.nbccheckoutsystem.payment.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "refunds")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Refund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false, unique = true)
    private Payment payment;

    @Column(nullable = false)
    private Long pgRefundAmount;

    @Column(nullable = false)
    private Integer pointRefundAmount;

    @Column(nullable = false)
    private String reason;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;

    public Refund(Payment payment, Long pgRefundAmount, Integer pointRefundAmount, String reason) {
        this.payment = payment;
        this.pgRefundAmount = pgRefundAmount;
        this.pointRefundAmount = pointRefundAmount;
        this.reason = reason;
    }
}
