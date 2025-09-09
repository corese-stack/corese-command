package fr.inria.corese.command.utils.coreseCoreWrapper;

import fr.inria.corese.core.Graph;

public class GraphWrapper {

    public static Graph createGraph() {
        return Graph.create();
    }

    public static void mergeGraph(Graph graph, Graph resultGraphUrl) {
        graph.merge(resultGraphUrl);
    }    
}
