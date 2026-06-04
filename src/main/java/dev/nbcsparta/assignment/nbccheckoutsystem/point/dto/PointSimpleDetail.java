package dev.nbcsparta.assignment.nbccheckoutsystem.point.dto;

import dev.nbcsparta.assignment.nbccheckoutsystem.point.domain.PointTransaction;

import java.util.List;

public record PointSimpleDetail(
        int used,
        int earned
) {

    public static PointSimpleDetail from(List<PointTransaction> pointTransaction) {
        int used = 0;
        int earned = 0;
        for (PointTransaction transaction : pointTransaction) {
            switch (transaction.getType()) {
                case USE -> used += transaction.getAmount();
                case EARN -> earned += transaction.getAmount();
                case USE_CANCEL -> {
                    used -= transaction.getAmount();
                    earned += transaction.getAmount();
                }
                case EARN_CANCEL -> earned -= transaction.getAmount();
            }
        }

        return new PointSimpleDetail(used, earned);
    }
}
