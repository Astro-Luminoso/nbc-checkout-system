package dev.nbcsparta.assignment.nbccheckoutsystem.payment.exception;

public class UnexpectedPaymentFailException extends RuntimeException{
    public UnexpectedPaymentFailException() {
        super("예상치 못한 결제 중단");
    }
}
