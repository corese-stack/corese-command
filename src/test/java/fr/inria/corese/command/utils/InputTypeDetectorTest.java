package fr.inria.corese.command.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import fr.inria.corese.command.utils.InputTypeDetector.InputType;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

class InputTypeDetectorTest {

    // Data provider for SPARQL detection tests
    static Stream<Arguments> sparqlInputProvider() {
        return Stream.of(
                Arguments.of("SELECT * WHERE { ?s ?p ?o }", "Detect valid SPARQL query"),
                Arguments.of("PREFIX : <http://example.com/>\nASK { :x ?p ?o }", "Detect SPARQL with complex characters"),
                Arguments.of("SELEeCT * WHERE { ?s ?p ?o }", "Detect invalid SPARQL with typo"),
                Arguments.of("""
                        PREFIX : <http://example.com/>
                            SELECT * WHERE {
                                ?s ?p ?o .
                            } LIMIT 10
                        """, "Detect multiline SPARQL"),
                Arguments.of("PREFIX ex: <http://example.org/> SELECT * WHERE { ex:Alice a ex:Person }", "Detect single-line SPARQL that embeds a URI")
        );
    }

    // Data provider for file path detection tests
    static Stream<Arguments> filePathProvider() {
        return Stream.of(
                Arguments.of("data/query.rq", "Detect local file path with extension"),
                Arguments.of("/home/user/query.rq", "Detect absolute path"),
                Arguments.of("queryWithoutExtension", "Detect file path without extension"),
                Arguments.of("select_data.ttl", "Detect text with keywords but clearly not SPARQL"),
                Arguments.of("queries/select_{lang}?pretty", "Detect complex file name with special characters"),
                Arguments.of("some/futureQueryFile", "Detect path that doesn't exist but is valid"),
                Arguments.of("data/my query.rq", "Detect file path with spaces")
        );
    }

    @ParameterizedTest
    @MethodSource("sparqlInputProvider")
    @DisplayName("SPARQL detection tests")
    void detectSparqlQueries(String input, String description) {
        assertEquals(InputType.SPARQL, InputTypeDetector.detect(input), description);
    }

    @ParameterizedTest
    @MethodSource("filePathProvider")
    @DisplayName("File path detection tests")
    void detectFilePaths(String input, String description) {
        assertEquals(InputType.FILE_PATH, InputTypeDetector.detect(input), description);
    }

    @Test
    @DisplayName("Detect clear URL")
    void detectValidUrl() {
        String url = "http://example.org/data.ttl";
        assertEquals(InputType.URL, InputTypeDetector.detect(url));
    }

    @Test
    @DisplayName("Detect invalid URL with SPARQL content")
    void detectInvalidUrl() {
        String broken = "SELECT * WHERE {?s ?p ?o}";
        assertEquals(InputType.SPARQL, InputTypeDetector.detect(broken));
    }

    @Test
    @DisplayName("Detect empty input")
    void detectEmptyInput() {
        assertEquals(InputType.UNKNOWN, InputTypeDetector.detect(""));
    }

    @Test
    @DisplayName("Detect null input")
    void detectNullInput() {
        assertEquals(InputType.UNKNOWN, InputTypeDetector.detect(null));
    }
}
