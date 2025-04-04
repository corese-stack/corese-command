package fr.inria.corese.command.utils.http;

import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;
import picocli.CommandLine.Model.CommandSpec;

public class SparqlHttpPrinter {
    private final PrintWriter err;

    public SparqlHttpPrinter(CommandSpec spec) {
        this.err = spec.commandLine().getErr();
    }

    public void printRequest(WebTarget webTarget, String bodyContent, String contentType,
            List<Pair<String, String>> headers, EnumRequestMethod method) {
        err.println("╔════════════════════════════════╗");
        err.println("║        REQUEST DETAILS         ║");
        err.println("╚════════════════════════════════╝\n");

        if (webTarget != null && webTarget.getUri() != null) {
            err.println("► URL");
            err.println("  " + webTarget.getUri());
        }

        if (method != null) {
            err.println("\n► METHOD");
            err.println("  " + method);
        }

        if (webTarget.getUri().getQuery() != null) {
            err.println("\n► QUERY PARAMETERS");
            for (String param : webTarget.getUri().getQuery().split("&")) {
                err.println("  " + param);
            }
        }

        if ((headers != null && !headers.isEmpty()) || (contentType != null && !contentType.isEmpty())) {
            err.println("\n► HEADERS");
            for (Pair<String, String> header : headers) {
                err.println("  " + header.getKey() + ": " + header.getValue());
            }
            if (contentType != null && !contentType.isEmpty()) {
                err.println("  Content-Type: " + contentType);
            }
        }

        if (bodyContent != null && !bodyContent.isEmpty()) {
            err.println("\n► REQUEST BODY");
            for (String line : bodyContent.split("\n")) {
                err.println("  " + line);
            }
        }

        err.println("\n──────────────────────────────────");
    }

    public void printResponse(Response response) {
        err.println("╔════════════════════════════════╗");
        err.println("║         RESPONSE DETAILS       ║");
        err.println("╚════════════════════════════════╝\n");

        if (response == null) {
            err.println("No response available.\n");
            err.println("──────────────────────────────────");
            return;
        }

        err.println("► HTTP CODE");
        err.println("  " + response.getStatus());

        if (response.getStatusInfo() != null) {
            err.println("\n► STATUS INFO");
            err.println("  " + response.getStatusInfo().toString());
        }

        Map<String, List<Object>> headers = response.getHeaders();
        if (!headers.isEmpty()) {
            err.println("\n► HEADERS");
            for (Map.Entry<String, List<Object>> entry : headers.entrySet()) {
                for (Object value : entry.getValue()) {
                    err.println("  " + entry.getKey() + ": " + value);
                }
            }
        }

        err.println("\n──────────────────────────────────");
    }

    public void printRedirect(String location) {
        err.println("Redirecting to: " + location);
    }
}
