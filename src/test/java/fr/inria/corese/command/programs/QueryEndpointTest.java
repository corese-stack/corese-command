package fr.inria.corese.command.programs;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import picocli.CommandLine;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

class QueryEndpointTest {

    // Picocli objects
    private QueryEndpoint queryRemote = new QueryEndpoint();
    private CommandLine cmd = new CommandLine(queryRemote);

    private StringWriter out = new StringWriter();
    private StringWriter err = new StringWriter();

    // WireMock objects
    private static WireMockServer wireMockServer;

    // Server informations
    private final String serverUrl = "http://localhost:8080/sparql";
    private final String graphUri = "http://example.orgraphUrig/graph";
    // Query
    private static final String QUERY_SPO = "SELECT * WHERE { ?s ?p ?o }";

    // ===== Before All =====

    @BeforeEach
    void initializePicoCli() {
        PrintWriter outputWriter = new PrintWriter(this.out);
        PrintWriter errorWriter = new PrintWriter(this.err);
        cmd.setOut(outputWriter);
        cmd.setErr(errorWriter);
    }

    @BeforeAll
    static void initializeWireMockServer() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().port(8080));

        wireMockServer.start();

        // Get
        wireMockServer.stubFor(
                get(urlPathEqualTo("/sparql"))
                        .withQueryParam("query", equalTo(QUERY_SPO))
                        .willReturn(
                                aResponse().withStatus(200).withBody("this is a fake response")));

        // Post-UrlEncoded
        wireMockServer.stubFor(
                post(urlPathEqualTo("/sparql"))
                        .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded"))
                        .willReturn(
                                aResponse().withStatus(200).withBody("this is a fake response")));

        // Post-Direct
        wireMockServer.stubFor(
                post(urlPathEqualTo("/sparql"))
                        .withHeader("Content-Type", equalTo("application/sparql-query"))
                        .willReturn(
                                aResponse().withStatus(200).withBody("this is a fake response")));
    }

    // ===== After All =====

    @AfterEach
    void resetStreams() {
        wireMockServer.resetRequests();
        out.getBuffer().setLength(0);
        err.getBuffer().setLength(0);
    }

    @AfterAll
    static void tearDown() {
        wireMockServer.stop();
        wireMockServer.shutdown();
    }

    // ===== Utilities =====

    private static String encode(String value) {
        try {
            // Encode the value using URLEncoder
            String encodedValue = URLEncoder.encode(value, "UTF-8");
            // Replace '+' with '%20'
            return encodedValue.replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 encoding is not supported", e);
        }
    }

    // ===== Test Cases =====

    // Query via get

    @Test
    void getQueryTest() {
        String[] args = {"-e", serverUrl, "-q", QUERY_SPO, "-m", "get"};
        int exitCode = cmd.execute(args);

        // Asserts
        assertEquals(0, exitCode);
        verify(
                exactly(1),
                getRequestedFor(urlPathEqualTo("/sparql"))
                        .withQueryParam("query", equalTo(QUERY_SPO))
                        .withHeader("Accept", equalTo("text/csv")));
    }

    @Test
    void getQueryTestDefaultGraphUri() {
        String[] args = {"-e", serverUrl, "-q", QUERY_SPO, "-m", "get", "-d", graphUri};
        int exitCode = cmd.execute(args);

        // Asserts
        assertEquals(0, exitCode);
        verify(
                exactly(1),
                getRequestedFor(urlPathEqualTo("/sparql"))
                        .withQueryParam("query", equalTo(QUERY_SPO))
                        .withQueryParam("default-graph-uri", equalTo(graphUri))
                        .withHeader("Accept", equalTo("text/csv")));
    }

    @Test
    void getQueryTestNamedGraphUri() {
        String[] args = {"-e", serverUrl, "-q", QUERY_SPO, "-m", "get", "-n", graphUri};
        int exitCode = cmd.execute(args);

        // Asserts
        assertEquals(0, exitCode);
        verify(
                exactly(1),
                getRequestedFor(urlPathEqualTo("/sparql"))
                        .withQueryParam("query", equalTo(QUERY_SPO))
                        .withQueryParam("named-graph-uri", equalTo(graphUri))
                        .withHeader("Accept", equalTo("text/csv")));
    }

    @Test
    void getQueryTestAcceptHeader() {
        String[] args = {"-e", serverUrl, "-q", QUERY_SPO, "-m", "get", "-a", "application/json"};
        int exitCode = cmd.execute(args);

        // Asserts
        assertEquals(0, exitCode);
        verify(
                exactly(1),
                getRequestedFor(urlPathEqualTo("/sparql"))
                        .withQueryParam("query", equalTo(QUERY_SPO))
                        .withHeader("Accept", equalTo("application/json")));
    }

    @Test
    void getQueryTestMultipleHeaders() {
        String[] args = {
            "-e",
            serverUrl,
            "-q",
            QUERY_SPO,
            "-m",
            "get",
            "-H",
            "Accept: application/json",
            "-H",
            "Authorization: Bearer 1234"
        };
        int exitCode = cmd.execute(args);

        // Asserts
        assertEquals(0, exitCode);
        verify(
                exactly(1),
                getRequestedFor(urlPathEqualTo("/sparql"))
                        .withQueryParam("query", equalTo(QUERY_SPO))
                        .withHeader("Accept", equalTo("application/json"))
                        .withHeader("Authorization", equalTo("Bearer 1234")));
    }

    // Query via POST URL-Encoded

    @Test
    void postQueryUrlEncodedQueryTest() {
        String[] args = {"-e", serverUrl, "-q", QUERY_SPO, "-m", "post-urlencoded"};
        int exitCode = cmd.execute(args);

        // Asserts
        assertEquals(0, exitCode);
        verify(
                exactly(1),
                postRequestedFor(urlPathEqualTo("/sparql"))
                        .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded"))
                        .withRequestBody(equalTo("query=" + encode(QUERY_SPO)))
                        .withHeader("Accept", equalTo("text/csv")));
    }

    @Test
    void postQueryUrlEncodedQueryTestDefaultGraphUri() {
        String[] args = {"-e", serverUrl, "-q", QUERY_SPO, "-m", "post-urlencoded", "-d", graphUri};
        int exitCode = cmd.execute(args);

        // Asserts
        assertEquals(0, exitCode);
        verify(
                exactly(1),
                postRequestedFor(urlPathEqualTo("/sparql"))
                        .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded"))
                        .withRequestBody(
                                equalTo(
                                        "query="
                                                + encode(QUERY_SPO)
                                                + "&default-graph-uri="
                                                + encode(graphUri)))
                        .withHeader("Accept", equalTo("text/csv")));
    }

    @Test
    void postQueryUrlEncodedQueryTestNamedGraphUri() {
        String[] args = {"-e", serverUrl, "-q", QUERY_SPO, "-m", "post-urlencoded", "-n", graphUri};
        int exitCode = cmd.execute(args);

        // Asserts
        assertEquals(0, exitCode);
        verify(
                exactly(1),
                postRequestedFor(urlPathEqualTo("/sparql"))
                        .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded"))
                        .withRequestBody(
                                equalTo(
                                        "query="
                                                + encode(QUERY_SPO)
                                                + "&named-graph-uri="
                                                + encode(graphUri)))
                        .withHeader("Accept", equalTo("text/csv")));
    }

    @Test
    void postQueryUrlEncodedQueryTestAcceptHeader() {
        String[] args = {
            "-e", serverUrl, "-q", QUERY_SPO, "-m", "post-urlencoded", "-a", "application/json"
        };
        int exitCode = cmd.execute(args);

        // Asserts
        assertEquals(0, exitCode);
        verify(
                exactly(1),
                postRequestedFor(urlPathEqualTo("/sparql"))
                        .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded"))
                        .withRequestBody(equalTo("query=" + encode(QUERY_SPO)))
                        .withHeader("Accept", equalTo("application/json")));
    }

    @Test
    void postQueryUrlEncodedQueryTestMultipleHeaders() {
        String[] args = {
            "-e",
            serverUrl,
            "-q",
            QUERY_SPO,
            "-m",
            "post-urlencoded",
            "-H",
            "Accept: application/json",
            "-H",
            "Authorization: Bearer 1234"
        };
        int exitCode = cmd.execute(args);

        // Asserts
        assertEquals(0, exitCode);
        verify(
                exactly(1),
                postRequestedFor(urlPathEqualTo("/sparql"))
                        .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded"))
                        .withRequestBody(equalTo("query=" + encode(QUERY_SPO)))
                        .withHeader("Accept", equalTo("application/json"))
                        .withHeader("Authorization", equalTo("Bearer 1234")));
    }

    // Query via POST Directly

    @Test
    void postQueryDirectQueryTest() {
        String[] args = {"-e", serverUrl, "-q", QUERY_SPO, "-m", "post-direct"};
        int exitCode = cmd.execute(args);

        // Asserts
        assertEquals(0, exitCode);
        verify(
                exactly(1),
                postRequestedFor(urlPathEqualTo("/sparql"))
                        .withHeader("Content-Type", equalTo("application/sparql-query"))
                        .withRequestBody(equalTo(QUERY_SPO))
                        .withHeader("Accept", equalTo("text/csv")));
    }

    @Test
    void postQueryDirectQueryTestDefaultGraphUri() {
        String[] args = {"-e", serverUrl, "-q", QUERY_SPO, "-m", "post-direct", "-d", graphUri};
        int exitCode = cmd.execute(args);

        // Asserts
        assertEquals(0, exitCode);
        verify(
                exactly(1),
                postRequestedFor(urlPathEqualTo("/sparql"))
                        .withHeader("Content-Type", equalTo("application/sparql-query"))
                        .withRequestBody(equalTo(QUERY_SPO))
                        .withQueryParam("default-graph-uri", equalTo(graphUri))
                        .withHeader("Accept", equalTo("text/csv")));
    }

    @Test
    void postQueryDirectQueryTestNamedGraphUri() {
        String[] args = {"-e", serverUrl, "-q", QUERY_SPO, "-m", "post-direct", "-n", graphUri};
        int exitCode = cmd.execute(args);

        // Asserts
        assertEquals(0, exitCode);
        verify(
                exactly(1),
                postRequestedFor(urlPathEqualTo("/sparql"))
                        .withHeader("Content-Type", equalTo("application/sparql-query"))
                        .withRequestBody(equalTo(QUERY_SPO))
                        .withQueryParam("named-graph-uri", equalTo(graphUri))
                        .withHeader("Accept", equalTo("text/csv")));
    }

    @Test
    void postQueryDirectQueryTestAcceptHeader() {
        String[] args = {
            "-e", serverUrl, "-q", QUERY_SPO, "-m", "post-direct", "-a", "application/json"
        };
        int exitCode = cmd.execute(args);

        // Asserts
        assertEquals(0, exitCode);
        verify(
                exactly(1),
                postRequestedFor(urlPathEqualTo("/sparql"))
                        .withHeader("Content-Type", equalTo("application/sparql-query"))
                        .withRequestBody(equalTo(QUERY_SPO))
                        .withHeader("Accept", equalTo("application/json")));
    }

    @Test
    void postQueryDirectQueryTestMultipleHeaders() {
        String[] args = {
            "-e",
            serverUrl,
            "-q",
            QUERY_SPO,
            "-m",
            "post-direct",
            "-H",
            "Accept: application/json",
            "-H",
            "Authorization: Bearer 1234"
        };
        int exitCode = cmd.execute(args);

        // Asserts
        assertEquals(0, exitCode);
        verify(
                exactly(1),
                postRequestedFor(urlPathEqualTo("/sparql"))
                        .withHeader("Content-Type", equalTo("application/sparql-query"))
                        .withRequestBody(equalTo(QUERY_SPO))
                        .withHeader("Accept", equalTo("application/json"))
                        .withHeader("Authorization", equalTo("Bearer 1234")));
    }
}
