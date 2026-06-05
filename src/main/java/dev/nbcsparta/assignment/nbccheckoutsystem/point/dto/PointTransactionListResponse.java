package dev.nbcsparta.assignment.nbccheckoutsystem.point.dto;

import java.util.List;

public record PointTransactionListResponse(
        List<PointTransactionResponse> transactions
) {
    public static PointTransactionListResponse from(List<PointTransactionResponse> transactions) {
        return new PointTransactionListResponse(transactions);
    }
}
