package fr.inria.corese.command.programs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.api.Loader;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.print.CanonicalRdf10Format;
import picocli.CommandLine;

public class ValidateTest {

    private Validate validate = new Validate();
    private CommandLine cmd = new CommandLine(validate);

    private StringWriter out = new StringWriter();
    private StringWriter err = new StringWriter();

    Path inputRdfPath;
    Path inputRdfPathRecursive;
    Path inputShaclPath;
    Path referencesPath;
    Path resultPath;

    public ValidateTest() throws URISyntaxException {
        this.inputRdfPath = Paths.get(
                ValidateTest.class.getResource("/fr/inria/corese/command/programs/validate/inputRdf").toURI());

        this.inputRdfPathRecursive = Paths.get(
                ValidateTest.class.getResource("/fr/inria/corese/command/programs/validate/inputRdf-Recursive1")
                        .toURI());

        this.inputShaclPath = Paths.get(
                ValidateTest.class.getResource("/fr/inria/corese/command/programs/validate/inputShacl").toURI());

        this.referencesPath = Paths.get(
                ValidateTest.class.getResource("/fr/inria/corese/command/programs/validate/references").toURI());

        this.resultPath = Paths.get(
                ValidateTest.class.getResource("/fr/inria/corese/command/programs/validate/results").toURI());
    }

    private static final String UUID_REGEX = "<urn:uuid:[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}>";
    private static final String NEUTRAL_UUID = "<urn:uuid:00000000-0000-0000-0000-000000000000>";

    @BeforeEach
    public void setUp() {
        PrintWriter out = new PrintWriter(this.out);
        PrintWriter err = new PrintWriter(this.err);
        cmd.setOut(out);
        cmd.setErr(err);
    }

    public boolean compareFiles(String filePath1, String filePath2, Loader.format coreseFormat) throws IOException {
        // Get content of files
        String content1 = getStringContent(filePath1);
        String content2 = getStringContent(filePath2);

        // Remove UUIDs and Blank Nodes
        String clearContent1 = maskUUIDs(content1);
        String clearContent2 = maskUUIDs(content2);

        // Canonicalize RDF content
        String canonicallFile1 = canonicalize(clearContent1, coreseFormat);
        String canonicallFile2 = canonicalize(clearContent2, coreseFormat);

        return canonicallFile1.equals(canonicallFile2);
    }

    private String maskUUIDs(String content) {
        content = Pattern.compile(UUID_REGEX).matcher(content).replaceAll(NEUTRAL_UUID);
        return content;
    }

    private String getStringContent(String path) throws IOException {
        return new String(java.nio.file.Files.readAllBytes(Paths.get(path)));
    }

