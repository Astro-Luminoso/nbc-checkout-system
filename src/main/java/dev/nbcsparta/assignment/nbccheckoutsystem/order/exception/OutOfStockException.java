package dev.nbcsparta.assignment.nbccheckoutsystem.order.exception;

public class OutOfStockException extends RuntimeException {
    public OutOfStockException() {
        super("Product Is Out of Stock");

    }
}
