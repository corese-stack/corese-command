package fr.inria.corese.command.utils.exporter.sparql;

import fr.inria.corese.command.utils.exporter.OutputFormat;

import picocli.CommandLine.ITypeConverter;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/** Converter for SPARQL output formats. */
public class SparqlOutputFormatConverter implements ITypeConverter<OutputFormat> {
    @Override
    public OutputFormat convert(String value) {
        // Check if it's a mapping format (for SELECT queries)
        if (OutputFormat.sparqlAliases().stream()
                .anyMatch(alias -> alias.equalsIgnoreCase(value))) {
            return OutputFormat.fromMapping(value);
        }

        // Check if it's an RDF format (for CONSTRUCT/DESCRIBE queries)
        if (OutputFormat.rdfAliases().stream().anyMatch(alias -> alias.equalsIgnoreCase(value))) {
            return OutputFormat.fromRdf(value);
        }

        // If not found, throw error with all supported formats (sorted by length then
        // alphabetically)
        List<String> allSupported =
                Stream.concat(
                                OutputFormat.sparqlAliases().stream(),
                                OutputFormat.rdfAliases().stream())
                        .distinct()
                        .sorted(
                                Comparator.comparingInt(String::length)
                                        .thenComparing(String::compareToIgnoreCase))
                        .toList();

        throw new IllegalArgumentException(
                "Unknown SPARQL output format: " + value + ". Supported: " + allSupported);
    }
}
