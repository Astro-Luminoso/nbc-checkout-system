package dev.nbcsparta.assignment.nbccheckoutsystem.order.dto;

import dev.nbcsparta.assignment.nbccheckoutsystem.global.entity.Item;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.entity.OrderItem;

public record ItemDetail(long id, String name, int price, int quantities) {

    public static ItemDetail from(Item item) {
        int price = (item instanceof OrderItem orderItem) ? orderItem.getPrice() : item.getProduct().getPrice();

        return new ItemDetail(
                item.getId(),
                item.getProduct().getName(),
                price,
                item.getQuantities()
        );
    }
}
