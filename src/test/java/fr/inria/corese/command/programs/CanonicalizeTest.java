package fr.inria.corese.command.programs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import picocli.CommandLine;

public class CanonicalizeTest {

    private Canonicalize canonicalize = new Canonicalize();
    private CommandLine cmd = new CommandLine(canonicalize);

    private StringWriter out = new StringWriter();
    private StringWriter err = new StringWriter();

    Path inputPath;
    Path referencesPath;
    Path resultPath;

    public CanonicalizeTest() throws URISyntaxException {
        this.inputPath = Paths.get(
                CanonicalizeTest.class.getResource("/fr/inria/corese/command/programs/canonicalize/input/").toURI());

        this.referencesPath = Paths.get(CanonicalizeTest.class
                .getResource("/fr/inria/corese/command/programs/canonicalize/references/").toURI());

        this.resultPath = Paths.get(
                CanonicalizeTest.class.getResource("/fr/inria/corese/command/programs/canonicalize/results/").toURI());
    }

    @BeforeEach
    public void setUp() {
        PrintWriter out = new PrintWriter(this.out);
        PrintWriter err = new PrintWriter(this.err);
        cmd.setOut(out);
        cmd.setErr(err);
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
    public void test1InputFile() {
        String input = inputPath.resolve("beatles.ttl").toString();
        String expected = referencesPath.resolve("beatles.nq").toString();
        String output = resultPath.resolve("beatles.nq").toString();

        String[] args = { "-i", input, "-a", "rdfc-1.0-sha256", "-o", output };
        int exitCode = cmd.execute(args);

        assertEquals(0, exitCode);
        assertEquals("", err.toString());
        assertEquals("", out.toString());
        assertEquals(getStringContent(expected), getStringContent(output));
    }

    @Test
    public void test1Url() {
        String input = "https://files.inria.fr/corese/data/unit-test/beatles.ttl";
        String expected = referencesPath.resolve("beatles.nq").toString();
        String output = resultPath.resolve("beatles.nq").toString();

        String[] args = { "-i", input, "-a", "rdfc-1.0-sha256", "-o", output };
        int exitCode = cmd.execute(args);

        assertEquals(0, exitCode);
        assertEquals("", err.toString());
        assertEquals("", out.toString());
        assertEquals(getStringContent(expected), getStringContent(output));
    }

    @Test
    public void test1Directory() {
        String input = inputPath.toString();
        String expected = referencesPath.resolve("beatles.nq").toString();
        String output = resultPath.resolve("beatles.nq").toString();

        String[] args = { "-i", input, "-a", "rdfc-1.0-sha256", "-o", output };
        int exitCode = cmd.execute(args);

        assertEquals(0, exitCode);
        assertEquals("", err.toString());
        assertEquals("", out.toString());
        assertEquals(getStringContent(expected), getStringContent(output));
    }

    @Test
    public void test1DirectoryRecursive() {
        String input = inputPath.toString();
        String expected = referencesPath.resolve("recursive.nq").toString();
        String output = resultPath.resolve("recursive.nq").toString();

        String[] args = { "-i", input, "-a", "rdfc-1.0-sha256", "-o", output, "-R" };
        int exitCode = cmd.execute(args);

        assertEquals(0, exitCode);
        assertEquals("", err.toString());
        assertEquals("", out.toString());
        assertEquals(getStringContent(expected), getStringContent(output));
    }

    @Test
    public void testMultipleSources() {
        String input1 = inputPath.resolve("beatles.ttl").toString();
        String input2 = Paths.get(inputPath.toString(), "recursive-level1", "person.ttl").toString();
        String expected = referencesPath.resolve("multiple.nq").toString();
        String output = resultPath.resolve("multiple.nq").toString();

        String[] args = { "-i", input1, input2, "-a", "rdfc-1.0-sha256", "-o", output };
        int exitCode = cmd.execute(args);

        assertEquals(0, exitCode);
        assertEquals("", err.toString());
        assertEquals("", out.toString());
        assertEquals(getStringContent(expected), getStringContent(output));
    }

    @Test
    public void testInputFormat() {
        String input = inputPath.resolve("beatles.ttl").toString();
        String expected = referencesPath.resolve("beatles.nq").toString();
        String output = resultPath.resolve("beatles.nq").toString();

        String[] args = { "-i", input, "-f", "text/turtle", "-a", "rdfc-1.0-sha256", "-o", output };
        int exitCode = cmd.execute(args);

        assertEquals(0, exitCode);
        assertEquals("", err.toString());
        assertEquals("", out.toString());
        assertEquals(getStringContent(expected), getStringContent(output));
    }

    @Test
    public void testInputBadFormat() {
        String input = inputPath.resolve("beatles.ttl").toString();
        String output = resultPath.resolve("beatles.nq").toString();

        String[] args = { "-i", input, "-f", "rdfxml", "-a", "rdfc-1.0-sha256", "-o", output };
        int exitCode = cmd.execute(args);

        assertEquals(1, exitCode);
        assertEquals("", out.toString());
        assertTrue(err.toString().contains("Failed to parse RDF file."));
    }

    @Test
    public void testSha384() {
        String input = inputPath.resolve("beatles.ttl").toString();
        String expected = referencesPath.resolve("beatles-sha384.nq").toString();
        String output = resultPath.resolve("beatles-sha384.nq").toString();

        String[] args = { "-i", input, "-a", "rdfc-1.0-sha384", "-o", output };
        int exitCode = cmd.execute(args);

        assertEquals(0, exitCode);
        assertEquals("", err.toString());
        assertEquals("", out.toString());
        assertEquals(getStringContent(expected), getStringContent(output));
    }

    @Test
    public void testDefaultAlgorithm() {
        String input = inputPath.resolve("beatles.ttl").toString();
        String expected = referencesPath.resolve("beatles.nq").toString();
        String output = resultPath.resolve("beatles.nq").toString();

        String[] args = { "-i", input, "-o", output };
        int exitCode = cmd.execute(args);

        assertEquals(0, exitCode);
        assertEquals("", err.toString());
        assertEquals("", out.toString());
        assertEquals(getStringContent(expected), getStringContent(output));
    }

}
