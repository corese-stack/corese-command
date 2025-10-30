package fr.inria.corese.command.utils.exporter.rdf;

import fr.inria.corese.command.utils.exporter.OutputFormat;

import java.util.Iterator;

/** Provides completion candidates for RDF output formats. */
public class RdfOutputFormatCandidates implements Iterable<String> {

    @Override
    public Iterator<String> iterator() {
        return OutputFormat.rdfAliases().iterator();
    }
}
