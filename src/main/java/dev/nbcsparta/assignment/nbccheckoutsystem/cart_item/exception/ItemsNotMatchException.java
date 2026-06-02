package dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.exception;

public class ItemsNotMatchException extends RuntimeException {
    public ItemsNotMatchException() {
        super("Number of Items we found and you want to purchase is not match. Perhaps the member is not unauthorised client?");
    }
}
