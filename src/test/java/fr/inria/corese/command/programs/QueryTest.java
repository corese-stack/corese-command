package fr.inria.corese.command.programs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.print.CanonicalRdf10Format;
import picocli.CommandLine;

public class QueryTest {

        private Query query = new Query();
        private CommandLine cmd = new CommandLine(query);

        private StringWriter out = new StringWriter();
        private StringWriter err = new StringWriter();

        Path inputPath;
        Path referencesPath;
        Path resultPath;
        Path queriesPath;

        public QueryTest() throws URISyntaxException {
                this.inputPath = Paths.get(
                                QueryTest.class.getResource("/fr/inria/corese/command/programs/query/input/").toURI());

                this.referencesPath = Paths.get(QueryTest.class
                                .getResource("/fr/inria/corese/command/programs/query/references/").toURI());

                this.resultPath = Paths.get(
                                QueryTest.class.getResource("/fr/inria/corese/command/programs/query/results").toURI());

                this.queriesPath = Paths.get(QueryTest.class
                                .getResource("/fr/inria/corese/command/programs/query/queries/").toURI());
        }

        @BeforeEach
        public void setUp() {
                PrintWriter out = new PrintWriter(this.out);
                PrintWriter err = new PrintWriter(this.err);
                cmd.setOut(out);
                cmd.setErr(err);
        }

        public boolean compareFiles(String filePath1, String filePath2) throws IOException {
                // Canonicalize RDF content
                String canonicallFile1 = canonicalize(filePath1);
                String canonicallFile2 = canonicalize(filePath2);

                return canonicallFile1.equals(canonicallFile2);
        }

        private String canonicalize(String filePath) {

                // Load RDF content into a Graph
                Graph graph = Graph.create();
                Load ld = Load.create(graph);

                try {
                        ld.parse(filePath, "");
                } catch (Exception e) {
                        e.printStackTrace();
                }

                // Return Canonical RDF content
                return CanonicalRdf10Format.create(graph).toString();
        }

        @Test
        public void testSelectRdfxmlInvalid() throws IOException {
                String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
                String pathResBeatlesSelectRdfxml = resultPath.resolve("select").resolve("beatles-select-rdfxml.rdf")
                                .toString();
                String pathQueryBeatlesAlbum = queriesPath.resolve("select").resolve("beatlesAlbums.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "rdfxml", "-o",
                                pathResBeatlesSelectRdfxml, "-q", pathQueryBeatlesAlbum);

                String expectedOutput = "Error: rdfxml is not a valid output format for select or ask requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.contains(expectedOutput));
                assertNotEquals("", actualOutput);
        }

        @Test
        public void testSelectTurtleInvalid() throws IOException {
                String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
                String pathResBeatlesSelectTurtle = resultPath.resolve("select").resolve("beatles-select-turtle.ttl")
                                .toString();
                String pathQueryBeatlesAlbum = queriesPath.resolve("select").resolve("beatlesAlbums.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "turtle", "-o",
                                pathResBeatlesSelectTurtle, "-q", pathQueryBeatlesAlbum);

                String expectedOutput = "Error: turtle is not a valid output format for select or ask requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.contains(expectedOutput));
                assertNotEquals("", actualOutput);
        }

        @Test
        public void testSelectTriginvalid() throws IOException {
                String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
                String pathResBeatlesSelectTrig = resultPath.resolve("select").resolve("beatles-select-trig.trig")
                                .toString();
                String pathQueryBeatlesAlbum = queriesPath.resolve("select").resolve("beatlesAlbums.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "trig", "-o",
                                pathResBeatlesSelectTrig, "-q", pathQueryBeatlesAlbum);

                String expectedOutput = "Error: trig is not a valid output format for select or ask requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.contains(expectedOutput));
                assertNotEquals("", actualOutput);
        }

        @Test
        public void testSelectJsonldInvalid() throws IOException {
                String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
                String pathResBeatlesSelectJsonld = resultPath.resolve("select").resolve("beatles-select-jsonld.jsonld")
                                .toString();
                String pathQueryBeatlesAlbum = queriesPath.resolve("select").resolve("beatlesAlbums.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "jsonld", "-o",
                                pathResBeatlesSelectJsonld, "-q", pathQueryBeatlesAlbum);

                String expectedOutput = "Error: jsonld is not a valid output format for select or ask requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.contains(expectedOutput));
                assertNotEquals("", actualOutput);
        }

