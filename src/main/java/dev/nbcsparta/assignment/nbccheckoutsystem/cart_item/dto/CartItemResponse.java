package dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.dto;


import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.entity.CartItem;

public record CartItemResponse(
            Long cartItemId,
            Long productId,
            Integer quantity
) {
    public static CartItemResponse from(CartItem cartItem){
        return new CartItemResponse(
                cartItem.getId(),
                cartItem.getProduct().getId(),
                cartItem.getQuantity()
        );
    }
}


