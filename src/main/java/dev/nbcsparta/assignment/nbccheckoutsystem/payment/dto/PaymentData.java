package dev.nbcsparta.assignment.nbccheckoutsystem.payment.dto;

import dev.nbcsparta.assignment.nbccheckoutsystem.order.entity.Order;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.enums.OrderStatus;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.domain.Payment;

public record PaymentData(
        Long orderId,
        String portOnePaymentId,
        int totalAmount,
        int usedPoint,
        OrderStatus orderStatus
) {

    public static PaymentData from(Payment payment) {
        Order order = payment.getOrder();
        return new PaymentData(
                order.getId(),
                payment.getPortOnePaymentId(),
                payment.getTotalAmount(),
                order.getUsedPoint(),
                payment.getOrder().getOrderStatus()
        );
    }
}
