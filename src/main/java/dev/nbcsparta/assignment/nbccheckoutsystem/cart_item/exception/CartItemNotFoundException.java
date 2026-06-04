package dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.exception;

import org.springframework.http.HttpStatus;

import dev.nbcsparta.assignment.nbccheckoutsystem.global.exception.BusinessException;

public class CartItemNotFoundException extends BusinessException {

    public CartItemNotFoundException() {
        super(HttpStatus.NOT_FOUND, "존재하지 않는 장바구니 항목입니다.");
    }
}
