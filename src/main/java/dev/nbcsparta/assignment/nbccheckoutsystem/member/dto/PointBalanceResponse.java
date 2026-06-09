package dev.nbcsparta.assignment.nbccheckoutsystem.member.dto;

import dev.nbcsparta.assignment.nbccheckoutsystem.member.domain.Member;

public record PointBalanceResponse(
        int pointBalance
) {
    public static PointBalanceResponse from(Member member) {
        int balance = member.getPointBalance();
        return new PointBalanceResponse(balance);
    }
}

