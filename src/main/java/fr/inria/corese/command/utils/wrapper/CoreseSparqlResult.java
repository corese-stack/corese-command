package fr.inria.corese.command.utils.wrapper;

import fr.inria.corese.core.kgram.core.Mappings;

/** SPARQL query result that wraps Mappings. */
public class CoreseSparqlResult {

    // ===== Fields =====

    private final Mappings mappings;

    // ===== Constructors =====

    /**
     * Constructor.
     * 
     * @param mappings The Corese Mappings to wrap.
     */
    public CoreseSparqlResult(Mappings mappings) {
        this.mappings = mappings;
    }

    // ==== Public methods =====

    /**
     * Returns true if the query result corresponds to an UPDATE operation.
     *
     * @return true if it is an UPDATE result, false otherwise
     */
    public boolean isUpdate() {
        return this.mappings.getAST().isUpdate();
    }

    /**
     * Returns true if the query result corresponds to a CONSTRUCT operation.
     *
     * @return true if it is a CONSTRUCT result, false otherwise
     */
    public boolean isConstruct() {
        return this.mappings.getAST().isConstruct();
    }

    /**
     * Returns true if the query result corresponds to an ASK operation.
     *
     * @return true if it is an ASK result, false otherwise
     */
    public boolean isAsk() {
        return this.mappings.getAST().isAsk();
    }

    /**
     * Returns true if the query result corresponds to a SELECT operation.
     *
     * @return true if it is a SELECT result, false otherwise
     */
    public boolean isSelect() {
        return this.mappings.getAST().isSelect();
    }

    /**
     * Returns true if the query result corresponds to a DESCRIBE operation.
     *
     * @return true if it is a DESCRIBE result, false otherwise
     */
    public boolean isDescribe() {
        return this.mappings.getAST().isDescribe();
    }

    /**
     * Get the underlying Corese Mappings object. This method is specific to Corese implementation.
     *
     * @return the Corese Mappings
     */
    public Mappings getMappings() {
        return mappings;
    }
}
