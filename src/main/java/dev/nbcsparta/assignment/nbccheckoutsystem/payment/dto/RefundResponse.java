package dev.nbcsparta.assignment.nbccheckoutsystem.payment.dto;

import dev.nbcsparta.assignment.nbccheckoutsystem.order.enums.OrderStatus;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.domain.Payment;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.domain.PaymentStatus;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.domain.Refund;

public record RefundResponse(
        Long refundId,
        Long paymentId,
        Long pgRefundAmount,
        Integer pointRefundAmount,
        String reason,
        PaymentStatus paymentStatus,
        OrderStatus orderStatus
) {
    public static RefundResponse from(Refund refund) {
        Payment payment = refund.getPayment();
        return new RefundResponse(
                refund.getId(),
                payment.getId(),
                refund.getPgRefundAmount(),
                refund.getPointRefundAmount(),
                refund.getReason(),
                payment.getStatus(),
                payment.getOrder().getOrderStatus()
        );
    }
}
