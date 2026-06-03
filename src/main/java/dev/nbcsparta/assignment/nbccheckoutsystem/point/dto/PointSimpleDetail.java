package dev.nbcsparta.assignment.nbccheckoutsystem.point.dto;

import dev.nbcsparta.assignment.nbccheckoutsystem.point.domain.PointTransaction;

public record PointSimpleDetail(
        int used,
        int earned
) {
    public static PointSimpleDetail from(PointTransaction pointTransaction) {
        return null;
    }
}
