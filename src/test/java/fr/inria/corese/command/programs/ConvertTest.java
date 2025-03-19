package fr.inria.corese.command.programs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fr.inria.corese.command.utils.loader.rdf.EnumRdfInputFormat;
import fr.inria.corese.command.utils.loader.rdf.RdfDataLoader;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.print.CanonicalRdf10Format;
import picocli.CommandLine;

public class ConvertTest {

    private Convert convert = new Convert();
    private CommandLine cmd = new CommandLine(convert);

    private StringWriter out = new StringWriter();
    private StringWriter err = new StringWriter();

    Path inputPath;
    Path referencesPath;
    Path resultPath;

    public ConvertTest() throws URISyntaxException {
        this.inputPath = Paths
                .get(ConvertTest.class.getResource("/fr/inria/corese/command/programs/convert/input/").toURI());

        this.referencesPath = Paths.get(
                ConvertTest.class.getResource("/fr/inria/corese/command/programs/convert/references/").toURI());

        this.resultPath = Paths.get(
                ConvertTest.class.getResource("/fr/inria/corese/command/programs/convert/results/").toURI());
    }

    private String canonicalize(String path) {
        Graph graph = Graph.create();
        Load ld = Load.create(graph);

        try {
            ld.parse(path, "");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return CanonicalRdf10Format.create(graph).toString();
    }

    @BeforeEach
    public void setUp() {
        PrintWriter out = new PrintWriter(this.out);
        PrintWriter err = new PrintWriter(this.err);
        cmd.setOut(out);
        cmd.setErr(err);
    }

    @Test
    public void testConvertTurtleToxml() {
        String pathinputBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
        String pathRefBeatlesXML = referencesPath.resolve("ttl.beatles.rdf").toString();
        String pathOutBeatlesXML = resultPath.resolve("ttl.beatles.rdf").toString();

        int exitCode = cmd.execute("-i", pathinputBeatlesTTL, "-of", "RDFXML", "-o", pathOutBeatlesXML);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertEquals(canonicalize(pathRefBeatlesXML), canonicalize(pathOutBeatlesXML));
        assertNotEquals("", pathOutBeatlesXML);

    }

    @Test
    public void testConvertTurtleToJsonld() {
        String pathInputBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
        String pathRefBeatlesJSON = referencesPath.resolve("ttl.beatles.jsonld").toString();
        String pathOutBeatlesJSON = resultPath.resolve("ttl.beatles.jsonld").toString();

        int exitCode = cmd.execute("-i", pathInputBeatlesTTL, "-of", "JSONLD", "-o", pathOutBeatlesJSON);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertEquals(canonicalize(pathRefBeatlesJSON), canonicalize(pathOutBeatlesJSON));
        assertNotEquals("", pathOutBeatlesJSON);

    }

    @Test
    public void testConvertTurtleToTrig() {
        String pathInputBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
        String pathRefBeatlesTRIG = referencesPath.resolve("ttl.beatles.trig").toString();
        String pathOutBeatlesTRIG = resultPath.resolve("ttl.beatles.trig").toString();

        int exitCode = cmd.execute("-i", pathInputBeatlesTTL, "-of", "TRIG", "-o", pathOutBeatlesTRIG);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertEquals(canonicalize(pathRefBeatlesTRIG), canonicalize(pathOutBeatlesTRIG));
        assertNotEquals("", pathOutBeatlesTRIG);

    }

    @Test
    public void testConvertTurtleToTurtle() {
        String pathInputBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
        String pathRefBeatlesTTL = referencesPath.resolve("ttl.beatles.ttl").toString();
        String pathOutBeatlesTTL = resultPath.resolve("ttl.beatles.ttl").toString();

        int exitCode = cmd.execute("-i", pathInputBeatlesTTL, "-of", "TURTLE", "-o", pathOutBeatlesTTL);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertEquals(canonicalize(pathRefBeatlesTTL), canonicalize(pathOutBeatlesTTL));
        assertNotEquals("", pathOutBeatlesTTL);

    }

    @Test
    public void testConvertTurtleToNt() {
        String pathInputBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
        String pathRefBeatlesNT = referencesPath.resolve("ttl.beatles.nt").toString();
        String pathOutBeatlesNT = resultPath.resolve("ttl.beatles.nt").toString();

        int exitCode = cmd.execute("-i", pathInputBeatlesTTL, "-of", "NTRIPLES", "-o", pathOutBeatlesNT);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertEquals(canonicalize(pathRefBeatlesNT), canonicalize(pathOutBeatlesNT));
        assertNotEquals("", pathOutBeatlesNT);

    }

    @Test
    public void testConvertTurtleToNq() {
        String pathInputBeatlesTTL = inputPath.resolve("beatles.ttl").toString();
        String pathRefBeatlesNQ = referencesPath.resolve("ttl.beatles.nq").toString();
        String pathOutBeatlesNQ = resultPath.resolve("ttl.beatles.nq").toString();

        int exitCode = cmd.execute("-i", pathInputBeatlesTTL, "-of", "NQUADS", "-o", pathOutBeatlesNQ);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertEquals(canonicalize(pathRefBeatlesNQ), canonicalize(pathOutBeatlesNQ));
        assertNotEquals("", pathOutBeatlesNQ);

    }

    @Test
    public void testConvertXmltoXml() {
        String pathInputBeatlesXML = inputPath.resolve("beatles.rdf").toString();
        String pathRefBeatlesXML = referencesPath.resolve("rdf.beatles.rdf").toString();
        String pathOutBeatlesXML = resultPath.resolve("rdf.beatles.rdf").toString();

        int exitCode = cmd.execute("-i", pathInputBeatlesXML, "-of", "RDFXML", "-o", pathOutBeatlesXML);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertEquals(canonicalize(pathRefBeatlesXML), canonicalize(pathOutBeatlesXML));
        assertNotEquals("", pathOutBeatlesXML);

    }

    @Test
    public void testConvertXmlToJsonld() {
        String pathInputBeatlesXML = inputPath.resolve("beatles.rdf").toString();
        String pathRefBeatlesJSON = referencesPath.resolve("rdf.beatles.jsonld").toString();
        String pathOutBeatlesJSON = resultPath.resolve("rdf.beatles.jsonld").toString();

        int exitCode = cmd.execute("-i", pathInputBeatlesXML, "-of", "JSONLD", "-o", pathOutBeatlesJSON);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertEquals(canonicalize(pathRefBeatlesJSON), canonicalize(pathOutBeatlesJSON));
        assertNotEquals("", pathOutBeatlesJSON);

    }

    @Test
    public void testConvertXmlToTrig() {
        String pathInputBeatlesXML = inputPath.resolve("beatles.rdf").toString();
        String pathRefBeatlesTRIG = referencesPath.resolve("rdf.beatles.trig").toString();
        String pathOutBeatlesTRIG = resultPath.resolve("rdf.beatles.trig").toString();

        int exitCode = cmd.execute("-i", pathInputBeatlesXML, "-of", "TRIG", "-o", pathOutBeatlesTRIG);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertEquals(canonicalize(pathRefBeatlesTRIG), canonicalize(pathOutBeatlesTRIG));
        assertNotEquals("", pathOutBeatlesTRIG);

    }

    @Test
    public void testConvertXmlTiTurtle() {
        String pathInputBeatlesXML = inputPath.resolve("beatles.rdf").toString();
        String pathRefBeatlesTTL = referencesPath.resolve("rdf.beatles.ttl").toString();
        String pathOutBeatlesTTL = resultPath.resolve("rdf.beatles.ttl").toString();

        int exitCode = cmd.execute("-i", pathInputBeatlesXML, "-of", "TURTLE", "-o", pathOutBeatlesTTL);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertEquals(canonicalize(pathRefBeatlesTTL), canonicalize(pathOutBeatlesTTL));
        assertNotEquals("", pathOutBeatlesTTL);

    }

    @Test
    public void testConvertXmlToNt() {
        String pathInputBeatlesXML = inputPath.resolve("beatles.rdf").toString();
        String pathRefBeatlesNT = referencesPath.resolve("rdf.beatles.nt").toString();
        String pathOutBeatlesNT = resultPath.resolve("rdf.beatles.nt").toString();

        int exitCode = cmd.execute("-i", pathInputBeatlesXML, "-of", "NTRIPLES", "-o", pathOutBeatlesNT);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertEquals(canonicalize(pathRefBeatlesNT), canonicalize(pathOutBeatlesNT));
        assertNotEquals("", pathOutBeatlesNT);

    }

    @Test
    public void testConvertXmlToNq() {
        String pathInputBeatlesXML = inputPath.resolve("beatles.rdf").toString();
        String pathRefBeatlesNQ = referencesPath.resolve("rdf.beatles.nq").toString();
        String pathOutBeatlesNQ = resultPath.resolve("rdf.beatles.nq").toString();

        int exitCode = cmd.execute("-i", pathInputBeatlesXML, "-of", "NQUADS", "-o", pathOutBeatlesNQ);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertEquals(canonicalize(pathRefBeatlesNQ), canonicalize(pathOutBeatlesNQ));
        assertNotEquals("", pathOutBeatlesNQ);

    }

    @Test
    public void testConvertTrigToXml() {
        String pathInputBeatlesTRIG = inputPath.resolve("beatles.trig").toString();
        String pathRefBeatlesXML = referencesPath.resolve("trig.beatles.rdf").toString();
        String pathOutBeatlesXML = resultPath.resolve("trig.beatles.rdf").toString();

        int exitCode = cmd.execute("-i", pathInputBeatlesTRIG, "-of", "RDFXML", "-o", pathOutBeatlesXML);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertEquals(canonicalize(pathRefBeatlesXML), canonicalize(pathOutBeatlesXML));
        assertNotEquals("", pathOutBeatlesXML);

    }

    @Test
    public void testConvertTrigToJsonld() {
        String pathInputBeatlesTRIG = inputPath.resolve("beatles.trig").toString();
        String pathRefBeatlesJSON = referencesPath.resolve("trig.beatles.jsonld").toString();
        String pathOutBeatlesJSON = resultPath.resolve("trig.beatles.jsonld").toString();

        int exitCode = cmd.execute("-i", pathInputBeatlesTRIG, "-of", "JSONLD", "-o", pathOutBeatlesJSON);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertEquals(canonicalize(pathRefBeatlesJSON), canonicalize(pathOutBeatlesJSON));
        assertNotEquals("", pathOutBeatlesJSON);

    }

    @Test
    public void testConvertTrigToTrig() {
        String pathInputBeatlesTRIG = inputPath.resolve("beatles.trig").toString();
        String pathExpectBeatlesTRIG = referencesPath.resolve("trig.beatles.trig").toString();
        String pathOutBeatlesTRIG = resultPath.resolve("trig.beatles.trig").toString();

        int exitCode = cmd.execute("-i", pathInputBeatlesTRIG, "-of", "TRIG", "-o", pathOutBeatlesTRIG);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertEquals(canonicalize(pathExpectBeatlesTRIG), canonicalize(pathOutBeatlesTRIG));
        assertNotEquals("", pathOutBeatlesTRIG);

    }

    @Test
    public void testConvertTrigToTurtle() {
        String pathInputBeatlesTRIG = inputPath.resolve("beatles.trig").toString();
        String pathRefBeatlesTTL = referencesPath.resolve("trig.beatles.ttl").toString();
        String pathOutBeatlesTTL = resultPath.resolve("trig.beatles.ttl").toString();

        int exitCode = cmd.execute("-i", pathInputBeatlesTRIG, "-of", "TURTLE", "-o", pathOutBeatlesTTL);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertEquals(canonicalize(pathRefBeatlesTTL), canonicalize(pathOutBeatlesTTL));
        assertNotEquals("", pathOutBeatlesTTL);

    }

    @Test
    public void testConvertTrigToNt() {
        String pathInputBeatlesTRIG = inputPath.resolve("beatles.trig").toString();
        String pathRefBeatlesNT = referencesPath.resolve("trig.beatles.nt").toString();
        String pathOutBeatlesNT = resultPath.resolve("trig.beatles.nt").toString();

        int exitCode = cmd.execute("-i", pathInputBeatlesTRIG, "-of", "NTRIPLES", "-o", pathOutBeatlesNT);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertEquals(canonicalize(pathRefBeatlesNT), canonicalize(pathOutBeatlesNT));
        assertNotEquals("", pathOutBeatlesNT);

    }

    @Test
    public void testConvertTrigToNq() {
        String pathInputBeatlesTRIG = inputPath.resolve("beatles.trig").toString();
        String pathRefBeatlesNQ = referencesPath.resolve("trig.beatles.nq").toString();
        String pathOutBeatlesNQ = resultPath.resolve("trig.beatles.nq").toString();

        int exitCode = cmd.execute("-i", pathInputBeatlesTRIG, "-of", "NQUADS", "-o", pathOutBeatlesNQ);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertEquals(canonicalize(pathRefBeatlesNQ), canonicalize(pathOutBeatlesNQ));
        assertNotEquals("", pathOutBeatlesNQ);

    }

    @Test
    public void testConvertJsonldToXml() {
        String pathInputBeatlesJSONLD = inputPath.resolve("beatles.jsonld").toString();
        String pathRefBeatlesXML = referencesPath.resolve("jsonld.beatles.rdf").toString();
        String pathOutBeatlesXML = resultPath.resolve("jsonld.beatles.rdf").toString();

        int exitCode = cmd.execute("-i", pathInputBeatlesJSONLD, "-of", "RDFXML", "-o", pathOutBeatlesXML);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertEquals(canonicalize(pathRefBeatlesXML), canonicalize(pathOutBeatlesXML));
        assertNotEquals("", pathOutBeatlesXML);

    }

    @Test
    public void testConvertJsonldToJsonld() {
        String pathInputBeatlesJSONLD = inputPath.resolve("beatles.jsonld").toString();
        String pathRefBeatlesJSON = referencesPath.resolve("jsonld.beatles.jsonld").toString();
        String pathOutBeatlesJSON = resultPath.resolve("jsonld.beatles.jsonld").toString();

        int exitCode = cmd.execute("-i", pathInputBeatlesJSONLD, "-of", "JSONLD", "-o", pathOutBeatlesJSON);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertEquals(canonicalize(pathRefBeatlesJSON), canonicalize(pathOutBeatlesJSON));
        assertNotEquals("", pathOutBeatlesJSON);

    }

    @Test
    public void testConvertJsonldToTrig() {
        String pathInputBeatlesJSONLD = inputPath.resolve("beatles.jsonld").toString();
        String pathRefBeatlesTRIG = referencesPath.resolve("jsonld.beatles.trig").toString();
        String pathOutBeatlesTRIG = resultPath.resolve("jsonld.beatles.trig").toString();

        int exitCode = cmd.execute("-i", pathInputBeatlesJSONLD, "-of", "TRIG", "-o", pathOutBeatlesTRIG);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertEquals(canonicalize(pathRefBeatlesTRIG), canonicalize(pathOutBeatlesTRIG));
        assertNotEquals("", pathOutBeatlesTRIG);

    }

    @Test
    public void testConvertJsonldToTurtle() {
        String pathInputBeatlesJSONLD = inputPath.resolve("beatles.jsonld").toString();
        String pathRefBeatlesTTL = referencesPath.resolve("jsonld.beatles.ttl").toString();
        String pathOutBeatlesTTL = resultPath.resolve("jsonld.beatles.ttl").toString();

        int exitCode = cmd.execute("-i", pathInputBeatlesJSONLD, "-of", "TURTLE", "-o", pathOutBeatlesTTL);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertEquals(canonicalize(pathRefBeatlesTTL), canonicalize(pathOutBeatlesTTL));
        assertNotEquals("", pathOutBeatlesTTL);

    }

    @Test
    public void testConvertJsonldToNt() {
        String pathInputBeatlesJSONLD = inputPath.resolve("beatles.jsonld").toString();
        String pathRefBeatlesNT = referencesPath.resolve("jsonld.beatles.nt").toString();
        String pathOutBeatlesNT = resultPath.resolve("jsonld.beatles.nt").toString();

        int exitCode = cmd.execute("-i", pathInputBeatlesJSONLD, "-of", "NTRIPLES", "-o", pathOutBeatlesNT);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertEquals(canonicalize(pathRefBeatlesNT), canonicalize(pathOutBeatlesNT));
        assertNotEquals("", pathOutBeatlesNT);

    }

    @Test
    public void testConvertJsonldToNq() {
        String pathInputBeatlesJSONLD = inputPath.resolve("beatles.jsonld").toString();
        String pathRefBeatlesNQ = referencesPath.resolve("jsonld.beatles.nq").toString();
        String pathOutBeatlesNQ = resultPath.resolve("jsonld.beatles.nq").toString();

        int exitCode = cmd.execute("-i", pathInputBeatlesJSONLD, "-of", "NQUADS", "-o", pathOutBeatlesNQ);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertEquals(canonicalize(pathRefBeatlesNQ), canonicalize(pathOutBeatlesNQ));
        assertNotEquals("", pathOutBeatlesNQ);

    }

    @Test
    public void testConvertNtToXml() {
        String pathInputBeatlesNT = inputPath.resolve("beatles.nt").toString();
        String pathRefBeatlesXML = referencesPath.resolve("nt.beatles.rdf").toString();
        String pathOutBeatlesXML = resultPath.resolve("nt.beatles.rdf").toString();

        int exitCode = cmd.execute("-i", pathInputBeatlesNT, "-of", "RDFXML", "-o", pathOutBeatlesXML);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertEquals(canonicalize(pathRefBeatlesXML), canonicalize(pathOutBeatlesXML));
        assertNotEquals("", pathOutBeatlesXML);

    }

    @Test
    public void testConvertNtToJsonld() {
        String pathInputBeatlesNT = inputPath.resolve("beatles.nt").toString();
        String pathRefBeatlesJSON = referencesPath.resolve("nt.beatles.jsonld").toString();
        String pathOutBeatlesJSON = resultPath.resolve("nt.beatles.jsonld").toString();

        int exitCode = cmd.execute("-i", pathInputBeatlesNT, "-of", "JSONLD", "-o", pathOutBeatlesJSON);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertEquals(canonicalize(pathRefBeatlesJSON), canonicalize(pathOutBeatlesJSON));
        assertNotEquals("", pathOutBeatlesJSON);

    }

    @Test
    public void testConvertNtToTrig() {
        String pathInputBeatlesNT = inputPath.resolve("beatles.nt").toString();
        String pathRefBeatlesTRIG = referencesPath.resolve("nt.beatles.trig").toString();
        String pathOutBeatlesTRIG = resultPath.resolve("nt.beatles.trig").toString();

        int exitCode = cmd.execute("-i", pathInputBeatlesNT, "-of", "TRIG", "-o", pathOutBeatlesTRIG);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertEquals(canonicalize(pathRefBeatlesTRIG), canonicalize(pathOutBeatlesTRIG));
        assertNotEquals("", pathOutBeatlesTRIG);

    }

    @Test
    public void testConvertNtToTurtle() {
        String pathInputBeatlesNT = inputPath.resolve("beatles.nt").toString();
        String pathRefBeatlesTTL = referencesPath.resolve("nt.beatles.ttl").toString();
        String pathOutBeatlesTTL = resultPath.resolve("nt.beatles.ttl").toString();

        int exitCode = cmd.execute("-i", pathInputBeatlesNT, "-of", "TURTLE", "-o", pathOutBeatlesTTL);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertEquals(canonicalize(pathRefBeatlesTTL), canonicalize(pathOutBeatlesTTL));
        assertNotEquals("", pathOutBeatlesTTL);

    }

    @Test
    public void testConvertNtToNt() {
        String pathInputBeatlesNT = inputPath.resolve("beatles.nt").toString();
        String pathRefBeatlesNT = referencesPath.resolve("nt.beatles.nt").toString();
        String pathOutBeatlesNT = resultPath.resolve("nt.beatles.nt").toString();

        int exitCode = cmd.execute("-i", pathInputBeatlesNT, "-of", "NTRIPLES", "-o", pathOutBeatlesNT);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertEquals(canonicalize(pathRefBeatlesNT), canonicalize(pathOutBeatlesNT));
        assertNotEquals("", pathOutBeatlesNT);

    }

    @Test
    public void testConvertNtToNq() {
        String pathInputBeatlesNT = inputPath.resolve("beatles.nt").toString();
        String pathRefBeatlesNQ = referencesPath.resolve("nt.beatles.nq").toString();
        String pathOutBeatlesNQ = resultPath.resolve("nt.beatles.nq").toString();

        int exitCode = cmd.execute("-i", pathInputBeatlesNT, "-of", "NQUADS", "-o", pathOutBeatlesNQ);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertEquals(canonicalize(pathRefBeatlesNQ), canonicalize(pathOutBeatlesNQ));
        assertNotEquals("", pathOutBeatlesNQ);

    }

    @Test
    public void testConvertNqToXml() {
        String pathInputBeatlesNQ = inputPath.resolve("beatles.nq").toString();
        String pathRefBeatlesXML = referencesPath.resolve("nq.beatles.rdf").toString();
        String pathOutBeatlesXML = resultPath.resolve("nq.beatles.rdf").toString();

        int exitCode = cmd.execute("-i", pathInputBeatlesNQ, "-of", "RDFXML", "-o", pathOutBeatlesXML);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertEquals(canonicalize(pathRefBeatlesXML), canonicalize(pathOutBeatlesXML));
        assertNotEquals("", pathOutBeatlesXML);

    }

    @Test
    public void testConvertNqToJsonld() {
        String pathInputBeatlesNQ = inputPath.resolve("beatles.nq").toString();
        String pathRefBeatlesJSON = referencesPath.resolve("nq.beatles.jsonld").toString();
        String pathOutBeatlesJSON = resultPath.resolve("nq.beatles.jsonld").toString();

        int exitCode = cmd.execute("-i", pathInputBeatlesNQ, "-of", "JSONLD", "-o", pathOutBeatlesJSON);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertEquals(canonicalize(pathRefBeatlesJSON), canonicalize(pathOutBeatlesJSON));
        assertNotEquals("", pathOutBeatlesJSON);

    }

    @Test
    public void testConvertNqToTrig() {
        String pathInputBeatlesNQ = inputPath.resolve("beatles.nq").toString();
        String pathRefBeatlesTRIG = referencesPath.resolve("nq.beatles.trig").toString();
        String pathOutBeatlesTRIG = resultPath.resolve("nq.beatles.trig").toString();

        int exitCode = cmd.execute("-i", pathInputBeatlesNQ, "-of", "TRIG", "-o", pathOutBeatlesTRIG);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertEquals(canonicalize(pathRefBeatlesTRIG), canonicalize(pathOutBeatlesTRIG));
        assertNotEquals("", pathOutBeatlesTRIG);

    }

    @Test
    public void testConvertNqToTurtle() {
        String pathInputBeatlesNQ = inputPath.resolve("beatles.nq").toString();
        String pathRefBeatlesTTL = referencesPath.resolve("nq.beatles.ttl").toString();
        String pathOutBeatlesTTL = resultPath.resolve("nq.beatles.ttl").toString();

        int exitCode = cmd.execute("-i", pathInputBeatlesNQ, "-of", "TURTLE", "-o", pathOutBeatlesTTL);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertEquals(canonicalize(pathRefBeatlesTTL), canonicalize(pathOutBeatlesTTL));
        assertNotEquals("", pathOutBeatlesTTL);

    }

    @Test
    public void testConvertNqToNt() {
        String pathInputBeatlesNQ = inputPath.resolve("beatles.nq").toString();
        String pathRefBeatlesNT = referencesPath.resolve("nq.beatles.nt").toString();
        String pathOutBeatlesNT = resultPath.resolve("nq.beatles.nt").toString();

        int exitCode = cmd.execute("-i", pathInputBeatlesNQ, "-of", "NTRIPLES", "-o", pathOutBeatlesNT);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertEquals(canonicalize(pathRefBeatlesNT), canonicalize(pathOutBeatlesNT));
        assertNotEquals("", pathOutBeatlesNT);

    }

    @Test
    public void testConvertNqToNq() {
        String pathInputBeatlesNQ = inputPath.resolve("beatles.nq").toString();
        String pathRefBeatlesNQ = referencesPath.resolve("nq.beatles.nq").toString();
        String pathOutBeatlesNQ = resultPath.resolve("nq.beatles.nq").toString();

        int exitCode = cmd.execute("-i", pathInputBeatlesNQ, "-of", "NQUADS", "-o", pathOutBeatlesNQ);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertEquals(canonicalize(pathRefBeatlesNQ), canonicalize(pathOutBeatlesNQ));
        assertNotEquals("", pathOutBeatlesNQ);

    }

    @Test
    public void testConvertRdfaToXml() {
        String pathInputStringHtml = inputPath.resolve("beatles.html").toString();
        String pathRefBeatlesXML = referencesPath.resolve("html.beatles.rdf").toString();
        String pathOutBeatlesXML = resultPath.resolve("html.beatles.rdf").toString();

        int exitCode = cmd.execute("-i", pathInputStringHtml, "-of", "RDFXML", "-o", pathOutBeatlesXML);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertEquals(canonicalize(pathRefBeatlesXML), canonicalize(pathOutBeatlesXML));
        assertNotEquals("", pathOutBeatlesXML);

    }

    @Test
    public void testConvertRdfaToJsonld() {
        String pathInputStringHtml = inputPath.resolve("beatles.html").toString();
        String pathRefBeatlesJSON = referencesPath.resolve("html.beatles.jsonld").toString();
        String pathOutBeatlesJSON = resultPath.resolve("html.beatles.jsonld").toString();

        int exitCode = cmd.execute("-i", pathInputStringHtml, "-of", "JSONLD", "-o", pathOutBeatlesJSON);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertEquals(canonicalize(pathRefBeatlesJSON), canonicalize(pathOutBeatlesJSON));
        assertNotEquals("", pathOutBeatlesJSON);

    }

    @Test
    public void testConvertRdfaToTrig() {
        String pathInputStringHtml = inputPath.resolve("beatles.html").toString();
        String pathRefBeatlesTRIG = referencesPath.resolve("html.beatles.trig").toString();
        String pathOutBeatlesTRIG = resultPath.resolve("html.beatles.trig").toString();

        int exitCode = cmd.execute("-i", pathInputStringHtml, "-of", "TRIG", "-o", pathOutBeatlesTRIG);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertEquals(canonicalize(pathRefBeatlesTRIG), canonicalize(pathOutBeatlesTRIG));
        assertNotEquals("", pathOutBeatlesTRIG);

    }

    @Test
    public void testConvertRdfaToTurtle() {
        String pathInputStringHtml = inputPath.resolve("beatles.html").toString();
        String pathRefBeatlesTTL = referencesPath.resolve("html.beatles.ttl").toString();
        String pathOutBeatlesTTL = resultPath.resolve("html.beatles.ttl").toString();

        int exitCode = cmd.execute("-i", pathInputStringHtml, "-of", "TURTLE", "-o", pathOutBeatlesTTL);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertEquals(canonicalize(pathRefBeatlesTTL), canonicalize(pathOutBeatlesTTL));
        assertNotEquals("", pathOutBeatlesTTL);

    }

    @Test
    public void testConvertRdfaToNt() {
        String pathInputStringHtml = inputPath.resolve("beatles.html").toString();
        String pathRefBeatlesNT = referencesPath.resolve("html.beatles.nt").toString();
        String pathOutBeatlesNT = resultPath.resolve("html.beatles.nt").toString();

        int exitCode = cmd.execute("-i", pathInputStringHtml, "-of", "NTRIPLES", "-o", pathOutBeatlesNT);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertEquals(canonicalize(pathRefBeatlesNT), canonicalize(pathOutBeatlesNT));
        assertNotEquals("", pathOutBeatlesNT);

    }

    @Test
    public void testConvertRdfaToNq() {
        String pathInputStringHtml = inputPath.resolve("beatles.html").toString();
        String pathRefBeatlesNQ = referencesPath.resolve("html.beatles.nq").toString();
        String pathOutBeatlesNQ = resultPath.resolve("html.beatles.nq").toString();

        int exitCode = cmd.execute("-i", pathInputStringHtml, "-of", "NQUADS", "-o", pathOutBeatlesNQ);

        assertEquals(0, exitCode);
        assertEquals(out.toString(), "");
        assertEquals(err.toString(), "");
        assertEquals(canonicalize(pathRefBeatlesNQ), canonicalize(pathOutBeatlesNQ));
        assertNotEquals("", pathOutBeatlesNQ);

    }

    @Test
    public void testConvertWithSameInputAndOutputPath() {
        String inputPath = referencesPath.resolve("beatles.ttl").toString();
        int exitCode = cmd.execute("-i", inputPath, "-of", "TURTLE", "-o", inputPath);
        assertEquals(1, exitCode);
        assertEquals(out.toString(), "");
        assertTrue(err.toString().trim().contains("Input path cannot be same as output path"));
    }

    @Test
    public void testConvertWithInvalidInputPath() {
        String inputPath = "invalid_path.ttl";
        String outputPath = resultPath.resolve("ttlbeatles.ttl").toString();

        int exitCode = cmd.execute("-i", inputPath, "-of", "TURTLE", "-o", outputPath);
        assertEquals(1, exitCode);
        assertEquals(out.toString(), "");
        assertTrue(err.toString().trim().contains("Failed to open RDF data file:"));
    }

    @Test
    public void testConvertWithInvalidOutputPath() {
        String inputPath = referencesPath.resolve("beatles.ttl").toString();
        String outputPath = "/invalid/path/for/output.ttl";

        int exitCode = cmd.execute("-i", inputPath, "-of", "TURTLE", "-o", outputPath);
        assertEquals(1, exitCode);
        assertEquals(out.toString(), "");
        assertTrue(err.toString().trim().contains("Failed to open RDF data file:"));
    }

    @Test
    public void testGraphUtilsLoadWithInvalidFormat() {
        Path inputPath = referencesPath.resolve("beatles.ttl");

        try {
            RdfDataLoader loader = new RdfDataLoader(null, false);
            loader.load(new String[] { inputPath.toString() }, EnumRdfInputFormat.JSONLD, false);
            fail("Expected an IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Failed to open RDF data file:"));
        }
    }

}