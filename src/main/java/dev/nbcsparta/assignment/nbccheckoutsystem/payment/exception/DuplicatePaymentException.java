package dev.nbcsparta.assignment.nbccheckoutsystem.payment.exception;

public class DuplicatePaymentException extends RuntimeException{
    public DuplicatePaymentException () {
        super("이미 결제한 주문입니다");
    }
}
