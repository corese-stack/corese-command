package fr.inria.corese.command.utils.http;

import java.io.PrintWriter;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;
import picocli.CommandLine.Model.CommandSpec;

/**
 * Utility class to print details of HTTP requests and responses.
 * This class is used for debugging and logging purposes.
 */
public class SparqlHttpPrinter {
    private final PrintWriter err;

    /**
     * Constructor to initialize the printer with a PrintWriter.
     * This is typically used to print to the standard error output.
     * 
     * @param spec The command specification containing the PrintWriter.
     */
    public SparqlHttpPrinter(CommandSpec spec) {
        this.err = spec.commandLine().getErr();
    }

    /**
     * Prints the details of an HTTP request.
     * This includes the URL, HTTP method, query parameters, headers, and body
     * content.
     * 
     * @param webTarget   The target of the HTTP request.
     * @param bodyContent The body content of the request.
     * @param contentType The content type of the request.
     * @param headers     The headers of the request.
     * @param method      The HTTP method used for the request (GET, POST, etc.).
     */
    public void printRequest(WebTarget webTarget, String bodyContent, String contentType,
            List<Pair<String, String>> headers, EnumRequestMethod method) {
        err.println("╔════════════════════════════════╗");
        err.println("║        REQUEST DETAILS         ║");
        err.println("╚════════════════════════════════╝\n");

        // Print the URL of the request
        if (webTarget != null && webTarget.getUri() != null) {
            err.println("► URL");
            err.println("  " + webTarget.getUri());
        }

        // Print the HTTP method of the request
        if (method != null) {
            err.println("\n► METHOD");
            err.println("  " + method.name());
        }

        // Print query parameters if present in the URL
        URI uri = webTarget.getUri();
        if (uri != null && uri.getQuery() != null) {
            err.println("\n► QUERY PARAMETERS");
            Map<String, String> decodedParams = parseQueryParams(uri.getQuery());
            for (Map.Entry<String, String> entry : decodedParams.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                String[] lines = value.split("\n");

                if (lines.length == 1) {
                    err.println("  " + key + "=" + value);
                } else {
                    err.println("  " + key + "=");
                    int lastNonEmpty = lines.length - 1;
                    while (lastNonEmpty >= 0 && lines[lastNonEmpty].isBlank()) {
                        lastNonEmpty--;
                    }
                    for (int i = 0; i <= lastNonEmpty; i++) {
                        err.println("    " + lines[i]);
                    }
                }
            }
        }

        // Print headers and content type if available
        if ((headers != null && !headers.isEmpty()) || (contentType != null && !contentType.isEmpty())) {
            err.println("\n► HEADERS");

            // Group headers by key
            Map<String, StringBuilder> normalized = new LinkedHashMap<>();
            for (Pair<String, String> header : headers) {
                String key = normalizeHeaderKey(header.getKey());
                normalized.computeIfAbsent(key, k -> new StringBuilder())
                        .append(header.getValue()).append(",");
            }

            // Add content type to headers
            if (contentType != null && !contentType.isEmpty()) {
                String key = "Content-Type";
                normalized.computeIfAbsent(key, k -> new StringBuilder())
                        .append(contentType).append(",");
            }

            // Print headers
            for (Map.Entry<String, StringBuilder> entry : normalized.entrySet()) {
                String values = entry.getValue().toString().replaceAll(",$", "");
                err.println("  " + entry.getKey() + ": " + values);
            }
        }

        // Print the body content of the request if available
        if (bodyContent != null && !bodyContent.isEmpty()) {
            err.println("\n► REQUEST BODY");
            for (String line : bodyContent.split("\n")) {
                err.println("  " + line);
            }
        }

        err.println("\n──────────────────────────────────");
    }

    /**
     * Prints the details of an HTTP response.
     * This includes the HTTP status code, status information, headers, and any
     * other relevant details.
     * 
     * @param response The HTTP response received from the server.
     */
    public void printResponse(Response response) {
        err.println("╔════════════════════════════════╗");
        err.println("║         RESPONSE DETAILS       ║");
        err.println("╚════════════════════════════════╝\n");

        // Handle case where response is null
        if (response == null) {
            err.println("No response available.\n");
            err.println("──────────────────────────────────");
            return;
        }

        // Print HTTP status code
        err.println("► HTTP CODE");
        err.println("  " + response.getStatus());

        // Print status information if available
        if (response.getStatusInfo() != null) {
            err.println("\n► STATUS INFO");
            err.println("  " + response.getStatusInfo().toString());
        }

        // Print response headers
        Map<String, List<Object>> headers = response.getHeaders();
        if (!headers.isEmpty()) {
            err.println("\n► HEADERS");
            for (Map.Entry<String, List<Object>> entry : headers.entrySet()) {
                String key = normalizeHeaderKey(entry.getKey());
                String value = String.join(", ",
                        entry.getValue().stream().map(Object::toString).collect(Collectors.toList()));
                err.println("  " + key + ": " + value);
            }
        }

        err.println("\n──────────────────────────────────");
    }

    /**
     * Normalizes the header key by converting it to a standard format.
     * This method capitalizes the first letter of each part of the header key
     * and removes any trailing hyphen.
     * 
     * @param key The header key to normalize.
     * @return The normalized header key.
     */
    private String normalizeHeaderKey(String key) {
        if (key == null || key.isEmpty())
            return "";
        String lower = key.toLowerCase();
        String[] parts = lower.split("-");
        StringBuilder capitalized = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                capitalized.append(Character.toUpperCase(part.charAt(0)))
                        .append(part.substring(1))
                        .append("-");
            }
        }
        return capitalized.toString().replaceAll("-$", "");
    }

    /**
     * Parses the query parameters from a URL-encoded query string.
     * This method decodes the key-value pairs and returns them as a map.
     * 
     * @param query The URL-encoded query string.
     * @return A map containing the decoded key-value pairs.
     */
    private Map<String, String> parseQueryParams(String query) {
        Map<String, String> map = new LinkedHashMap<>();
        if (query == null || query.isBlank())
            return map;

        for (String param : query.split("&")) {
            String[] kv = param.split("=", 2);
            String key = kv.length > 0 ? decode(kv[0]) : "";
            String val = kv.length > 1 ? decode(kv[1]) : "";
            map.put(key, val);
        }
        return map;
    }

    /**
     * Decodes a URL-encoded string using UTF-8 encoding.
     * If decoding fails, it returns the original string.
     * 
     * @param value The URL-encoded string to decode.
     * @return The decoded string or the original string if decoding fails.
     */
    private String decode(String value) {
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8.toString());
        } catch (Exception e) {
            return value;
        }
    }
}
