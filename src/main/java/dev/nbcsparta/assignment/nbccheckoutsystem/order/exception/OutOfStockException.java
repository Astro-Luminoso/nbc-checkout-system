package dev.nbcsparta.assignment.nbccheckoutsystem.order.exception;

import org.springframework.http.HttpStatus;

import dev.nbcsparta.assignment.nbccheckoutsystem.global.error.BusinessException;

public class OutOfStockException extends BusinessException {

    public OutOfStockException() {
        super(HttpStatus.CONFLICT, "Product Is Out of Stock");
    }
}
