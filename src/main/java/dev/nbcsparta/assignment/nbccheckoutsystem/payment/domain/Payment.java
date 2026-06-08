package dev.nbcsparta.assignment.nbccheckoutsystem.payment.domain;

import dev.nbcsparta.assignment.nbccheckoutsystem.order.entity.Order;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
    private int paidAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    private LocalDateTime paidAt;

    public Payment(Order order) {
        this.order = order;
        this.portOnePaymentId = UUID.randomUUID().toString();
        this.paidAmount = order.getTotalAmount() - order.getUsedPoint();
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

    public void refund() {
        this.status = PaymentStatus.REFUNDED;
    }

    public void setPaidAmount(int pgAmount) {
        this.paidAmount = pgAmount;
    }

    public void cancelPayment() {
        this.status = PaymentStatus.FAILED;
    }
}
