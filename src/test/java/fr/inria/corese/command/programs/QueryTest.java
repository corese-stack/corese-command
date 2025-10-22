package fr.inria.corese.command.programs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.inria.corese.command.utils.coresecorowrapper.CoreseRdfGraph;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import picocli.CommandLine;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Stream;

class QueryTest {

    private Query query = new Query();
    private CommandLine cmd = new CommandLine(query);

    private StringWriter out = new StringWriter();
    private StringWriter err = new StringWriter();

    Path inputPath;
    Path referencesPath;
    Path resultPath;
    Path queriesPath;

    QueryTest() throws URISyntaxException {
        this.inputPath =
                Paths.get(
                        QueryTest.class
                                .getResource("/fr/inria/corese/command/programs/query/input/")
                                .toURI());

        this.referencesPath =
                Paths.get(
                        QueryTest.class
                                .getResource("/fr/inria/corese/command/programs/query/references/")
                                .toURI());

        this.resultPath =
                Paths.get(
                        QueryTest.class
                                .getResource("/fr/inria/corese/command/programs/query/results")
                                .toURI());

        this.queriesPath =
                Paths.get(
                        QueryTest.class
                                .getResource("/fr/inria/corese/command/programs/query/queries/")
                                .toURI());
    }

    @BeforeEach
    void setUp() {
        PrintWriter outputWriter = new PrintWriter(this.out);
        PrintWriter errorWriter = new PrintWriter(this.err);
        cmd.setOut(outputWriter);
        cmd.setErr(errorWriter);
    }

    private boolean compareFiles(String filePath1, String filePath2) {
        if (filePath1.endsWith(".ttl")
                || filePath1.endsWith(".rdf")
                || filePath1.endsWith(".trig")
                || filePath1.endsWith(".nt")
                || filePath1.endsWith(".nq")
                || filePath1.endsWith(".jsonld")) {
            return compareFilesRdfGraph(filePath1, filePath2);
        } else {
            return comapreFilesRaw(filePath1, filePath2);
        }
    }

    private boolean comapreFilesRaw(String filePath1, String filePath2) {

        // Load in string content of both files
        String content1 = "";
        String content2 = "";

        try {
            content1 = new String(java.nio.file.Files.readAllBytes(Paths.get(filePath1)));
            content2 = new String(java.nio.file.Files.readAllBytes(Paths.get(filePath2)));

        } catch (IOException e) {
            e.printStackTrace();
        }

        // Sort line by line the content of the files
        content1 = sortByLines(content1);
        content2 = sortByLines(content2);

        // Clean the content of the files
        content1 = content1.replaceAll("\\s+", "").trim();
        content2 = content2.replaceAll("\\s+", "").trim();

        // Compare the content of the two files
        return content1.equals(content2) && content1 != "";
    }

    private String sortByLines(String content) {
        String[] lines = content.split("\n");
        Arrays.sort(lines);
        return String.join("\n", lines);
    }

    private boolean compareFilesRdfGraph(String filePath1, String filePath2) {
        // Canonicalize RDF content
        String canonicallFile1 = canonicalize(filePath1);
        String canonicallFile2 = canonicalize(filePath2);

        return canonicallFile1.equals(canonicallFile2) && canonicallFile1 != "";
    }

    private String canonicalize(String filePath) {

        // Load RDF content into a Graph
        CoreseRdfGraph graph = new CoreseRdfGraph();
        graph.load(filePath, "");

        // Return Canonical RDF content
        return graph.canonicalRdf10Format();
    }

    // Data source for invalid Select output formats
    static Stream<Arguments> invalidSelectFormatProvider() {
        return Stream.of(
                Arguments.of(
                        "rdfxml",
                        "Error: rdfxml is not a valid output format for select or ask requests."),
                Arguments.of(
                        "turtle",
                        "Error: turtle is not a valid output format for select or ask requests."),
                Arguments.of(
                        "trig",
                        "Error: trig is not a valid output format for select or ask requests."),
                Arguments.of(
                        "jsonld",
                        "Error: jsonld is not a valid output format for select or ask requests."));
    }

    @ParameterizedTest
    @MethodSource("invalidSelectFormatProvider")
    void testSelectInvalidFormat(String format, String expectedError) {
        String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
        String pathResBeatlesSelect =
                resultPath
                        .resolve("select")
                        .resolve("beatles-select-" + format + ".output")
                        .toString();
        String pathQueryBeatlesAlbum =
                queriesPath.resolve("select").resolve("beatlesAlbums.rq").toString();

        int exitCode =
                cmd.execute(
                        "-i",
                        pathInpBeatlesTTL,
                        "-r",
                        format,
                        "-o",
                        pathResBeatlesSelect,
                        "-q",
                        pathQueryBeatlesAlbum);

        String actualOutput = err.toString().trim();

        assertEquals(1, exitCode);
        assertEquals("", out.toString());
        assertTrue(actualOutput.contains(expectedError));
        assertNotEquals("", actualOutput);
    }

