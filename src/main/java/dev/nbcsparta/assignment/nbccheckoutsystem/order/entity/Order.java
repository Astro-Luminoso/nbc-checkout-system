package dev.nbcsparta.assignment.nbccheckoutsystem.order.entity;

import dev.nbcsparta.assignment.nbccheckoutsystem.order.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "orders")
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Order {

    @Id
    private Long id;

    private String portOnePaymentId;

    @Column(nullable = false)
    private Long totalAmount;

    @Column(nullable = false)
    private Integer usedPoint;

    private Long pgAmount;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @CreatedDate
    private LocalDateTime createdDate;

    @LastModifiedDate
    private LocalDateTime updatedDate;

    // --------------


    public Order(Long totalAmount, Integer usedPoint, String portOnePaymentId) {
        this.totalAmount = totalAmount;
        this.usedPoint = usedPoint;
        this.portOnePaymentId = portOnePaymentId;
        this.orderStatus = OrderStatus.STANDBY;
    }

    public OrderStatus paymentResult(Long amount) {
        this.pgAmount = amount;
        this.orderStatus = ((this.pgAmount - this.usedPoint) == this.totalAmount) ? OrderStatus.PAID : OrderStatus.DECLINED;

        return this.orderStatus;
    }

}