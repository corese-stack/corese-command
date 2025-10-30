package fr.inria.corese.command.exceptions;

/**
 * Exception thrown when an error occurs during SPARQL query execution.
 * 
 * <p>This includes errors such as:
 * <ul>
 *   <li>Invalid SPARQL syntax</li>
 *   <li>Query execution failures</li>
 *   <li>Engine errors during query processing</li>
 * </ul>
 */
public class SparqlExecutionException extends CoreseCommandException {

    /**
     * Constructs a new SparqlExecutionException with the specified detail message.
     *
     * @param message the detail message
     */
    public SparqlExecutionException(String message) {
        super(message);
    }

    /**
     * Constructs a new SparqlExecutionException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause of this exception
     */
    public SparqlExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