    @Test
    void testSelectBidingXml() {
        String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
        String pathRefBeatlesSelectXml =
                referencesPath.resolve("select").resolve("beatles-select-bidingxml.xml").toString();
        String pathResBeatlesSelectXml =
                resultPath.resolve("select").resolve("beatles-select-xml.xml").toString();
        String pathQueryBeatlesAlbum =
                queriesPath.resolve("select").resolve("beatlesAlbums.rq").toString();

        int exitCode =
                cmd.execute(
                        "-i",
                        pathInpBeatlesTTL,
                        "-r",
                        "xml",
                        "-o",
                        pathResBeatlesSelectXml,
                        "-q",
                        pathQueryBeatlesAlbum);

        assertEquals(0, exitCode);
        assertEquals("", out.toString());
        assertEquals("", err.toString());
        assertTrue(compareFiles(pathRefBeatlesSelectXml, pathResBeatlesSelectXml));
    }

    @Test
    void testSelectBidingJson() {
        String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
        String pathRefBeatlesSelectJson =
                referencesPath
                        .resolve("select")
                        .resolve("beatles-select-bidingjson.json")
                        .toString();
        String pathResBeatlesSelectJson =
                resultPath.resolve("select").resolve("beatles-select-json.json").toString();
        String pathQueryBeatlesAlbum =
                queriesPath.resolve("select").resolve("beatlesAlbums.rq").toString();

        int exitCode =
                cmd.execute(
                        "-i",
                        pathInpBeatlesTTL,
                        "-r",
                        "json",
                        "-o",
                        pathResBeatlesSelectJson,
                        "-q",
                        pathQueryBeatlesAlbum);

        assertEquals(0, exitCode);
        assertEquals("", out.toString());
        assertEquals("", err.toString());
        assertTrue(compareFiles(pathRefBeatlesSelectJson, pathResBeatlesSelectJson));
    }

    @Test
    void testSelectBidingCsv() {
        String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
        String pathRefBeatlesSelectCsv =
                referencesPath.resolve("select").resolve("beatles-select-bidingcsv.csv").toString();
        String pathResBeatlesSelectCsv =
                resultPath.resolve("select").resolve("beatles-select-csv.csv").toString();
        String pathQueryBeatlesAlbum =
                queriesPath.resolve("select").resolve("beatlesAlbums.rq").toString();

        int exitCode =
                cmd.execute(
                        "-i",
                        pathInpBeatlesTTL,
                        "-r",
                        "csv",
                        "-o",
                        pathResBeatlesSelectCsv,
                        "-q",
                        pathQueryBeatlesAlbum);

        assertEquals(0, exitCode);
        assertEquals("", out.toString());
        assertEquals("", err.toString());
        assertTrue(compareFiles(pathRefBeatlesSelectCsv, pathResBeatlesSelectCsv));
    }

    @Test
    void testSelectBidingTSV() {
        String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
        String pathRefBeatlesSelectTsv =
                referencesPath.resolve("select").resolve("beatles-select-bidingtsv.tsv").toString();
        String pathResBeatlesSelectTsv =
                resultPath.resolve("select").resolve("beatles-select-tsv.tsv").toString();
        String pathQueryBeatlesAlbum =
                queriesPath.resolve("select").resolve("beatlesAlbums.rq").toString();

        int exitCode =
                cmd.execute(
                        "-i",
                        pathInpBeatlesTTL,
                        "-r",
                        "tsv",
                        "-o",
                        pathResBeatlesSelectTsv,
                        "-q",
                        pathQueryBeatlesAlbum);

        assertEquals(0, exitCode);
        assertEquals("", out.toString());
        assertEquals("", err.toString());
        assertTrue(compareFiles(pathRefBeatlesSelectTsv, pathResBeatlesSelectTsv));
    }

    @Test
    void testSelectMarkdown() {
        String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
        String pathRefBeatlesSelectMarkdown =
                referencesPath
                        .resolve("select")
                        .resolve("beatles-select-bidingmarkdown.md")
                        .toString();
        String pathResBeatlesSelectMarkdown =
                resultPath.resolve("select").resolve("beatles-select-bidingmarkdown.md").toString();
        String pathQueryBeatlesAlbum =
                queriesPath.resolve("select").resolve("beatlesAlbums.rq").toString();

        int exitCode =
                cmd.execute(
                        "-i",
                        pathInpBeatlesTTL,
                        "-r",
                        "markdown",
                        "-o",
                        pathResBeatlesSelectMarkdown,
                        "-q",
                        pathQueryBeatlesAlbum);

        assertEquals(0, exitCode);
        assertEquals("", out.toString());
        assertEquals("", err.toString());
        assertTrue(compareFiles(pathRefBeatlesSelectMarkdown, pathResBeatlesSelectMarkdown));
    }

