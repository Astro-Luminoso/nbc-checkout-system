package dev.nbcsparta.assignment.nbccheckoutsystem.order.exception;

import org.springframework.http.HttpStatus;

import dev.nbcsparta.assignment.nbccheckoutsystem.global.error.BusinessException;

public class NoCartItemException extends BusinessException {

    public NoCartItemException() {
        super(HttpStatus.NOT_FOUND, "No cart items found for the provided IDs.");
    }
}
