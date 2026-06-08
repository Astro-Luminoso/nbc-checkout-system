package dev.nbcsparta.assignment.nbccheckoutsystem.order.entity;

import dev.nbcsparta.assignment.nbccheckoutsystem.global.entity.BaseEntity;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.enums.OrderStatus;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.domain.Payment;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "orders")
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private int totalAmount;

    @Column(nullable = false)
    private int usedPoint;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    // --------------

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems;

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Payment payment;

    // ========================

    public Order(int totalAmount, int usedPoint, Long memberId, List<OrderItem> orderItems) {
        this.usedPoint = usedPoint;
        this.memberId = memberId;
        this.totalAmount = totalAmount;
        this.orderItems = new ArrayList<>();
        this.orderStatus = OrderStatus.STANDBY;
        orderItems.forEach(this::addOrderItem);
    }

    public void addOrderItem(OrderItem orderItem) {
        this.orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    public void paymentResult(Long paidAmount) {
        if (this.orderStatus != OrderStatus.STANDBY) {
            return;
        }
        if ((this.totalAmount - this.usedPoint) == paidAmount ) {
            this.orderStatus = OrderStatus.PAID;
        } else {
            this.orderStatus = OrderStatus.DECLINED;
        }
    }

    public void cancelOrder() {
        this.orderItems.forEach(item -> item.getProduct().restoreStockValue(item.getQuantities()));
        this.payment.cancelPayment();
        this.orderStatus = OrderStatus.CANCELLED;
    }

    // 환불 시 주문 상태를 CANCELLED로 변경
    public void cancel() {
        this.orderStatus = OrderStatus.CANCELLED;
    }
}