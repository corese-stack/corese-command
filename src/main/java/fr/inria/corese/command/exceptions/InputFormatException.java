package fr.inria.corese.command.exceptions;

/**
 * Exception thrown when an invalid input format is encountered.
 * 
 * <p>This includes errors such as:
 * <ul>
 *   <li>Unknown or unsupported serialization format</li>
 *   <li>Format detection failures</li>
 *   <li>Format mismatch with actual data</li>
 * </ul>
 */
public class InputFormatException extends CoreseCommandException {

    /**
     * Constructs a new InputFormatException with the specified detail message.
     *
     * @param message the detail message
     */
    public InputFormatException(String message) {
        super(message);
    }

    /**
     * Constructs a new InputFormatException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause of this exception
     */
    public InputFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
