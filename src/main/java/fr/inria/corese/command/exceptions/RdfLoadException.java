package fr.inria.corese.command.exceptions;

/**
 * Exception thrown when an error occurs while loading RDF data.
 * 
 * <p>This includes errors such as:
 * <ul>
 *   <li>File not found</li>
 *   <li>Invalid RDF syntax</li>
 *   <li>Unsupported RDF format</li>
 *   <li>I/O errors during loading</li>
 * </ul>
 */
public class RdfLoadException extends CoreseCommandException {

    /**
     * Constructs a new RdfLoadException with the specified detail message.
     *
     * @param message the detail message
     */
    public RdfLoadException(String message) {
        super(message);
    }

    /**
     * Constructs a new RdfLoadException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause of this exception
     */
    public RdfLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
