package dev.nbcsparta.assignment.nbccheckoutsystem.payment.exception;

import dev.nbcsparta.assignment.nbccheckoutsystem.global.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class UnauthorizeAccessException extends BusinessException {
    public UnauthorizeAccessException() {
        super(HttpStatus.UNAUTHORIZED, "잘못된 접근입니다!");
    }
}
