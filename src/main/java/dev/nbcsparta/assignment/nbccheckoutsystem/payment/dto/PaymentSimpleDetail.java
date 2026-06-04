package dev.nbcsparta.assignment.nbccheckoutsystem.payment.dto;

import dev.nbcsparta.assignment.nbccheckoutsystem.payment.domain.Payment;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.domain.PaymentStatus;

public record PaymentSimpleDetail(
        PaymentStatus paymentStatus,
        int amount
) {
        public static PaymentSimpleDetail from(Payment payment) {
            return new PaymentSimpleDetail(payment.getStatus(), payment.getPaidAmount());
        }
}
