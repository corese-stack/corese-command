package fr.inria.corese.command.utils.coresecorowrapper;

import fr.inria.corese.core.kgram.core.Mappings;
import fr.inria.corese.core.kgram.core.Query;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.core.sparql.exceptions.EngineException;
import fr.inria.corese.core.sparql.triple.update.ASTUpdate;
import fr.inria.corese.core.sparql.triple.update.Composite;
import fr.inria.corese.core.sparql.triple.update.Update;

/**
 * Wrapper class for corese core KGRAM query All interactions with corese.core.kgram.core.Query
 * should be done through this class
 */
public class CoreseSparqlQuery {

    /** The wrapped KGRAM query */
    protected Query query;

    public CoreseSparqlQuery(Query query) {
        this.query = query;
    }

    public CoreseSparqlQuery(String query) throws EngineException {
        CoreseRdfGraph graph = new CoreseRdfGraph();
        QueryProcess exec = QueryProcess.create(graph.getGraph());

        this.query = exec.compile(query);
    }

    /**
     * Check if the input string is a valid SPARQL query.
     *
     * @param input String to check.
     * @return True if the input is a valid SPARQL query, false otherwise.
     */
    public static boolean isValidSparqlQuery(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }

        try {
            return new CoreseSparqlQuery(input) != null;
        } catch (EngineException e) {
            return false;
        }
    }

    /**
     * Executes the stringQuery on the graph
     *
     * @param graph The graph to execute the query on
     * @param stringQuery The SPARQL query string
     * @return The mappings resulting from the query execution
     * @throws EngineException If an error occurs during query execution
     */
    public static Mappings execute(CoreseRdfGraph graph, String stringQuery)
            throws EngineException {
        QueryProcess exec = QueryProcess.create(graph.getGraph());

        return exec.query(stringQuery);
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
    public boolean isSPARQLUpdate() {
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
}
