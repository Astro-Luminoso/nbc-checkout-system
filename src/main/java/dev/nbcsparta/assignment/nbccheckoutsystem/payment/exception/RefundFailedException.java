package dev.nbcsparta.assignment.nbccheckoutsystem.payment.exception;

import dev.nbcsparta.assignment.nbccheckoutsystem.global.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class RefundFailedException extends BusinessException {

    public RefundFailedException() {
        super(HttpStatus.BAD_GATEWAY, "환불 요청 처리에 실패했습니다.");
    }
}
