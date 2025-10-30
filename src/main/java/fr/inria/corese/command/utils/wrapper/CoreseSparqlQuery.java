package fr.inria.corese.command.utils.wrapper;

import fr.inria.corese.command.exceptions.SparqlExecutionException;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.kgram.core.Mappings;
import fr.inria.corese.core.kgram.core.Query;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.core.sparql.exceptions.EngineException;
import fr.inria.corese.core.sparql.triple.update.ASTUpdate;
import fr.inria.corese.core.sparql.triple.update.Composite;
import fr.inria.corese.core.sparql.triple.update.Update;

/** SPARQL query operations. */
public class CoreseSparqlQuery {

    // ===== Fields =====

    private Query query;

    // ===== Constructors =====

    /**
     * Constructs a SPARQL query from a string representation.
     * 
     * @param stringQuery The SPARQL query as a string.
     * @throws SparqlExecutionException If the query is invalid.
     */
    public CoreseSparqlQuery(String stringQuery) {
        QueryProcess exec = QueryProcess.create(Graph.create());

        try {
            this.query = exec.compile(stringQuery);
        } catch (EngineException e) {
            throw new SparqlExecutionException("Invalid SPARQL query.", e);
        }
    }

    // ===== Public methods =====

    /**
     * Execute the SPARQL query on the given RDF graph.
     *
     * @param graph The RDF graph to execute the query on.
     * @return The result of the query execution.
     * @throws SparqlExecutionException If there is an error during query execution.
     */
    public CoreseSparqlResult execute(CoreseRdfGraph graph) {
        try {
            Mappings mappings = executeInternal(graph);
            return new CoreseSparqlResult(mappings);
        } catch (EngineException e) {
            throw new SparqlExecutionException("Error executing SPARQL query", e);
        }
    }

    /**
     * Checks if the query contains FROM clause.
     *
     * @return true if the query contains FROM clause, false otherwise
     */
    public boolean containsFromClause() {
        return query.getFrom() != null && !query.getFrom().isEmpty();
    }

    /**
     * Checks if the query is an update query.
     *
     * @return true if the query is an update, false otherwise
     */
    public boolean isSparqlUpdate() {
        return query.getAST().isSPARQLUpdate();
    }

    /**
     * Checks if the query contains WITH clause.
     *
     * @return true if the query contains WITH clause, false otherwise
     */
    public boolean containsWithClause() {
        ASTUpdate astUpdate = query.getAST().getUpdate();
        if (astUpdate != null) {
            for (Update update : astUpdate.getUpdates()) {
                Composite composite = update.getComposite();
                if (composite != null && composite.getWith() != null) {
                    return true;
                }
            }
        }

        return false;
    }

    // ===== Private methods =====

    /**
     * Execute the SPARQL query on the given RDF graph. This is a Corese-specific method that
     * returns Mappings directly.
     *
     * @param graph The RDF graph to execute the query on.
     * @return The Mappings result of the query execution.
     * @throws EngineException If there is an error during query execution.
     */
    private Mappings executeInternal(CoreseRdfGraph graph) throws EngineException {
        QueryProcess exec = QueryProcess.create(graph.getGraph());
        return exec.query(query);
    }
}
