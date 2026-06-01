package dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.dto;

import lombok.Getter;

@Getter

public class Cart_ItemResponse {

    private final Long cartItemId;
    private final Long productId;
    private final Integer quantity;


    public Cart_ItemResponse(Long cartItemId, Long productId, Integer quantity) {
        this.cartItemId = cartItemId;
        this.productId = productId;
        this.quantity = quantity;
    }
}
