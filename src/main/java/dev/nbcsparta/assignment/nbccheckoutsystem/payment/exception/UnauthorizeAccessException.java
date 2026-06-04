package dev.nbcsparta.assignment.nbccheckoutsystem.payment.exception;

public class UnauthorizeAccessException extends RuntimeException{
    public UnauthorizeAccessException() {
        super("잘못된 접근입니다!");
    }
}
