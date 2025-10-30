package fr.inria.corese.command.programs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import picocli.CommandLine;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

class CanonicalizeTest {

    private Canonicalize canonicalize = new Canonicalize();
    private CommandLine cmd = new CommandLine(canonicalize);

    private StringWriter out = new StringWriter();
    private StringWriter err = new StringWriter();

    Path inputPath;
    Path referencesPath;
    Path resultPath;

    CanonicalizeTest() throws URISyntaxException {
        this.inputPath =
                Paths.get(
                        CanonicalizeTest.class
                                .getResource(
                                        "/fr/inria/corese/command/programs/canonicalize/input/")
                                .toURI());

        this.referencesPath =
                Paths.get(
                        CanonicalizeTest.class
                                .getResource(
                                        "/fr/inria/corese/command/programs/canonicalize/references/")
                                .toURI());

        this.resultPath =
                Paths.get(
                        CanonicalizeTest.class
                                .getResource(
                                        "/fr/inria/corese/command/programs/canonicalize/results/")
                                .toURI());
    }

    @BeforeEach
    void setUp() {
        PrintWriter outputWriter = new PrintWriter(this.out);
        PrintWriter errorWriter = new PrintWriter(this.err);
        cmd.setOut(outputWriter);
        cmd.setErr(errorWriter);
    }

    private String getStringContent(String path) {
        try {
            return Files.readString(Paths.get(path));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Test
    void test1InputFile() {
        String input = inputPath.resolve("beatles.ttl").toString();
        String expected = referencesPath.resolve("beatles.nq").toString();
        String output = resultPath.resolve("beatles.nq").toString();

        String[] args = {"-i", input, "-a", "rdfc-1.0-sha256", "-o", output};
        int exitCode = cmd.execute(args);

        assertEquals(0, exitCode);
        assertEquals("", err.toString());
        assertEquals("", out.toString());
        assertEquals(getStringContent(expected), getStringContent(output));
    }

    @Test
    void test1Url() {
        String input = "https://files.inria.fr/corese/data/unit-test/beatles.ttl";
        String expected = referencesPath.resolve("beatles.nq").toString();
        String output = resultPath.resolve("beatles.nq").toString();

        String[] args = {"-i", input, "-a", "rdfc-1.0-sha256", "-o", output};
        int exitCode = cmd.execute(args);

        assertEquals(0, exitCode);
        assertEquals("", err.toString());
        assertEquals("", out.toString());
        assertEquals(getStringContent(expected), getStringContent(output));
    }

    @Test
    void test1Directory() {
        String input = inputPath.toString();
        String expected = referencesPath.resolve("beatles.nq").toString();
        String output = resultPath.resolve("beatles.nq").toString();

        String[] args = {"-i", input, "-a", "rdfc-1.0-sha256", "-o", output};
        int exitCode = cmd.execute(args);

        assertEquals(0, exitCode);
        assertEquals("", err.toString());
        assertEquals("", out.toString());
        assertEquals(getStringContent(expected), getStringContent(output));
    }

    @Test
    void test1DirectoryRecursive() {
        String input = inputPath.toString();
        String expected = referencesPath.resolve("recursive.nq").toString();
        String output = resultPath.resolve("recursive.nq").toString();

        String[] args = {"-i", input, "-a", "rdfc-1.0-sha256", "-o", output, "-R"};
        int exitCode = cmd.execute(args);

        assertEquals(0, exitCode);
        assertEquals("", err.toString());
        assertEquals("", out.toString());
        assertEquals(getStringContent(expected), getStringContent(output));
    }

    @Test
    void testMultipleSources() {
        String input1 = inputPath.resolve("beatles.ttl").toString();
        String input2 =
                Paths.get(inputPath.toString(), "recursive-level1", "person.ttl").toString();
        String expected = referencesPath.resolve("multiple.nq").toString();
        String output = resultPath.resolve("multiple.nq").toString();

        String[] args = {"-i", input1, input2, "-a", "rdfc-1.0-sha256", "-o", output};
        int exitCode = cmd.execute(args);

        assertEquals(0, exitCode);
        assertEquals("", err.toString());
        assertEquals("", out.toString());
        assertEquals(getStringContent(expected), getStringContent(output));
    }

    @Test
    void testInputFormat() {
        String input = inputPath.resolve("beatles.ttl").toString();
        String expected = referencesPath.resolve("beatles.nq").toString();
        String output = resultPath.resolve("beatles.nq").toString();

        String[] args = {"-i", input, "-f", "text/turtle", "-a", "rdfc-1.0-sha256", "-o", output};
        int exitCode = cmd.execute(args);

        assertEquals(0, exitCode);
        assertEquals("", err.toString());
        assertEquals("", out.toString());
        assertEquals(getStringContent(expected), getStringContent(output));
    }

    @Test
    void testInputBadFormat() {
        String input = inputPath.resolve("beatles.ttl").toString();
        String output = resultPath.resolve("beatles.nq").toString();

        String[] args = {"-i", input, "-f", "rdfxml", "-a", "rdfc-1.0-sha256", "-o", output};
        int exitCode = cmd.execute(args);

        assertEquals(1, exitCode);
        assertEquals("", out.toString());
        assertTrue(err.toString().contains("Failed to parse RDF file."));
    }

    @Test
    void testSha384() {
        String input = inputPath.resolve("beatles.ttl").toString();
        String expected = referencesPath.resolve("beatles-sha384.nq").toString();
        String output = resultPath.resolve("beatles-sha384.nq").toString();

        String[] args = {"-i", input, "-a", "rdfc-1.0-sha384", "-o", output};
        int exitCode = cmd.execute(args);

        assertEquals(0, exitCode);
        assertEquals("", err.toString());
        assertEquals("", out.toString());
        assertEquals(getStringContent(expected), getStringContent(output));
    }

    @Test
    void testDefaultAlgorithm() {
        String input = inputPath.resolve("beatles.ttl").toString();
        String expected = referencesPath.resolve("beatles.nq").toString();
        String output = resultPath.resolve("beatles.nq").toString();

        String[] args = {"-i", input, "-o", output};
        int exitCode = cmd.execute(args);

        assertEquals(0, exitCode);
        assertEquals("", err.toString());
        assertEquals("", out.toString());
        assertEquals(getStringContent(expected), getStringContent(output));
    }
}
