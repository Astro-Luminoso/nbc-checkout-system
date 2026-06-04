package dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.exception;

import org.springframework.http.HttpStatus;

import dev.nbcsparta.assignment.nbccheckoutsystem.global.exception.BusinessException;

public class ItemsNotMatchException extends BusinessException {

    public ItemsNotMatchException() {
        super(HttpStatus.FORBIDDEN, "Number of Items we found and you want to purchase is not match. Perhaps the member is not unauthorised client?");
    }
}
