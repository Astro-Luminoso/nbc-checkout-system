package dev.nbcsparta.assignment.nbccheckoutsystem.payment.exception;

import dev.nbcsparta.assignment.nbccheckoutsystem.global.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class DuplicateRefundException extends BusinessException {

    public DuplicateRefundException() {
        super(HttpStatus.CONFLICT, "이미 환불된 결제입니다.");
    }
}
