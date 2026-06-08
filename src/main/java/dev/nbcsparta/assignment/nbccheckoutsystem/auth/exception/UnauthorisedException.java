package dev.nbcsparta.assignment.nbccheckoutsystem.auth.exception;

import org.springframework.http.HttpStatus;

import dev.nbcsparta.assignment.nbccheckoutsystem.global.exception.BusinessException;

public class UnauthorisedException extends BusinessException {
    public UnauthorisedException() {
        super(HttpStatus.FORBIDDEN,"본인의 주문만 접근할 수 있습니다.");
    }
}
