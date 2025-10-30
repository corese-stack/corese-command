package fr.inria.corese.command.utils.wrapper;

import fr.inria.corese.command.utils.loader.rdf.RdfInputFormat;
import fr.inria.corese.core.api.Loader;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.core.load.LoadFormat;

import java.io.InputStream;

/** RDF loader for loading RDF data into an RDF graph. */
public class CoreseRdfLoader {

    // ===== Fields =====

    private Load loader;

    // ===== Constructors =====

    /**
     * Constructs an RdfLoader for the given RDF graph.
     *
     * @param graph The RDF graph to load data into.
     */
    public CoreseRdfLoader(CoreseRdfGraph graph) {
        this.loader = Load.create(graph.getGraph());
    }

    // ===== Public methods =====

    /**
     * Parse RDF data from an input stream and load it into the graph.
     *
     * @param inputStream The input stream containing RDF data.
     * @param format The format of the RDF data.
     * @throws LoadException If an error occurs during parsing or loading.
     */
    public void parse(InputStream inputStream, RdfInputFormat format) throws LoadException {
        this.loader.parse(inputStream, CoreseRdfLoader.toCoreseInternalFormat(format));
    }

    /**
     * Detect the RDF format of the given input (file path or URL).
     *
     * @param input The input file path or URL.
     * @return The detected RDF format.
     * @throws IllegalArgumentException If the format cannot be detected.
     */
    public static RdfInputFormat detectFormat(String input) throws IllegalArgumentException {
        Loader.format loadFormat = LoadFormat.getFormat(input);

        if (loadFormat == null) {
            throw new IllegalArgumentException("Cannot detect RDF format for input: " + input);
        }

        return CoreseRdfLoader.fromCoreseInternalFormat(loadFormat);
    }

    // ===== Private methods =====

    /**
     * Convert RdfInputFormat to Corese internal Loader.format.
     *
     * @param format The RdfInputFormat to convert.
     * @return The corresponding Loader.format.
     */
    private static Loader.format toCoreseInternalFormat(RdfInputFormat format) {
        switch (format) {
            case RDFXML:
                return Loader.format.RDFXML_FORMAT;
            case TURTLE:
                return Loader.format.TURTLE_FORMAT;
            case TRIG:
                return Loader.format.TRIG_FORMAT;
            case JSONLD:
                return Loader.format.JSONLD_FORMAT;
            case NTRIPLES:
                return Loader.format.NT_FORMAT;
            case NQUADS:
                return Loader.format.NQUADS_FORMAT;
            case RDFA:
                return Loader.format.RDFA_FORMAT;
            default:
                throw new IllegalArgumentException("Unsupported RDF format: " + format);
        }
    }

    /**
     * Convert Corese internal Loader.format to RdfInputFormat.
     *
     * @param format The Loader.format to convert.
     * @return The corresponding RdfInputFormat.
     */
    private static RdfInputFormat fromCoreseInternalFormat(Loader.format format) {
        switch (format) {
            case RDFXML_FORMAT:
                return RdfInputFormat.RDFXML;
            case TURTLE_FORMAT:
                return RdfInputFormat.TURTLE;
            case TRIG_FORMAT:
                return RdfInputFormat.TRIG;
            case JSONLD_FORMAT:
                return RdfInputFormat.JSONLD;
            case NT_FORMAT:
                return RdfInputFormat.NTRIPLES;
            case NQUADS_FORMAT:
                return RdfInputFormat.NQUADS;
            case RDFA_FORMAT:
                return RdfInputFormat.RDFA;
            default:
                throw new IllegalArgumentException("Unsupported RDF format: " + format);
        }
    }
}
