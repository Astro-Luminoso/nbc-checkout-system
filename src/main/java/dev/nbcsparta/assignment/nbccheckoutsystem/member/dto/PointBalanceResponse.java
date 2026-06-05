package dev.nbcsparta.assignment.nbccheckoutsystem.member.dto;

import dev.nbcsparta.assignment.nbccheckoutsystem.member.domain.Members;

public record PointBalanceResponse(
        int pointBalance
) {
    public static PointBalanceResponse from(Members member) {
        int balance = member.getPointBalance();
        return new PointBalanceResponse(balance);
    }
}

