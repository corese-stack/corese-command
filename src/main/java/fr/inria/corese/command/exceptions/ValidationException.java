package fr.inria.corese.command.exceptions;

/**
 * Exception thrown when an error occurs during SHACL validation.
 * 
 * <p>This includes errors such as:
 * <ul>
 *   <li>Invalid SHACL shapes</li>
 *   <li>Validation engine errors</li>
 *   <li>Missing or malformed shapes graph</li>
 * </ul>
 */
public class ValidationException extends CoreseCommandException {

    /**
     * Constructs a new ValidationException with the specified detail message.
     *
     * @param message the detail message
     */
    public ValidationException(String message) {
        super(message);
    }

    /**
     * Constructs a new ValidationException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause of this exception
     */
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
