package dev.nbcsparta.assignment.nbccheckoutsystem.auth.exception;

import org.springframework.http.HttpStatus;

import dev.nbcsparta.assignment.nbccheckoutsystem.global.exception.BusinessException;

public class DuplicateEmailException extends BusinessException {

    public DuplicateEmailException() {
        super(HttpStatus.CONFLICT, "이미 가입된 이메일입니다.");
    }
}
