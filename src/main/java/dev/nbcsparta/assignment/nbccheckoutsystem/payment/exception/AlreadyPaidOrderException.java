package dev.nbcsparta.assignment.nbccheckoutsystem.payment.exception;

import dev.nbcsparta.assignment.nbccheckoutsystem.global.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class AlreadyPaidOrderException extends BusinessException {
    public AlreadyPaidOrderException() {
        super(HttpStatus.CONFLICT, "이미 결제한 주문입니다");
    }
}
