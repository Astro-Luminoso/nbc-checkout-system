package dev.nbcsparta.assignment.nbccheckoutsystem.payment.exception;

import dev.nbcsparta.assignment.nbccheckoutsystem.global.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class ForbiddenPaymentException extends BusinessException {

    public ForbiddenPaymentException() {
        super(HttpStatus.FORBIDDEN, "본인의 결제만 환불할 수 있습니다.");
    }
}
