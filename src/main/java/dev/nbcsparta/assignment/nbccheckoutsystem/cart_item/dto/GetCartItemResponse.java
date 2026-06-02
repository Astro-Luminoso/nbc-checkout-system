package dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.dto;

import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.entity.CartItem;

public record GetCartItemResponse(
    Long cartItemId,
    Long productId,
    String productName,
    Integer price,
    Integer quantity,
    Integer lineAmount
) {
    public static GetCartItemResponse from(CartItem cartItem) {
        return new GetCartItemResponse(
            cartItem.getId(),
            cartItem.getProductId().getId(),
            cartItem.getProductId().getName(),
            cartItem.getProductId().getPrice(),
            cartItem.getQuantity(),
            cartItem.getProductId().getPrice() * cartItem.getQuantity()
        );
    }
}