    // Data source for invalid Ask output formats
    static Stream<Arguments> invalidAskFormatProvider() {
        return Stream.of(
                Arguments.of(
                        "rdfxml",
                        "beatlesTrue.rq",
                        "Error: rdfxml is not a valid output format for select or ask requests."),
                Arguments.of(
                        "rdfxml",
                        "beatlesFalse.rq",
                        "Error: rdfxml is not a valid output format for select or ask requests."),
                Arguments.of(
                        "turtle",
                        "beatlesTrue.rq",
                        "Error: turtle is not a valid output format for select or ask requests."),
                Arguments.of(
                        "turtle",
                        "beatlesFalse.rq",
                        "Error: turtle is not a valid output format for select or ask requests."),
                Arguments.of(
                        "trig",
                        "beatlesTrue.rq",
                        "Error: trig is not a valid output format for select or ask requests."),
                Arguments.of(
                        "trig",
                        "beatlesFalse.rq",
                        "Error: trig is not a valid output format for select or ask requests."),
                Arguments.of(
                        "jsonld",
                        "beatlesTrue.rq",
                        "Error: jsonld is not a valid output format for select or ask requests."),
                Arguments.of(
                        "jsonld",
                        "beatlesFalse.rq",
                        "Error: jsonld is not a valid output format for select or ask requests."));
    }

    @ParameterizedTest
    @MethodSource("invalidAskFormatProvider")
    void testAskInvalidFormat(String format, String queryFile, String expectedError) {
        String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
        String pathResAsk =
                resultPath.resolve("ask").resolve("beatles-ask-" + format + ".output").toString();
        String pathQueryAsk = queriesPath.resolve("ask").resolve(queryFile).toString();

        int exitCode =
                cmd.execute(
                        "-i",
                        pathInpBeatlesTTL,
                        "-r",
                        format,
                        "-o",
                        pathResAsk,
                        "-q",
                        pathQueryAsk);

        String actualOutput = err.toString().trim();

        assertEquals(1, exitCode);
        assertEquals("", out.toString());
        assertTrue(actualOutput.contains(expectedError));
        assertNotEquals("", actualOutput);
    }

    @Test
    void testAskTrueBidingXml() {
        String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
        String pathRefBeatlesAskXml =
                referencesPath.resolve("ask").resolve("beatles-ask-bidingxml-true.xml").toString();
        String pathResBeatlesAskTrue =
                resultPath.resolve("ask").resolve("beatles-ask-bidingxml-true.xml").toString();
        String pathQueryBeatlesAskTrue =
                queriesPath.resolve("ask").resolve("beatlesTrue.rq").toString();

        int exitCode =
                cmd.execute(
                        "-i",
                        pathInpBeatlesTTL,
                        "-r",
                        "xml",
                        "-o",
                        pathResBeatlesAskTrue,
                        "-q",
                        pathQueryBeatlesAskTrue);

        assertEquals(0, exitCode);
        assertEquals("", out.toString());
        assertEquals("", err.toString());
        assertTrue(compareFiles(pathRefBeatlesAskXml, pathResBeatlesAskTrue));
    }

    @Test
    void testAskFalseBidingXml() {
        String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
        String pathRefBeatlesAskXml =
                referencesPath.resolve("ask").resolve("beatles-ask-bidingxml-false.xml").toString();
        String pathResBeatlesAskFalse =
                resultPath.resolve("ask").resolve("beatles-ask-bidingxml-false.xml").toString();
        String pathQueryBeatlesAskFalse =
                queriesPath.resolve("ask").resolve("beatlesFalse.rq").toString();

        int exitCode =
                cmd.execute(
                        "-i",
                        pathInpBeatlesTTL,
                        "-r",
                        "xml",
                        "-o",
                        pathResBeatlesAskFalse,
                        "-q",
                        pathQueryBeatlesAskFalse);

        assertEquals(0, exitCode);
        assertEquals("", out.toString());
        assertEquals("", err.toString());
        assertTrue(compareFiles(pathRefBeatlesAskXml, pathResBeatlesAskFalse));
    }

    @Test
    void testAskTrueBidingJson() {
        String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
        String pathRefBeatlesAskJSON =
                referencesPath
                        .resolve("ask")
                        .resolve("beatles-ask-bidingjson-true.json")
                        .toString();
        String pathResBeatlesAskTrue =
                resultPath.resolve("ask").resolve("beatles-ask-bidingjson-true.json").toString();
        String pathQueryBeatlesAskTrue =
                queriesPath.resolve("ask").resolve("beatlesTrue.rq").toString();

        int exitCode =
                cmd.execute(
                        "-i",
                        pathInpBeatlesTTL,
                        "-r",
                        "json",
                        "-o",
                        pathResBeatlesAskTrue,
                        "-q",
                        pathQueryBeatlesAskTrue);

        assertEquals(0, exitCode);
        assertEquals("", out.toString());
        assertEquals("", err.toString());
        assertTrue(compareFiles(pathRefBeatlesAskJSON, pathResBeatlesAskTrue));
    }

    @Test
    void testAskFalseBidingJson() {
        String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
        String pathRefBeatlesAskJSON =
                referencesPath
                        .resolve("ask")
                        .resolve("beatles-ask-bidingjson-false.json")
                        .toString();
        String pathResBeatlesAskFalse =
                resultPath.resolve("ask").resolve("beatles-ask-bidingjson-false.json").toString();
        String pathQueryBeatlesAskFalse =
                queriesPath.resolve("ask").resolve("beatlesFalse.rq").toString();

        int exitCode =
                cmd.execute(
                        "-i",
                        pathInpBeatlesTTL,
                        "-r",
                        "json",
                        "-o",
                        pathResBeatlesAskFalse,
                        "-q",
                        pathQueryBeatlesAskFalse);

        assertEquals(0, exitCode);
        assertEquals("", out.toString());
        assertEquals("", err.toString());
        assertTrue(compareFiles(pathRefBeatlesAskJSON, pathResBeatlesAskFalse));
    }

