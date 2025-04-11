package fr.inria.corese.command.utils.loader.rdf;

import java.security.InvalidParameterException;

import fr.inria.corese.core.api.Loader;

/**
 * Enumeration of input RDF serialization formats.
 */
public enum EnumRdfInputFormat {

    // RdfXml
    RDFXML("rdfxml", Loader.format.RDFXML_FORMAT),
    APPLICATION_RDF_XML("application/rdf+xml", Loader.format.RDFXML_FORMAT),
    RDF("rdf", Loader.format.RDFXML_FORMAT),

    // Turtle
    TURTLE("turtle", Loader.format.TURTLE_FORMAT),
    TEXT_TURTLE("text/turtle", Loader.format.TURTLE_FORMAT),
    TTL("ttl", Loader.format.TURTLE_FORMAT),

    // Trig
    TRIG("trig", Loader.format.TRIG_FORMAT),
    APPLICATION_TRIG("application/trig", Loader.format.TRIG_FORMAT),

    // JsonLd
    JSONLD("jsonld", Loader.format.JSONLD_FORMAT),
    APPLICATION_LD_JSON("application/ld+json", Loader.format.JSONLD_FORMAT),

    // Ntriples
    NTRIPLES("ntriples", Loader.format.NT_FORMAT),
    APPLICATION_N_TRIPLES("application/n-triples", Loader.format.NT_FORMAT),
    NT("nt", Loader.format.NT_FORMAT),

    // Nquads
    NQUADS("nquads", Loader.format.NQUADS_FORMAT),
    APPLICATION_N_QUADS("application/n-quads", Loader.format.NQUADS_FORMAT),
    NQ("nq", Loader.format.NQUADS_FORMAT),

    // Rdfa
    RDFA("rdfa", Loader.format.RDFA_FORMAT),
    APPLICATION_XHTML_XML("application/xhtml+xml", Loader.format.RDFA_FORMAT),
    XHTML("xhtml", Loader.format.RDFA_FORMAT),
    HTML("html", Loader.format.RDFA_FORMAT);

    private final String name;
    private final Loader.format coreseFormat;

    /**
     * Constructor.
     * 
     * @param name         The name of the format.
     * @param coreseFormat The Corese format.
     */
    private EnumRdfInputFormat(String name, Loader.format coreseFormat) {
        this.name = name;
        this.coreseFormat = coreseFormat;
    }

    /**
     * Create an EnumInputFormat from a Corese format.
     */
    public static EnumRdfInputFormat create(Loader.format loaderFormat) {
        for (EnumRdfInputFormat format : EnumRdfInputFormat.values()) {
            if (format.coreseFormat == loaderFormat) {
                return format;
            }
        }
        throw new InvalidParameterException(
                "Impossible to determine the input format, please specify it with the -f or -if or --input-format option.");
    }

    /**
     * Get the Corese format.
     */
    public Loader.format getCoreseFormat() {
        return this.coreseFormat;
    }

    @Override
    public String toString() {
        return name;
    }
}
