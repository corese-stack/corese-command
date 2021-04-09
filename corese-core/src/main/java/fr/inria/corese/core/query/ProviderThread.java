package fr.inria.corese.core.query;

import fr.inria.corese.kgram.core.Eval;
import fr.inria.corese.kgram.core.Exp;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.parser.URLServer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2018
 *
 */
public class ProviderThread extends Thread {

    ProviderImpl p;
    Query q;
    Exp exp;
    private URLServer service;
    Mappings map, sol;
    Eval eval;
    CompileService c;
    boolean slice;
    int length;
    int timeout;
    
    ProviderThread(ProviderImpl p, Query q, URLServer service, Exp exp, Mappings map, Mappings sol, Eval eval, CompileService compiler, boolean slice, int length, int timeout){
        this.p = p;
        this.q = q;
        this.service = service;
        this.exp = exp;
        this.map = map;
        this.sol = sol;
        this.eval = eval; 
        this.c = compiler;
        this.slice = slice;
        this.length = length;
        this.timeout = timeout;
    }
    
    @Override
    public void run() {
        process();
    }
    
    void process() {
        try {
            p.process(q, service, exp, map, sol, eval, c, slice, length, timeout);
        } catch (EngineException ex) {           
            ProviderImpl.logger.error(ex.getMessage());
        }
    }
       
    public URLServer getService() {
        return service;
    }
    
    public void setService(URLServer service) {
        this.service = service;
    }
    

}
