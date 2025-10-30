package fr.inria.corese.command.exceptions;

/**
 * Exception thrown when an HTTP error occurs while communicating with a SPARQL endpoint.
 * 
 * <p>This exception includes the HTTP status code to allow for specific error handling
 * based on the type of HTTP error encountered.
 */
public class HttpClientException extends CoreseCommandException {

    private final int statusCode;

    /**
     * Constructs a new HttpClientException with the specified detail message and HTTP status code.
     *
     * @param message the detail message
     * @param statusCode the HTTP status code
     */
    public HttpClientException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    /**
     * Constructs a new HttpClientException with the specified detail message, HTTP status code, and cause.
     *
     * @param message the detail message
     * @param statusCode the HTTP status code
     * @param cause the cause of this exception
     */
    public HttpClientException(String message, int statusCode, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    /**
     * Returns the HTTP status code associated with this exception.
     *
     * @return the HTTP status code
     */
    public int getStatusCode() {
        return statusCode;
    }
}
