package dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.exception;

import org.springframework.http.HttpStatus;

import dev.nbcsparta.assignment.nbccheckoutsystem.global.error.BusinessException;

public class OutOfStockException extends BusinessException {

    public OutOfStockException() {
        super(HttpStatus.CONFLICT, "재고가 부족합니다.");
    }
}
