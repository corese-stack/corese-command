package fr.inria.corese.command.utils.http;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import fr.inria.corese.command.VersionProvider;
import fr.inria.corese.command.utils.ContentValidator;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.kgram.core.Query;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.core.sparql.triple.parser.Constant;
import fr.inria.corese.core.sparql.triple.update.ASTUpdate;
import fr.inria.corese.core.sparql.triple.update.Composite;
import fr.inria.corese.core.sparql.triple.update.Update;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation.Builder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import picocli.CommandLine.Model.CommandSpec;

/**
 * This class provides functionalities to send HTTP requests to a SPARQL
 * endpoint.
 */
public class SparqlHttpClient {

    private final SparqlHttpPrinter printer;

    private final String endpointUrl;
    private EnumRequestMethod requestMethod = EnumRequestMethod.GET;
    private Boolean requestMethodIsDefinedByUser = false;
    private List<Pair<String, String>> headers = new ArrayList<>();

    private boolean verbose = false;

    private final String USERAGENT = "Corese-Command/" + VersionProvider.getCommandVersion();

    /////////////////
    // Constructor //
    /////////////////

    /**
     * Constructor.
     * 
     * @param endpointUrl URL of the SPARQL endpoint to send the request to.
     */
    public SparqlHttpClient(CommandSpec spec, String endpointUrl) {
        this.printer = new SparqlHttpPrinter(spec);
        if (endpointUrl == null || endpointUrl.isEmpty()) {
            throw new IllegalArgumentException("Endpoint URL must be specified");
        }
        this.endpointUrl = endpointUrl;
    }

    ///////////////////////
    // Getters & Setters //
    ///////////////////////

    /**
     * Sets the request method.
     * 
     * @param requestMethod the request method
     */
    public void setRequestMethod(EnumRequestMethod requestMethod) {
        if (requestMethod != null) {
            this.requestMethod = requestMethod;
            this.requestMethodIsDefinedByUser = true;
        }
    }

    /**
     * Sets the verbose mode.
     * 
     * @param verbose true to enable verbose mode, false otherwise
     */
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    /**
     * Gets the endpoint URL.
     * 
     * @return the endpoint URL
     */
    public String getEndpointUrl() {
        return this.endpointUrl;
    }

    /**
     * Sets the personalized header.
     * 
     * @param key   the key of the header
     * @param value the value of the header
     */
    public void addHeader(String key, String value) {
        // Check if the key and value are not null or empty
        if (key == null || key.isBlank() || value == null || value.isBlank()) {
            return;
        }
        this.headers.add(Pair.of(key.trim(), value.trim()));
    }

    /////////////////////////
    // HTTP request method //
    /////////////////////////

    /**
     * Sends a SPARQL query to the SPARQL endpoint.
     * 
     * @param query SPARQL query to send
     * @return the response from the endpoint
     * @throws Exception if an error occurs while sending the request
     */
    public String sendRequest(String query) throws Exception {
        return sendRequest(query, new ArrayList<>(), new ArrayList<>(), false);
    }

    /**
     * Sends a SPARQL query to the SPARQL endpoint.
     * 
     * @param query                 SPARQL query to send
     * @param defaultGraphUris      default graph URIs to use
     * @param namedGraphUris        named graph URIs to use
     * @param ignoreQueryValidation true to ignore query validation, false otherwise
     * @return the response from the endpoint
     * @throws Exception if an error occurs while sending the request
     */
    public String sendRequest(String query, List<String> defaultGraphUris, List<String> namedGraphUris,
            boolean ignoreQueryValidation)
            throws Exception {

        // If the "User-Agent" header is not present, add it
        if (!this.headers.stream().anyMatch(header -> header.getLeft().equalsIgnoreCase("User-Agent"))) {
            this.addHeader("User-Agent", this.USERAGENT);
        }

        // Fix parameters
        if (defaultGraphUris == null) {
            defaultGraphUris = new ArrayList<>();
        }

        if (namedGraphUris == null) {
            namedGraphUris = new ArrayList<>();
        }

        // Validate the query
        if (!ignoreQueryValidation) {
            this.validateQuery(query, defaultGraphUris, namedGraphUris);
        }

        // Create the web target based on type of request method
        WebTarget webTarget = this.buildWebTarget(this.endpointUrl, query, defaultGraphUris, namedGraphUris);

        // Create the request body based on type of request method
        String bodyContent = this.buildRequestBody(query, defaultGraphUris, namedGraphUris);

        Response response;

        // Execute the request
        response = this.executeRequest(webTarget, bodyContent);

        // Print the response if verbose mode is enabled
        if (this.verbose) {
            this.printer.printResponse(response);
        }

        // Validate the response
        validateResponse(response);

        // Return the response
        return response.readEntity(String.class);
    }

