package dev.nbcsparta.assignment.nbccheckoutsystem.infrastructure.portone.dto;

public record PortOnePaymentResponse(
        String id,
        String status,
        PortOneAmount amount
) {
    public record PortOneAmount(int total) {}
}

