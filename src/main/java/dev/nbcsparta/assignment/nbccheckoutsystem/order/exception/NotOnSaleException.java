package dev.nbcsparta.assignment.nbccheckoutsystem.order.exception;

import org.springframework.http.HttpStatus;

import dev.nbcsparta.assignment.nbccheckoutsystem.global.error.BusinessException;

public class NotOnSaleException extends BusinessException {

    public NotOnSaleException() {
        super(HttpStatus.CONFLICT, "Product Is Not On Sale");
    }
}
