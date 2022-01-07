package cc.ricecx.ricestats.core.event;

public class InvalidNmsOperationsState extends RuntimeException {
    public InvalidNmsOperationsState(final String message) {
        super(message);
    }
    public InvalidNmsOperationsState(final String message, Throwable cause) {
        super(message, cause);
    }
}