package dev.nbcsparta.assignment.nbccheckoutsystem.order.entity;

import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.entity.CartItem;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.exception.ItemsNotMatchException;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.exception.PointExceedTotalCostException;
import dev.nbcsparta.assignment.nbccheckoutsystem.global.entity.BaseEntity;
import dev.nbcsparta.assignment.nbccheckoutsystem.member.domain.Member;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.dto.OrderCreateRequest;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.enums.OrderStatus;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.exception.NoCartItemException;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.domain.Payment;
import dev.nbcsparta.assignment.nbccheckoutsystem.product.entity.Product;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

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

    public Order(Member member, OrderCreateRequest request, List<CartItem> cartItems) {
        this.orderStatus = OrderStatus.STANDBY;
        this.member = member;
        this.orderItems = this.toOrderItem(cartItems, request.cartItemIds().size());
        this.totalAmount = this.orderItems.stream()
                .mapToInt(item -> item.getPrice() * item.getQuantities()).sum();
        this.usedPoint = this.inspectRequestedPoint(request.usePoint());
    }

    private List<OrderItem> toOrderItem(List<CartItem> cartItems, int requestSize) {
        if (cartItems.isEmpty()) {
            throw new NoCartItemException();
        }
        if (cartItems.size() != requestSize) {
            throw new ItemsNotMatchException();
        }

        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem item : cartItems) {
            Product product = item.getProduct();
            product.isSellable(item.getQuantities());
            orderItems.add(new OrderItem(this, product, item.getQuantities()));
        }

        return orderItems;
    }

    private int inspectRequestedPoint(int requested) {
        if (this.totalAmount < requested || this.member.getPointBalance() < requested) {
            throw new PointExceedTotalCostException();
        }

        return requested;
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

    public boolean isEqualStatus(OrderStatus status) {
        return this.orderStatus == status;
    }
}