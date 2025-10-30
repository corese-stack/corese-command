package fr.inria.corese.command.utils.exporter;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/** Enumeration of output RDF serialization formats. */
public enum OutputFormat {

    // ===== RDF Formats =====

    // RdfXml
    RDFXML(List.of("rdfxml", "application/rdf+xml", "rdf"), "rdf", true, false, false),

    // Turtle
    TURTLE(List.of("turtle", "text/turtle", "ttl"), "ttl", true, false, false),

    // Trig
    TRIG(List.of("trig", "application/trig", "trig"), "trig", true, false, false),

    // JsonLd
    JSONLD(List.of("jsonld", "application/ld+json", "jsonld"), "jsonld", true, false, false),

    // Ntriples
    NTRIPLES(List.of("ntriples", "application/n-triples", "nt"), "nt", true, false, false),

    // Nquads
    NQUADS(List.of("nquads", "application/n-quads", "nq"), "nq", true, false, false),

    // Rdfc-1.0-sha256
    RDFC10(List.of("rdfc-1.0", "rdfc-1.0-sha256"), "nt", true, false, true),

    // Rdfc-1.0-sha384
    RDFC10SHA384(List.of("rdfc-1.0-sha384"), "nt", true, false, true),

    // Xml
    XML(List.of("xml", "application/sparql-results+xml", "srx"), "srx", false, true, false),

    // Json
    JSON(List.of("json", "application/sparql-results+json", "srj"), "srj", false, true, false),

    // Csv
    CSV(List.of("csv", "text/csv"), "csv", false, true, false),

    // Tsv
    TSV(List.of("tsv", "text/tab-separated-values"), "tsv", false, true, false),

    // Markdown - not standard but convenient for humans to read
    MARKDOWN(List.of("markdown", "text/markdown", "md"), "md", false, true, false);

    // ===== Fields =====

    private final List<String> aliases;
    private final String extension;
    private final Boolean isRdfFormat;
    private final Boolean isMappingFormat;
    private final Boolean isCanonicalFormat;

    // ===== Constructor =====

    /**
     * Constructor for RDF output format.
     *
     * @param aliases Aliases for the RDF format.
     * @param extension The extension file for the format.
     */
    private OutputFormat(
            List<String> aliases,
            String extension,
            Boolean isRdfFormat,
            Boolean isMappingFormat,
            Boolean isCanonicalFormat) {
        this.aliases = aliases;
        this.extension = extension;
        this.isRdfFormat = isRdfFormat;
        this.isMappingFormat = isMappingFormat;
        this.isCanonicalFormat = isCanonicalFormat;
    }

    // ===== Public methods =====

    /**
     * Get the extension of the file format associated with the format.
     *
     * @return The extension of the file format associated with the format.
     */
    public String getExtension() {
        return this.extension;
    }

    /**
     * Get the name of the RDF output format.
     *
     * @return The name of the RDF output format.
     */
    public String getName() {
        return this.name();
    }

    @Override
    public String toString() {
        return this.name();
    }

    /**
     * Get the OutputFormat corresponding to the given RDF value.
     *
     * @param value the value to convert
     * @return the corresponding OutputFormat
     */
    public static OutputFormat fromRdf(String value) {
        return rdfFormats().stream()
                .filter(f -> f.matches(value))
                .findFirst()
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Unknown RDF format: "
                                                + value
                                                + ". Supported: "
                                                + rdfAliases()));
    }

    /**
     * Get the OutputFormat corresponding to the given mapping value.
     *
     * @param value the value to convert
     * @return the corresponding OutputFormat
     */
    public static OutputFormat fromMapping(String value) {
        return mappingFormats().stream()
                .filter(f -> f.matches(value))
                .findFirst()
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Unknown SPARQL mapping format: "
                                                + value
                                                + ". Supported: "
                                                + sparqlAliases()));
    }

    /**
     * Get the OutputFormat corresponding to the given canonical value.
     *
     * @param value the value to convert
     * @return the corresponding OutputFormat
     */
    public static OutputFormat fromCanonical(String value) {
        return canonicalFormats().stream()
                .filter(f -> f.matches(value))
                .findFirst()
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Unknown canonical RDF format: "
                                                + value
                                                + ". Supported: "
                                                + canonicalAliases()));
    }

    /**
     * Get all aliases for RDF formats.
     *
     * @return a sorted list of all RDF format aliases
     */
    public static List<String> rdfAliases() {
        return rdfFormats().stream()
                .flatMap(format -> format.aliases.stream())
                .filter(alias -> alias != null && !alias.isEmpty())
                .distinct()
                .sorted(
                        Comparator.comparingInt(String::length)
                                .thenComparing(String::compareToIgnoreCase))
                .toList();
    }

    /**
     * Get all aliases for SPARQL mapping formats.
     *
     * @return a sorted list of all SPARQL mapping format aliases
     */
    public static List<String> sparqlAliases() {
        return mappingFormats().stream()
                .flatMap(format -> format.aliases.stream())
                .filter(alias -> alias != null && !alias.isEmpty())
                .distinct()
                .sorted(
                        Comparator.comparingInt(String::length)
                                .thenComparing(String::compareToIgnoreCase))
                .toList();
    }

    /**
     * Get all aliases for canonical RDF formats.
     *
     * @return a sorted list of all canonical RDF format aliases
     */
    public static List<String> canonicalAliases() {
        return canonicalFormats().stream()
                .flatMap(format -> format.aliases.stream())
                .filter(alias -> alias != null && !alias.isEmpty())
                .distinct()
                .sorted(
                        Comparator.comparingInt(String::length)
                                .thenComparing(String::compareToIgnoreCase))
                .toList();
    }

    /**
     * Check if this format is an RDF format.
     *
     * @return true if this is an RDF format, false otherwise
     */
    public boolean isRdfFormat() {
        return this.isRdfFormat;
    }

    /**
     * Check if this format is a SPARQL mapping format.
     *
     * @return true if this is a SPARQL mapping format, false otherwise
     */
    public boolean isMappingFormat() {
        return this.isMappingFormat;
    }

    /**
     * Check if this format is a canonical format.
     *
     * @return true if this is a canonical format, false otherwise
     */
    public boolean isCanonicalFormat() {
        return this.isCanonicalFormat;
    }

    // ===== Private methods =====

    /**
     * Check if the given value matches this format's name or extension.
     *
     * @param value the value to check
     * @return true if the value matches, false otherwise
     */
    private boolean matches(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        String normalized = value.toLowerCase().trim();
        return this.aliases.stream().anyMatch(alias -> alias.equalsIgnoreCase(normalized))
                || this.extension.equalsIgnoreCase(normalized);
    }

    /**
     * Get all RDF format values.
     *
     * @return a list of all RDF formats
     */
    private static List<OutputFormat> rdfFormats() {
        return Arrays.stream(values()).filter(format -> format.isRdfFormat).toList();
    }

    /**
     * Get all SPARQL mapping format values.
     *
     * @return a list of all SPARQL mapping formats
     */
    private static List<OutputFormat> mappingFormats() {
        return Arrays.stream(values()).filter(format -> format.isMappingFormat).toList();
    }

    /**
     * Get all canonical RDF format values.
     *
     * @return a list of all canonical RDF formats
     */
    private static List<OutputFormat> canonicalFormats() {
        return Arrays.stream(values()).filter(format -> format.isCanonicalFormat).toList();
    }
}
