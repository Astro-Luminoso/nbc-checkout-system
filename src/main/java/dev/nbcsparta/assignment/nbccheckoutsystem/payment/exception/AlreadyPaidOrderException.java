package dev.nbcsparta.assignment.nbccheckoutsystem.payment.exception;

public class AlreadyPaidOrderException extends RuntimeException{
    public AlreadyPaidOrderException() {
        super("이미 결제한 주문입니다");
    }
}