        @Test
        public void testSelectBidingXml() throws IOException {
                String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
                String pathRefBeatlesSelectXml = referencesPath.resolve("select")
                                .resolve("beatles-select-bidingxml.xml")
                                .toString();
                String pathResBeatlesSelectXml = resultPath.resolve("select").resolve("beatles-select-xml.xml")
                                .toString();
                String pathQueryBeatlesAlbum = queriesPath.resolve("select").resolve("beatlesAlbums.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "xml", "-o",
                                pathResBeatlesSelectXml, "-q", pathQueryBeatlesAlbum);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesSelectXml, pathResBeatlesSelectXml));
        }

        @Test
        public void testSelectBidingJson() throws IOException {
                String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
                String pathRefBeatlesSelectJson = referencesPath.resolve("select")
                                .resolve("beatles-select-bidingjson.json")
                                .toString();
                String pathResBeatlesSelectJson = resultPath.resolve("select").resolve("beatles-select-json.json")
                                .toString();
                String pathQueryBeatlesAlbum = queriesPath.resolve("select").resolve("beatlesAlbums.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "json", "-o",
                                pathResBeatlesSelectJson, "-q", pathQueryBeatlesAlbum);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesSelectJson, pathResBeatlesSelectJson));
        }

        @Test
        public void testSelectBidingCsv() throws IOException {
                String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
                String pathRefBeatlesSelectCsv = referencesPath.resolve("select")
                                .resolve("beatles-select-bidingcsv.csv")
                                .toString();
                String pathResBeatlesSelectCsv = resultPath.resolve("select").resolve("beatles-select-csv.csv")
                                .toString();
                String pathQueryBeatlesAlbum = queriesPath.resolve("select").resolve("beatlesAlbums.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "csv", "-o",
                                pathResBeatlesSelectCsv, "-q", pathQueryBeatlesAlbum);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesSelectCsv, pathResBeatlesSelectCsv));
        }

        @Test
        public void testSelectBidingTSV() throws IOException {
                String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
                String pathRefBeatlesSelectTsv = referencesPath.resolve("select")
                                .resolve("beatles-select-bidingtsv.tsv")
                                .toString();
                String pathResBeatlesSelectTsv = resultPath.resolve("select").resolve("beatles-select-tsv.tsv")
                                .toString();
                String pathQueryBeatlesAlbum = queriesPath.resolve("select").resolve("beatlesAlbums.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "tsv", "-o",
                                pathResBeatlesSelectTsv, "-q", pathQueryBeatlesAlbum);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesSelectTsv, pathResBeatlesSelectTsv));
        }

        @Test
        public void testSelectMarkdown() throws IOException {
                String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
                String pathRefBeatlesSelectMarkdown = referencesPath.resolve("select")
                                .resolve("beatles-select-bidingmarkdown.md")
                                .toString();
                String pathResBeatlesSelectMarkdown = resultPath.resolve("select")
                                .resolve("beatles-select-bidingmarkdown.md")
                                .toString();
                String pathQueryBeatlesAlbum = queriesPath.resolve("select").resolve("beatlesAlbums.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "markdown", "-o",
                                pathResBeatlesSelectMarkdown, "-q", pathQueryBeatlesAlbum);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesSelectMarkdown, pathResBeatlesSelectMarkdown));
        }

        @Test
        public void testAskTrueRdfxmlInvalid() throws IOException {
                String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
                String pathResBeatlesAskFalse = resultPath.resolve("ask").resolve("beatles-ask-rdfxml-false.rdf")
                                .toString();
                String pathQueryBeatlesAskFalse = queriesPath.resolve("ask").resolve("beatlesFalse.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "rdfxml", "-o", pathResBeatlesAskFalse, "-q",
                                pathQueryBeatlesAskFalse);

                String expectedOutput = "Error: rdfxml is not a valid output format for select or ask requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.contains(expectedOutput));
                assertNotEquals("", actualOutput);

        }

        @Test
        public void testAskFalseRdfxmlInvalid() throws IOException {
                String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
                String pathResBeatlesAskFalse = resultPath.resolve("ask").resolve("beatles-ask-rdfxml-false.rdf")
                                .toString();
                String pathQueryBeatlesAskFalse = queriesPath.resolve("ask").resolve("beatlesFalse.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "rdfxml", "-o", pathResBeatlesAskFalse, "-q",
                                pathQueryBeatlesAskFalse);

                String expectedOutput = "Error: rdfxml is not a valid output format for select or ask requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.contains(expectedOutput));
                assertNotEquals("", actualOutput);
        }

        @Test
        public void testAskTrueTurtleInvalid() throws IOException {
                String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
                String pathResBeatlesAskTrue = resultPath.resolve("ask").resolve("beatles-ask-turtle-true.ttl")
                                .toString();
                String pathQueryBeatlesAskTrue = queriesPath.resolve("ask").resolve("beatlesTrue.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "turtle", "-o", pathResBeatlesAskTrue, "-q",
                                pathQueryBeatlesAskTrue);

                String expectedOutput = "Error: turtle is not a valid output format for select or ask requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.contains(expectedOutput));
                assertNotEquals("", actualOutput);
        }

        @Test
        public void testAskFalseTurtleInvalid() throws IOException {
                String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
                String pathResBeatlesAskFalse = resultPath.resolve("ask").resolve("beatles-ask-turtle-false.ttl")
                                .toString();
                String pathQueryBeatlesAskFalse = queriesPath.resolve("ask").resolve("beatlesFalse.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "turtle", "-o", pathResBeatlesAskFalse, "-q",
                                pathQueryBeatlesAskFalse);

                String expectedOutput = "Error: turtle is not a valid output format for select or ask requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.contains(expectedOutput));
                assertNotEquals("", actualOutput);
        }

        @Test
        public void testAskTrigTrueInvalid() throws IOException {
                String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
                String pathResBeatlesAskTrue = resultPath.resolve("ask").resolve("beatles-ask-trig-true.trig")
                                .toString();
                String pathQueryBeatlesAskTrue = queriesPath.resolve("ask").resolve("beatlesTrue.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "trig", "-o", pathResBeatlesAskTrue, "-q",
                                pathQueryBeatlesAskTrue);

                String expectedOutput = "Error: trig is not a valid output format for select or ask requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.contains(expectedOutput));
                assertNotEquals("", actualOutput);
        }

        @Test
        public void testAskTrigFalseInvalid() throws IOException {
                String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
                String pathResBeatlesAskFalse = resultPath.resolve("ask").resolve("beatles-ask-trig-false.trig")
                                .toString();
                String pathQueryBeatlesAskFalse = queriesPath.resolve("ask").resolve("beatlesFalse.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "trig", "-o", pathResBeatlesAskFalse, "-q",
                                pathQueryBeatlesAskFalse);

                String expectedOutput = "Error: trig is not a valid output format for select or ask requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.contains(expectedOutput));
                assertNotEquals("", actualOutput);
        }

        @Test
        public void testAskTrueJsonldInvalid() throws IOException {
                String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
                String pathResBeatlesAskTrue = resultPath.resolve("ask").resolve("beatles-ask-jsonld-true.jsonld")
                                .toString();
                String pathQueryBeatlesAskTrue = queriesPath.resolve("ask").resolve("beatlesTrue.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "jsonld", "-o", pathResBeatlesAskTrue, "-q",
                                pathQueryBeatlesAskTrue);

                String expectedOutput = "Error: jsonld is not a valid output format for select or ask requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.contains(expectedOutput));
                assertNotEquals("", actualOutput);
        }

        @Test
        public void testAskFalseJsonldInvalid() throws IOException {
                String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
                String pathResBeatlesAskFalse = resultPath.resolve("ask").resolve("beatles-ask-jsonld-false.jsonld")
                                .toString();
                String pathQueryBeatlesAskFalse = queriesPath.resolve("ask").resolve("beatlesFalse.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "jsonld", "-o", pathResBeatlesAskFalse, "-q",
                                pathQueryBeatlesAskFalse);

                String expectedOutput = "Error: jsonld is not a valid output format for select or ask requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.contains(expectedOutput));
                assertNotEquals("", actualOutput);
        }

        @Test
        public void testAskTrueBidingXml() throws IOException {
                String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
                String pathRefBeatlesAskXml = referencesPath.resolve("ask").resolve("beatles-ask-bidingxml-true.xml")
                                .toString();
                String pathResBeatlesAskTrue = resultPath.resolve("ask").resolve("beatles-ask-bidingxml-true.xml")
                                .toString();
                String pathQueryBeatlesAskTrue = queriesPath.resolve("ask").resolve("beatlesTrue.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "xml", "-o", pathResBeatlesAskTrue, "-q",
                                pathQueryBeatlesAskTrue);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesAskXml, pathResBeatlesAskTrue));
        }

        @Test
        public void testAskFalseBidingXml() throws IOException {
                String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
                String pathRefBeatlesAskXml = referencesPath.resolve("ask").resolve("beatles-ask-bidingxml-false.xml")
                                .toString();
                String pathResBeatlesAskFalse = resultPath.resolve("ask").resolve("beatles-ask-bidingxml-false.xml")
                                .toString();
                String pathQueryBeatlesAskFalse = queriesPath.resolve("ask").resolve("beatlesFalse.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "xml", "-o", pathResBeatlesAskFalse, "-q",
                                pathQueryBeatlesAskFalse);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesAskXml, pathResBeatlesAskFalse));
        }

        @Test
        public void testAskTrueBidingJson() throws IOException {
                String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
                String pathRefBeatlesAskJSON = referencesPath.resolve("ask").resolve("beatles-ask-bidingjson-true.json")
                                .toString();
                String pathResBeatlesAskTrue = resultPath.resolve("ask").resolve("beatles-ask-bidingjson-true.json")
                                .toString();
                String pathQueryBeatlesAskTrue = queriesPath.resolve("ask").resolve("beatlesTrue.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "json", "-o", pathResBeatlesAskTrue, "-q",
                                pathQueryBeatlesAskTrue);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesAskJSON, pathResBeatlesAskTrue));
        }

        @Test
        public void testAskFalseBidingJson() throws IOException {
                String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
                String pathRefBeatlesAskJSON = referencesPath.resolve("ask")
                                .resolve("beatles-ask-bidingjson-false.json")
                                .toString();
                String pathResBeatlesAskFalse = resultPath.resolve("ask").resolve("beatles-ask-bidingjson-false.json")
                                .toString();
                String pathQueryBeatlesAskFalse = queriesPath.resolve("ask").resolve("beatlesFalse.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "json", "-o", pathResBeatlesAskFalse, "-q",
                                pathQueryBeatlesAskFalse);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesAskJSON, pathResBeatlesAskFalse));
        }

        @Test
        public void testAskTrueBidingCsv() throws IOException {
                String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
                String pathRefBeatlesAskCSV = referencesPath.resolve("ask").resolve("beatles-ask-bidingcsv-true.csv")
                                .toString();
                String pathResBeatlesAskTrue = resultPath.resolve("ask").resolve("beatles-ask-bidingcsv-true.csv")
                                .toString();
                String pathQueryBeatlesAskTrue = queriesPath.resolve("ask").resolve("beatlesTrue.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "csv", "-o", pathResBeatlesAskTrue, "-q",
                                pathQueryBeatlesAskTrue);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesAskCSV, pathResBeatlesAskTrue));
        }

        @Test
        public void testAskFalseBidingCsv() throws IOException {
                String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
                String pathRefBeatlesAskCSV = referencesPath.resolve("ask").resolve("beatles-ask-bidingcsv-false.csv")
                                .toString();
                String pathResBeatlesAskFalse = resultPath.resolve("ask").resolve("beatles-ask-bidingcsv-false.csv")
                                .toString();
                String pathQueryBeatlesAskFalse = queriesPath.resolve("ask").resolve("beatlesFalse.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "csv", "-o", pathResBeatlesAskFalse, "-q",
                                pathQueryBeatlesAskFalse);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesAskCSV, pathResBeatlesAskFalse));
        }

        @Test
        public void testAskTrueBidingTsv() throws IOException {
                String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
                String pathRefBeatlesAskTSV = referencesPath.resolve("ask").resolve("beatles-ask-bidingtsv-true.tsv")
                                .toString();
                String pathResBeatlesAskTrue = resultPath.resolve("ask").resolve("beatles-ask-bidingtsv-true.tsv")
                                .toString();
                String pathQueryBeatlesAskTrue = queriesPath.resolve("ask").resolve("beatlesTrue.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "tsv", "-o", pathResBeatlesAskTrue, "-q",
                                pathQueryBeatlesAskTrue);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesAskTSV, pathResBeatlesAskTrue));
        }

        @Test
        public void testAskFalseBidingTsv() throws IOException {
                String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
                String pathRefBeatlesAskTSV = referencesPath.resolve("ask").resolve("beatles-ask-bidingtsv-false.tsv")
                                .toString();
                String pathResBeatlesAskFalse = resultPath.resolve("ask").resolve("beatles-ask-bidingtsv-false.tsv")
                                .toString();
                String pathQueryBeatlesAskFalse = queriesPath.resolve("ask").resolve("beatlesFalse.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "tsv", "-o", pathResBeatlesAskFalse, "-q",
                                pathQueryBeatlesAskFalse);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesAskTSV, pathResBeatlesAskFalse));
        }

        @Test
        public void testAskTrueBidingMarkdown() throws IOException {
                String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
                String pathRefBeatlesAskMarkdown = referencesPath.resolve("ask")
                                .resolve("beatles-ask-bidingmarkdown-true.md").toString();
                String pathResBeatlesAskTrue = resultPath.resolve("ask").resolve("beatles-ask-bidingmarkdown-true.md")
                                .toString();
                String pathQueryBeatlesAskTrue = queriesPath.resolve("ask").resolve("beatlesTrue.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "markdown", "-o", pathResBeatlesAskTrue, "-q",
                                pathQueryBeatlesAskTrue);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesAskMarkdown, pathResBeatlesAskTrue));
        }

        @Test
        public void testAskFalseBidingMarkdown() throws IOException {
                String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
                String pathRefBeatlesAskMarkdown = referencesPath.resolve("ask")
                                .resolve("beatles-ask-bidingmarkdown-false.md").toString();
                String pathResBeatlesAskFalse = resultPath.resolve("ask").resolve("beatles-ask-bidingmarkdown-false.md")
                                .toString();
                String pathQueryBeatlesAskFalse = queriesPath.resolve("ask").resolve("beatlesFalse.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "markdown", "-o", pathResBeatlesAskFalse,
                                "-q",
                                pathQueryBeatlesAskFalse);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesAskMarkdown, pathResBeatlesAskFalse));
        }

        @Test
        public void testInsertRdfxml() throws IOException {
                String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
                String pathRefBeatlesInsert = referencesPath.resolve("insert").resolve("beatles-insert-rdfxml.xml")
                                .toString();
                String pathResBeatlesInsert = resultPath.resolve("insert").resolve("beatles-insert-rdfxml.xml")
                                .toString();
                String pathQueryBeatlesInsert = queriesPath.resolve("insert").resolve("beatlesInsertRock.rq")
                                .toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "rdfxml", "-o", pathResBeatlesInsert, "-q",
                                pathQueryBeatlesInsert);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesInsert, pathResBeatlesInsert));
        }

        @Test
        public void testInsertTurtle() throws IOException {
                String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
                String pathRefBeatlesInsert = referencesPath.resolve("insert").resolve("beatles-insert-turtle.ttl")
                                .toString();
                String pathResBeatlesInsert = resultPath.resolve("insert").resolve("beatles-insert-turtle.ttl")
                                .toString();
                String pathQueryBeatlesInsert = queriesPath.resolve("insert").resolve("beatlesInsertRock.rq")
                                .toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "turtle", "-o", pathResBeatlesInsert, "-q",
                                pathQueryBeatlesInsert);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesInsert, pathResBeatlesInsert));
        }

        @Test
        public void testInsertTrig() throws IOException {
                String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
                String pathRefBeatlesInsert = referencesPath.resolve("insert").resolve("beatles-insert-trig.trig")
                                .toString();
                String pathResBeatlesInsert = resultPath.resolve("insert").resolve("beatles-insert-trig.trig")
                                .toString();
                String pathQueryBeatlesInsert = queriesPath.resolve("insert").resolve("beatlesInsertRock.rq")
                                .toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "trig", "-o", pathResBeatlesInsert, "-q",
                                pathQueryBeatlesInsert);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesInsert, pathResBeatlesInsert));
        }

        @Test
        public void testInsertJsonld() throws IOException {
                String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
                String pathRefBeatlesInsert = referencesPath.resolve("insert").resolve("beatles-insert-jsonld.jsonld")
                                .toString();
                String pathResBeatlesInsert = resultPath.resolve("insert").resolve("beatles-insert-jsonld.jsonld")
                                .toString();
                String pathQueryBeatlesInsert = queriesPath.resolve("insert").resolve("beatlesInsertRock.rq")
                                .toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "jsonld", "-o", pathResBeatlesInsert, "-q",
                                pathQueryBeatlesInsert);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesInsert, pathResBeatlesInsert));
        }

        @Test
        public void testInsertBidingXmlInvalid() throws IOException {
                String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
                String pathResBeatlesInsert = resultPath.resolve("insert").resolve("beatles-insert-bidingrdfxml.xml")
                                .toString();
                String pathQueryBeatlesInsert = queriesPath.resolve("insert").resolve("beatlesInsertRock.rq")
                                .toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "xml", "-o", pathResBeatlesInsert, "-q",
                                pathQueryBeatlesInsert);

                String expectedOutput = "Error: xml is not a valid output format for insert, delete, describe or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.contains(expectedOutput));
                assertNotEquals("", actualOutput);
        }

        @Test
        public void testInsertBidingJsonInvalid() throws IOException {
                String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
                String pathResBeatlesInsert = resultPath.resolve("insert").resolve("beatles-insert-bidingjsonld.jsonld")
                                .toString();
                String pathQueryBeatlesInsert = queriesPath.resolve("insert").resolve("beatlesInsertRock.rq")
                                .toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "json", "-o",
                                pathResBeatlesInsert, "-q",
                                pathQueryBeatlesInsert);

                String expectedOutput = "Error: json is not a valid output format for insert, delete, describe or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.contains(expectedOutput));
                assertNotEquals("", actualOutput);
        }

        @Test
        public void testInsertBidingCsvInvalid() throws IOException {
                String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
                String pathResBeatlesInsert = resultPath.resolve("insert").resolve("beatles-insert-bidingrdfxml.xml")
                                .toString();
                String pathQueryBeatlesInsert = queriesPath.resolve("insert").resolve("beatlesInsertRock.rq")
                                .toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "csv", "-o",
                                pathResBeatlesInsert, "-q",
                                pathQueryBeatlesInsert);

                String expectedOutput = "Error: csv is not a valid output format for insert, delete, describe or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.contains(expectedOutput));
                assertNotEquals("", actualOutput);
        }

        @Test
        public void testInsertBidingTsvInvalid() throws IOException {
                String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
                String pathResBeatlesInsert = resultPath.resolve("insert").resolve("beatles-insert-bidingrdfxml.xml")
                                .toString();
                String pathQueryBeatlesInsert = queriesPath.resolve("insert").resolve("beatlesInsertRock.rq")
                                .toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "tsv", "-o",
                                pathResBeatlesInsert, "-q",
                                pathQueryBeatlesInsert);

                String expectedOutput = "Error: tsv is not a valid output format for insert, delete, describe or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.contains(expectedOutput));
                assertNotEquals("", actualOutput);
        }

        @Test
        public void testInsertBidingMarkdownInvalid() throws IOException {
                String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
                String pathResBeatlesInsert = resultPath.resolve("insert").resolve("beatles-insert-bidingrdfxml.xml")
                                .toString();
                String pathQueryBeatlesInsert = queriesPath.resolve("insert").resolve("beatlesInsertRock.rq")
                                .toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "markdown", "-o",
                                pathResBeatlesInsert, "-q", pathQueryBeatlesInsert);

                String expectedOutput = "Error: markdown is not a valid output format for insert, delete, describe or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.contains(expectedOutput));
                assertNotEquals("", actualOutput);
        }

        @Test
        public void testInsertWhereRdfxml() throws IOException {
                String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
                String pathRefBeatlesInsertwhere = referencesPath.resolve("insert-where")
                                .resolve("beatles-insertwhere-bidingrdfxml.xml").toString();
                String pathResBeatlesInsertwhere = resultPath.resolve("insert-where")
                                .resolve("beatles-insertwhere-bidingrdfxml.xml").toString();
                String pathQueryBeatlesInsertwhere = queriesPath.resolve("insert-where").resolve("beatlesAge.rq")
                                .toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "rdfxml", "-o", pathResBeatlesInsertwhere,
                                "-q",
                                pathQueryBeatlesInsertwhere);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesInsertwhere, pathResBeatlesInsertwhere));
        }

        @Test
        public void testInsertWhereTurtle() throws IOException {
                String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
                String pathRefBeatlesInsertwhere = referencesPath.resolve("insert-where")
                                .resolve("beatles-insertwhere-bidingturtle.ttl").toString();
                String pathResBeatlesInsertwhere = resultPath.resolve("insert-where")
                                .resolve("beatles-insertwhere-bidingturtle.ttl").toString();
                String pathQueryBeatlesInsertwhere = queriesPath.resolve("insert-where").resolve("beatlesAge.rq")
                                .toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "turtle", "-o", pathResBeatlesInsertwhere,
                                "-q",
                                pathQueryBeatlesInsertwhere);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesInsertwhere, pathResBeatlesInsertwhere));
        }

        @Test
        public void testInsertWhereTrig() throws IOException {
                String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
                String pathRefBeatlesInsertwhere = referencesPath.resolve("insert-where")
                                .resolve("beatles-insertwhere-bidingtrig.trig").toString();
                String pathResBeatlesInsertwhere = resultPath.resolve("insert-where")
                                .resolve("beatles-insertwhere-bidingtrig.trig").toString();
                String pathQueryBeatlesInsertwhere = queriesPath.resolve("insert-where").resolve("beatlesAge.rq")
                                .toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "trig", "-o", pathResBeatlesInsertwhere, "-q",
                                pathQueryBeatlesInsertwhere);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesInsertwhere, pathResBeatlesInsertwhere));
        }

        @Test
        public void testInsertWhereJsonLd() throws IOException {
                String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
                String pathRefBeatlesInsertwhere = referencesPath.resolve("insert-where")
                                .resolve("beatles-insertwhere-bidingjsonld.jsonld")
                                .toString();
                String pathResBeatlesInsertwhere = resultPath.resolve("insert-where")
                                .resolve("beatles-insertwhere-bidingjsonld.jsonld").toString();
                String pathQueryBeatlesInsertwhere = queriesPath.resolve("insert-where").resolve("beatlesAge.rq")
                                .toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "jsonld", "-o", pathResBeatlesInsertwhere,
                                "-q",
                                pathQueryBeatlesInsertwhere);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesInsertwhere, pathResBeatlesInsertwhere));
        }

        @Test
        public void testInsertWhereBidingxmlInvalid() throws IOException {
                String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
                String pathResBeatlesInsertwhere = resultPath.resolve("insert-where")
                                .resolve("beatles-insertwhere-bidingrdfxml.xml").toString();
                String pathQueryBeatlesInsertwhere = queriesPath.resolve("insert-where").resolve("beatlesAge.rq")
                                .toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "xml", "-o",
                                pathResBeatlesInsertwhere, "-q",
                                pathQueryBeatlesInsertwhere);

                String expectedOutput = "Error: xml is not a valid output format for insert, delete, describe or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.contains(expectedOutput));
                assertNotEquals("", actualOutput);
        }

        @Test
        public void testInsertWhereBidingjsonInvalid() throws IOException {
                String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
                String pathResBeatlesInsertwhere = resultPath.resolve("insert-where")
                                .resolve("beatles-insertwhere-bidingrdfxml.xml").toString();
                String pathQueryBeatlesInsertwhere = queriesPath.resolve("insert-where").resolve("beatlesAge.rq")
                                .toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "json", "-o",
                                pathResBeatlesInsertwhere, "-q",
                                pathQueryBeatlesInsertwhere);

                String expectedOutput = "Error: json is not a valid output format for insert, delete, describe or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.contains(expectedOutput));
                assertNotEquals("", actualOutput);
        }

        @Test
        public void testInsertWhereBidingCsvInvalid() throws IOException {
                String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
                String pathQueryBeatlesInsertwhere = queriesPath.resolve("insert-where").resolve("beatlesAge.rq")
                                .toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "csv", "-o",
                                "references/insert-where/beatles-insertwhere-bidingcsv.csv", "-q",
                                pathQueryBeatlesInsertwhere);

                String expectedOutput = "Error: csv is not a valid output format for insert, delete, describe or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.contains(expectedOutput));
                assertNotEquals("", actualOutput);
        }

        @Test
        public void testInsertWhereBidingTsvInvalid() throws IOException {
                String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
                String pathQueryBeatlesInsertwhere = queriesPath.resolve("insert-where").resolve("beatlesAge.rq")
                                .toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "tsv", "-o",
                                "references/insert-where/beatles-insertwhere-bidingtsv.tsv", "-q",
                                pathQueryBeatlesInsertwhere);

                String expectedOutput = "Error: tsv is not a valid output format for insert, delete, describe or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.contains(expectedOutput));
                assertNotEquals("", actualOutput);
        }

        @Test
        public void testInsertWhereBidingMarkdownInvalid() throws IOException {
                String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
                String pathQueryBeatlesInsertwhere = queriesPath.resolve("insert-where").resolve("beatlesAge.rq")
                                .toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "markdown", "-o",
                                "references/insert-where/beatles-insertwhere-bidingmarkdown.md", "-q",
                                pathQueryBeatlesInsertwhere);

                String expectedOutput = "Error: markdown is not a valid output format for insert, delete, describe or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.contains(expectedOutput));
                assertNotEquals("", actualOutput);
        }

        @Test
        public void testDeleteRdfxml() throws IOException {
                String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
                String pathRefBeatlesDelete = referencesPath.resolve("delete").resolve("beatles-delete-rdfxml.xml")
                                .toString();
                String pathResBeatlesDelete = resultPath.resolve("delete").resolve("beatles-delete-rdfxml.xml")
                                .toString();
                String pathQueryBeatlesDelete = queriesPath.resolve("delete").resolve("deleteMcCartney.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "rdfxml", "-o", pathResBeatlesDelete, "-q",
                                pathQueryBeatlesDelete);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesDelete, pathResBeatlesDelete));
        }

        @Test
        public void testDeleteTurtle() throws IOException {
                String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
                String pathRefBeatlesDelete = referencesPath.resolve("delete").resolve("beatles-delete-turtle.ttl")
                                .toString();
                String pathResBeatlesDelete = resultPath.resolve("delete").resolve("beatles-delete-turtle.ttl")
                                .toString();
                String pathQueryBeatlesDelete = queriesPath.resolve("delete").resolve("deleteMcCartney.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "turtle", "-o", pathResBeatlesDelete, "-q",
                                pathQueryBeatlesDelete);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesDelete, pathResBeatlesDelete));
        }

        @Test
        public void testDeleteTrig() throws IOException {
                String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
                String pathRefBeatlesDelete = referencesPath.resolve("delete").resolve("beatles-delete-trig.trig")
                                .toString();
                String pathResBeatlesDelete = resultPath.resolve("delete").resolve("beatles-delete-trig.trig")
                                .toString();
                String pathQueryBeatlesDelete = queriesPath.resolve("delete").resolve("deleteMcCartney.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "trig", "-o", pathResBeatlesDelete, "-q",
                                pathQueryBeatlesDelete);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesDelete, pathResBeatlesDelete));
        }

        @Test
        public void testDeleteJsonld() throws IOException {
                String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
                String pathRefBeatlesDelete = referencesPath.resolve("delete").resolve("beatles-delete-jsonld.jsonld")
                                .toString();
                String pathResBeatlesDelete = resultPath.resolve("delete").resolve("beatles-delete-jsonld.jsonld")
                                .toString();
                String pathQueryBeatlesDelete = queriesPath.resolve("delete").resolve("deleteMcCartney.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "jsonld", "-o", pathResBeatlesDelete, "-q",
                                pathQueryBeatlesDelete);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesDelete, pathResBeatlesDelete));
        }

        @Test
        public void testDeleteBidingXmlInvalid() throws IOException {
                String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
                String pathQueryBeatlesDelete = queriesPath.resolve("delete").resolve("deleteMcCartney.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "xml", "-o",
                                "references/delete/beatles-delete-xml.xml", "-q",
                                pathQueryBeatlesDelete);

                String expectedOutput = "Error: xml is not a valid output format for insert, delete, describe or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.contains(expectedOutput));
                assertNotEquals("", actualOutput);
        }

        @Test
        public void testDeleteBidingJsonInvalid() throws IOException {
                String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
                String pathQueryBeatlesDelete = queriesPath.resolve("delete").resolve("deleteMcCartney.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "json", "-o",
                                "references/delete/beatles-delete-jsonld.jsonld", "-q",
                                pathQueryBeatlesDelete);

                String expectedOutput = "Error: json is not a valid output format for insert, delete, describe or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.contains(expectedOutput));
                assertNotEquals("", actualOutput);
        }

        @Test
        public void testDeleteBidingCsvInvalid() throws IOException {
                String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
                String pathQueryBeatlesDelete = queriesPath.resolve("delete").resolve("deleteMcCartney.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "csv", "-o",
                                "references/delete/beatles-delete-csv.csv", "-q",
                                pathQueryBeatlesDelete);

                String expectedOutput = "Error: csv is not a valid output format for insert, delete, describe or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.contains(expectedOutput));
                assertNotEquals("", actualOutput);
        }

        @Test
        public void testDeleteBidingTsvInvalid() throws IOException {
                String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
                String pathQueryBeatlesDelete = queriesPath.resolve("delete").resolve("deleteMcCartney.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "tsv", "-o",
                                "references/delete/beatles-delete-tsv.tsv", "-q",
                                pathQueryBeatlesDelete);

                String expectedOutput = "Error: tsv is not a valid output format for insert, delete, describe or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.contains(expectedOutput));
                assertNotEquals("", actualOutput);
        }

        @Test
        public void testDeleteBidingMarkdownInvalid() throws IOException {
                String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
                String pathQueryBeatlesDelete = queriesPath.resolve("delete").resolve("deleteMcCartney.rq").toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "markdown", "-o",
                                "references/delete/beatles-delete-markdown.md", "-q",
                                pathQueryBeatlesDelete);

                String expectedOutput = "Error: markdown is not a valid output format for insert, delete, describe or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.contains(expectedOutput));
                assertNotEquals("", actualOutput);
        }

        @Test
        public void testDeleteWhereRdfxml() throws IOException {
                String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
                String pathRefBeatlesDelete = referencesPath.resolve("delete-where")
                                .resolve("beatles-delete-where-rdfxml.xml")
                                .toString();
                String pathResBeatlesDelete = resultPath.resolve("delete-where")
                                .resolve("beatles-delete-where-rdfxml.xml")
                                .toString();
                String pathQueryBeatlesDelete = queriesPath.resolve("delete-where").resolve("deleteLenon.rq")
                                .toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "rdfxml", "-o", pathResBeatlesDelete, "-q",
                                pathQueryBeatlesDelete);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesDelete, pathResBeatlesDelete));
        }

        @Test
        public void testDeleteWhereTurtle() throws IOException {
                String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
                String pathRefBeatlesDelete = referencesPath.resolve("delete-where")
                                .resolve("beatles-delete-where-turtle.ttl")
                                .toString();
                String pathResBeatlesDelete = resultPath.resolve("delete-where")
                                .resolve("beatles-delete-where-turtle.ttl")
                                .toString();
                String pathQueryBeatlesDelete = queriesPath.resolve("delete-where").resolve("deleteLenon.rq")
                                .toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "turtle", "-o", pathResBeatlesDelete, "-q",
                                pathQueryBeatlesDelete);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesDelete, pathResBeatlesDelete));
        }

        @Test
        public void testDeleteWhereTrig() throws IOException {
                String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
                String pathRefBeatlesDelete = referencesPath.resolve("delete-where")
                                .resolve("beatles-delete-where-trig.trig")
                                .toString();
                String pathResBeatlesDelete = resultPath.resolve("delete-where")
                                .resolve("beatles-delete-where-trig.trig")
                                .toString();
                String pathQueryBeatlesDelete = queriesPath.resolve("delete-where").resolve("deleteLenon.rq")
                                .toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "trig", "-o", pathResBeatlesDelete, "-q",
                                pathQueryBeatlesDelete);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesDelete, pathResBeatlesDelete));
        }

        @Test
        public void testDeleteWhereJsonld() throws IOException {
                String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
                String pathRefBeatlesDelete = referencesPath.resolve("delete-where")
                                .resolve("beatles-delete-where-jsonld.jsonld")
                                .toString();
                String pathResBeatlesDelete = resultPath.resolve("delete-where")
                                .resolve("beatles-delete-where-jsonld.jsonld")
                                .toString();
                String pathQueryBeatlesDelete = queriesPath.resolve("delete-where").resolve("deleteLenon.rq")
                                .toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "jsonld", "-o", pathResBeatlesDelete, "-q",
                                pathQueryBeatlesDelete);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesDelete, pathResBeatlesDelete));
        }

        @Test
        public void testDeleteWhereBidingXmlInvalid() throws IOException {
                String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
                String pathQueryBeatlesDelete = queriesPath.resolve("delete-where").resolve("deleteLenon.rq")
                                .toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "xml", "-o",
                                "references/delete-where/beatles-delete-where-xml.xml", "-q",
                                pathQueryBeatlesDelete);

                String expectedOutput = "Error: xml is not a valid output format for insert, delete, describe or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.contains(expectedOutput));
                assertNotEquals("", actualOutput);
        }

        @Test
        public void testDeleteWhereBidingJsonInvalid() throws IOException {
                String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
                String pathQueryBeatlesDelete = queriesPath.resolve("delete-where").resolve("deleteLenon.rq")
                                .toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "json", "-o",
                                "references/delete-where/beatles-delete-where-json.json", "-q",
                                pathQueryBeatlesDelete);

                String expectedOutput = "Error: json is not a valid output format for insert, delete, describe or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.contains(expectedOutput));
                assertNotEquals("", actualOutput);
        }

        @Test
        public void testDeleteWhereBidingCsvInvalid() throws IOException {
                String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
                String pathQueryBeatlesDelete = queriesPath.resolve("delete-where").resolve("deleteLenon.rq")
                                .toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "csv", "-o",
                                "references/delete-where/beatles-delete-where-csv.csv", "-q",
                                pathQueryBeatlesDelete);

                String expectedOutput = "Error: csv is not a valid output format for insert, delete, describe or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.contains(expectedOutput));
                assertNotEquals("", actualOutput);
        }

        @Test
        public void testDeleteWhereBidingTsvInvalid() throws IOException {
                String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
                String pathQueryBeatlesDelete = queriesPath.resolve("delete-where").resolve("deleteLenon.rq")
                                .toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "tsv", "-o",
                                "references/delete-where/beatles-delete-where-tsv.tsv", "-q",
                                pathQueryBeatlesDelete);

                String expectedOutput = "Error: tsv is not a valid output format for insert, delete, describe or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.contains(expectedOutput));
                assertNotEquals("", actualOutput);
        }

        @Test
        public void testDeleteWhereBidingMarkdownInvalid() throws IOException {
                String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
                String pathQueryBeatlesDelete = queriesPath.resolve("delete-where").resolve("deleteLenon.rq")
                                .toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "markdown", "-o",
                                "references/delete-where/beatles-delete-where-markdown.md", "-q",
                                pathQueryBeatlesDelete);

                String expectedOutput = "Error: markdown is not a valid output format for insert, delete, describe or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.contains(expectedOutput));
                assertNotEquals("", actualOutput);
        }

        @Test
        public void testConstructRdfxml() throws IOException {
                String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
                String pathRefBeatlesConstruct = referencesPath.resolve("construct")
                                .resolve("beatles-construct-rdfxml.xml")
                                .toString();
                String pathResBeatlesConstruct = resultPath.resolve("construct").resolve("beatles-construct-rdfxml.xml")
                                .toString();
                String pathQueryBeatlesConstruct = queriesPath.resolve("construct").resolve("albumBeatles.rq")
                                .toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "rdfxml", "-o", pathResBeatlesConstruct, "-q",
                                pathQueryBeatlesConstruct);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesConstruct, pathResBeatlesConstruct));
        }

        @Test
        public void testConstructTurtle() throws IOException {
                String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
                String pathRefBeatlesConstruct = referencesPath.resolve("construct")
                                .resolve("beatles-construct-turtle.ttl")
                                .toString();
                String pathResBeatlesConstruct = resultPath.resolve("construct").resolve("beatles-construct-turtle.ttl")
                                .toString();
                String pathQueryBeatlesConstruct = queriesPath.resolve("construct").resolve("albumBeatles.rq")
                                .toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "turtle", "-o", pathResBeatlesConstruct, "-q",
                                pathQueryBeatlesConstruct);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesConstruct, pathResBeatlesConstruct));
        }

        @Test
        public void testConstructTrig() throws IOException {
                String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
                String pathRefBeatlesConstruct = referencesPath.resolve("construct")
                                .resolve("beatles-construct-trig.trig")
                                .toString();
                String pathResBeatlesConstruct = resultPath.resolve("construct").resolve("beatles-construct-trig.trig")
                                .toString();
                String pathQueryBeatlesConstruct = queriesPath.resolve("construct").resolve("albumBeatles.rq")
                                .toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "trig", "-o", pathResBeatlesConstruct, "-q",
                                pathQueryBeatlesConstruct);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesConstruct, pathResBeatlesConstruct));
        }

        @Test
        public void testConstructJsonld() throws IOException {
                String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
                String pathRefBeatlesConstruct = referencesPath.resolve("construct")
                                .resolve("beatles-construct-jsonld.jsonld")
                                .toString();
                String pathResBeatlesConstruct = resultPath.resolve("construct")
                                .resolve("beatles-construct-jsonld.jsonld")
                                .toString();
                String pathQueryBeatlesConstruct = queriesPath.resolve("construct").resolve("albumBeatles.rq")
                                .toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "jsonld", "-o", pathResBeatlesConstruct, "-q",
                                pathQueryBeatlesConstruct);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesConstruct, pathResBeatlesConstruct));
        }

        @Test
        public void testBidingConstructXmlInvalid() throws IOException {
                String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
                String pathQueryBeatlesConstruct = queriesPath.resolve("construct").resolve("albumBeatles.rq")
                                .toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "xml", "-o",
                                "references/construct/beatles-construct-xml.xml", "-q", pathQueryBeatlesConstruct);

                String expectedOutput = "Error: xml is not a valid output format for insert, delete, describe or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.contains(expectedOutput));
                assertNotEquals("", actualOutput);
        }

        @Test
        public void testBidingConstructJsonInvalid() throws IOException {
                String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
                String pathQueryBeatlesConstruct = queriesPath.resolve("construct").resolve("albumBeatles.rq")
                                .toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "json", "-o",
                                "references/construct/beatles-construct-jsonld.jsonld", "-q",
                                pathQueryBeatlesConstruct);

                String expectedOutput = "Error: json is not a valid output format for insert, delete, describe or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.contains(expectedOutput));
                assertNotEquals("", actualOutput);
        }

        @Test
        public void testBidingConstructCsvInvalid() throws IOException {
                String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
                String pathQueryBeatlesConstruct = queriesPath.resolve("construct").resolve("albumBeatles.rq")
                                .toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "csv", "-o",
                                "references/construct/beatles-construct-csv.csv", "-q", pathQueryBeatlesConstruct);

                String expectedOutput = "Error: csv is not a valid output format for insert, delete, describe or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.contains(expectedOutput));
                assertNotEquals("", actualOutput);
        }

        @Test
        public void testBidingConstructTsvInvalid() throws IOException {
                String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
                String pathQueryBeatlesConstruct = queriesPath.resolve("construct").resolve("albumBeatles.rq")
                                .toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "tsv", "-o",
                                "references/construct/beatles-construct-tsv.tsv", "-q", pathQueryBeatlesConstruct);

                String expectedOutput = "Error: tsv is not a valid output format for insert, delete, describe or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.contains(expectedOutput));
                assertNotEquals("", actualOutput);
        }

        @Test
        public void testBidingConstructMarkdownInvalid() throws IOException {
                String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
                String pathQueryBeatlesConstruct = queriesPath.resolve("construct").resolve("albumBeatles.rq")
                                .toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "markdown", "-o",
                                "references/construct/beatles-construct-markdown.md", "-q", pathQueryBeatlesConstruct);

                String expectedOutput = "Error: markdown is not a valid output format for insert, delete, describe or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.contains(expectedOutput));
                assertNotEquals("", actualOutput);
        }

        @Test
        public void testDescribeRdfxml() throws IOException {
                String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
                String pathRefBeatlesDescribe = referencesPath.resolve("describe")
                                .resolve("beatles-describe-rdfxml.xml")
                                .toString();
                String pathResBeatlesDescribe = resultPath.resolve("describe").resolve("beatles-describe-rdfxml.xml")
                                .toString();
                String pathQueryBeatlesDescribe = queriesPath.resolve("describe").resolve("describeBeatles.rq")
                                .toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "rdfxml", "-o", pathResBeatlesDescribe, "-q",
                                pathQueryBeatlesDescribe);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesDescribe, pathResBeatlesDescribe));
        }

        @Test
        public void testDescribeTurtle() throws IOException {
                String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
                String pathRefBeatlesDescribe = referencesPath.resolve("describe")
                                .resolve("beatles-describe-turtle.ttl")
                                .toString();
                String pathResBeatlesDescribe = resultPath.resolve("describe").resolve("beatles-describe-turtle.ttl")
                                .toString();
                String pathQueryBeatlesDescribe = queriesPath.resolve("describe").resolve("describeBeatles.rq")
                                .toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "turtle", "-o", pathResBeatlesDescribe, "-q",
                                pathQueryBeatlesDescribe);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesDescribe, pathResBeatlesDescribe));
        }

        @Test
        public void testDescribeTrigl() throws IOException {
                String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
                String pathRefBeatlesDescribe = referencesPath.resolve("describe").resolve("beatles-describe-trig.trig")
                                .toString();
                String pathResBeatlesDescribe = resultPath.resolve("describe").resolve("beatles-describe-trig.trig")
                                .toString();
                String pathQueryBeatlesDescribe = queriesPath.resolve("describe").resolve("describeBeatles.rq")
                                .toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "trig", "-o", pathResBeatlesDescribe, "-q",
                                pathQueryBeatlesDescribe);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesDescribe, pathResBeatlesDescribe));
        }

        @Test
        public void testDescribeJsonld() throws IOException {
                String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
                String pathRefBeatlesDescribe = referencesPath.resolve("describe")
                                .resolve("beatles-describe-jsonld.jsonld")
                                .toString();
                String pathResBeatlesDescribe = resultPath.resolve("describe").resolve("beatles-describe-jsonld.jsonld")
                                .toString();
                String pathQueryBeatlesDescribe = queriesPath.resolve("describe").resolve("describeBeatles.rq")
                                .toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "jsonld", "-o", pathResBeatlesDescribe, "-q",
                                pathQueryBeatlesDescribe);

                assertEquals(0, exitCode);
                assertEquals("", out.toString());
                assertEquals("", err.toString());
                assertTrue(compareFiles(pathRefBeatlesDescribe, pathResBeatlesDescribe));
        }

        @Test
        public void testBidingDescribeXmlInvalid() throws IOException {
                String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
                String pathQueryBeatlesDescribe = queriesPath.resolve("describe").resolve("describeBeatles.rq")
                                .toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "xml", "-o",
                                "references/describe/beatles-describe-xml.xml", "-q", pathQueryBeatlesDescribe);

                String expectedOutput = "Error: xml is not a valid output format for insert, delete, describe or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.contains(expectedOutput));
                assertNotEquals("", actualOutput);
        }

        @Test
        public void testBidingDescribeJsonInvalid() throws IOException {
                String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
                String pathQueryBeatlesDescribe = queriesPath.resolve("describe").resolve("describeBeatles.rq")
                                .toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "json", "-o",
                                "references/describe/beatles-describe-json.json", "-q", pathQueryBeatlesDescribe);

                String expectedOutput = "Error: json is not a valid output format for insert, delete, describe or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.contains(expectedOutput));
                assertNotEquals("", actualOutput);
        }

        @Test
        public void testBidingDescribeCsvInvalid() throws IOException {
                String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
                String pathQueryBeatlesDescribe = queriesPath.resolve("describe").resolve("describeBeatles.rq")
                                .toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "csv", "-o",
                                "references/describe/beatles-describe-csv.csv", "-q", pathQueryBeatlesDescribe);

                String expectedOutput = "Error: csv is not a valid output format for insert, delete, describe or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.contains(expectedOutput));
                assertNotEquals("", actualOutput);
        }

        @Test
        public void testBidingDescribeTsvInvalid() throws IOException {
                String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
                String pathQueryBeatlesDescribe = queriesPath.resolve("describe").resolve("describeBeatles.rq")
                                .toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "tsv", "-o",
                                "references/describe/beatles-describe-tsv.tsv", "-q", pathQueryBeatlesDescribe);

                String expectedOutput = "Error: tsv is not a valid output format for insert, delete, describe or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.contains(expectedOutput));
                assertNotEquals("", actualOutput);
        }

        @Test
        public void testBidingDescribeMarkdownInvalid() throws IOException {
                String pathInpBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
                String pathQueryBeatlesDescribe = queriesPath.resolve("describe").resolve("describeBeatles.rq")
                                .toString();

                int exitCode = cmd.execute("-i", pathInpBeatlesTTL, "-r", "markdown", "-o",
                                "references/describe/beatles-describe-markdown.md", "-q", pathQueryBeatlesDescribe);

                String expectedOutput = "Error: markdown is not a valid output format for insert, delete, describe or construct requests.";
                String actualOutput = err.toString().trim();

                assertEquals(1, exitCode);
                assertEquals("", out.toString());
                assertTrue(actualOutput.contains(expectedOutput));
                assertNotEquals("", actualOutput);
        }

        @Test
        public void testExecute_WhenInputFileDoesNotExist_ThrowsException() {
                String nonExistentFile = "non_existent_file.rq";
                String pathQueryBeatlesConstruct = queriesPath.resolve("construct").resolve("albumBeatles.rq")
                                .toString();

                int exitCode = cmd.execute("-i", nonExistentFile, "-f", "turtle", "-o", "output.ttl", "-q",
                                pathQueryBeatlesConstruct);

                assertEquals(1, exitCode);
                assertTrue(err.toString().contains("Failed to open RDF data file"));
        }

        @Test
        public void testExecute_WhenQueryFileDoesNotExist_ThrowsException() {
                String nonExistentQueryFile = "non_existent_query_file.rq";
                String validInputFile = inputPath.resolve("beatles.ttl").toString();

                int exitCode = cmd.execute("-i", validInputFile, "-f", "turtle", "-r", "turtle", "-o",
                                "output.ttl", "-q", nonExistentQueryFile);

                assertEquals(1, exitCode);
                assertTrue(err.toString().contains("Failed to open SPARQL query file"));
        }

        @Test
        public void testExecute_WhenInvalidQuery_ThrowsException() {
                String validInputFile = inputPath.resolve("beatles.ttl").toString();

                int exitCode = cmd.execute("-i", validInputFile, "-f", "turtle", "-r", "turtle", "-o",
                                "output.ttl", "-q", "SERRORELECT * WHERE { ?s ?p ?o }");

                assertEquals(1, exitCode);
                assertTrue(err.toString().contains("Failed to open SPARQL query file"));
        }

        @Test
        public void testLoadUniqFiles() throws IOException {
                String beatlesFile = inputPath.resolve("beatles.ttl").toString();
                String pathRefMultiFile = referencesPath.resolve("count").resolve("beatles.md").toString();
                String pathResMultiFile = resultPath.resolve("count").resolve("beatles.md").toString();

                int exitCode = cmd.execute("-i", beatlesFile, "-q", "SELECT (COUNT(*) AS ?count) WHERE { ?s ?p ?o }",
                                "-o", pathResMultiFile);

                assertEquals(0, exitCode);
                assertTrue(compareFiles(pathRefMultiFile, pathResMultiFile));
        }

        @Test
        public void testLoadMutiFiles() throws IOException {
                String beatlesFile = inputPath.resolve("beatles.ttl").toString();
                String cityFile = inputPath.resolve("city.ttl").toString();
                String pathRefMultiFile = referencesPath.resolve("count").resolve("beatles+city.md").toString();
                String pathResMultiFile = resultPath.resolve("count").resolve("beatles+city.md").toString();

                int exitCode = cmd.execute("-i", beatlesFile, cityFile, "-q",
                                "SELECT (COUNT(*) AS ?count) WHERE { ?s ?p ?o }", "-o", pathResMultiFile);

                assertEquals(0, exitCode);
                assertTrue(compareFiles(pathRefMultiFile, pathResMultiFile));
        }

        @Test
        public void testLoadMutiFilesRepertory() throws IOException {
                String input = inputPath.toString();
                String pathRefMultiFile = referencesPath.resolve("count").resolve("repertory.md").toString();
                String pathResMultiFile = resultPath.resolve("count").resolve("repertory.md").toString();

                int exitCode = cmd.execute("-i", input, "-q", "SELECT (COUNT(*) AS ?count) WHERE { ?s ?p ?o }", "-o",
                                pathResMultiFile);

                assertEquals(0, exitCode);
                assertTrue(compareFiles(pathRefMultiFile, pathResMultiFile));
        }

        @Test
        public void testLoadMutiFilesRepertoryRecursive() throws IOException {
                String input = inputPath.toString();
                String pathRefMultiFile = referencesPath.resolve("count").resolve("repertoryRecursive.md").toString();
                String pathResMultiFile = resultPath.resolve("count").resolve("repertoryRecursive.md").toString();

                int exitCode = cmd.execute("-i", input, "-q", "SELECT (COUNT(*) AS ?count) WHERE { ?s ?p ?o }", "-o",
                                pathResMultiFile, "-R");

                assertEquals(0, exitCode);
                assertTrue(compareFiles(pathRefMultiFile, pathResMultiFile));
        }

        @Test
        public void testLoadFromUrl() throws IOException {
                String rdfData = "https://files.inria.fr/corese/data/unit-test/beatles.ttl";
                String sparqlQuery = "https://files.inria.fr/corese/data/unit-test/spo.rq";

                String pathRefMultiFile = referencesPath.resolve("select").resolve("url.md").toString();
                String pathResMultiFile = resultPath.resolve("select").resolve("url.md").toString();

                int exitCode = cmd.execute("-i", rdfData, "-q", sparqlQuery, "-o", pathResMultiFile);

                assertEquals(0, exitCode);
                assertTrue(compareFiles(pathRefMultiFile, pathResMultiFile));
        }

}