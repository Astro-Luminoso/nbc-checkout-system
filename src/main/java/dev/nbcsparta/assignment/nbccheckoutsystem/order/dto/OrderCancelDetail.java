package dev.nbcsparta.assignment.nbccheckoutsystem.order.dto;

import dev.nbcsparta.assignment.nbccheckoutsystem.order.entity.Order;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.enums.OrderStatus;

public record OrderCancelDetail(
        long id,
        OrderStatus status
) {
    public static OrderCancelDetail from (Order order) {
        return new OrderCancelDetail(
                order.getId(),
                order.getOrderStatus()
        );
    }
}
