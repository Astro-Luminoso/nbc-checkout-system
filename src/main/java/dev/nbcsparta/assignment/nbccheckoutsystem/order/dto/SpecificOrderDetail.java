package dev.nbcsparta.assignment.nbccheckoutsystem.order.dto;

import dev.nbcsparta.assignment.nbccheckoutsystem.order.entity.Order;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.entity.OrderItem;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.enums.OrderStatus;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.domain.Payment;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.domain.PaymentStatus;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.dto.PaymentSimpleDetail;
import dev.nbcsparta.assignment.nbccheckoutsystem.point.domain.PointTransaction;
import dev.nbcsparta.assignment.nbccheckoutsystem.point.dto.PointSimpleDetail;

import java.util.List;

public record SpecificOrderDetail(
        Long orderId,
        OrderStatus orderStatus,
        List<OrderItemDetail> items,
        PaymentSimpleDetail payment,
        PointSimpleDetail point,
        int totalAmount
) {
    public static SpecificOrderDetail from (Order order, List<PointTransaction> pointDetail) {
        return new SpecificOrderDetail(
                order.getId(),
                order.getOrderStatus(),
                order.getOrderItems().stream().map(OrderItemDetail::from).toList(),
                PaymentSimpleDetail.from(order.getPayment()),
                PointSimpleDetail.from(pointDetail),
                order.getTotalAmount()
        );
    }
}
