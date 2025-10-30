package fr.inria.corese.command.utils.http;

import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

import org.apache.commons.lang3.tuple.Pair;

import picocli.CommandLine.Model.CommandSpec;

import java.io.PrintWriter;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class to print details of HTTP requests and responses. This class is used for debugging
 * and logging purposes.
 */
public class SparqlHttpPrinter {
    private final PrintWriter err;

    /**
     * Constructor to initialize the printer with a PrintWriter. This is typically used to print to
     * the standard error output.
     *
     * @param spec The command specification containing the PrintWriter.
     */
    public SparqlHttpPrinter(CommandSpec spec) {
        this.err = spec.commandLine().getErr();
    }

    /**
     * Prints the details of an HTTP request. This includes the URL, HTTP method, query parameters,
     * headers, and body content.
     *
     * @param webTarget The target of the HTTP request.
     * @param bodyContent The body content of the request.
     * @param contentType The content type of the request.
     * @param headers The headers of the request.
     * @param method The HTTP method used for the request (GET, POST, etc.).
     */
    public void printRequest(
            WebTarget webTarget,
            String bodyContent,
            String contentType,
            List<Pair<String, String>> headers,
            HttpRequestMethod method) {
        err.println("╔════════════════════════════════╗");
        err.println("║        REQUEST DETAILS         ║");
        err.println("╚════════════════════════════════╝\n");

        printUrl(webTarget);
        printMethod(method);
        printQueryParameters(webTarget);
        printHeaders(headers, contentType);
        printRequestBody(bodyContent);

        err.println("\n──────────────────────────────────");
    }

    /**
     * Prints the URL of the request.
     *
     * @param webTarget The target of the HTTP request.
     */
    private void printUrl(WebTarget webTarget) {
        if (webTarget != null && webTarget.getUri() != null) {
            err.println("► URL");
            err.println("  " + webTarget.getUri());
        }
    }

    /**
     * Prints the HTTP method of the request.
     *
     * @param method The HTTP method used for the request.
     */
    private void printMethod(HttpRequestMethod method) {
        if (method != null) {
            err.println("\n► METHOD");
            err.println("  " + method.name());
        }
    }

