/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.edelweiss.kgdqp.strategies;

import fr.inria.acacia.corese.triple.parser.*;
import fr.inria.edelweiss.kgdqp.core.QueryProcessDQP;
import fr.inria.edelweiss.kgdqp.core.RemoteProducerWSImpl;
import fr.inria.edelweiss.kgdqp.core.Util;
import fr.inria.edelweiss.kgenv.api.QueryVisitor;
import fr.inria.edelweiss.kgram.api.query.Producer;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgram.tool.MetaProducer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.apache.log4j.Logger;

/**
 * A class aimint at grouping, if possible, consecutive tripple into SPARQL
 * SERVICE clauses.
 *
 * @author Alban Gaignard <alban.gaignard@cnrs.fr>
 */
public class ServiceGrouper implements QueryVisitor {

    Logger logger = Logger.getLogger(ServiceGrouper.class);
    private QueryProcessDQP execDQP;

    public ServiceGrouper(QueryProcessDQP execDQP) {
        this.execDQP = execDQP;
    }

    @Override
    public void visit(Query query) {
    }

    /**
     * Entry point to navigate through a query abstract syntax tree (AST). This
     * vistor transforms a query to possibly group tripple patterns into SERVICE
     * clauses.
     *
     * @param ast The input query AST.
     */
    @Override
    public void visit(ASTQuery ast) {
        HashMap<Triple, ArrayList<String>> indexEdgeSource = new HashMap<Triple, ArrayList<String>>();
        HashMap<String, ArrayList<Triple>> indexSourceEdge = new HashMap<String, ArrayList<Triple>>();
        ArrayList<Triple> orderedTPs = new ArrayList<Triple>();

        //index initialization
        Exp body = ast.getBody();
        buildIndex(body, indexEdgeSource, indexSourceEdge, ast, orderedTPs);
        // Source -> property index initialization
        for (Triple t : indexEdgeSource.keySet()) {
            if (indexEdgeSource.get(t).size() > 0) {
                for (String url : indexEdgeSource.get(t)) {
                    if (indexSourceEdge.containsKey(url)) {
                        ArrayList<Triple> triples = indexSourceEdge.get(url);
                        triples.add(t);
                    } else {
                        ArrayList<Triple> triples = new ArrayList<Triple>();
                        triples.add(t);
                        indexSourceEdge.put(url, triples);
                    }
                }
            }
        }

        dumpEdgeIndex(indexEdgeSource);
        dumpSourceIndex(indexSourceEdge);

        //Query rewriting
        ArrayList<Exp> globalFilters = new ArrayList<Exp>();
        // !! not generalized to graph filters tht would be included inside union ..
        for (int i = 0; i < body.size(); i++) {
            Exp exp = body.get(i);
            if (exp.isFilter()) {
                globalFilters.add(exp);
            }
        }

        rewriteQueryWithServices(body, globalFilters, indexSourceEdge, indexEdgeSource, orderedTPs);
        logger.info("Final rewritten query");
        logger.info(ast.toSparql());
    }

