package dev.nbcsparta.assignment.nbccheckoutsystem.order.exception;

public class NoCartItemException extends RuntimeException {

    public NoCartItemException() {
        super("No cart items found for the provided IDs.");
    }
}
