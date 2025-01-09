package fr.inria.corese.command.utils.exporter.sparql;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import fr.inria.corese.core.sparql.api.ResultFormatDef;

/**
 * Enumeration of result serialization formats.
 */
public enum EnumResultFormat {

    /////////////////
    // RDF Formats //
    /////////////////

    // RdfXml
    RDFXML("rdfxml", ResultFormatDef.format.RDF_XML_FORMAT, "rdf", false),
    APPLICATION_RDF_XML("application/rdf+xml", ResultFormatDef.format.RDF_XML_FORMAT, "rdf", false),
    RDF("rdf", ResultFormatDef.format.RDF_XML_FORMAT, "rdf", false),

    // Turtle
    TURTLE("turtle", ResultFormatDef.format.TURTLE_FORMAT, "ttl", false),
    TEXT_TURTLE("text/turtle", ResultFormatDef.format.TURTLE_FORMAT, "ttl", false),
    TTL("ttl", ResultFormatDef.format.TURTLE_FORMAT, "ttl", false),

    // Trig
    TRIG("trig", ResultFormatDef.format.TRIG_FORMAT, "trig", false),
    APPLICATION_TRIG("application/trig", ResultFormatDef.format.TRIG_FORMAT, "trig", false),

    // JsonLd
    JSONLD("jsonld", ResultFormatDef.format.JSONLD_FORMAT, "jsonld", false),
    APPLICATION_LD_JSON("application/ld+json", ResultFormatDef.format.JSONLD_FORMAT, "jsonld", false),

    // Ntriples
    NTRIPLES("ntriples", ResultFormatDef.format.NTRIPLES_FORMAT, "nt", false),
    APPLICATION_N_TRIPLES("application/n-triples", ResultFormatDef.format.NTRIPLES_FORMAT, "nt", false),
    NT("nt", ResultFormatDef.format.NTRIPLES_FORMAT, "nt", false),

    // Nquads
    NQUADS("nquads", ResultFormatDef.format.NQUADS_FORMAT, "nq", false),
    APPLICATION_N_QUADS("application/n-quads", ResultFormatDef.format.NQUADS_FORMAT, "nq", false),
    NQ("nq", ResultFormatDef.format.NQUADS_FORMAT, "nq", false),

    // Rdfc-1.0-sha256
    RDFC10("rdfc-1.0", ResultFormatDef.format.RDFC10_FORMAT, "nq", false),
    RDFC10SHA256("rdfc-1.0-sha256", ResultFormatDef.format.RDFC10_FORMAT, "nq", false),

    // Rdfc-1.0-sha384
    RDFC10SHA384("rdfc-1.0-sha384", ResultFormatDef.format.RDFC10_SHA384_FORMAT, "nq", false),

    /////////////////////
    // Mapping Formats //
    /////////////////////

    // Xml
    XML("xml", ResultFormatDef.format.XML_FORMAT, "srx", true),
    APPLICATION_SPARQL_RESULTS_XML("application/sparql-results+xml", ResultFormatDef.format.XML_FORMAT, "srx", true),
    SRX("srx", ResultFormatDef.format.XML_FORMAT, "srx", true),

    // Json
    JSON("json", ResultFormatDef.format.JSON_FORMAT, "srj", true),
    APPLICATION_SPARQL_RESULTS_JSON("application/sparql-results+json", ResultFormatDef.format.JSON_FORMAT, "srj", true),
    SRJ("srj", ResultFormatDef.format.JSON_FORMAT, "srj", true),

    // Csv
    CSV("csv", ResultFormatDef.format.CSV_FORMAT, "csv", true),
    TEXT_CSV("text/csv", ResultFormatDef.format.CSV_FORMAT, "csv", true),

    // Tsv
    TSV("tsv", ResultFormatDef.format.TSV_FORMAT, "tsv", true),
    TEXT_TAB_SEPARATED_VALUES("text/tab-separated-values", ResultFormatDef.format.TSV_FORMAT, "tsv", true),

    // Markdown
    MARKDOWN("markdown", ResultFormatDef.format.MARKDOWN_FORMAT, "md", true),
    TEXT_MARKDOWN("text/markdown", ResultFormatDef.format.MARKDOWN_FORMAT, "md", true),
    MD("md", ResultFormatDef.format.MARKDOWN_FORMAT, "md", true);

    private final String name;
    private final ResultFormatDef.format coreseFormat;
    private final String extention;
    private final boolean isMappingFormat;

    /**
     * Constructor.
     * 
     * @param name            The name of the format.
     * @param coreseFormat    The Corese format.
     * @param extention       The extension of the format.
     * @param isMappingFormat True if the format is a mapping format, false
     *                        otherwise.
     */
    private EnumResultFormat(String name, ResultFormatDef.format coreseFormat, String extention,
            boolean isMappingFormat) {
        this.name = name;
        this.coreseFormat = coreseFormat;
        this.extention = extention;
        this.isMappingFormat = isMappingFormat;
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

    /**
     * Check if the format is a mapping format.
     * 
     * @return True if the format is a mapping format, false otherwise.
     */
    public boolean isMappingFormat() {
        return this.isMappingFormat;
    }

    /**
     * Check if the format is an RDF graph format.
     * 
     * @return True if the format is an RDF graph format, false otherwise.
     */
    public boolean isRdfGraphFormat() {
        return !this.isMappingFormat;
    }

    /**
     * Filter the formats based on the given predicate.
     * 
     * @param predicate Predicate to filter the formats.
     * @return List of formats that satisfy the predicate.
     */
    private static List<EnumResultFormat> filterFormats(Predicate<EnumResultFormat> predicate) {
        List<EnumResultFormat> formatsList = new ArrayList<>();
        for (EnumResultFormat format : EnumResultFormat.values()) {
            if (predicate.test(format)) {
                formatsList.add(format);
            }
        }
        return formatsList;
    }

    /**
     * Get the list of RDF graph formats.
     * 
     * @return List of RDF graph formats.
     */
    public static List<EnumResultFormat> getRdfFormats() {
        return filterFormats(EnumResultFormat::isRdfGraphFormat);
    }

    /**
     * Get the list of mapping formats.
     * 
     * @return List of mapping formats.
     */
    public static List<EnumResultFormat> getMappingFormats() {
        return filterFormats(EnumResultFormat::isMappingFormat);
    }

    @Override
    public String toString() {
        return this.name;
    }
}
