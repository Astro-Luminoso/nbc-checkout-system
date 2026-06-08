package dev.nbcsparta.assignment.nbccheckoutsystem.point.domain;

import dev.nbcsparta.assignment.nbccheckoutsystem.member.domain.Member;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.entity.Order;
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
    private Member members;

    @Column(nullable = false)
    private int amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PointTransactionType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(nullable = false, updatable = false)
    @CreatedDate
    private LocalDateTime createdDate;

    private PointTransaction(Member members, int amount, PointTransactionType type, Order order) {
        this.members = members;
        this.amount = amount;
        this.type = type;
        this.order = order;
    }

    public static PointTransaction createUse(Member members, int amount, Order order) {
        return new PointTransaction(members, amount, PointTransactionType.USE, order);
    }

    public static PointTransaction createEarn(Member members, int amount, Order order) {
        return new PointTransaction(members, amount, PointTransactionType.EARN, order);
    }

    public static PointTransaction createCancel(Member members, int amount, PointTransactionType type, Order order) {
        return new PointTransaction(members, amount, type, order);
    }
}
