package dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateCartItemRequest (
        @NotNull(message = "변경할 수량은 필수입니다.")
        @Min(value = 1, message = "변경 수량은 1이상 이어야합니다.")
        Integer quantity){
}
