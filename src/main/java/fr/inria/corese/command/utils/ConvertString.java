package fr.inria.corese.command.utils;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Utility class to convert strings to other types.
 */
public class ConvertString {

    /**
     * Convert a string to a URL.
     *
     * @param input String to convert.
     * @return The URL if the conversion was successful, an empty Optional
     *         otherwise.
     */
    public static Optional<URL> toUrl(String input) {
        try {
            return Optional.of(URI.create(input).toURL());
        } catch (IllegalArgumentException | MalformedURLException e) {
            return Optional.empty();
        }
    }

    /**
     * Convert a string to a Path.
     *
     * @param input String to convert.
     * @return The Path if the conversion was successful, an empty Optional
     *         otherwise.
     */
    public static Optional<Path> toPath(String input) {
        try {
            return Optional.of(Path.of(input));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
