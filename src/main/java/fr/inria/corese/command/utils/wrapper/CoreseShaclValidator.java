package fr.inria.corese.command.utils.wrapper;

import fr.inria.corese.core.kgram.api.core.Node;
import fr.inria.corese.core.shacl.Shacl;
import fr.inria.corese.core.sparql.datatype.DatatypeMap;
import fr.inria.corese.core.sparql.exceptions.EngineException;

/**
 * SHACL validator for validating RDF graphs against SHACL shapes.
 */
public class CoreseShaclValidator {

    // ===== Fields =====

    private Shacl shacl;

    // ===== Constructors =====

    public CoreseShaclValidator(CoreseRdfGraph dataGraph) {
        this.shacl = new Shacl(dataGraph.getGraph());
    }

    // ===== Public methods =====

    /**
     * Detect if the given graph contains SHACL shapes.
     *
     * @param shapesGraph The graph to check for SHACL shapes.
     * @return True if the graph contains SHACL shapes, false otherwise.
     */
    public static boolean containsShaclShapes(CoreseRdfGraph shapesGraph) {
        if (shapesGraph == null
                || shapesGraph.getGraph() == null
                || shapesGraph.getGraph().size() == 0) {
            return false;
        }

        shapesGraph.getGraph().init();
        Node nodeShape = DatatypeMap.createResource("http://www.w3.org/ns/shacl#NodeShape");
        Node propertyShape = DatatypeMap.createResource("http://www.w3.org/ns/shacl#PropertyShape");

        return shapesGraph.getGraph().getEdgesRDF4J(null, null, nodeShape).iterator().hasNext()
                || shapesGraph
                        .getGraph()
                        .getEdgesRDF4J(null, null, propertyShape)
                        .iterator()
                        .hasNext();
    }

    /**
     * Evaluate SHACL shapes against the data graph.
     *
     * @param shapesGraph The graph containing SHACL shapes.
     * @return The validation report as an RDF graph.
     * @throws EngineException If an error occurs during SHACL evaluation.
     */
    public CoreseRdfGraph eval(CoreseRdfGraph shapesGraph) throws EngineException {
        this.shacl.setShacl(shapesGraph.getGraph());
        return new CoreseRdfGraph(this.shacl.eval());
    }
}
