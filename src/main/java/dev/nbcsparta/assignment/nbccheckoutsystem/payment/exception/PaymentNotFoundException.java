package dev.nbcsparta.assignment.nbccheckoutsystem.payment.exception;

public class PaymentNotFoundException extends RuntimeException{
    public PaymentNotFoundException() {
        super("결제 정보가 없습니다!");
    }
}