    /**
     * Prints the query parameters if present in the URL.
     *
     * @param webTarget The target of the HTTP request.
     */
    private void printQueryParameters(WebTarget webTarget) {
        if (webTarget == null || webTarget.getUri() == null) {
            return;
        }

        URI uri = webTarget.getUri();
        if (uri.getQuery() == null) {
            return;
        }

        err.println("\n► QUERY PARAMETERS");
        Map<String, String> decodedParams = parseQueryParams(uri.getQuery());
        for (Map.Entry<String, String> entry : decodedParams.entrySet()) {
            printQueryParameter(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Prints a single query parameter.
     *
     * @param key The parameter key.
     * @param value The parameter value.
     */
    private void printQueryParameter(String key, String value) {
        String[] lines = value.split("\n");

        if (lines.length == 1) {
            err.println("  " + key + "=" + value);
        } else {
            err.println("  " + key + "=");
            int lastNonEmpty = findLastNonEmptyLine(lines);
            for (int i = 0; i <= lastNonEmpty; i++) {
                err.println("    " + lines[i]);
            }
        }
    }

    /**
     * Finds the index of the last non-empty line in an array of lines.
     *
     * @param lines The array of lines.
     * @return The index of the last non-empty line.
     */
    private int findLastNonEmptyLine(String[] lines) {
        int lastNonEmpty = lines.length - 1;
        while (lastNonEmpty >= 0 && lines[lastNonEmpty].isBlank()) {
            lastNonEmpty--;
        }
        return lastNonEmpty;
    }

    /**
     * Prints the headers and content type if available.
     *
     * @param headers The headers of the request.
     * @param contentType The content type of the request.
     */
    private void printHeaders(List<Pair<String, String>> headers, String contentType) {
        boolean hasHeaders = headers != null && !headers.isEmpty();
        boolean hasContentType = contentType != null && !contentType.isEmpty();

        if (!hasHeaders && !hasContentType) {
            return;
        }

        err.println("\n► HEADERS");

        Map<String, StringBuilder> normalized = normalizeHeaders(headers);
        addContentTypeHeader(normalized, contentType);
        printNormalizedHeaders(normalized);
    }

    /**
     * Normalizes headers by grouping them by key.
     *
     * @param headers The headers to normalize.
     * @return A map of normalized headers.
     */
    private Map<String, StringBuilder> normalizeHeaders(List<Pair<String, String>> headers) {
        Map<String, StringBuilder> normalized = new LinkedHashMap<>();
        
        if (headers == null) {
            return normalized;
        }

        for (Pair<String, String> header : headers) {
            String key = normalizeHeaderKey(header.getKey());
            normalized
                    .computeIfAbsent(key, k -> new StringBuilder())
                    .append(header.getValue())
                    .append(",");
        }
        return normalized;
    }

    /**
     * Adds the content type to the normalized headers.
     *
     * @param normalized The normalized headers map.
     * @param contentType The content type to add.
     */
    private void addContentTypeHeader(Map<String, StringBuilder> normalized, String contentType) {
        if (contentType != null && !contentType.isEmpty()) {
            String key = "Content-Type";
            normalized
                    .computeIfAbsent(key, k -> new StringBuilder())
                    .append(contentType)
                    .append(",");
        }
    }

    /**
     * Prints the normalized headers.
     *
     * @param normalized The normalized headers to print.
     */
    private void printNormalizedHeaders(Map<String, StringBuilder> normalized) {
        for (Map.Entry<String, StringBuilder> entry : normalized.entrySet()) {
            String values = entry.getValue().toString().replaceAll(",$", "");
            err.println("  " + entry.getKey() + ": " + values);
        }
    }

    /**
     * Prints the body content of the request if available.
     *
     * @param bodyContent The body content to print.
     */
    private void printRequestBody(String bodyContent) {
        if (bodyContent != null && !bodyContent.isEmpty()) {
            err.println("\n► REQUEST BODY");
            for (String line : bodyContent.split("\n")) {
                err.println("  " + line);
            }
        }
    }

    /**
     * Prints the details of an HTTP response. This includes the HTTP status code, status
     * information, headers, and any other relevant details.
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
                String value =
                        String.join(", ", entry.getValue().stream().map(Object::toString).toList());
                err.println("  " + key + ": " + value);
            }
        }

        err.println("\n──────────────────────────────────");
    }

    /**
     * Normalizes the header key by converting it to a standard format. This method capitalizes the
     * first letter of each part of the header key and removes any trailing hyphen.
     *
     * @param key The header key to normalize.
     * @return The normalized header key.
     */
    private String normalizeHeaderKey(String key) {
        if (key == null || key.isEmpty()) return "";
        String lower = key.toLowerCase();
        String[] parts = lower.split("-");
        StringBuilder capitalized = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                capitalized
                        .append(Character.toUpperCase(part.charAt(0)))
                        .append(part.substring(1))
                        .append("-");
            }
        }
        return capitalized.toString().replaceAll("-$", "");
    }

    /**
     * Parses the query parameters from a URL-encoded query string. This method decodes the
     * key-value pairs and returns them as a map.
     *
     * @param query The URL-encoded query string.
     * @return A map containing the decoded key-value pairs.
     */
    private Map<String, String> parseQueryParams(String query) {
        Map<String, String> map = new LinkedHashMap<>();
        if (query == null || query.isBlank()) return map;

        for (String param : query.split("&")) {
            String[] kv = param.split("=", 2);
            String key = kv.length > 0 ? decode(kv[0]) : "";
            String val = kv.length > 1 ? decode(kv[1]) : "";
            map.put(key, val);
        }
        return map;
    }

    /**
     * Decodes a URL-encoded string using UTF-8 encoding. If decoding fails, it returns the original
     * string.
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
