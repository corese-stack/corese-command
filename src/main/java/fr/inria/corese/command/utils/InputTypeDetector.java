package fr.inria.corese.command.utils;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utility class to detect the type of an input string.
 * <p>
 * The input can represent:
 * - A SPARQL query (based on common keywords)
 * - A valid URL (http, https, ftp, file)
 * - A file path (relative or absolute)
 * <p>
 * Detection order prioritizes explicit types (URL, file) over heuristic guesses
 * (SPARQL).
 */
public class InputTypeDetector {

    /**
     * The type of input detected.
     */
    public enum InputType {
        SPARQL,
        URL,
        FILE_PATH,
        UNKNOWN
    }

    /**
     * Detect the type of the given input string.
     *
     * @param input The input string to classify.
     * @return The detected input type (SPARQL, URL, FILE_PATH or UNKNOWN).
     */
    public static InputType detect(String input) {
        if (input == null || input.trim().isEmpty()) {
            return InputType.UNKNOWN;
        }

        String trimmed = input.trim();

        // Detect SPARQL first if it's clearly not a URL or file path
        if (isClearlySparql(trimmed)) {
            return InputType.SPARQL;
        }

        if (isValidURL(trimmed)) {
            return InputType.URL;
        }

        if (isValidPath(trimmed)) {
            return InputType.FILE_PATH;
        }

        return InputType.UNKNOWN;
    }

    /**
     * Heuristic to detect whether the input is likely a SPARQL query.
     * Excludes obvious URLs and file names with RDF extensions.
     *
     * @param input The original input string.
     * @return True if the input looks like a SPARQL query.
     */
    private static boolean isClearlySparql(String input) {
        String lowered = input.toLowerCase();

        // Must contain SPARQL content
        boolean hasKeywords = containsSparqlKeywords(lowered);
        boolean hasStructure = input.contains("{") && input.contains("}") && input.contains("?");

        if (!(hasKeywords || hasStructure)) {
            return false;
        }

        // Exclude if clearly a URL
        if (input.matches("^(https?|ftp|file)://.*")) {
            return false;
        }

        // Exclude if known file extension
        if (input.matches(".*\\.(ttl|rdf|nt|n3|jsonld|trig|nq|rq|sparql|txt)$")) {
            return false;
        }

        /*
         * Treat it as a real path only when it is
         * 1) single-line **and**
         * 2) contains no whitespace (typical for file names)
         */
        boolean isSingleLine = !input.contains("\n");
        boolean hasWhitespace = input.matches(".*\\s+.*");
        boolean looksLikePath = input.matches(".*[\\\\/].*") // slash or back-slash
                || input.matches(".*\\.[\\w]{1,4}$"); // ends with .ext

        if (isSingleLine && !hasWhitespace && looksLikePath) {
            return false; // definitely a path ⇒ not SPARQL
        }

        return true;
    }

    /**
     * Heuristic check for common SPARQL query keywords.
     *
     * @param input Lowercased input string.
     * @return True if SPARQL-like keywords are found.
     */
    private static boolean containsSparqlKeywords(String input) {
        return input.contains("select") || input.contains("construct")
                || input.contains("ask") || input.contains("describe")
                || input.contains("prefix") || input.contains("base");
    }

    /**
     * Check if the input string is a valid URL.
     *
     * @param input Input string.
     * @return True if the input is a valid URL with a recognized scheme.
     */
    private static boolean isValidURL(String input) {
        if (!input.matches("^(https?|ftp|file)://.*")) {
            return false;
        }

        try {
            URI uri = URI.create(input);
            uri.toURL(); // will fail if illegal characters
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if the input string is a valid file path.
     *
     * @param input Input string.
     * @return True if the input is a syntactically valid file path.
     */
    private static boolean isValidPath(String input) {
        try {
            Path path = Paths.get(input);
            return Files.exists(path) || path.getNameCount() > 0;
        } catch (Exception e) {
            return false;
        }
    }
}
