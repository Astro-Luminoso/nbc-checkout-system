package dev.nbcsparta.assignment.nbccheckoutsystem.payment.exception;

import dev.nbcsparta.assignment.nbccheckoutsystem.global.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class InvalidPaymentIdentifierException extends BusinessException {
    public InvalidPaymentIdentifierException() {
        super(HttpStatus.BAD_REQUEST, "결제 식별 정보(portOnePaymentID)가 일치하지 않습니다");
    }
}
