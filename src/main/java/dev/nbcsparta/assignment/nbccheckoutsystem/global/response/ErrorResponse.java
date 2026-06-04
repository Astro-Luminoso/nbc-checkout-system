package dev.nbcsparta.assignment.nbccheckoutsystem.global.response;

public record ErrorResponse(
	boolean success,
	String message
) {
	public static ErrorResponse fail(String message) {
		return new ErrorResponse(false, message);
	}
}
