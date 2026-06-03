package dev.nbcsparta.assignment.nbccheckoutsystem.payment.dto;

import dev.nbcsparta.assignment.nbccheckoutsystem.payment.domain.PaymentStatus;

public record PaymentSimpleDetail(
        PaymentStatus paymentStatus,
        int amount
) {
        public static PaymentSimpleDetail from(PaymentStatus paymentStatus, int amount) {
            return new PaymentSimpleDetail(paymentStatus, amount);
        }
}
