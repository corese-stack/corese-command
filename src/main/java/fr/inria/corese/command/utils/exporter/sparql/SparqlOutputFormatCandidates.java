package fr.inria.corese.command.utils.exporter.sparql;

import fr.inria.corese.command.utils.exporter.OutputFormat;

import java.util.Comparator;
import java.util.Iterator;
import java.util.stream.Stream;

/** Provides completion candidates for SPARQL mapping output formats. */
public class SparqlOutputFormatCandidates implements Iterable<String> {

    @Override
    public Iterator<String> iterator() {
        return Stream.concat(
                        OutputFormat.sparqlAliases().stream(), OutputFormat.rdfAliases().stream())
                .distinct()
                .sorted(
                        Comparator.comparingInt(String::length)
                                .thenComparing(String::compareToIgnoreCase))
                .iterator();
    }
}