    /////////////////////
    // Private methods //
    /////////////////////

    /**
     * Validates the query. The query must be defined and must be a valid SPARQL
     * query and respect the SPARQL specification.
     * 
     * @param queryString      the query to validate
     * @param defaultGraphUris default graph URIs to use
     * @param namedGraphUris   named graph URIs to use
     */
    private void validateQuery(String queryString, List<String> defaultGraphUris, List<String> namedGraphUris) {

        // Check if the query is defined
        if (queryString == null || queryString.isEmpty()) {
            throw new IllegalArgumentException("SPARQL query must be specified");
        }

        // Check if the query is a valid SPARQL query
        if (!ContentValidator.isValidSparqlQuery(queryString)) {
            throw new IllegalArgumentException("Invalid SPARQL query");
        }

        Query query = buildQuery(queryString);

        if (!this.requestMethodIsDefinedByUser) {
            // Check if the query is an update query.
            if (query.getAST().isSPARQLUpdate()) {
                // If it is an update query, set the request method to POST_Encoded.
                this.requestMethod = EnumRequestMethod.POST_URLENCODED;
            } else {
                // If the query is not an update query, set the request method to GET.
                // No need to set it here as GET is already the default value.
            }
        }

        // Check if the query is an update query and the method is GET
        // which is not allowed by the SPARQL specification
        // (see https://www.w3.org/TR/sparql11-protocol/#update-operation)
        if (this.requestMethod == EnumRequestMethod.GET && query.getAST().isSPARQLUpdate()) {
            throw new IllegalArgumentException(
                    "SPARQL query is an update query, but GET method is used. Please use a POST method instead.");
        }

        // Check if the query contains FROM clause and default/named graph URIs
        // which is not allowed by the SPARQL specification
        // (see https://www.w3.org/TR/sparql11-protocol/#query-operation)
        if (containsFromClause(query) && !(defaultGraphUris.isEmpty() && namedGraphUris.isEmpty())) {
            throw new IllegalArgumentException(
                    "SPARQL query contains FROM clause, but default and named graph URIs are specified. It is not allowed to specify both FROM clause and default/named graph URIs. Please remove FROM clause from the query or remove default/named graph URIs.");
        }

        // Check if the update query contains USING, USING NAMED, or WITH clauses
        // and the using-graph-uri/using-named-graph-uri parameters are also specified
        // which is not allowed by the SPARQL specification
        // (see https://www.w3.org/TR/sparql11-protocol/#update-operation)
        List<String> sparqlConstants = new ArrayList<>();
        ASTUpdate astUpdate = query.getAST().getUpdate();
        if (astUpdate != null) {
            for (Update update : astUpdate.getUpdates()) {
                Composite composite = update.getComposite();
                if (composite != null) {
                    Constant with = composite.getWith();
                    if (with != null) {
                        sparqlConstants.add(with.getLabel());
                    }
                }
            }
        }

        if (!sparqlConstants.isEmpty() && (!defaultGraphUris.isEmpty() || !namedGraphUris.isEmpty())) {
            throw new IllegalArgumentException(
                    "SPARQL update query contains USING, USING NAMED, or WITH clause and the using-graph-uri/using-named-graph-uri parameters are also specified. It is not allowed to specify both USING, USING NAMED, or WITH clause and the using-graph-uri/using-named-graph-uri parameters. Please remove USING, USING NAMED, or WITH clause from the query or remove the using-graph-uri/using-named-graph-uri parameters.");
        }

    }

    /**
     * Builds a query object from the given query string.
     * 
     * @param query the query string
     * @return the query object
     */
    private Query buildQuery(String query) {
        QueryProcess exec = QueryProcess.create(Graph.create());
        Query q;
        try {
            q = exec.compile(query);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid SPARQL query", e);
        }
        return q;
    }

    /**
     * Checks if the query contains FROM clause.
     * 
     * @param query the query to check
     * @return true if the query contains FROM clause, false otherwise
     */
    private boolean containsFromClause(Query query) {
        return query.getFrom() != null && !query.getFrom().isEmpty();
    }

