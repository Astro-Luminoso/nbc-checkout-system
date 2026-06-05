package dev.nbcsparta.assignment.nbccheckoutsystem.order.dto;

import dev.nbcsparta.assignment.nbccheckoutsystem.order.entity.Order;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.enums.OrderStatus;

import java.time.LocalDateTime;

public record SimpleOrderDetail(Long orderId,
                                OrderStatus status,
                                int totalAmount,
                                LocalDateTime orderedAt) {

    public static SimpleOrderDetail from (Order order) {
        return new SimpleOrderDetail(
                order.getId(),
                order.getOrderStatus(),
                order.getTotalAmount(),
                order.getCreatedDate()
        );
    }



}
