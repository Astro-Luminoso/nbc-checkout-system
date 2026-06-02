package dev.nbcsparta.assignment.nbccheckoutsystem.point.dto;

import dev.nbcsparta.assignment.nbccheckoutsystem.point.domain.PointTransaction;
import dev.nbcsparta.assignment.nbccheckoutsystem.point.domain.PointTransactionType;
import java.time.LocalDateTime;

public record PointTransactionResponse(
        PointTransactionType type,
        int amount,
        LocalDateTime createdDate
) {
    public static PointTransactionResponse from(PointTransaction transaction) {
        return new PointTransactionResponse(
                transaction.getType(),
                transaction.getAmount(),
                transaction.getCreatedDate()
        );
    }
}
