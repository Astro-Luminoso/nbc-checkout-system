package dev.nbcsparta.assignment.nbccheckoutsystem.order.dto;

public record OrderResponse <T>(
        boolean success,
        T data
) {

}