    /**
     * Builds a web target.
     * 
     * @param endpoint         the endpoint URL
     * @param query            the query
     * @param defaultGraphUris default graph URIs to use
     * @param namedGraphUris   named graph URIs to use
     * @return the web target object
     */
    private WebTarget buildWebTarget(
            String endpoint,
            String query,
            List<String> defaultGraphUris,
            List<String> namedGraphUris) {

        // Create the web target
        ClientBuilder clientBuilder = ClientBuilder.newBuilder();
        Client client = clientBuilder.build();
        WebTarget webTarget = client.target(endpoint);

        // Add the query parameter
        if (this.requestMethod == EnumRequestMethod.GET) {
            webTarget = webTarget.queryParam("query", this.encode(query));
        }

        // Add graph URIs
        if (this.requestMethod == EnumRequestMethod.GET || this.requestMethod == EnumRequestMethod.POST_DIRECT) {
            for (String defaultGraphUri : defaultGraphUris) {
                webTarget = webTarget.queryParam("default-graph-uri", this.encode(defaultGraphUri));
            }
            for (String namedGraphUri : namedGraphUris) {
                webTarget = webTarget.queryParam("named-graph-uri", this.encode(namedGraphUri));
            }
        }

        return webTarget;
    }

    /**
     * Builds the request body.
     * 
     * @param query            the query
     * @param defaultGraphUris default graph URIs to use
     * @param namedGraphUris   named graph URIs to use
     * @return the request body
     */
    private String buildRequestBody(
            String query,
            List<String> defaultGraphUris,
            List<String> namedGraphUris) {

        StringBuilder bodyContent = new StringBuilder();

        if (this.requestMethod == EnumRequestMethod.POST_URLENCODED) {
            // Add the query parameter
            bodyContent.append("query=").append(this.encode(query));

            // Add graph URIs
            for (String defaultGraphUri : defaultGraphUris) {
                bodyContent.append("&default-graph-uri=").append(this.encode(defaultGraphUri));
            }
            for (String namedGraphUri : namedGraphUris) {
                bodyContent.append("&named-graph-uri=").append(this.encode(namedGraphUri));
            }
        } else if (this.requestMethod == EnumRequestMethod.POST_DIRECT) {
            // Add the query parameter
            bodyContent.append(query);
        }

        return bodyContent.toString();
    }

    /**
     * Executes the request.
     * 
     * @param webTarget   the web target of the request
     * @param bodyContent the body content of the request
     * @return the response from the endpoint
     */
    private Response executeRequest(WebTarget webTarget, String bodyContent) {
        Response response = null;

        // Add headers
        Builder builder = webTarget.request();

        for (Pair<String, String> header : this.headers) {
            builder = builder.header(header.getKey(), header.getValue());
        }

        // Add request content types
        String contentType = null;
        if (this.requestMethod == EnumRequestMethod.POST_URLENCODED) {
            contentType = MediaType.APPLICATION_FORM_URLENCODED;
        } else if (this.requestMethod == EnumRequestMethod.POST_DIRECT) {
            contentType = "application/sparql-query";
        }

        // Print query and body content if verbose mode is enabled
        if (this.verbose) {
            this.printer.printRequest(webTarget, bodyContent, contentType, headers, requestMethod);
        }

        // Send the request
        if (this.requestMethod == EnumRequestMethod.GET) {
            response = builder.get();
        } else if (this.requestMethod == EnumRequestMethod.POST_URLENCODED) {
            response = builder.post(Entity.entity(bodyContent, contentType));
        } else if (this.requestMethod == EnumRequestMethod.POST_DIRECT) {
            response = builder.post(Entity.entity(bodyContent, contentType));
        }

        return response;
    }

    /**
     * Encodes the given value using the UTF-8 encoding scheme.
     *
     * @param value the value to be encoded
     * @return the encoded value
     * @throws IllegalStateException if the UTF-8 encoding is not supported, which
     *                               should never happen as it is guaranteed to be
     *                               supported by the JVM
     *                               (see <a href=
     *                               "https://docs.oracle.com/javase/8/docs/api/java/nio/charset/Charset.html">Java
     *                               Charset documentation</a>).
     */
    private String encode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString()).replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Failed to encode value: " + value, e);
        }
    }

    /**
     * Validates the response. Throws an exception if the HTTP status code is not
     * 2xx.
     *
     * @param response the response to validate
     * @throws Exception if the response status is not successful
     */
    private void validateResponse(Response response) throws Exception {
        int status = response.getStatus();

        if (status < 200 || status >= 300) {
            String body = response.readEntity(String.class);
            String reason = response.getStatusInfo().getReasonPhrase();

            throw new Exception("HTTP " + status + " " + reason + "\n" +
                    "Response body:\n" + body);
        }
    }

}
