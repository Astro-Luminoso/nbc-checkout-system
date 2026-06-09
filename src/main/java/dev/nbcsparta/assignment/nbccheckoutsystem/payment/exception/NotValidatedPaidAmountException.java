package dev.nbcsparta.assignment.nbccheckoutsystem.payment.exception;

import dev.nbcsparta.assignment.nbccheckoutsystem.global.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class NotValidatedPaidAmountException extends BusinessException {
    public NotValidatedPaidAmountException() {
        super(HttpStatus.BAD_REQUEST, "금액 검증에 실패하였습니다!");
    }
}
