package dev.nbcsparta.assignment.nbccheckoutsystem.payment.exception;

import dev.nbcsparta.assignment.nbccheckoutsystem.global.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class UnexpectedPaymentFailException extends BusinessException {
    public UnexpectedPaymentFailException() {
        super(HttpStatus.CONFLICT, "예상치 못한 결제 중단");
    }
}
