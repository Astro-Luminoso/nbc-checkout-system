package dev.nbcsparta.assignment.nbccheckoutsystem.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.List;

public record OrderCreateRequest(
        @NotNull
        List<Long> cartItemIds,

        @Min(0)
        @NonNull
        Integer usePoint
) {
}
