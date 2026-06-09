package dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.dto;

import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.entity.CartItem;

import java.util.List;

public record GetCartResponse(
    List<GetCartItemResponse> items,
    Integer totalAmount
) {
    public static GetCartResponse from(List<CartItem> cartItems) {
        List<GetCartItemResponse> items = cartItems.stream()
                .map(GetCartItemResponse::from)
                .toList();

        int totalAmount = items.stream()
                .mapToInt(GetCartItemResponse::lineAmount)
                .sum();

        return new GetCartResponse(items, totalAmount);
    }
}