    @Test
    void testAskTrueBidingCsv() {
        String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
        String pathRefBeatlesAskCSV =
                referencesPath.resolve("ask").resolve("beatles-ask-bidingcsv-true.csv").toString();
        String pathResBeatlesAskTrue =
                resultPath.resolve("ask").resolve("beatles-ask-bidingcsv-true.csv").toString();
        String pathQueryBeatlesAskTrue =
                queriesPath.resolve("ask").resolve("beatlesTrue.rq").toString();

        int exitCode =
                cmd.execute(
                        "-i",
                        pathInpBeatlesTTL,
                        "-r",
                        "csv",
                        "-o",
                        pathResBeatlesAskTrue,
                        "-q",
                        pathQueryBeatlesAskTrue);

        assertEquals(0, exitCode);
        assertEquals("", out.toString());
        assertEquals("", err.toString());
        assertTrue(compareFiles(pathRefBeatlesAskCSV, pathResBeatlesAskTrue));
    }

    @Test
    void testAskFalseBidingCsv() {
        String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
        String pathRefBeatlesAskCSV =
                referencesPath.resolve("ask").resolve("beatles-ask-bidingcsv-false.csv").toString();
        String pathResBeatlesAskFalse =
                resultPath.resolve("ask").resolve("beatles-ask-bidingcsv-false.csv").toString();
        String pathQueryBeatlesAskFalse =
                queriesPath.resolve("ask").resolve("beatlesFalse.rq").toString();

        int exitCode =
                cmd.execute(
                        "-i",
                        pathInpBeatlesTTL,
                        "-r",
                        "csv",
                        "-o",
                        pathResBeatlesAskFalse,
                        "-q",
                        pathQueryBeatlesAskFalse);

        assertEquals(0, exitCode);
        assertEquals("", out.toString());
        assertEquals("", err.toString());
        assertTrue(compareFiles(pathRefBeatlesAskCSV, pathResBeatlesAskFalse));
    }

    @Test
    void testAskTrueBidingTsv() {
        String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
        String pathRefBeatlesAskTSV =
                referencesPath.resolve("ask").resolve("beatles-ask-bidingtsv-true.tsv").toString();
        String pathResBeatlesAskTrue =
                resultPath.resolve("ask").resolve("beatles-ask-bidingtsv-true.tsv").toString();
        String pathQueryBeatlesAskTrue =
                queriesPath.resolve("ask").resolve("beatlesTrue.rq").toString();

        int exitCode =
                cmd.execute(
                        "-i",
                        pathInpBeatlesTTL,
                        "-r",
                        "tsv",
                        "-o",
                        pathResBeatlesAskTrue,
                        "-q",
                        pathQueryBeatlesAskTrue);

        assertEquals(0, exitCode);
        assertEquals("", out.toString());
        assertEquals("", err.toString());
        assertTrue(compareFiles(pathRefBeatlesAskTSV, pathResBeatlesAskTrue));
    }

    @Test
    void testAskFalseBidingTsv() {
        String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
        String pathRefBeatlesAskTSV =
                referencesPath.resolve("ask").resolve("beatles-ask-bidingtsv-false.tsv").toString();
        String pathResBeatlesAskFalse =
                resultPath.resolve("ask").resolve("beatles-ask-bidingtsv-false.tsv").toString();
        String pathQueryBeatlesAskFalse =
                queriesPath.resolve("ask").resolve("beatlesFalse.rq").toString();

        int exitCode =
                cmd.execute(
                        "-i",
                        pathInpBeatlesTTL,
                        "-r",
                        "tsv",
                        "-o",
                        pathResBeatlesAskFalse,
                        "-q",
                        pathQueryBeatlesAskFalse);

        assertEquals(0, exitCode);
        assertEquals("", out.toString());
        assertEquals("", err.toString());
        assertTrue(compareFiles(pathRefBeatlesAskTSV, pathResBeatlesAskFalse));
    }

    @Test
    void testAskTrueBidingMarkdown() {
        String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
        String pathRefBeatlesAskMarkdown =
                referencesPath
                        .resolve("ask")
                        .resolve("beatles-ask-bidingmarkdown-true.md")
                        .toString();
        String pathResBeatlesAskTrue =
                resultPath.resolve("ask").resolve("beatles-ask-bidingmarkdown-true.md").toString();
        String pathQueryBeatlesAskTrue =
                queriesPath.resolve("ask").resolve("beatlesTrue.rq").toString();

        int exitCode =
                cmd.execute(
                        "-i",
                        pathInpBeatlesTTL,
                        "-r",
                        "markdown",
                        "-o",
                        pathResBeatlesAskTrue,
                        "-q",
                        pathQueryBeatlesAskTrue);

        assertEquals(0, exitCode);
        assertEquals("", out.toString());
        assertEquals("", err.toString());
        assertTrue(compareFiles(pathRefBeatlesAskMarkdown, pathResBeatlesAskTrue));
    }

