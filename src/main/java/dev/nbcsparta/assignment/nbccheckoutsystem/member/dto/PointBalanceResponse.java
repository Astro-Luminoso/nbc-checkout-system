package dev.nbcsparta.assignment.nbccheckoutsystem.member.dto;

import dev.nbcsparta.assignment.nbccheckoutsystem.member.domain.Members;

public record PointBalanceResponse(
        Long pointBalance
) {
    public static PointBalanceResponse from(Members member) {
        Long balance = member.getPointBalance() != null ? member.getPointBalance() : 0L;
        return new PointBalanceResponse(balance);
    }
}

