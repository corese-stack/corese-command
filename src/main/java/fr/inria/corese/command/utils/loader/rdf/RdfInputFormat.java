package fr.inria.corese.command.utils.loader.rdf;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/** Enumeration of input RDF serialization formats. */
public enum RdfInputFormat {

    // ===== RDF Formats =====

    // RDF/XML
    RDFXML("rdfxml", "application/rdf+xml", "rdf"),

    // Turtle
    TURTLE("turtle", "text/turtle", "ttl"),

    // TriG
    TRIG("trig", "application/trig"),

    // JSON-LD
    JSONLD("jsonld", "application/ld+json"),

    // N-Triples
    NTRIPLES("ntriples", "application/n-triples", "nt"),

    // N-Quads
    NQUADS("nquads", "application/n-quads", "nq"),

    // RDFa / HTML embedded RDF
    RDFA("rdfa", "application/xhtml+xml", "xhtml", "html");

    // ===== Fields =====

    private final List<String> aliases;

    // ===== Constructor =====

    /**
     * Constructor for RDF input format.
     *
     * @param aliases Aliases for the RDF format.
     */
    RdfInputFormat(String... aliases) {
        this.aliases = Arrays.asList(aliases);
    }

    // ===== Public methods =====

    @Override
    public String toString() {
        return aliases.get(0);
    }

    // ===== Protected methods =====

    /**
     * Get the RDF input format from a given string value.
     *
     * @param value String representation of the RDF format.
     * @return Corresponding RdfInputFormat.
     * @throws IllegalArgumentException If the value does not match any known format.
     */
    protected static RdfInputFormat from(String value) {
        return Arrays.stream(values())
                .filter(format -> format.matches(value))
                .findFirst()
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Unknown RDF input format: "
                                                + value
                                                + ". Supported: "
                                                + allAliases()));
    }

    /**
     * Get a list of all aliases for all RDF input formats.
     *
     * @return List of all aliases.
     */
    public static List<String> allAliases() {
        return Arrays.stream(values())
                .flatMap(f -> f.aliases.stream())
                .distinct()
                .sorted(
                        Comparator.comparingInt(String::length)
                                .thenComparing(String::compareToIgnoreCase))
                                
                .toList();
    }

    // ==== Private methods =====

    /**
     * Check if the given value matches any of the aliases for the RDF format.
     *
     * @param value Value to check.
     * @return True if the value matches any alias, false otherwise.
     */
    private boolean matches(String value) {
        return aliases.stream().anyMatch(a -> a.equalsIgnoreCase(value));
    }
}
