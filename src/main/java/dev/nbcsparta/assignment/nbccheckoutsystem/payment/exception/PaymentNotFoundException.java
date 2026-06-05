package dev.nbcsparta.assignment.nbccheckoutsystem.payment.exception;

import org.springframework.http.HttpStatus;

import dev.nbcsparta.assignment.nbccheckoutsystem.global.exception.BusinessException;

public class PaymentNotFoundException extends BusinessException {
    public PaymentNotFoundException() {
        super(HttpStatus.NOT_FOUND,"결제 정보가 없습니다!");
    }
}
