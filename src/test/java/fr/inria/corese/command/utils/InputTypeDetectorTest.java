package fr.inria.corese.command.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import fr.inria.corese.command.utils.InputTypeDetector.InputType;

public class InputTypeDetectorTest {

    @Test
    @DisplayName("Detect valid SPARQL query")
    void detectValidSparql() {
        String query = "SELECT * WHERE { ?s ?p ?o }";
        assertEquals(InputType.SPARQL, InputTypeDetector.detect(query));
    }

    @Test
    @DisplayName("Detect SPARQL with complex characters")
    void detectComplexSparql() {
        String query = "PREFIX : <http://example.com/>\nASK { :x ?p ?o }";
        assertEquals(InputType.SPARQL, InputTypeDetector.detect(query));
    }

    @Test
    @DisplayName("Detect invalid SPARQL with typo")
    void detectTypoSparql() {
        String query = "SELEeCT * WHERE { ?s ?p ?o }";
        assertEquals(InputType.SPARQL, InputTypeDetector.detect(query));
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
    @DisplayName("Detect local file path with extension")
    void detectFilePath() {
        String path = "data/query.rq";
        assertEquals(InputType.FILE_PATH, InputTypeDetector.detect(path));
    }

    @Test
    @DisplayName("Detect absolute path")
    void detectAbsolutePath() {
        String path = "/home/user/query.rq";
        assertEquals(InputType.FILE_PATH, InputTypeDetector.detect(path));
    }

    @Test
    @DisplayName("Detect file path without extension")
    void detectFilePathWithoutExtension() {
        String path = "queryWithoutExtension";
        assertEquals(InputType.FILE_PATH, InputTypeDetector.detect(path));
    }

    @Test
    @DisplayName("Detect text with keywords but clearly not SPARQL")
    void detectFilenameWithSparqlKeywords() {
        String path = "select_data.ttl";
        assertEquals(InputType.FILE_PATH, InputTypeDetector.detect(path));
    }

    @Test
    @DisplayName("Detect complex file name with special characters")
    void detectFilenameWithSpecialCharacters() {
        String path = "queries/select_{lang}?pretty";
        assertEquals(InputType.FILE_PATH, InputTypeDetector.detect(path));
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

    @Test
    @DisplayName("Detect multiline SPARQL")
    void detectMultilineSparql() {
        String query = "PREFIX : <http://example.com/>\n" +
                "SELECT * WHERE {\n" +
                "  ?s ?p ?o .\n" +
                "} LIMIT 10";
        assertEquals(InputType.SPARQL, InputTypeDetector.detect(query));
    }

    @Test
    @DisplayName("Detect path that doesn't exist but is valid")
    void detectNonExistentButValidPath() {
        String path = "some/futureQueryFile";
        assertEquals(InputType.FILE_PATH, InputTypeDetector.detect(path));
    }
}
