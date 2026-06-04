package dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.exception;

import org.springframework.http.HttpStatus;

import dev.nbcsparta.assignment.nbccheckoutsystem.global.error.BusinessException;

public class PointExceedTotalCostException extends BusinessException {

    public PointExceedTotalCostException() {
        super(HttpStatus.BAD_REQUEST, "Point Cannot exceed total cost");
    }
}
