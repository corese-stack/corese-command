package fr.inria.corese.command.utils.http;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import fr.inria.corese.command.exceptions.SparqlExecutionException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import picocli.CommandLine;
import picocli.CommandLine.Model.CommandSpec;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class SparqlHttpClientTest {

    private static final String SERVER_URL = "http://localhost:8080/sparql";
    private static final String GRAPH_URI = "http://example.org/graph";
    private static final List<String> EMPTY_LIST = Collections.emptyList();

    private SparqlHttpClient httpClient;

    @BeforeEach
    void setUp() {
        CommandSpec commandSpec = createMockedCommandSpec();
        httpClient = new SparqlHttpClient(commandSpec, SERVER_URL);
    }

    /**
     * Creates a mocked CommandSpec instance for testing.
     * 
     * This mock provides a valid CommandSpec without requiring reflection,
     * making the tests cleaner and more maintainable. The CommandSpec is
     * only needed for the SparqlHttpPrinter to access output writers,
     * which are not used in these validation tests.
     *
     * @return A mocked CommandSpec instance
     */
    private CommandSpec createMockedCommandSpec() {
        CommandSpec commandSpec = mock(CommandSpec.class);
        CommandLine commandLine = mock(CommandLine.class);
        
        // Mock the necessary methods
        when(commandSpec.commandLine()).thenReturn(commandLine);
        when(commandLine.getErr()).thenReturn(new PrintWriter(new StringWriter()));
        when(commandLine.getOut()).thenReturn(new PrintWriter(new StringWriter()));
        
        return commandSpec;
    }

    /**
     * Creates a list containing a single graph URI for testing.
     *
     * @return A list with one graph URI
     */
    private List<String> createNonEmptyGraphList() {
        List<String> graphList = new ArrayList<>();
        graphList.add(GRAPH_URI);
        return graphList;
    }

    @Test
    void testValidateQuery_withNullQuery_shouldThrowException() {
        assertThrows(
                SparqlExecutionException.class,
                () -> httpClient.validateQuery(null, EMPTY_LIST, EMPTY_LIST),
                "Null query should throw SparqlExecutionException");
    }

    @Test
    void testValidateQuery_withEmptyQuery_shouldThrowException() {
        assertThrows(
                SparqlExecutionException.class,
                () -> httpClient.validateQuery("", EMPTY_LIST, EMPTY_LIST),
                "Empty query should throw SparqlExecutionException");
    }

    @Test
    void testValidateQuery_withValidQuery_shouldPass() {
        String validQuery = "SELECT * WHERE { ?s ?p ?o }";
        
        assertDoesNotThrow(
                () -> httpClient.validateQuery(validQuery, EMPTY_LIST, EMPTY_LIST),
                "Valid query should not throw exception");
    }

    @Test
    void testValidateQuery_withSyntaxError_shouldThrowException() {
        String invalidQuery = "SELECT * WHERE";
        
        assertThrows(
                SparqlExecutionException.class,
                () -> httpClient.validateQuery(invalidQuery, EMPTY_LIST, EMPTY_LIST),
                "Query with syntax error should throw SparqlExecutionException");
    }

    @Test
    void testValidateQuery_withUpdateQueryAndAutoMethod_shouldPass() {
        String updateQuery = "DELETE WHERE { ?s ?p ?o }";
        
        assertDoesNotThrow(
                () -> httpClient.validateQuery(updateQuery, EMPTY_LIST, EMPTY_LIST),
                "Update query with auto-detected method should not throw exception");
    }

    @Test
    void testValidateQuery_withUpdateQueryAndPostMethod_shouldPass() {
        String updateQuery = "DELETE WHERE { ?s ?p ?o }";
        httpClient.setRequestMethod(HttpRequestMethod.POST_URLENCODED);
        
        assertDoesNotThrow(
                () -> httpClient.validateQuery(updateQuery, EMPTY_LIST, EMPTY_LIST),
                "Update query with POST method should not throw exception");
    }

    @Test
    void testValidateQuery_withUpdateQueryAndGetMethod_shouldThrowException() {
        String updateQuery = "DELETE WHERE { ?s ?p ?o }";
        httpClient.setRequestMethod(HttpRequestMethod.GET);
        
        assertThrows(
                SparqlExecutionException.class,
                () -> httpClient.validateQuery(updateQuery, EMPTY_LIST, EMPTY_LIST),
                "Update query with GET method should throw SparqlExecutionException");
    }

    @Test
    void testValidateQuery_withFromClauseAndEmptyGraphList_shouldPass() {
        String queryWithFromClause = "SELECT * FROM <" + GRAPH_URI + "> WHERE { ?s ?p ?o }";
        
        assertDoesNotThrow(
                () -> httpClient.validateQuery(queryWithFromClause, EMPTY_LIST, EMPTY_LIST),
                "Query with FROM clause and empty graph list should not throw exception");
    }

    @Test
    void testValidateQuery_withFromClauseAndNonEmptyGraphList_shouldThrowException() {
        String queryWithFromClause = "SELECT * FROM <" + GRAPH_URI + "> WHERE { ?s ?p ?o }";
        List<String> graphList = createNonEmptyGraphList();
        
        assertThrows(
                SparqlExecutionException.class,
                () -> httpClient.validateQuery(queryWithFromClause, graphList, EMPTY_LIST),
                "Query with FROM clause and non-empty graph list should throw SparqlExecutionException");
    }

    @Test
    void testValidateQuery_withWithClauseAndEmptyGraphList_shouldPass() {
        String queryWithWithClause = "WITH <" + GRAPH_URI + "> DELETE { ?s ?p ?o } WHERE { ?s ?p ?o }";
        
        assertDoesNotThrow(
                () -> httpClient.validateQuery(queryWithWithClause, EMPTY_LIST, EMPTY_LIST),
                "Query with WITH clause and empty graph list should not throw exception");
    }

    @Test
    void testValidateQuery_withWithClauseAndNonEmptyGraphList_shouldThrowException() {
        String queryWithWithClause = "WITH <" + GRAPH_URI + "> DELETE { ?s ?p ?o } WHERE { ?s ?p ?o }";
        List<String> graphList = createNonEmptyGraphList();
        
        assertThrows(
                SparqlExecutionException.class,
                () -> httpClient.validateQuery(queryWithWithClause, graphList, EMPTY_LIST),
                "Query with WITH clause and non-empty graph list should throw SparqlExecutionException");
    }
}
