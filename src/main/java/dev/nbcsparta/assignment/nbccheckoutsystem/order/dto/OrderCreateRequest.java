package dev.nbcsparta.assignment.nbccheckoutsystem.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OrderCreateRequest(
        @NotNull
        List<Long> cartItemIds,

        @Min(0)
        Integer usePoint
) {
}
