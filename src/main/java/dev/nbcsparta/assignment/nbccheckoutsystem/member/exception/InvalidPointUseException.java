package dev.nbcsparta.assignment.nbccheckoutsystem.member.exception;

public class InvalidPointUseException extends RuntimeException{
    public InvalidPointUseException() {
        super("포인트를 사용 또는 적립할 수 없습니다.");
    }
}
