package exceptions;

public class InvalidSubtaskException extends RuntimeException {
    public InvalidSubtaskException(String message) {
        super(message);
    }
}
