package dev.nbcsparta.assignment.nbccheckoutsystem.auth.exception;

import org.springframework.http.HttpStatus;

import dev.nbcsparta.assignment.nbccheckoutsystem.global.error.BusinessException;

public class LoginFailedException extends BusinessException {

    public LoginFailedException() {
        super(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다.");
    }
}