    private String canonicalize(String content, Loader.format coreseFormat) {

        // Content String to Input Stream
        InputStream is = new ByteArrayInputStream(content.getBytes());

        // Load RDF content into a Graph
        Graph graph = Graph.create();
        Load ld = Load.create(graph);

        try {
            ld.parse(is, coreseFormat);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Return Canonical RDF content
        return CanonicalRdf10Format.create(graph).toString();
    }

    @Test
    public void test1RDF1SHACLBeatlesOk() throws IOException {

        String inputRdf = this.inputRdfPath.resolve("beatles-ok.ttl").toString();
        String inputShacl = this.inputShaclPath.resolve("beatles-validator.ttl").toString();

        String expected = this.referencesPath.resolve("beatles-ok.ttl").toString();
        String result = this.resultPath.resolve("beatles-ok.ttl").toString();

        int exitCode = cmd.execute(
                "-i", inputRdf,
                "-s", inputShacl,
                "-o", result);

        assertEquals(0, exitCode);
        assertEquals("", this.out.toString());
        assertEquals("", this.err.toString());
        assertTrue(this.compareFiles(expected, result, Loader.format.TURTLE_FORMAT));
        assertNotEquals("", result);
    }

    @Test
    public void test1RDF1SHACLBeatlesErr() throws IOException {

        String inputRdf = this.inputRdfPath.resolve("beatles-err.ttl").toString();
        String inputShacl = this.inputShaclPath.resolve("beatles-validator.ttl").toString();

        String expected = this.referencesPath.resolve("beatles-err.ttl").toString();
        String result = this.resultPath.resolve("beatles-err.ttl").toString();

        int exitCode = cmd.execute(
                "-i", inputRdf,
                "-s", inputShacl,
                "-o", result);

        assertEquals(0, exitCode);
        assertEquals("", this.out.toString());
        assertEquals("", this.err.toString());
        assertTrue(this.compareFiles(expected, result, Loader.format.TURTLE_FORMAT));
        assertNotEquals("", result);
    }

    @Test
    public void test2RDF2SHACLBeatlesOk() throws IOException {

        String inputRdf1 = this.inputRdfPath.resolve("beatles-ok.ttl").toString();
        String inputShacl1 = this.inputShaclPath.resolve("beatles-validator.ttl").toString();

        String inputRdf2 = this.inputRdfPath.resolve("person-ok.ttl").toString();
        String inputShacl2 = this.inputShaclPath.resolve("person-validator.ttl").toString();

        String expected = this.referencesPath.resolve("beatles-person-ok.ttl").toString();
        String result = this.resultPath.resolve("beatles-person-ok.ttl").toString();

        int exitCode = cmd.execute(
                "-i", inputRdf1,
                "-i", inputRdf2,
                "-s", inputShacl1,
                "-s", inputShacl2,
                "-o", result);

        assertEquals(0, exitCode);
        assertEquals("", this.out.toString());
        assertEquals("", this.err.toString());
        assertTrue(this.compareFiles(expected, result, Loader.format.TURTLE_FORMAT));
        assertNotEquals("", result);
    }

    @Test
    public void test2RDF2SHACLBeatlesErr() throws IOException {

        String inputRdf1 = this.inputRdfPath.resolve("beatles-err.ttl").toString();
        String inputShacl1 = this.inputShaclPath.resolve("beatles-validator.ttl").toString();

        String inputRdf2 = this.inputRdfPath.resolve("person-err.ttl").toString();
        String inputShacl2 = this.inputShaclPath.resolve("person-validator.ttl").toString();

        String expected = this.referencesPath.resolve("beatles-person-err.ttl").toString();
        String result = this.resultPath.resolve("beatles-person-err.ttl").toString();

        int exitCode = cmd.execute(
                "-i", inputRdf1,
                "-i", inputRdf2,
                "-s", inputShacl1,
                "-s", inputShacl2,
                "-o", result);

        assertEquals(0, exitCode);
        assertEquals("", this.out.toString());
        assertEquals("", this.err.toString());
        assertTrue(this.compareFiles(expected, result, Loader.format.TURTLE_FORMAT));
        assertNotEquals("", result);
    }

    @Test
    public void test1RDFUrl1SHACLBeatlesOk() throws IOException {

        String inputRdf = "https://files.inria.fr/corese/data/unit-test/beatles.ttl";
        String inputShacl = this.inputShaclPath.resolve("beatles-validator.ttl").toString();

        String expected = this.referencesPath.resolve("beatles-ok.ttl").toString();
        String result = this.resultPath.resolve("beatles-ok.ttl").toString();

        int exitCode = cmd.execute(
                "-i", inputRdf,
                "-s", inputShacl,
                "-o", result);

        assertEquals(0, exitCode);
        assertEquals("", this.out.toString());
        assertEquals("", this.err.toString());
        assertTrue(this.compareFiles(expected, result, Loader.format.TURTLE_FORMAT));
        assertNotEquals("", result);
    }

    @Test
    public void test1RDF1SHACLUrlBeatlesOk() throws IOException {

        String inputRdf = this.inputRdfPath.resolve("beatles-ok.ttl").toString();
        String inputShacl = "https://files.inria.fr/corese/data/unit-test/beatles-validator.ttl";

        String expected = this.referencesPath.resolve("beatles-ok.ttl").toString();
        String result = this.resultPath.resolve("beatles-ok.ttl").toString();

        int exitCode = cmd.execute(
                "-i", inputRdf,
                "-s", inputShacl,
                "-o", result);

        assertEquals(0, exitCode);
        assertEquals("", this.out.toString());
        assertEquals("", this.err.toString());
        assertTrue(this.compareFiles(expected, result, Loader.format.TURTLE_FORMAT));
        assertNotEquals("", result);
    }

    @Test
    public void testRDFSHACLDirectoryBeatlesErr() throws IOException {

        String inputRdf = this.inputRdfPath.toString();
        String inputShacl = this.inputShaclPath.toString();

        String expected = this.referencesPath.resolve("directory-err.ttl").toString();
        String result = this.resultPath.resolve("directory-err.ttl").toString();

        int exitCode = cmd.execute(
                "-i", inputRdf,
                "-s", inputShacl,
                "-o", result);

        assertEquals(0, exitCode);
        assertEquals("", this.out.toString());
        assertEquals("", this.err.toString());
        assertTrue(this.compareFiles(expected, result, Loader.format.TURTLE_FORMAT));
        assertNotEquals("", result);
    }

    @Test
    public void test1RDF1SHACLBeatlesOkrdf() throws IOException {

        String inputRdf = this.inputRdfPath.resolve("beatles-ok.rdf").toString();
        String inputShacl = this.inputShaclPath.resolve("beatles-validator.rdf").toString();

        String expected = this.referencesPath.resolve("beatles-ok.rdf").toString();
        String result = this.resultPath.resolve("beatles-ok.rdf").toString();

        int exitCode = cmd.execute(
                "-i", inputRdf,
                "-if", "rdfxml",
                "-s", inputShacl,
                "-sf", "rdfxml",
                "-o", result,
                "-of", "rdfxml");

        assertEquals(0, exitCode);
        assertEquals("", this.out.toString());
        assertEquals("", this.err.toString());
        assertTrue(this.compareFiles(expected, result, Loader.format.RDFXML_FORMAT));
        assertNotEquals("", result);
    }

    @Test
    public void test1RDF1SHACLBeatlesOkjsonld() throws IOException {

        String inputRdf = this.inputRdfPath.resolve("beatles-ok.jsonld").toString();
        String inputShacl = this.inputShaclPath.resolve("beatles-validator.jsonld").toString();

        String expected = this.referencesPath.resolve("beatles-ok.jsonld").toString();
        String result = this.resultPath.resolve("beatles-ok.jsonld").toString();

        int exitCode = cmd.execute(
                "-i", inputRdf,
                "-if", "jsonld",
                "-s", inputShacl,
                "-sf", "jsonld",
                "-o", result,
                "-of", "jsonld");

        assertEquals(0, exitCode);
        assertEquals("", this.out.toString());
        assertEquals("", this.err.toString());
        assertTrue(this.compareFiles(expected, result, Loader.format.JSONLD_FORMAT));
        assertNotEquals("", result);
    }

    @Test
    public void testRDFSHACLDirectoryRecursiveBeatlesErr() throws IOException {

        String inputRdf = this.inputRdfPathRecursive.toString();
        String inputShacl = this.inputShaclPath.toString();

        String expected = this.referencesPath.resolve("directory-err.ttl").toString();
        String result = this.resultPath.resolve("directory-err.ttl").toString();

        int exitCode = cmd.execute(
                "-i", inputRdf,
                "-s", inputShacl,
                "-o", result,
                "-R");

        assertEquals(0, exitCode);
        assertEquals("", this.out.toString());
        assertEquals("", this.err.toString());
        assertTrue(this.compareFiles(expected, result, Loader.format.TURTLE_FORMAT));
        assertNotEquals("", result);
    }

}
