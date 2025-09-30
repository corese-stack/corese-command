package fr.inria.corese.command.utils.coreseCoreWrapper;

import fr.inria.corese.core.kgram.core.Mappings;
import fr.inria.corese.core.sparql.triple.parser.ASTQuery;

public class CoreseMappingType {

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
