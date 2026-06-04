package dev.nbcsparta.assignment.nbccheckoutsystem.order.exception;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException() {
        super("Order Not Found");
    }
}
