package fr.inria.corese.command.utils.exporter.rdf;

import fr.inria.corese.core.sparql.api.ResultFormatDef;

/**
 * Enumeration of output RDF serialization formats.
 */
public enum EnumRdfOutputFormat {
    // RdfXml
    RDFXML("rdfxml", ResultFormatDef.format.RDF_XML_FORMAT, "rdf"),
    APPLICATION_RDF_XML("application/rdf+xml", ResultFormatDef.format.RDF_XML_FORMAT, "rdf"),
    RDF("rdf", ResultFormatDef.format.RDF_XML_FORMAT, "rdf"),

    // Turtle
    TURTLE("turtle", ResultFormatDef.format.TURTLE_FORMAT, "ttl"),
    TEXT_TURTLE("text/turtle", ResultFormatDef.format.TURTLE_FORMAT, "ttl"),
    TTL("ttl", ResultFormatDef.format.TURTLE_FORMAT, "ttl"),

    // Trig
    TRIG("trig", ResultFormatDef.format.TRIG_FORMAT, "trig"),
    APPLICATION_TRIG("application/trig", ResultFormatDef.format.TRIG_FORMAT, "trig"),

    // JsonLd
    JSONLD("jsonld", ResultFormatDef.format.JSONLD_FORMAT, "jsonld"),
    APPLICATION_LD_JSON("application/ld+json", ResultFormatDef.format.JSONLD_FORMAT, "jsonld"),

    // Ntriples
    NTRIPLES("ntriples", ResultFormatDef.format.NTRIPLES_FORMAT, "nt"),
    APPLICATION_N_TRIPLES("application/n-triples", ResultFormatDef.format.NTRIPLES_FORMAT, "nt"),
    NT("nt", ResultFormatDef.format.NTRIPLES_FORMAT, "nt"),

    // Nquads
    NQUADS("nquads", ResultFormatDef.format.NQUADS_FORMAT, "nq"),
    APPLICATION_N_QUADS("application/n-quads", ResultFormatDef.format.NQUADS_FORMAT, "nq"),
    NQ("nq", ResultFormatDef.format.NQUADS_FORMAT, "nq"),

    // Rdfc-1.0-sha256
    RDFC10("rdfc-1.0", ResultFormatDef.format.RDFC10_FORMAT, "nt"),
    RDFC10SHA256("rdfc-1.0-sha256", ResultFormatDef.format.RDFC10_FORMAT, "nt"),

    // Rdfc-1.0-sha384
    RDFC10SHA384("rdfc-1.0-sha384", ResultFormatDef.format.RDFC10_SHA384_FORMAT, "nt");

    private final String name;
    private final ResultFormatDef.format coreseFormat;
    private final String extention;

    /**
     * Constructor.
     * 
     * @param name         The name of the format.
     * @param coreseFormat The Corese format.
     * @param extention    The extension file for the format.
     */
    private EnumRdfOutputFormat(String name, ResultFormatDef.format coreseFormat, String extention) {
        this.name = name;
        this.coreseFormat = coreseFormat;
        this.extention = extention;
    }

    /**
     * Get the Corese format.
     * 
     * @return The Corese format.
     */
    public ResultFormatDef.format getCoreseFormat() {
        return this.coreseFormat;
    }

    /**
     * Get the extension of the file format associated with the format.
     * 
     * @return The extension of the file format associated with the format.
     */
    public String getExtention() {
        return this.extention;
    }

    @Override
    public String toString() {
        return this.name;
    }

}
