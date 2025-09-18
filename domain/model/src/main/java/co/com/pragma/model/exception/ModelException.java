package co.com.pragma.model.exception;

/**
 * Base exception for model-related errors
 */
public class ModelException extends RuntimeException {
    public ModelException(String message) {
        super(message);
    }

    public ModelException(String message, Throwable cause) {
        super(message, cause);
    }
}
