package dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.dto;

import java.util.List;

public record GetCartResponse(
    List<GetCartItemResponse> items,
    Integer totalAmount
) {}
