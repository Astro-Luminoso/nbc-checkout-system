package dev.nbcsparta.assignment.nbccheckoutsystem.order.dto;

import dev.nbcsparta.assignment.nbccheckoutsystem.order.entity.OrderItem;

public record OrderItemDetail(long id, String name, int price, int quantities) {

    public static OrderItemDetail from(OrderItem item) {
        return new OrderItemDetail(
                item.getId(),
                item.getName(),
                item.getPrice(),
                item.getQuantities()
        );
    }
}
