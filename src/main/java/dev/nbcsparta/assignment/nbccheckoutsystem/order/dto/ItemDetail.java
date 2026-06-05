package dev.nbcsparta.assignment.nbccheckoutsystem.order.dto;

import dev.nbcsparta.assignment.nbccheckoutsystem.global.entity.Item;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.entity.OrderItem;

public record ItemDetail(long id, String name, int price, int quantities) {

    public static ItemDetail from(Item item) {
        int price;
        String name;
        long id;
        if (item instanceof OrderItem orderItem) {
            id = orderItem.getId();
            price = orderItem.getPrice();
            name = orderItem.getName();
        } else {
            id = item.getProduct().getId();
            price = item.getProduct().getPrice();
            name = item.getProduct().getName();
        }

        return new ItemDetail(
                id,
                name,
                price,
                item.getQuantities()
        );
    }
}
