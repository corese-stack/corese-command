package fr.inria.corese.command.programs;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.github.jsonldjava.shaded.com.google.common.io.Files;

import fr.inria.corese.command.utils.http.EnumRequestMethod;
import fr.inria.corese.command.utils.http.SparqlHttpClient;
import fr.inria.corese.command.utils.loader.sparql.SparqlQueryLoader;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "query-remote", description = "Execute a SPARQL query on a remote endpoint.", mixinStandardHelpOptions = true)
public class QueryRemote extends AbstractCommand {

    @Option(names = { "-q",
            "--query" }, description = "Specifies the SPARQL query to execute. This can be provided as a URL or a file path.", required = false)
    private String queryUrlOrFile;

    @Option(names = { "-e", "--endpoint" }, description = "Specifies the SPARQL endpoint URL.", required = true)
    private String endpoint_url;

    @Option(names = { "-H",
            "--header" }, description = "Adds an HTTP header to the request. Multiple headers can be specified.", arity = "0..")
    private List<String> headers;

    @Option(names = { "-a", "-of",
            "--accept" }, description = "Specifies the Accept header value for the HTTP request.")
    private String accept;

    @Option(names = { "-m",
            "--request-method" }, description = "Specifies the HTTP request method to use. Possible values are: :@|fg(magenta) ${COMPLETION-CANDIDATES}|@.")
    private EnumRequestMethod requestMethod;

    @Option(names = { "-r",
            "--max-redirection" }, description = "Specifies the maximum number of redirections to follow. Default value: ${DEFAULT-VALUE}.", defaultValue = "5")
    private int maxRedirection;

    @Option(names = { "-d",
            "--default-graph" }, description = "Specifies the default graph URI. Multiple URIs can be specified.", arity = "0..")
    private List<String> default_graph;

    @Option(names = { "-n",
            "--named-graph" }, description = "Specifies the named graph URI. Multiple URIs can be specified.", arity = "0..")
    private List<String> named_graph;

    @Option(names = { "-i",
            "--ignore-query-validation" }, description = "Ignores query validation if set to true. Default value: ${DEFAULT-VALUE}.", required = false, defaultValue = "false")
    private boolean ignoreQueryValidation;

    private String query;

    private final String DEFAULT_ACCEPT_HEADER = "text/csv";

    @Override
    public Integer call() {

        super.call();

        try {

            // if accept is not defined, set it to text/csv
            if (this.accept == null && !this.containsAcceptHeader(this.headers)) {
                this.accept = DEFAULT_ACCEPT_HEADER;
            }

            // Load query
            SparqlQueryLoader queryLoader = new SparqlQueryLoader(this.spec, this.verbose);
            this.query = queryLoader.load(this.queryUrlOrFile);

            // Execute query
            String res = this.sendRequest();

            // Export result
            this.exportResult(res);

        } catch (Exception e) {
            this.spec.commandLine().getErr().println("Error: " + e.getMessage());
            return this.ERROR_EXIT_CODE_ERROR;
        }

        return this.ERROR_EXIT_CODE_SUCCESS;
    }

    /**
     * Check if the headers contain an accept header.
     * 
     * @param headers The headers to check.
     * @return True if the headers contain an accept header, false otherwise.
     */
    private boolean containsAcceptHeader(List<String> headers) {
        return headers != null && headers.stream().anyMatch(
                header -> header.trim().toLowerCase().startsWith("accept:"));
    }

    /**
     * Send the SPARQL query to the endpoint.
     * 
     * @return The response from the endpoint.
     * @throws Exception If an error occurs.
     */
    public String sendRequest() throws Exception {

        SparqlHttpClient client = new SparqlHttpClient(this.spec, this.endpoint_url);
        this.parseHeader(client);
        client.setRequestMethod(this.requestMethod);
        client.setVerbose(this.verbose);
        client.setMaxRedirection(this.maxRedirection);

        return client.sendRequest(this.query, this.default_graph, this.named_graph, this.ignoreQueryValidation);
    }

    /**
     * Parse the header and add them to the client.
     * 
     * @param client The client to add the header to.
     */
    private void parseHeader(SparqlHttpClient client) {

        // Add Accept header
            client.addHeader("Accept", this.accept);

        // Add custom headers
        if (this.headers != null) {
            for (String header : this.headers) {
                String[] headerParts = header.split(":", 2);
                if (headerParts.length == 2) {
                    client.addHeader(headerParts[0], headerParts[1]);
                } else {
                    throw new RuntimeException("Invalid header format (expected key: value): " + header);
                }
            }
        }
    }

    /**
     * Export the result to a file or to standard output.
     * 
     * @param response The response to export.
     */
    private void exportResult(String response) {

        if (this.output != null) {
            // Write result to file
            try {
                Files.write(response.getBytes(StandardCharsets.UTF_8), this.output.toFile());
            } catch (IOException e) {
                throw new RuntimeException("Error while writing result to file: " + e.getMessage());
            }
        } else {
            // Write result to standard output
            this.spec.commandLine().getOut().println(response);
        }

    }
}
