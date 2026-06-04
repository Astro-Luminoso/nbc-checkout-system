package dev.nbcsparta.assignment.nbccheckoutsystem.payment.dto;

import dev.nbcsparta.assignment.nbccheckoutsystem.payment.domain.PaymentStatus;

public record PaymentConfirmResponse(
        Long orderId,
        String portOnePaymentId,
        PaymentStatus status
) {}
