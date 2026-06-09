package dev.nbcsparta.assignment.nbccheckoutsystem.payment.exception;

public class InvalidPaymentIdentifierException extends RuntimeException {
    public InvalidPaymentIdentifierException() {
        super("주문과 결제 ID가 일치하지 않습니다");
    }
}
