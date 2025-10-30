package fr.inria.corese.command.utils.wrapper;

import fr.inria.corese.core.Graph;

/**
 * Wrapper class representing an RDF graph. Provides methods to interact with and query RDF data.
 */
public class CoreseRdfGraph {

    // ===== Fields =====

    private Graph graph;

    // ===== Constructors =====

    /**
     * Constructs an empty RDF graph.
     */
    public CoreseRdfGraph() {
        this.graph = Graph.create();
    }

    /**
     * Constructs an RDF graph from an existing Corese Graph.
     *
     * @param graph The Corese Graph to wrap.
     */
    public CoreseRdfGraph(Graph graph) {
        this.graph = graph;
    }

    // ===== Private methods =====

    /**
     * Returns the underlying Corese Graph.
     * 
     * @return The Corese Graph.
     */
    protected Graph getGraph() {
        return graph;
    }
}
