package fr.inria.corese.command.utils;

import fr.inria.corese.command.utils.exporter.OutputFormat;
import fr.inria.corese.command.utils.loader.rdf.RdfInputFormat;
import fr.inria.corese.command.utils.loader.rdf.RdfLoader;
import fr.inria.corese.command.utils.wrapper.CoreseExporter;
import fr.inria.corese.command.utils.wrapper.CoreseRdfGraph;
import fr.inria.corese.command.utils.wrapper.CoreseRdfLoader;

import java.io.InputStream;

/**
 * Utility class for canonicalizing RDF data in tests. This class is only used for test purposes to
 * compare RDF graphs.
 */
public final class TestCanonicalizer {

    private TestCanonicalizer() {
        // Utility class, prevent instantiation
    }

    /**
     * Canonicalizes an RDF file for comparison purposes.
     *
     * @param path Path to the RDF file to canonicalize.
     * @return Canonical representation of the RDF graph.
     */
    public static String canonicalize(String path) {
        CoreseRdfGraph graph = new CoreseRdfGraph();
        RdfLoader loader = new RdfLoader(null, false, graph);
        loader.load(new String[] {path}, null, false);

        return canonicalize(graph);
    }

    /**
     * Canonicalizes an RDF graph for comparison purposes.
     *
     * @param graph The RDF graph to canonicalize.
     * @return Canonical representation of the RDF graph.
     */
    public static String canonicalize(CoreseRdfGraph graph) {
        CoreseExporter resultFormat = new CoreseExporter(graph);
        resultFormat.setSelectFormat(OutputFormat.RDFC10);
        resultFormat.setConstructFormat(OutputFormat.RDFC10);
        return resultFormat.toString();
    }

    /**
     * Canonicalizes RDF content from an InputStream.
     *
     * @param inputStream The input stream containing RDF content.
     * @param format The RDF format of the content.
     * @return Canonical representation of the RDF graph.
     */
    public static String canonicalize(InputStream inputStream, RdfInputFormat format) {
        CoreseRdfGraph graph = new CoreseRdfGraph();
        CoreseRdfLoader loader = new CoreseRdfLoader(graph);

        try {
            loader.parse(inputStream, format);
            return canonicalize(graph);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to canonicalize from InputStream", e);
        }
    }
}
