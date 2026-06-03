package dev.nbcsparta.assignment.nbccheckoutsystem.order.dto;

public record OrderResponse(
        boolean success,
        CreateOrderData data
) {

    public static OrderResponse from(CreateOrderData createOrderData) {
        return new OrderResponse(createOrderData != null, createOrderData);
    }
}
