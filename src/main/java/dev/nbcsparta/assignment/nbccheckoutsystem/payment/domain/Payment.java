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

    @Version
    private Long version;

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

    public void confirmSuccess(LocalDateTime paidAt) {
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException("Payment must be in PENDING state to confirm success, but is " + this.status);
        }
        this.status = PaymentStatus.COMPLETED;
        this.paidAt = paidAt;
    }

    public void confirmFailed() {
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException("Payment must be in PENDING state to fail, but is " + this.status);
        }
        this.status = PaymentStatus.FAILED;
    }

    public void refund() {
        if (this.status != PaymentStatus.COMPLETED) {
            throw new IllegalStateException("Payment must be in COMPLETED state to be refunded, but is " + this.status);
        }
        this.status = PaymentStatus.REFUNDED;
    }

    public void setPaidAmount(int pgAmount) {
        this.paidAmount = pgAmount;
    }

    public void cancelPayment() {
        if (this.status == PaymentStatus.PENDING) {
            this.status = PaymentStatus.FAILED;
        } else if (this.status == PaymentStatus.COMPLETED) {
            this.status = PaymentStatus.REFUNDED;
        }
    }
}