    @Test
    void testAskFalseBidingMarkdown() {
        String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
        String pathRefBeatlesAskMarkdown =
                referencesPath
                        .resolve("ask")
                        .resolve("beatles-ask-bidingmarkdown-false.md")
                        .toString();
        String pathResBeatlesAskFalse =
                resultPath.resolve("ask").resolve("beatles-ask-bidingmarkdown-false.md").toString();
        String pathQueryBeatlesAskFalse =
                queriesPath.resolve("ask").resolve("beatlesFalse.rq").toString();

        int exitCode =
                cmd.execute(
                        "-i",
                        pathInpBeatlesTTL,
                        "-r",
                        "markdown",
                        "-o",
                        pathResBeatlesAskFalse,
                        "-q",
                        pathQueryBeatlesAskFalse);

        assertEquals(0, exitCode);
        assertEquals("", out.toString());
        assertEquals("", err.toString());
        assertTrue(compareFiles(pathRefBeatlesAskMarkdown, pathResBeatlesAskFalse));
    }

    // Data source for valid Insert/Delete/Construct/Describe RDF output formats
    static Stream<Arguments> validRdfFormatProvider() {
        return Stream.of(
                Arguments.of("rdfxml"),
                Arguments.of("turtle"),
                Arguments.of("trig"),
                Arguments.of("jsonld"));
    }

    @ParameterizedTest
    @MethodSource("validRdfFormatProvider")
    void testInsertValidFormat(String format) {
        String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
        String extension =
                format.equals("rdfxml") ? "xml" : (format.equals("jsonld") ? "jsonld" : format);
        String pathRefBeatlesInsert =
                referencesPath
                        .resolve("insert")
                        .resolve("beatles-insert-" + format + "." + extension)
                        .toString();
        String pathResBeatlesInsert =
                resultPath
                        .resolve("insert")
                        .resolve("beatles-insert-" + format + "." + extension)
                        .toString();
        String pathQueryBeatlesInsert =
                queriesPath.resolve("insert").resolve("beatlesInsertRock.rq").toString();

        int exitCode =
                cmd.execute(
                        "-i",
                        pathInpBeatlesTTL,
                        "-r",
                        format,
                        "-o",
                        pathResBeatlesInsert,
                        "-q",
                        pathQueryBeatlesInsert);

        assertEquals(0, exitCode);
        assertEquals("", out.toString());
        assertEquals("", err.toString());
        assertTrue(compareFiles(pathRefBeatlesInsert, pathResBeatlesInsert));
    }

    // Data source for invalid Insert/Delete/Construct/Describe output formats
    static Stream<Arguments> invalidInsertDeleteConstructDescribeFormatProvider() {
        return Stream.of(
                Arguments.of(
                        "xml",
                        "Error: xml is not a valid output format for insert, delete, describe or"
                            + " construct requests."),
                Arguments.of(
                        "json",
                        "Error: json is not a valid output format for insert, delete, describe or"
                            + " construct requests."),
                Arguments.of(
                        "csv",
                        "Error: csv is not a valid output format for insert, delete, describe or"
                            + " construct requests."),
                Arguments.of(
                        "tsv",
                        "Error: tsv is not a valid output format for insert, delete, describe or"
                            + " construct requests."),
                Arguments.of(
                        "markdown",
                        "Error: markdown is not a valid output format for insert, delete, describe"
                            + " or construct requests."));
    }

    @ParameterizedTest
    @MethodSource("invalidInsertDeleteConstructDescribeFormatProvider")
    void testInsertInvalidFormat(String format, String expectedError) {
        String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
        String pathResBeatlesInsert =
                resultPath.resolve("insert").resolve("beatles-insert." + format).toString();
        String pathQueryBeatlesInsert =
                queriesPath.resolve("insert").resolve("beatlesInsertRock.rq").toString();

        int exitCode =
                cmd.execute(
                        "-i",
                        pathInpBeatlesTTL,
                        "-r",
                        format,
                        "-o",
                        pathResBeatlesInsert,
                        "-q",
                        pathQueryBeatlesInsert);

        String actualOutput = err.toString().trim();

        assertEquals(1, exitCode);
        assertEquals("", out.toString());
        assertTrue(actualOutput.contains(expectedError));
        assertNotEquals("", actualOutput);
    }

