package dev.nbcsparta.assignment.nbccheckoutsystem.order.exception;

import dev.nbcsparta.assignment.nbccheckoutsystem.global.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class CancellationNotAllowedException extends BusinessException {
    public CancellationNotAllowedException() {
        super(HttpStatus.BAD_REQUEST, "Cancellation is not allowed");
    }
}
