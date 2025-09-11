package fr.inria.corese.command.utils.coreseCoreWrapper;

import fr.inria.corese.core.Graph;

/**
 * A wrapper class for Corese Graph operations.
 * This class provides static methods to create and manipulate Graph instances.
 */
public class GraphWrapper {

    /**
     * Creates a new empty Graph instance.
     *
     * @return a new Graph object
     */
    public static Graph createGraph() {
        return Graph.create();
    }

    /**
     * Merges the contents of the resultGraphUrl into the given graph.
     *
     * @param graph the graph to merge into
     * @param resultGraphUrl the graph whose contents are to be merged
     */
    public static void mergeGraph(Graph graph, Graph resultGraphUrl) {
        graph.merge(resultGraphUrl);
    }    
}
