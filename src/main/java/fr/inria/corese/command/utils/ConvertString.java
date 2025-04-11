package fr.inria.corese.command.utils;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

/**
 * Utility class to convert strings to other types like URL and Path.
 */
public class ConvertString {

    /**
     * Convert a string to a URL, throwing an exception if invalid.
     *
     * @param input String to convert.
     * @return The converted URL.
     * @throws IllegalArgumentException If the input is not a valid URL.
     */
    public static URL toUrlOrThrow(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("URL input is blank");
        }

        try {
            return URI.create(input).toURL();
        } catch (IllegalArgumentException | MalformedURLException e) {
            throw new IllegalArgumentException("Invalid URL: " + input, e);
        }
    }

    /**
     * Convert a string to a Path, throwing an exception if invalid.
     *
     * @param input String to convert.
     * @return The converted Path.
     * @throws IllegalArgumentException If the input is not a valid Path.
     */
    public static Path toPathOrThrow(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("Path input is blank");
        }

        try {
            return Path.of(input);
        } catch (InvalidPathException e) {
            throw new IllegalArgumentException("Invalid path: " + input, e);
        }
    }
}
