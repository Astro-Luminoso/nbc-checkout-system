package dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.dto;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CartItemRequest(
        @NotNull(message = "상품 ID는 필수 항목입니다.")
        Long productId,

        @NotNull(message = "수량은 필수 항목입니다.")
        @Min(value = 1, message = "장바구니에 최소 1개 이상 담아야 합니다.")
        Integer quantity
){

}
