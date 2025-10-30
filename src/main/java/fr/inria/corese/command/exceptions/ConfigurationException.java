package fr.inria.corese.command.exceptions;

/**
 * Exception thrown when an error occurs while loading or processing configuration.
 * 
 * <p>This includes errors such as:
 * <ul>
 *   <li>Configuration file not found</li>
 *   <li>Invalid configuration format</li>
 *   <li>Configuration parsing errors</li>
 * </ul>
 */
public class ConfigurationException extends CoreseCommandException {

    /**
     * Constructs a new ConfigurationException with the specified detail message.
     *
     * @param message the detail message
     */
    public ConfigurationException(String message) {
        super(message);
    }

    /**
     * Constructs a new ConfigurationException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause of this exception
     */
    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