    /**
     * This methods builds two indices. The first one annotates triple patterns
     * with data source possibly hosting candidates. The second one give
     * information, for each data source, on the triple pattern candidates.
     *
     * @param exp a KGRAM expression containing triple patterns.
     * @param indexEdgeSource the first index "TP -> data source" as output.
     * @param indexSourceEdge the second index "data source -> TP" as output.
     * @param ast the input AST query.
     * @param orderedTPs accumulated triple patterns appearing in the initial
     * query. They are reused when rewriting the query.
     */
    // How to build an index from a "flat" query ? 
    public void buildIndex(Exp exp, HashMap<Triple, ArrayList<String>> indexEdgeSource, HashMap<String, ArrayList<Triple>> indexSourceEdge, ASTQuery ast, ArrayList<Triple> orderedTPs) {

        for (int i = 0; i < exp.size(); i++) {
            Exp subExp = exp.get(i);
            if (subExp.isOptional()) {
                Option opt = (Option) subExp;
                for (int j = 0; j < opt.size(); j++) {
                    buildIndex(opt.get(j), indexEdgeSource, indexSourceEdge, ast, orderedTPs);
                }
            } else if (subExp.isUnion()) {
                Or union = (Or) subExp;
                Exp arg0 = union.get(0);
                Exp arg1 = union.get(1);
                //recursion 1
                buildIndex(arg0, indexEdgeSource, indexSourceEdge, ast, orderedTPs);
                //recursion 2
                buildIndex(arg1, indexEdgeSource, indexSourceEdge, ast, orderedTPs);
            } else if (subExp.isTriple() && (!subExp.isFilter())) {
                Producer mp = execDQP.getProducer();
                if (mp instanceof MetaProducer) {
                    for (Producer p : ((MetaProducer) mp).getProducers()) {
                        if (p instanceof RemoteProducerWSImpl) {
                            RemoteProducerWSImpl rp = (RemoteProducerWSImpl) p;
                            String url = rp.getEndpoint().getEndpoint();
//                            System.out.println("ASK (" + url + ") -> " + exp.toString());

                            Triple triple = subExp.getTriple();
                            if (!orderedTPs.contains(triple)) {
                                orderedTPs.add(triple);
                            }

                            Boolean ask = SourceSelectorWS.ask(triple.getPredicate().toSparql(), rp, ast);

                            if (ask) {
                                if (indexEdgeSource.get(triple) == null) {
                                    ArrayList<String> urls = new ArrayList<String>();
                                    urls.add(url);
                                    indexEdgeSource.put(triple, urls);
                                } else {
                                    ArrayList<String> urls = indexEdgeSource.get(triple);
                                    urls.add(url);
                                    indexEdgeSource.put(triple, urls);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Core of query rewriting. Takes as input the two indices, and the flat
     * list of triple patterns and possibly generates SERVICE clauses. SERVICES
     * are sorted based on the number of free variables. If they have the same
     * number of free variables, the initial TPs order is preserved.
     *
     * @param exp the KGRAM expression represinting the input query.
     * @param globalFilters some of the query filters. To be refined.
     * @param indexSourceEdge the data source -> triple pattern index.
     * @param indexEdgeSource thet triple pattern -> data source index.
     * @param orderedTPs the flat list of TPs appearing in the inout query.
     * @return a rewriten expression with possibly SERVICE clauses.
     */
    public Exp rewriteQueryWithServices(Exp exp, ArrayList<Exp> globalFilters,
            HashMap<String, ArrayList<Triple>> indexSourceEdge,
            HashMap<Triple, ArrayList<String>> indexEdgeSource,
            ArrayList<Triple> orderedTPs) {

        ArrayList<Exp> consecutiveTP = new ArrayList<Exp>();
        ArrayList<Exp> tpToBeRemoved = new ArrayList<Exp>();

        for (int i = 0; i < exp.size(); i++) {
            Exp subExp = exp.get(i);
            if (subExp.isOptional()) {
                Option opt = (Option) subExp;
                //TODO include optional elements into services ? 
            } else if (subExp.isUnion()) {
                Or union = (Or) subExp;
                Exp arg0 = union.get(0);
                Exp arg1 = union.get(1);
                //recursion 1
                union.set(0, rewriteQueryWithServices(arg0, globalFilters, indexSourceEdge, indexEdgeSource, orderedTPs));
                //recursion 2
                union.set(1, rewriteQueryWithServices(arg1, globalFilters, indexSourceEdge, indexEdgeSource, orderedTPs));
            } else if (subExp.isAnd()) {
                And and = (And) subExp;
                Exp arg0 = and.get(0);
                Exp arg1 = and.get(1);
                //recursion 1
                and.set(0, rewriteQueryWithServices(arg0, globalFilters, indexSourceEdge, indexEdgeSource, orderedTPs));
                //recursion 2
                and.set(1, rewriteQueryWithServices(arg1, globalFilters, indexSourceEdge, indexEdgeSource, orderedTPs));
            } else if (subExp.isGraph()) {

            } else if (subExp.isTriple() && (!subExp.isFilter())) {
                consecutiveTP.add(subExp);
                tpToBeRemoved.add(subExp);
            }
        }

        Exp services = null;
        /// Grouping of consecutive TPs into SERVICE clauses
        if (consecutiveTP.size() > 0) {
            // SERVICE clause generation
            services = getSparqlServices(globalFilters, consecutiveTP, indexSourceEdge, indexEdgeSource, orderedTPs);
            exp.getBody().removeAll(tpToBeRemoved);
        }

        // RANKING consecutive SERVICE clauses
        HashMap<Service, Integer> rankedServices = new HashMap<Service, Integer>();

        logger.info("Ranking : ");
        logger.info(services.toSparql());
        for (Exp e : services.getBody()) {
            if (e instanceof Service) {
                Service s = (Service) e;
                int freeVars = countFreeVars(s);
                logger.info(freeVars + " free variables for " + s.toSparql());
                rankedServices.put(s, freeVars);
            }
        }

        // REORDERING consecutive SERVICE clauses
        logger.info("Sorting services with the same score");
        ArrayList<Map.Entry<Service, Integer>> serviceList = new ArrayList<Map.Entry<Service, Integer>>();
        for (Map.Entry<Service, Integer> e : rankedServices.entrySet()) {
            serviceList.add(e);
        }

        final ArrayList<Exp> initTps = consecutiveTP;
        Collections.sort(serviceList, new Comparator<Map.Entry<Service, Integer>>() {
            @Override
            public int compare(Map.Entry<Service, Integer> o1, Map.Entry<Service, Integer> o2) {
                if (o1.getValue() == o2.getValue()) {
                    Service s1 = o1.getKey();
                    Service s2 = o2.getKey();

                    Exp e1 = s1.getBody().get(0).get(0);
                    Exp e2 = s2.getBody().get(0).get(0);
                    Integer initRankeE1 = getInitialRank(e1, initTps);
                    Integer initRankeE2 = getInitialRank(e2, initTps);

                    if (initRankeE1 == -1) {
                        logger.warn(e1.toSparql() + " was not found in " + initTps);
                    }
                    if (initRankeE2 == -1) {
                        logger.warn(e2.toSparql() + " was not found in the list of consecutive triple patterns.");
                    }

                    logger.info("Comparing : " + e1.toSparql() + " with " + e2.toSparql());
                    return initRankeE1.compareTo(initRankeE2);
                } else {
                    return o1.getValue().compareTo(o2.getValue());
                }
            }

        });

        for (Map.Entry<Service, Integer> e : serviceList) {
            exp.add(e.getKey());
        }
        logger.info(exp.toSparql());
        return exp;
    }

    /**
     * Get the rank of a triple pattern in a list.
     *
     * @param exp the input triple pattern.
     * @param consecutiveTP the lists of consecutive triple patterns.
     * @return the rank of the expression in the list of consecutive triple
     * patterns, -1 if the triple pattern is not found.
     */
    private int getInitialRank(Exp exp, ArrayList<Exp> consecutiveTP) {
        logger.warn("Searching for " + exp.toSparql() + " in " + consecutiveTP);
        int r = 1;
        for (Exp e : consecutiveTP) {
            if (e.equals(exp)) {
                return r;
            }
            r++;
        }
        return -1;
    }

    /**
     * Counts free variables in an expression. 
     * @param s an input expression.
     * @return the number of free variables. 
     */
    private int countFreeVars(Exp s) {
        int count = 0;
        ArrayList<Atom> freeVars = new ArrayList<Atom>();
        for (Exp e : s.getBody()) {
            if (e.isBGP()) {
                count += countFreeVars(e);
            } else if (e.isTriple()) {
                Triple t = (Triple) e;
                if (t.getSubject().isVariable() && !freeVars.contains(t.getSubject())) {
                    freeVars.add(t.getSubject());
                }
                if (t.getObject().isVariable() && !freeVars.contains(t.getObject())) {
                    freeVars.add(t.getObject());
                }
                if (t.getPredicate().isVariable() && !freeVars.contains(t.getPredicate())) {
                    freeVars.add(t.getPredicate());
                }
            }
        }
        count += freeVars.size();
        return count;
    }

    /**
     * Transdorms a flat list of consecutive triple patterns into possibly a set of SERVICE clauses. 
     * If TP candidates are hosted in more than one single data source, they are excluded from SERVICE clauses. 
     * 
     * @param globalFilters some of the query filters. To be refined.
     * @param consecutiveTPs the list of consecutive triple paterns to be possibly grouped. 
     * @param indexSourceEdge the data source -> triple pattern index.
     * @param indexEdgeSource the triple pattern -> data source index.
     * @param orderedTPs the flat list of TPs appearing in the inout query.
     * @return an expression containing triple patterns possibly grouped. 
     */
    public Exp getSparqlServices(ArrayList<Exp> globalFilters, ArrayList<Exp> consecutiveTPs,
            HashMap<String, ArrayList<Triple>> indexSourceEdge,
            HashMap<Triple, ArrayList<String>> indexEdgeSource,
            ArrayList<Triple> orderedTPs) {

        logger.info("Processing\n" + consecutiveTPs.toString());
        ArrayList<Service> services = new ArrayList<Service>();
        BasicGraphPattern outOfServicesBgp = BasicGraphPattern.create();

        for (String url : indexSourceEdge.keySet()) {
            BasicGraphPattern unsortedBgp = BasicGraphPattern.create();
            ArrayList<Triple> triples = indexSourceEdge.get(url);
            for (Triple t : triples) {
                //todo check if toKeep is really needed ? 
                if (consecutiveTPs.contains(t)) {
                    unsortedBgp.add(t);
                }
            }

            //sorting triples with respect to the initial query (orderedTPs)
            BasicGraphPattern servBgp = BasicGraphPattern.create();
            for (Triple t : orderedTPs) {
                if (unsortedBgp.getBody().contains(t)) {
                    //checking if the TP also appears in another source, if so, adding it into an OPTIONAL clause
                    if (indexEdgeSource.get(t).size() > 1) {
                        logger.warn("Triple pattern " + t.toSparql() + " appears in more than one data source, excluded from SERVICE.");
//                        Option opt = Option.create(BasicGraphPattern.create(t));
//                        servBgp.add(opt);
                        if (!outOfServicesBgp.getBody().contains(t)) {
                            outOfServicesBgp.add(t);
                        }
                    } else {
                        servBgp.add(t);
                    }
                }
            }

            // Include filters
            // !! attention cas particulier du filtre !exist()
            List<Exp> applicableFilters = Util.getApplicableFilter(globalFilters, servBgp);
            for (Exp f : applicableFilters) {
                servBgp.append(f);
            }

            if (servBgp.size() != 0) {
                outOfServicesBgp.add(Service.create(Constant.create(url), servBgp));
            }

        }
        return outOfServicesBgp;
    }

    /**
     * Transdorms a flat list of consecutive triple patterns into possibly a set of SERVICE clauses. 
     * If TP candidates are hosted in more than one single data source, they are included into SERVICE clauses
     * through OPTIONAL statements. 
     * 
     * @param globalFilters some of the query filters. To be refined. 
     * @param consecutiveTPs the list of consecutive triple paterns to be possibly grouped. 
     * @param indexSourceEdge the data source -> triple pattern index. 
     * @param indexEdgeSource the triple pattern -> data source index. 
     * @param orderedTPs the flat list of TPs appearing in the inout query.
     * @return an expression containing triple patterns possibly grouped. 
     */
    public Exp getSparqlServicesWithOptionals(ArrayList<Exp> globalFilters, ArrayList<Exp> consecutiveTPs,
            HashMap<String, ArrayList<Triple>> indexSourceEdge,
            HashMap<Triple, ArrayList<String>> indexEdgeSource,
            ArrayList<Triple> orderedTPs) {

        logger.info("Processing\n" + consecutiveTPs.toString());

        BasicGraphPattern bgpWithServices = BasicGraphPattern.create();

        for (String url : indexSourceEdge.keySet()) {
            BasicGraphPattern unsortedBgp = BasicGraphPattern.create();
            ArrayList<Triple> triples = indexSourceEdge.get(url);
            for (Triple t : triples) {
                //todo check if toKeep is really needed ? 
                if (consecutiveTPs.contains(t)) {
                    unsortedBgp.add(t);
                }
            }

            //sorting triples with respect to the initial query (orderedTPs)
            BasicGraphPattern servBgp = BasicGraphPattern.create();
            for (Triple t : orderedTPs) {
                if (unsortedBgp.getBody().contains(t)) {
                    //checking if the TP also appears in another source, if so, adding it into an OPTIONAL clause
                    if (indexEdgeSource.get(t).size() > 1) {
                        logger.warn("Triple pattern " + t.toSparql() + " appears in more than one data source, included in OPTIONAL.");
                        Option opt = Option.create(BasicGraphPattern.create(t));
                        servBgp.add(opt);
                    } else {
                        servBgp.add(t);
                    }
                }
            }

            // Include filters
            // !! be careful with !exist()
            List<Exp> applicableFilters = Util.getApplicableFilter(globalFilters, servBgp);
            for (Exp f : applicableFilters) {
                servBgp.append(f);
            }

            if (servBgp.size() != 0) {
                bgpWithServices.add(Service.create(Constant.create(url), servBgp));
            }

        }
        return bgpWithServices;
    }

    /**
     * Checks if a triple pattern exists in the source -> triple pattern index. 
     * @param t the triple patterns.
     * @param indexSourceEdge the source -> triple pattern index. 
     * @return true if it exists in the index, false otherwise. 
     */
    public boolean existsInSourceIndex(Triple t, HashMap<String, ArrayList<Triple>> indexSourceEdge) {
        for (String url : indexSourceEdge.keySet()) {
            ArrayList<Triple> triples = indexSourceEdge.get(url);
            if (triples.contains(t)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Prints the content of the triple pattern -> data source index. 
     * @param indexEdgeSource the triple pattern -> data source index. 
     */
    private void dumpEdgeIndex(HashMap<Triple, ArrayList<String>> indexEdgeSource) {
        logger.info("Edge index");
        for (Triple t : indexEdgeSource.keySet()) {
            System.out.println(t.getPredicate());
            ArrayList<String> urls = indexEdgeSource.get(t);
            for (String url : urls) {
                System.out.println("\t->" + url);
            }
        }
    }

    /**
     * Prints the content of the data source -> triple pattern. 
     * @param indexSourceEdge the data source -> triple pattern. 
     */
    private void dumpSourceIndex(HashMap<String, ArrayList<Triple>> indexSourceEdge) {
        logger.info("Source index");
        for (String url : indexSourceEdge.keySet()) {
            System.out.println(url);
            ArrayList<Triple> triples = indexSourceEdge.get(url);
            for (Triple t : triples) {
                System.out.println("\t->" + t.getPredicate());
            }
        }
    }
}
