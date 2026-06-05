package dev.nbcsparta.assignment.nbccheckoutsystem.order.dto;

import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.entity.CartItem;

import java.util.List;

public record OrderPreviewDetail(
        List<ItemDetail> items,
        int totalAmount
) {
    public static OrderPreviewDetail from(List<CartItem> items) {
        return new OrderPreviewDetail(
                items.stream().map(ItemDetail::from).toList(),
                items.stream().mapToInt(item -> item.getProduct().getPrice() * item.getQuantities()).sum()
        );
    }
}
