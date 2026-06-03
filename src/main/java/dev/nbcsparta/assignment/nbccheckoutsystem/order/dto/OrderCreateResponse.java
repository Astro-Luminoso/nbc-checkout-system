package dev.nbcsparta.assignment.nbccheckoutsystem.order.dto;

import dev.nbcsparta.assignment.nbccheckoutsystem.payment.dto.PaymentData;

public record OrderCreateResponse(
        boolean success,
        PaymentData data
) {

    public static OrderCreateResponse from(PaymentData paymentData) {
        return new OrderCreateResponse(paymentData != null, paymentData);
    }
}
