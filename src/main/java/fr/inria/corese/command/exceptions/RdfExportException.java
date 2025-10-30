package fr.inria.corese.command.exceptions;

/**
 * Exception thrown when an error occurs during RDF export operations.
 * 
 * <p>This includes errors such as:
 * <ul>
 *   <li>File write errors</li>
 *   <li>Unsupported export format</li>
 *   <li>Serialization errors</li>
 * </ul>
 */
public class RdfExportException extends CoreseCommandException {

    /**
     * Constructs a new RdfExportException with the specified detail message.
     *
     * @param message the detail message
     */
    public RdfExportException(String message) {
        super(message);
    }

    /**
     * Constructs a new RdfExportException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause of this exception
     */
    public RdfExportException(String message, Throwable cause) {
        super(message, cause);
    }
}
