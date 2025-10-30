package fr.inria.corese.command.utils.loader.rdf;

import java.util.Iterator;

/** Provides completion candidates for RDF input formats. */
public class RdfInputFormatCandidates implements Iterable<String> {

    @Override
    public Iterator<String> iterator() {
        return RdfInputFormat.allAliases().iterator();
    }
}
