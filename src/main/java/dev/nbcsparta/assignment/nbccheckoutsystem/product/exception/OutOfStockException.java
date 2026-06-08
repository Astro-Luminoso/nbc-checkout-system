package dev.nbcsparta.assignment.nbccheckoutsystem.product.exception;

import org.springframework.http.HttpStatus;

import dev.nbcsparta.assignment.nbccheckoutsystem.global.exception.BusinessException;

public class OutOfStockException extends BusinessException {

	public OutOfStockException() {
		super(HttpStatus.CONFLICT, "재고가 부족합니다.");
	}
}
