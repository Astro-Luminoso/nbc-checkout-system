package dev.nbcsparta.assignment.nbccheckoutsystem.auth.exception;

public class UnauthorisedException extends RuntimeException {
    public UnauthorisedException() {
        super("Client Not Match");
    }
}
