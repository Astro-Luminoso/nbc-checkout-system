package dev.nbcsparta.assignment.nbccheckoutsystem.payment.exception;

import dev.nbcsparta.assignment.nbccheckoutsystem.global.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class InvalidPaymentStatusException extends BusinessException {

    public InvalidPaymentStatusException() {
        super(HttpStatus.CONFLICT, "결제 완료 상태에서만 환불할 수 있습니다.");
    }
}