    @ParameterizedTest
    @MethodSource("validRdfFormatProvider")
    void testInsertWhereValidFormat(String format) {
        String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
        String extension =
                format.equals("rdfxml") ? "xml" : (format.equals("jsonld") ? "jsonld" : format);
        String pathRefBeatlesInsertwhere =
                referencesPath
                        .resolve("insert-where")
                        .resolve("beatles-insertwhere-biding" + format + "." + extension)
                        .toString();
        String pathResBeatlesInsertwhere =
                resultPath
                        .resolve("insert-where")
                        .resolve("beatles-insertwhere-biding" + format + "." + extension)
                        .toString();
        String pathQueryBeatlesInsertwhere =
                queriesPath.resolve("insert-where").resolve("beatlesAge.rq").toString();

        int exitCode =
                cmd.execute(
                        "-i",
                        pathInpBeatlesTTL,
                        "-r",
                        format,
                        "-o",
                        pathResBeatlesInsertwhere,
                        "-q",
                        pathQueryBeatlesInsertwhere);

        assertEquals(0, exitCode);
        assertEquals("", out.toString());
        assertEquals("", err.toString());
        assertTrue(compareFiles(pathRefBeatlesInsertwhere, pathResBeatlesInsertwhere));
    }

    @ParameterizedTest
    @MethodSource("invalidInsertDeleteConstructDescribeFormatProvider")
    void testInsertWhereInvalidFormat(String format, String expectedError) {
        String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
        String pathResBeatlesInsertwhere =
                resultPath
                        .resolve("insert-where")
                        .resolve("beatles-insertwhere." + format)
                        .toString();
        String pathQueryBeatlesInsertwhere =
                queriesPath.resolve("insert-where").resolve("beatlesAge.rq").toString();

        int exitCode =
                cmd.execute(
                        "-i",
                        pathInpBeatlesTTL,
                        "-r",
                        format,
                        "-o",
                        pathResBeatlesInsertwhere,
                        "-q",
                        pathQueryBeatlesInsertwhere);

        String actualOutput = err.toString().trim();

        assertEquals(1, exitCode);
        assertEquals("", out.toString());
        assertTrue(actualOutput.contains(expectedError));
        assertNotEquals("", actualOutput);
    }

    // Delete tests - reusing validRdfFormatProvider and
    // invalidInsertDeleteConstructDescribeFormatProvider

    @ParameterizedTest
    @MethodSource("validRdfFormatProvider")
    void testDeleteValidFormat(String format) {
        String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
        String extension =
                format.equals("rdfxml") ? "xml" : (format.equals("jsonld") ? "jsonld" : format);
        String pathRefBeatlesDelete =
                referencesPath
                        .resolve("delete")
                        .resolve("beatles-delete-" + format + "." + extension)
                        .toString();
        String pathResBeatlesDelete =
                resultPath
                        .resolve("delete")
                        .resolve("beatles-delete-" + format + "." + extension)
                        .toString();
        String pathQueryBeatlesDelete =
                queriesPath.resolve("delete").resolve("deleteMcCartney.rq").toString();

        int exitCode =
                cmd.execute(
                        "-i",
                        pathInpBeatlesTTL,
                        "-r",
                        format,
                        "-o",
                        pathResBeatlesDelete,
                        "-q",
                        pathQueryBeatlesDelete);

        assertEquals(0, exitCode);
        assertEquals("", out.toString());
        assertEquals("", err.toString());
        assertTrue(compareFiles(pathRefBeatlesDelete, pathResBeatlesDelete));
    }

    @ParameterizedTest
    @MethodSource("invalidInsertDeleteConstructDescribeFormatProvider")
    void testDeleteInvalidFormat(String format, String expectedError) {
        String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
        String pathQueryBeatlesDelete =
                queriesPath.resolve("delete").resolve("deleteMcCartney.rq").toString();

        int exitCode =
                cmd.execute(
                        "-i",
                        pathInpBeatlesTTL,
                        "-r",
                        format,
                        "-o",
                        "references/delete/beatles-delete." + format,
                        "-q",
                        pathQueryBeatlesDelete);

        String actualOutput = err.toString().trim();

        assertEquals(1, exitCode);
        assertEquals("", out.toString());
        assertTrue(actualOutput.contains(expectedError));
        assertNotEquals("", actualOutput);
    }

    @ParameterizedTest
    @MethodSource("validRdfFormatProvider")
    void testDeleteWhereValidFormat(String format) {
        String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
        String extension =
                format.equals("rdfxml") ? "xml" : (format.equals("jsonld") ? "jsonld" : format);
        String pathRefBeatlesDelete =
                referencesPath
                        .resolve("delete-where")
                        .resolve("beatles-delete-where-" + format + "." + extension)
                        .toString();
        String pathResBeatlesDelete =
                resultPath
                        .resolve("delete-where")
                        .resolve("beatles-delete-where-" + format + "." + extension)
                        .toString();
        String pathQueryBeatlesDelete =
                queriesPath.resolve("delete-where").resolve("deleteLenon.rq").toString();

        int exitCode =
                cmd.execute(
                        "-i",
                        pathInpBeatlesTTL,
                        "-r",
                        format,
                        "-o",
                        pathResBeatlesDelete,
                        "-q",
                        pathQueryBeatlesDelete);

        assertEquals(0, exitCode);
        assertEquals("", out.toString());
        assertEquals("", err.toString());
        assertTrue(compareFiles(pathRefBeatlesDelete, pathResBeatlesDelete));
    }

