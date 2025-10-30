package fr.inria.corese.command.exceptions;

/**
 * Base exception for all Corese Command errors.
 * 
 * <p>This is an unchecked exception (extends RuntimeException) to simplify
 * error handling in the CLI application while still providing structured
 * error information.
 */
public class CoreseCommandException extends RuntimeException {

    /**
     * Constructs a new CoreseCommandException with the specified detail message.
     *
     * @param message the detail message
     */
    public CoreseCommandException(String message) {
        super(message);
    }

    /**
     * Constructs a new CoreseCommandException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause of this exception
     */
    public CoreseCommandException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new CoreseCommandException with the specified cause.
     *
     * @param cause the cause of this exception
     */
    public CoreseCommandException(Throwable cause) {
        super(cause);
    }
}
