package dev.nbcsparta.assignment.nbccheckoutsystem.auth.dto;

public record SignupResponse(
        Long memberId,
        String email,
        String name,
        String phoneNumber
) {
}
