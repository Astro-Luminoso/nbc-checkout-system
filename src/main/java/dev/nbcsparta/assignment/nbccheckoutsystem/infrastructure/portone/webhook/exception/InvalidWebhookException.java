package dev.nbcsparta.assignment.nbccheckoutsystem.infrastructure.portone.webhook.exception;

import dev.nbcsparta.assignment.nbccheckoutsystem.global.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class InvalidWebhookException extends BusinessException {

    public InvalidWebhookException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
