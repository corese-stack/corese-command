package fr.inria.corese.command.utils.coresecorowrapper;

import fr.inria.corese.core.kgram.core.Mappings;
import fr.inria.corese.core.sparql.triple.parser.ASTQuery;

/** Wrapper class for corese.core.kgram.core.Mappings */
public class CoreseMappingType {

    /** AST of the Mapping wrapped */
    private ASTQuery mappingAST;

    public CoreseMappingType(Mappings map) {
        mappingAST = map.getAST();
    }

    public boolean isUpdate() {
        return mappingAST.isUpdate();
    }

    public boolean isConstruct() {
        return mappingAST.isConstruct();
    }

    public boolean isAsk() {
        return mappingAST.isAsk();
    }

    public boolean isSelect() {
        return mappingAST.isSelect();
    }

    public boolean isDescribe() {
        return mappingAST.isDescribe();
    }
}
