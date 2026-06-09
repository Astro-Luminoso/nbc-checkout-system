package dev.nbcsparta.assignment.nbccheckoutsystem.payment.exception;

import dev.nbcsparta.assignment.nbccheckoutsystem.global.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class RefundFailedException extends BusinessException {

    public RefundFailedException() {
        super(HttpStatus.BAD_GATEWAY, "내부 주문은 취소되었으나 일시적인 오류로 결제 취소 요청이 실패했습니다. 고객센터에 문의해주세요.");
    }
}
