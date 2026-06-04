package dev.nbcsparta.assignment.nbccheckoutsystem.product.exception;

import org.springframework.http.HttpStatus;

import dev.nbcsparta.assignment.nbccheckoutsystem.global.error.BusinessException;

public class ProductNotFoundException extends BusinessException {

    public ProductNotFoundException() {
        super(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다.");
    }
}
