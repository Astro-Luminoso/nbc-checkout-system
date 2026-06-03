package dev.nbcsparta.assignment.nbccheckoutsystem.payment.port;

public record PaymentGatewayResponse(
        String id,
        String status,
        int totalAmount
) {}
