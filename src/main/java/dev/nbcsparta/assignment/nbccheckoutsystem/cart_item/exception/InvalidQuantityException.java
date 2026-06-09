package dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.exception;

import org.springframework.http.HttpStatus;

import dev.nbcsparta.assignment.nbccheckoutsystem.global.exception.BusinessException;

public class InvalidQuantityException extends BusinessException {

    public InvalidQuantityException() {
        super(HttpStatus.BAD_REQUEST, "수량은 1 이상이어야 합니다.");
    }
}
