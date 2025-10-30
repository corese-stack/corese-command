package fr.inria.corese.command.utils.exporter.canonical;

import fr.inria.corese.command.utils.exporter.OutputFormat;

import java.util.Iterator;

/** Provides completion candidates for canonical RDF output formats. */
public class CanonicalOutputFormatCandidates implements Iterable<String> {

    @Override
    public Iterator<String> iterator() {
        return OutputFormat.canonicalAliases().iterator();
    }
}