    @ParameterizedTest
    @MethodSource("invalidInsertDeleteConstructDescribeFormatProvider")
    void testDeleteWhereInvalidFormat(String format, String expectedError) {
        String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
        String pathQueryBeatlesDelete =
                queriesPath.resolve("delete-where").resolve("deleteLenon.rq").toString();

        int exitCode =
                cmd.execute(
                        "-i",
                        pathInpBeatlesTTL,
                        "-r",
                        format,
                        "-o",
                        "references/delete-where/beatles-delete-where." + format,
                        "-q",
                        pathQueryBeatlesDelete);

        String actualOutput = err.toString().trim();

        assertEquals(1, exitCode);
        assertEquals("", out.toString());
        assertTrue(actualOutput.contains(expectedError));
        assertNotEquals("", actualOutput);
    }

    // Construct tests - reusing validRdfFormatProvider and
    // invalidInsertDeleteConstructDescribeFormatProvider

    @ParameterizedTest
    @MethodSource("validRdfFormatProvider")
    void testConstructValidFormat(String format) {
        String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
        String extension =
                format.equals("rdfxml") ? "xml" : (format.equals("jsonld") ? "jsonld" : format);
        String pathRefBeatlesConstruct =
                referencesPath
                        .resolve("construct")
                        .resolve("beatles-construct-" + format + "." + extension)
                        .toString();
        String pathResBeatlesConstruct =
                resultPath
                        .resolve("construct")
                        .resolve("beatles-construct-" + format + "." + extension)
                        .toString();
        String pathQueryBeatlesConstruct =
                queriesPath.resolve("construct").resolve("albumBeatles.rq").toString();

        int exitCode =
                cmd.execute(
                        "-i",
                        pathInpBeatlesTTL,
                        "-r",
                        format,
                        "-o",
                        pathResBeatlesConstruct,
                        "-q",
                        pathQueryBeatlesConstruct);

        assertEquals(0, exitCode);
        assertEquals("", out.toString());
        assertEquals("", err.toString());
        assertTrue(compareFiles(pathRefBeatlesConstruct, pathResBeatlesConstruct));
    }

    @ParameterizedTest
    @MethodSource("invalidInsertDeleteConstructDescribeFormatProvider")
    void testConstructInvalidFormat(String format, String expectedError) {
        String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
        String pathQueryBeatlesConstruct =
                queriesPath.resolve("construct").resolve("albumBeatles.rq").toString();

        int exitCode =
                cmd.execute(
                        "-i",
                        pathInpBeatlesTTL,
                        "-r",
                        format,
                        "-o",
                        "references/construct/beatles-construct-" + format + "." + format,
                        "-q",
                        pathQueryBeatlesConstruct);

        String actualOutput = err.toString().trim();

        assertEquals(1, exitCode);
        assertEquals("", out.toString());
        assertTrue(actualOutput.contains(expectedError));
        assertNotEquals("", actualOutput);
    }

    // Describe tests - reusing validRdfFormatProvider and
    // invalidInsertDeleteConstructDescribeFormatProvider

    @ParameterizedTest
    @MethodSource("validRdfFormatProvider")
    void testDescribeValidFormat(String format) {
        String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
        String extension =
                format.equals("rdfxml") ? "xml" : (format.equals("jsonld") ? "jsonld" : format);
        String pathRefBeatlesDescribe =
                referencesPath
                        .resolve("describe")
                        .resolve("beatles-describe-" + format + "." + extension)
                        .toString();
        String pathResBeatlesDescribe =
                resultPath
                        .resolve("describe")
                        .resolve("beatles-describe-" + format + "." + extension)
                        .toString();
        String pathQueryBeatlesDescribe =
                queriesPath.resolve("describe").resolve("describeBeatles.rq").toString();

        int exitCode =
                cmd.execute(
                        "-i",
                        pathInpBeatlesTTL,
                        "-r",
                        format,
                        "-o",
                        pathResBeatlesDescribe,
                        "-q",
                        pathQueryBeatlesDescribe);

        assertEquals(0, exitCode);
        assertEquals("", out.toString());
        assertEquals("", err.toString());
        assertTrue(compareFiles(pathRefBeatlesDescribe, pathResBeatlesDescribe));
    }

    @ParameterizedTest
    @MethodSource("invalidInsertDeleteConstructDescribeFormatProvider")
    void testDescribeInvalidFormat(String format, String expectedError) {
        String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
        String pathQueryBeatlesDescribe =
                queriesPath.resolve("describe").resolve("describeBeatles.rq").toString();

        int exitCode =
                cmd.execute(
                        "-i",
                        pathInpBeatlesTTL,
                        "-r",
                        format,
                        "-o",
                        "references/describe/beatles-describe-" + format + "." + format,
                        "-q",
                        pathQueryBeatlesDescribe);

        String actualOutput = err.toString().trim();

        assertEquals(1, exitCode);
        assertEquals("", out.toString());
        assertTrue(actualOutput.contains(expectedError));
        assertNotEquals("", actualOutput);
    }

