package dev.nbcsparta.assignment.nbccheckoutsystem.order.exception;

public class NotOnSaleException extends RuntimeException {
    public NotOnSaleException() {
        super("Product Is Not On Sale");
    }
}
