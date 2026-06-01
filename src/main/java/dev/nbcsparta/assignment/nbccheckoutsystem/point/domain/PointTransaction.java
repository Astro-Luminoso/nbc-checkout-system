package dev.nbcsparta.assignment.nbccheckoutsystem.point.domain;

import dev.nbcsparta.assignment.nbccheckoutsystem.member.domain.Members;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.domain.Payment;
import dev.nbcsparta.assignment.nbccheckoutsystem.refund.domain.Refund;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "point_transactions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class PointTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "members_id", nullable = false)
    private Members members;

    @Column(nullable = false)
    private int amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PointTransactionType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private Payment payment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "refund_id")
    private Refund refund;

    @Column(nullable = false, updatable = false)
    @CreatedDate
    private LocalDateTime createdDate;

    private PointTransaction(Members members, int amount, PointTransactionType type, Payment payment, Refund refund) {
        this.members = members;
        this.amount = amount;
        this.type = type;
        this.payment = payment;
        this.refund = refund;
    }

    public static PointTransaction createUse(Members members, int amount, Payment payment) {
        return new PointTransaction(members, amount, PointTransactionType.USE, payment, null);
    }

    public static PointTransaction createEarn(Members members, int amount, Payment payment) {
        return new PointTransaction(members, amount, PointTransactionType.EARN, payment, null);
    }

    public static PointTransaction createCancel(Members members, int amount, PointTransactionType type, Refund refund) {
        return new PointTransaction(members, amount, type, null, refund);
    }
}
