package fr.inria.corese.command.utils.loader.rdf;

import java.util.Iterator;

public class RdfFormatsCandidateProvider implements Iterable<String> {
    @Override
    public Iterator<String> iterator() {
        return RdfInputFormat.allAliases().iterator();
    }
}
