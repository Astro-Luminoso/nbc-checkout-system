package dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.exception;

public class PointExceedTotalCostException extends RuntimeException {
    public PointExceedTotalCostException() {
        super("Point Cannot exceed total cost");
    }
}
