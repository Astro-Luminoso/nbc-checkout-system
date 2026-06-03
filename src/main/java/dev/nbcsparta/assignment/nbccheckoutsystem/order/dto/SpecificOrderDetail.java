package dev.nbcsparta.assignment.nbccheckoutsystem.order.dto;

import dev.nbcsparta.assignment.nbccheckoutsystem.order.entity.OrderItem;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.enums.OrderStatus;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.dto.PaymentSimpleDetail;
import dev.nbcsparta.assignment.nbccheckoutsystem.point.dto.PointSimpleDetail;

import java.util.List;

public record SpecificOrderDetail(
        Long orderId,
        OrderStatus orderStatus,
        List<OrderItem> items,
        PaymentSimpleDetail payment,
        PointSimpleDetail point,
        int totalAmount
) {
}
