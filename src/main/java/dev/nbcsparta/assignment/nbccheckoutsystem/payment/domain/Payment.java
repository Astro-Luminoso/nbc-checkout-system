package dev.nbcsparta.assignment.nbccheckoutsystem.payment.domain;

import dev.nbcsparta.assignment.nbccheckoutsystem.order.entity.Order;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @Column(nullable = false, unique = true)
    private String portOnePaymentId;

    @Column(nullable = false)
    private int totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    private LocalDateTime paidAt;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedDate;

    public Payment(Order order) {
        this.order = order;
        this.portOnePaymentId = UUID.randomUUID().toString();
        this.totalAmount = Math.toIntExact(order.getTotalAmount() - order.getUsedPoint());
        this.status = PaymentStatus.PENDING;
    }


    public void confirmSuccess(String portOnePaymentId, LocalDateTime paidAt) {
        this.portOnePaymentId = portOnePaymentId;
        this.status = PaymentStatus.COMPLETED;
        this.paidAt = paidAt;
    }

    public void confirmFailed() {
        this.status = PaymentStatus.FAILED;
    }
}
