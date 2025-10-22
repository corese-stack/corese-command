package fr.inria.corese.command.utils.coresecorowrapper;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.shacl.Shacl;
import fr.inria.corese.core.sparql.exceptions.EngineException;

/**
 * Wrapper class for corese.coer.shacl.Shacl All interactions with corese.core.shacl.Shacl should be
 * done through this class
 */
public class CoreseShacl {

    /** The wrapped Shacl */
    protected Shacl shacl;

    public CoreseShacl(Graph graph, Graph graph2) {
        shacl = new Shacl(graph, graph2);
    }

    public Graph eval() throws EngineException {
        return shacl.eval();
    }
}
