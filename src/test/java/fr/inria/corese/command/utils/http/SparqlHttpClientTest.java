package fr.inria.corese.command.utils.http;

import static org.junit.jupiter.api.Assertions.assertThrows;

import fr.inria.corese.command.programs.AbstractCommand;
import fr.inria.corese.command.programs.QueryEndpoint;

import org.junit.jupiter.api.Test;

import picocli.CommandLine;
import picocli.CommandLine.Model.CommandSpec;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

class SparqlHttpClientTest {

    private final ArrayList<String> emptyList = new ArrayList<String>();

    // Default data for the tests
    private final String serverUrl = "http://localhost:8080/sparql";
    private final String graphUri = "http://example.orgraphUrig/graph";

    /**
     * Don't look at it. This is an ugly hack to get a valid CommandSpec instance for the tests. (It
     * uses reflective API to get a protected attribute of the QueryEndpoint class) We are only
     * testing the query validation which is not concerned with CommandSpec (but a valid one is
     * required anyway).
     *
     * @return
     */
    private CommandSpec createCommandSpecObject() {
        Field spec = null;

        QueryEndpoint queryEndpoint = new QueryEndpoint();
        CommandLine cmd = new CommandLine(queryEndpoint);

        PrintWriter out = new PrintWriter(new StringWriter());
        PrintWriter err = new PrintWriter(new StringWriter());
        cmd.setOut(out);
        cmd.setErr(err);

        try {
            spec = AbstractCommand.class.getDeclaredField("spec");
        } catch (IllegalArgumentException | SecurityException | NoSuchFieldException e) {
            System.err.println("*** Error in reflective API getting a ComandSpec object");
            e.printStackTrace();
            return null;
        }

        try {
            spec.setAccessible(true);
            return (CommandSpec) (spec.get(queryEndpoint));
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    /** Test that validateQuery fails for an empty query */
    @Test
    void testValidateQueryFalseForEmptyQuery() {

        SparqlHttpClient httpClient = new SparqlHttpClient(createCommandSpecObject(), serverUrl);

        /* Invalid null query */
        assertThrows(
                IllegalArgumentException.class,
                () -> httpClient.validateQuery(null, emptyList, emptyList));

        /* Invalid empty query */
        assertThrows(
                IllegalArgumentException.class,
                () -> httpClient.validateQuery("", emptyList, emptyList));
    }

    /** Test that validateQuery fails for query with a syntax error */
    @Test
    void testValidateQueryFalseForSyntaxError() {

        SparqlHttpClient httpClient = new SparqlHttpClient(createCommandSpecObject(), serverUrl);

        /* valid query : passes */
        httpClient.validateQuery("SELECT * WHERE { ?s ?p ?o }", emptyList, emptyList);

        /* Invalid query: syntax error */
        assertThrows(
                IllegalArgumentException.class,
                () -> httpClient.validateQuery("SELECT * WHERE", emptyList, emptyList));
    }

    /** Test that validateQuery fails for update query using the POST request method */
    @Test
    void testValidateQueryFalseForUpdateWithGET() {
        String query = "DELETE WHERE { ?s ?p ?o }";
        SparqlHttpClient httpClient = new SparqlHttpClient(createCommandSpecObject(), serverUrl);

        /* update query not defined by user: will set the requestMethod and pass */
        httpClient.validateQuery(query, emptyList, emptyList);

        /* update query defined by user with POST request method: passes */
        httpClient.setRequestMethod(EnumRequestMethod.POST_URLENCODED);
        httpClient.validateQuery(query, emptyList, emptyList);

        /* Invalid update query: defined by user with GET request method */
        httpClient.setRequestMethod(EnumRequestMethod.GET);
        assertThrows(
                IllegalArgumentException.class,
                () -> httpClient.validateQuery(query, emptyList, emptyList));
    }

    /** Test that validateQuery fails for an clause FROM and non empty GraphURI */
    @Test
    void testValidateQueryFalseForFromClauseAndGraphURI() {
        String query = "SELECT * FROM <" + graphUri + "> WHERE { ?s ?p ?o }";

        List<String> nonEmptyList = new ArrayList<String>();
        nonEmptyList.add(graphUri);

        SparqlHttpClient httpClient = new SparqlHttpClient(createCommandSpecObject(), serverUrl);

        /* Valid query with FROM clause and empty URI list  */
        httpClient.validateQuery(query, emptyList, emptyList);

        /* Invalid query with FROM clause and non empty URI list */
        assertThrows(
                IllegalArgumentException.class,
                () -> httpClient.validateQuery(query, nonEmptyList, emptyList));
    }

    /** Test that validateQuery fails for a WITH clause and non empty GraphURI */
    @Test
    void testValidateQueryFalseForWithClauseAndGraphURI() {
        String query = "WITH <" + graphUri + "> DELETE { ?s ?p ?o } WHERE { ?s ?p ?o }";

        List<String> nonEmptyList = new ArrayList<String>();
        nonEmptyList.add(graphUri);

        SparqlHttpClient httpClient = new SparqlHttpClient(createCommandSpecObject(), serverUrl);

        /* Valid query having WITH clause and empty URI list  */
        httpClient.validateQuery(query, emptyList, emptyList);

        /* Invalid query having WITH clause and non empty URI list */
        assertThrows(
                IllegalArgumentException.class,
                () -> httpClient.validateQuery(query, nonEmptyList, emptyList));
    }
}
