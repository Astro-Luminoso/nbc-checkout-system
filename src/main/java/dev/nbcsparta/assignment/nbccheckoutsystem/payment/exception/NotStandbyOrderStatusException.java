package dev.nbcsparta.assignment.nbccheckoutsystem.payment.exception;

import dev.nbcsparta.assignment.nbccheckoutsystem.global.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class NotStandbyOrderStatusException extends BusinessException {
    public NotStandbyOrderStatusException() {
        super(HttpStatus.BAD_REQUEST, "결제 대기 상태인 상품만 결제 가능합니다!");
    }
}
