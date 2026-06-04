package dev.nbcsparta.assignment.nbccheckoutsystem.order.dto;

import dev.nbcsparta.assignment.nbccheckoutsystem.order.entity.Order;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.enums.OrderStatus;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.domain.Payment;

public record CreateOrderData(
        Long orderId,
        String portOnePaymentId,
        int totalAmount,
        int usedPoint,
        OrderStatus orderStatus
) {

    public static CreateOrderData from(Payment payment) {
        Order order = payment.getOrder();
        return new CreateOrderData(
                order.getId(),
                payment.getPortOnePaymentId(),
                payment.getPaidAmount(),
                order.getUsedPoint(),
                order.getOrderStatus()
        );
    }
}