    @Test
    void testExecute_WhenInputFileDoesNotExist_ThrowsException() {
        String nonExistentFile = "non_existent_file.rq";
        String pathQueryBeatlesConstruct =
                queriesPath.resolve("construct").resolve("albumBeatles.rq").toString();

        int exitCode =
                cmd.execute(
                        "-i",
                        nonExistentFile,
                        "-f",
                        "turtle",
                        "-o",
                        "output.ttl",
                        "-q",
                        pathQueryBeatlesConstruct);

        assertEquals(1, exitCode);
        assertTrue(err.toString().contains("Failed to open RDF data file"));
    }

    @Test
    void testExecute_WhenQueryFileDoesNotExist_ThrowsException() {
        String nonExistentQueryFile = "non_existent_query_file.rq";
        String validInputFile = inputPath.resolve("beatles.ttl").toString();

        int exitCode =
                cmd.execute(
                        "-i",
                        validInputFile,
                        "-f",
                        "turtle",
                        "-r",
                        "turtle",
                        "-o",
                        "output.ttl",
                        "-q",
                        nonExistentQueryFile);

        assertEquals(1, exitCode);
        assertTrue(err.toString().contains("Failed to open SPARQL query file"));
    }

    @Test
    void testExecute_WhenInvalidQuery_ThrowsException() {
        String validInputFile = inputPath.resolve("beatles.ttl").toString();

        int exitCode =
                cmd.execute(
                        "-i",
                        validInputFile,
                        "-f",
                        "turtle",
                        "-r",
                        "turtle",
                        "-o",
                        "output.ttl",
                        "-q",
                        "SERRORELECT * WHERE { ?s ?p ?o }");

        assertEquals(1, exitCode);
        assertTrue(err.toString().contains("Error when executing SPARQL query"));
    }

    @Test
    void testLoadUniqFiles() {
        String beatlesFile = inputPath.resolve("beatles.ttl").toString();
        String pathRefMultiFile = referencesPath.resolve("count").resolve("beatles.md").toString();
        String pathResMultiFile = resultPath.resolve("count").resolve("beatles.md").toString();

        int exitCode =
                cmd.execute(
                        "-i",
                        beatlesFile,
                        "-q",
                        "SELECT (COUNT(*) AS ?count) WHERE { ?s ?p ?o }",
                        "-o",
                        pathResMultiFile);

        assertEquals(0, exitCode);
        assertTrue(compareFiles(pathRefMultiFile, pathResMultiFile));
    }

    @Test
    void testLoadMutiFiles() {
        String beatlesFile = inputPath.resolve("beatles.ttl").toString();
        String cityFile = inputPath.resolve("city.ttl").toString();
        String pathRefMultiFile =
                referencesPath.resolve("count").resolve("beatles+city.md").toString();
        String pathResMultiFile = resultPath.resolve("count").resolve("beatles+city.md").toString();

        int exitCode =
                cmd.execute(
                        "-i",
                        beatlesFile,
                        cityFile,
                        "-q",
                        "SELECT (COUNT(*) AS ?count) WHERE { ?s ?p ?o }",
                        "-o",
                        pathResMultiFile);

        assertEquals(0, exitCode);
        assertTrue(compareFiles(pathRefMultiFile, pathResMultiFile));
    }

    @Test
    void testLoadMutiFilesRepertory() {
        String input = inputPath.toString();
        String pathRefMultiFile =
                referencesPath.resolve("count").resolve("repertory.md").toString();
        String pathResMultiFile = resultPath.resolve("count").resolve("repertory.md").toString();

        int exitCode =
                cmd.execute(
                        "-i",
                        input,
                        "-q",
                        "SELECT (COUNT(*) AS ?count) WHERE { ?s ?p ?o }",
                        "-o",
                        pathResMultiFile);

        assertEquals(0, exitCode);
        assertTrue(compareFiles(pathRefMultiFile, pathResMultiFile));
    }

    @Test
    void testLoadMutiFilesRepertoryRecursive() {
        String input = inputPath.toString();
        String pathRefMultiFile =
                referencesPath.resolve("count").resolve("repertoryRecursive.md").toString();
        String pathResMultiFile =
                resultPath.resolve("count").resolve("repertoryRecursive.md").toString();

        int exitCode =
                cmd.execute(
                        "-i",
                        input,
                        "-q",
                        "SELECT (COUNT(*) AS ?count) WHERE { ?s ?p ?o }",
                        "-o",
                        pathResMultiFile,
                        "-R");

        assertEquals(0, exitCode);
        assertTrue(compareFiles(pathRefMultiFile, pathResMultiFile));
    }

    @Test
    void testLoadFromUrl() {
        String rdfData = "https://files.inria.fr/corese/data/unit-test/beatles.ttl";
        String sparqlQuery = "https://files.inria.fr/corese/data/unit-test/spo.rq";

        String pathRefMultiFile = referencesPath.resolve("select").resolve("url.md").toString();
        String pathResMultiFile = resultPath.resolve("select").resolve("url.md").toString();

        int exitCode = cmd.execute("-i", rdfData, "-q", sparqlQuery, "-o", pathResMultiFile);

        assertEquals(0, exitCode);
        assertTrue(compareFiles(pathRefMultiFile, pathResMultiFile));
    }
}
