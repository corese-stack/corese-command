package fr.inria.corese.command.utils.coreseCoreWrapper;

import java.util.List;

import fr.inria.corese.core.kgram.api.core.Node;
import fr.inria.corese.core.kgram.core.Query;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.core.sparql.exceptions.EngineException;
import fr.inria.corese.core.sparql.triple.update.ASTUpdate;

/**
 * Wrapper class for corese core KGRAM query
 */
public class KgramQuery {

    /**
     * The wrapped KGRAM query
     */
    protected Query query;

    public KgramQuery(Query query) {
        this.query = query;
    }

    public KgramQuery(String query) throws EngineException {
        CoreseGraph graph = new CoreseGraph();
        QueryProcess exec = QueryProcess.create(graph.getGraph());

        this.query = exec.compile( query);
    }

    public List<Node> getFrom() {
        return query.getFrom();
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

    public ASTUpdate getUpdate() {
        return query.getAST().getUpdate();
    }

}
