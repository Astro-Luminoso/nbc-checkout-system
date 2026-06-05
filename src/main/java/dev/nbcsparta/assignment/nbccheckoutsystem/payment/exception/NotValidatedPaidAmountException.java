package dev.nbcsparta.assignment.nbccheckoutsystem.payment.exception;

public class NotValidatedPaidAmountException extends RuntimeException{
    public NotValidatedPaidAmountException() {
        super("금액 검증에 실패하였습니다!");
    }
}
