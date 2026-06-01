package dev.nbcsparta.assignment.nbccheckoutsystem.auth.dto;

import dev.nbcsparta.assignment.nbccheckoutsystem.member.domain.Members;

public record SignupResponse(
        Long memberId,
        String email,
        String name,
        String phoneNumber
) {

    public static SignupResponse from(Members member) {
        return new SignupResponse(
                member.getId(),
                member.getEmail(),
                member.getName(),
                member.getPhoneNumber()
        );
    }
}
