package fr.inria.edelweiss.kgraph.query;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.edelweiss.kgenv.eval.SQLResult;
import fr.inria.edelweiss.kgram.api.core.DatatypeValue;
import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Filter;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.core.Regex;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Matcher;
import fr.inria.edelweiss.kgram.api.query.Producer;
import fr.inria.edelweiss.kgram.core.Eval;
import fr.inria.edelweiss.kgram.core.Exp;
import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.core.Memory;
import fr.inria.edelweiss.kgram.core.Query;
import static fr.inria.edelweiss.kgram.sorter.core.Const.ALL;
import static fr.inria.edelweiss.kgram.sorter.core.Const.NA;
import static fr.inria.edelweiss.kgram.sorter.core.Const.OBJECT;
import static fr.inria.edelweiss.kgram.sorter.core.Const.PREDICATE;
import static fr.inria.edelweiss.kgram.sorter.core.Const.SUBJECT;
import static fr.inria.edelweiss.kgram.sorter.core.Const.TRIPLE;
import fr.inria.edelweiss.kgram.sorter.core.IProducerQP;
import fr.inria.edelweiss.kgram.sorter.core.QPGNode;
import fr.inria.edelweiss.kgram.tool.EntityImpl;
import fr.inria.edelweiss.kgram.tool.MetaIterator;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.core.EdgeIterator;
import fr.inria.edelweiss.kgraph.core.Index;
import fr.inria.edelweiss.kgtool.util.ValueCache;
import java.util.Collection;

/**
 * Producer
 *
 * @author Olivier Corby, Edelweiss INRIA 2010
 *
 */
public class ProducerImpl implements Producer, IProducerQP {

    public static final int OWL_RL = 1;

    static final int IGRAPH = Graph.IGRAPH;
    static final int ILIST = Graph.ILIST;
    static final String TOPREL = Graph.TOPREL;
    List<Entity> empty = new ArrayList<Entity>();
    EdgeIterator ei;
    Graph graph,
            // cache for handling (fun() as var) created Nodes
            local;
    Mapper mapper;
    MatcherImpl match;
    QueryEngine qengine;
    FuzzyMatch fuzzy = new FuzzyMatch();
    ValueCache vcache;
    RDFizer toRDF;
    Node graphNode;
    private Query query;

    // if true, perform local match
    boolean isMatch = false;
    private boolean selfValue;
    private boolean speedUp = false;
    private int index = -1;
    int mode = DEFAULT;
    private IDatatype prevdt;
    private Node prevnode;

    public ProducerImpl() {
        this(Graph.create());
    }

    public ProducerImpl(Graph g) {
        graph = g;
        local = Graph.create();
        mapper = new Mapper(this);
        ei = EdgeIterator.create(g);
        toRDF = new RDFizer();
        vcache = new ValueCache();
    }

    public static ProducerImpl create(Graph g) {
        ProducerImpl p = new ProducerImpl(g);
        return p;
    }

    public FuzzyMatch getFuzzyMatch() {
        return fuzzy;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int n) {
        switch (n) {
            case SKIP_DUPLICATE_TEST:
                setSpeedUp(true);
                break;

            case EXTENSION:
                mode = n;
                break;

        }
    }

    void setMatch(boolean b) {
        isMatch = b;
    }

    boolean isMatch() {
        return isMatch;
    }

    public void set(Matcher m) {
        if (m instanceof MatcherImpl) {
            match = (MatcherImpl) m;
        }
    }

    public void set(QueryEngine qe) {
        qengine = qe;
    }

    public Graph getGraph() {
        return graph;
    }

    public Graph getLocalGraph() {
        return local;
    }

    public void set(Mapper m) {
        mapper = m;
    }

    Node getNode(Edge edge, Node gNode, int i) {
        switch (i) {
            case IGRAPH:
                return gNode;
            case ILIST:
                return null;
            default:
                return edge.getNode(i);
        }
    }
    
    /**
     * Possibly get a Node with a different number datatype than required
     * for D entailment
     */
    Node getValue(Node qNode, Node node, Environment env) {        
        if (node == null) {
            if (qNode.isConstant()) {
                node = graph.getExtNode(qNode);                
            }
        } else if (isExtern(node, env)) {
            node = graph.getExtNode(node);
        }
        return node;
    }
    
    // eg BIND(node as ?x)
    boolean isExtern(Node node, Environment env){
        return node.getIndex() == -1
                || mode == EXTENSION
                || env.getQuery().getGlobalQuery().isExtension() ;
    }

    boolean isType(Edge edge, Environment env) {
        return graph.isType(edge) || env.getQuery().isRelax(edge);
    }

    /**
     *
     * @param gNode : null or graph name of a graph pattern: graph gNode { }
     * @param from : null, from or from named if gNode != null
     * @param edge : query Edge
     * @param env : Environment with partial variable bindings if gNode == null
     * : simple triple enumerate one occurrence of each edge (not the duplicates
     * in named graphs) else : triple in named graph enumerate all occurrences
     * of edge in named graphs gNode may be a constant value or it may have a
     * value in env
     *
     */
    @Override
    public Iterable<Entity> getEdges(Node gNode, List<Node> from, Edge edge,
            Environment env) {

        Node predicate = getPredicate(edge, env);
        if (predicate == null) {
            return empty;
        }

        Query q = env.getQuery();

        int level = -1;
        int n = 0;

        if (q.isRule()) {
            if (q.getEdgeList() != null
                    && edge.getIndex() == q.getEdgeIndex()) {
                // draft: transitive rule (see RuleEngine)
                //System.out.println("PI: " + q.getEdgeList().size() + " " + q.getAST());
                return q.getEdgeList();
            } else {
                Exp exp = env.getExp();
                if (exp != null && exp.getEdge() == edge && exp.getLevel() != -1) {
                    level = exp.getLevel();
                    n = ILIST;
                    //System.out.println("PI: " + level);
                }
            }
        }

        Node node = null, node2 = null;
        boolean isType = false;

        if (level != -1) {
        } else {
            for (Index ei : graph.getIndexList()) {
                // enumerate graph index to get the index i of nodes in edge: 0, 1, GRAPHINDEX
                // by convention the index of last is the index of graph node
                int i = ei.getIndex();
                if (i < edge.nbNode()) {
                    // Edge has a node that is bound or constant ?
                    Node qNode = getNode(edge, gNode, i);
                    if (qNode != null) {
                        // there may be no gNode
                        if (i == 1
                                && qNode.isConstant()
                                && isType(edge, env)
                                && graph.hasEntailment()) {
                            // RDFS entailment on ?x rdf:type c:Engineer
                            // no dichotomy on c:Engineer to get subsumption
                        } else {
                            Node val = env.getNode(qNode);
                            node = getValue(qNode, val, env);
                          
                            if (node == null) {
                                if (qNode.isConstant() || val != null) {
                                    // search a constant that is not in the graph: fail
                                    return empty;
                                }
                            } 
//                            else if (q.isMatchBlank() && node.isBlank()) {
//                                // blank node may deserve a recursive match, we may not join
//                                // use case: OWL blank match
//                                if (isFuzzy(edge, i)) {
//                                    // fuzzy match: skip join on node
//                                    node = null;
//                                } else {
//                                    // real join 
//                                    n = i;
//                                    break;
//                                }
//                            } 
                            else {
                                n = i;
                                if (i == 0 && !isType(edge, env)) {
                                    Node val2 = env.getNode(edge.getNode(1));
                                    node2 = getValue(edge.getNode(1), val2, env);
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }

        if (node == null && from.size() > 0) {
            // no query node has a value, there is a from [named]
            // from named <uri>
            // graph ?g { }
            // <<bind>> ?g to uri 
            if (gNode != null) {
                return complete(getEdgesFrom(predicate, from), gNode, edge, env);
            } else if (!isFromOK(from)) {
                // from are unknown
                return empty;
            }
        }

        Iterable<Entity> it = graph.getEdges(predicate, node, node2, n);

        // check gNode/from/named
        if (mode == EXTENSION) {
            q.getGlobalQuery().setExtension(true);
        }
        if (mode == EXTENSION && getQuery() == q) {
            if (it == null) {
                return empty;
            }
        } else {
            it = complete(q, it, gNode, getNode(gNode, env), from, level);
        }
        // in case of local Matcher
        it = complete(it, gNode, edge, env);

        return it;
    }
    
    
    @Override
    public Mappings getMappings(Node gNode, List<Node> from, Exp exp, Environment env) {
        if (env instanceof Memory){
            if (env.getQuery().isDebug()){
                System.out.println("BGP:\n" + exp);
            }
            Memory mem = (Memory) env;
            Eval eval = mem.getEval();   
            // prevent loop on BGP exp:
            exp.setType(Exp.AND);
            Mappings map = eval.subEval(this, gNode, gNode, exp, null);
            if (env.getQuery().isDebug()){
                System.out.println("BGP:\n" + map);
            }
            exp.setType(Exp.BGP);
            return map;
        }
        else {
            return Mappings.create(query, true);
        }
    }


    /**
     *
     */
    boolean isFuzzy(Edge edge, int i) {
        int type = fuzzy.fuzzy(edge.getLabel());
        return 0 <= i && i <= 1
                && (i == type || type == 2);
    }

    /**
     * Iterator of Entity that performs local match.match() Enable to have a
     * local ontology in case of several graphs with local ontologies
     */
    Iterable<Entity> complete(Iterable<Entity> it, Node gNode, Edge edge, Environment env) {
        if (isMatch && !env.getQuery().isRelax()) {
            MatchIterator mit = new MatchIterator(it, gNode, edge, graph, env, match);
            return mit;
        } else {
            // if query is relax, we want all types to find best match
            // hence skip MatchIterator
            return it;
        }
    }

    Node getNode(Node gNode, Environment env) {
        if (gNode == null) {
            return null;
        }
        return env.getNode(gNode);
    }

    /**
     * Iterate edges in from/named Retrieve edges from index IGRAPH with node
     * bound to from
     */
    Iterable<Entity> getEdgesFrom(Node predicate, List<Node> from) {
        MetaIterator<Entity> meta = new MetaIterator<Entity>();

        for (Node src : from) {
            Node tfrom = graph.getNode(src);
            if (tfrom != null && graph.isGraphNode(tfrom)) {
                //src = graph.getGraphNode(src.getLabel());
                Iterable<Entity> it = graph.getEdges(predicate, tfrom, IGRAPH);
                if (it != null) {
                    meta.next(it);
                }
            }
        }

        if (meta.isEmpty()) {
            return empty;
        } else {
            return meta;
        }
    }

//	Iterable<Entity> getTypeEdges(Node predicate, Node type, Environment env){
//		MetaIterator<Entity> meta = new MetaIterator<Entity>();
//		
//		for (Node node : graph.getTypeNodes()){
//
//			if (node.same(type) || match.isSubClassOf(node, type, env)){
//				Iterable<Entity> it = graph.getEdges(predicate, node, 1);
//				if (it != null){
//					meta.next(it);
//				}
//			}
//		}
//		
//		if (meta.isEmpty()){
//			return empty;
//		}
//		
//		return meta;
//	}
    /**
     * Check from/named if no gNode, eliminate duplicate successive edges
     */
    Iterable<Entity> complete(Query q, Iterable<Entity> it, Node gNode, Node sNode,
            List<Node> from) {
        return complete(q, it, gNode, sNode, from, -1);
    }

    Iterable<Entity> complete(Query q, Iterable<Entity> it, Node gNode, Node sNode,
            List<Node> from, int level) {
        if (it == null) {
            it = empty;
        } else if (gNode == null) {
            // eliminate similar edges
            // check from 
            EdgeIterator ii = new EdgeIterator(graph, it, from, false);
            ii.setLevel(level);
            ii.setSpeedUp(speedUp);
            it = ii;
        } else if (from.size() > 0 && sNode == null) {
            // check from [named]			
            it = new EdgeIterator(graph, it, from, true);
        }
        return it;
    }

    /**
     * Return Node that represents the predicate of the Edge
     */
    Node getPredicate(Edge edge, Environment env) {
        Node var = edge.getEdgeVariable();
        Node predicate = edge.getEdgeNode();
        if (var != null) {
            predicate = env.getNode(var);
            if (predicate == null) {
                predicate = edge.getEdgeNode();
            }
        }
        String name = predicate.getLabel();
        if (!name.equals(TOPREL)) {
            predicate = graph.getPropertyNode(name);
        }
        return predicate;
    }

    boolean isFromOK(List<Node> from) {
        for (Node node : from) {
            Node tfrom = graph.getNode(node);
            if (tfrom != null && graph.isGraphNode(tfrom)) {
                return true;
            }
        }
        return false;
    }

    boolean isFrom(Node ent, List<Node> from) {
        for (Node node : from) {
            if (ent.same(node)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Edges for Property Path Regex
     */
    public Iterable<Entity> getEdges(Node gNode, List<Node> from, Edge edge, Environment env,
            Regex exp, Node src, Node start, int index) {
        
        if (start == null) {
            Node qNode = edge.getNode(index);
            if (qNode.isConstant()) {
                //	start = qNode;
                start = graph.getNode(qNode);
                if (start == null) {
                    return empty;
                }
            }
        }
        
        if (start != null && isExtern(start, env)){
            start = graph.getNode(start);
        }

        if (exp.isReverse()) {
            // ?x ^p ?y
            // start node is now reverse(index) node
            index = (index == 0) ? 1 : 0;
        }

        if (exp.isNot()) {
            return getNegEdges(gNode, from, edge, env, exp, src, start, index);
        }

        Node predicate = graph.getPropertyNode(exp.getLongName());
        if (predicate == null) {
            return empty;
        }

        // draft: computes edges with construct-where queries
//		if (qengine!=null){
//			return construct(start, exp, index);
//		}
        Iterable<Entity> it = graph.getEdges(predicate, start, null, index);

//		if (gNode != null || from.size()>0 || ! graph.hasDefault()){
//			it = graph.getEdges(predicate, start, null, index);
//		}
//		else {
//			it = graph.getDefaultEdges(predicate, start, null, index);			
//			return it;
//		}
        // gNode, from
        it = complete(env.getQuery(), it, gNode, src, from);
        return it;
    }

    // draft: computes edges with construct-where queries
    Iterable<Entity> construct(Node start, Regex exp, int index) {
        Mappings map = qengine.process(start, exp.getLongName(), index);
        if (map == null || map.getGraph() == null) {
            return empty;
        }
        Graph g = (Graph) map.getGraph();
        g.prepare();
        return g.getEdges();
    }

    /**
     * regex is a negation ?x ! rdf:type ?y ?x !(rdf:type|rdfs:subClassOf) ?y
     * enumerate properties but those in the negation
     */
    public Iterable<Entity> getNegEdges(Node gNode, List<Node> from, Edge edge, Environment env,
            Regex exp, Node src, Node start, int index) {

        exp = exp.getArg(0);
        MetaIterator<Entity> meta = new MetaIterator<Entity>();
        for (Node pred : graph.getSortedProperties()) {
            if (match(exp, pred)) {
                // exclude
            } else {
                Iterable<Entity> it = graph.getEdges(pred, start, index);
                if (it != null) {
                    meta.next(it);
                }
            }
        }
        if (meta.isEmpty()) {
            return empty;
        }
        Iterable<Entity> it = complete(env.getQuery(), meta, gNode, src, from);
        return it;
    }

    boolean match(Regex exp, Node pred) {
        if (exp.isConstant()) {
            return exp.getLongName().equals(pred.getLabel());
        }
        for (int i = 0; i < exp.getArity(); i++) {
            if (exp.getArg(i).getLongName().equals(pred.getLabel())) {
                return true;
            }
        }
        return false;
    }

    /**
     * use case: path finder requires all nodes for zero length path
     *
     * TODO: env.isBound(gNode) is not thread safe
     */
    public Iterable<Entity> getNodes(Node gNode, List<Node> from, Edge edge,
            Environment env, List<Regex> exp, int index) {

        Node node = edge.getNode(index);
        if (node.isConstant()) {
            Node nn = graph.copy(node);
            ArrayList<Entity> list = new ArrayList<Entity>();
            list.add(EntityImpl.create(null, nn));
            return list;
        } else if (gNode == null) {
            if (from.size() > 0) {
                return getNodes(gNode, from, env);
            } else {
                return graph.getAllNodes();
            }
        } else {
            if (env.isBound(gNode)) {
                node = env.getNode(gNode);
                return graph.getNodes(node);
            } else if (gNode.isConstant()) {
                node = graph.getGraphNode(gNode.getLabel());
                if (node != null) {
                    return graph.getNodes(node);
                }
            } else if (from.size() > 0) {
                return getNodes(gNode, from, env);
            } else {
                return graph.getAllNodes();
            }
        }

        return new ArrayList<Entity>();
    }

    /**
     * Does graph gNode contain node use case: graph ?g {?x :p* ?y} ?g and ?x
     * are bound
     *
     */
    public boolean contains(Node gNode, Node node) {
        return true;
    }

    /**
     * Enumerate nodes from graphs in the list gNode == null : from gNode !=
     * null : from named
     */
    Iterable<Entity> getNodes(Node gNode, List<Node> from, Environment env) {
        MetaIterator<Entity> meta = new MetaIterator<Entity>();
        for (Node gn : getGraphNodes(gNode, from, env)) {
            meta.next(graph.getNodes(gn));
        }
        if (meta.isEmpty()) {
            return new ArrayList<Entity>();
        }
        if (gNode == null) {
            // eliminate duplicates
            return getNodes(meta);
        }
        return meta;
    }

    /**
     * Use case:
     *
     * from <g1>
     * from <g2>
     * ?x :p{*} ?y
     *
     * enumerate nodes from g1 and g2 and eliminate duplicates
     */
    Iterable<Entity> getNodes(final Iterable<Entity> iter) {

        return new Iterable<Entity>() {
            public Iterator<Entity> iterator() {

                final Hashtable<Node, Node> table = new Hashtable<Node, Node>();
                final Iterator<Entity> it = iter.iterator();

                return new Iterator<Entity>() {
                    public boolean hasNext() {
                        return it.hasNext();
                    }

                    public Entity next() {
                        while (hasNext()) {
                            Entity ent = it.next();
                            if (ent == null) {
                                return null;
                            }
                            if (!table.contains(ent.getNode())) {
                                table.put(ent.getNode(), ent.getNode());
                                return ent;
                            }
                        }
                        return null;
                    }

                    public void remove() {
                    }
                };
            }
        };
    }

    @Override
    public Iterable<Node> getGraphNodes(Node node, List<Node> from,
            Environment env) {
        // TODO check from
        if (from.size() > 0) {
            return getGraphNodes2(node, from, env);
        }

        return graph.getGraphNodes();
    }

    public Iterable<Node> getGraphNodes2(Node node, final List<Node> from,
            Environment env) {

        List<Node> list = new ArrayList<Node>();
        for (Node nn : from) {
            Node target = graph.getGraphNode(nn.getLabel());
            if (target != null) {
                list.add(target);
            }
        }

        return list;
    }

    /**
     *   Cast Java value into IDatatype 
     */
    public IDatatype getValue(Object value){
        IDatatype dt = DatatypeMap.cast(value);
        if (dt == null){
            dt = DatatypeMap.createObject("tmp", value);
        }
        return dt;
    }
    
    // cast IDatatype or Java value into DatatypeValue
    public DatatypeValue getDatatypeValue(Object value){
        if (value == null){
            return null;
        }
        else if (value instanceof IDatatype){
            return (IDatatype) value;
        }
        else {
            return getValue(value);
        }
    }
    
    /**
     * Return a Node given a value (IDatatype value) Use case: select/bind (exp
     * as node) return the Node in the graph or return the IDatatype value as is
     * (to speed up)
     *
     */
      synchronized public Node getNode(Object value) {
        // TODO Auto-generated method stub
        if (!(value instanceof IDatatype)) {
            return DatatypeMap.createObject("tmp", value);
        }
        IDatatype dt = (IDatatype) value;
        if (selfValue || dt.isFuture()) {
            // future: template intermediate result 
            return dt;
        }

        Node node = vcache.get(dt);
        if (node != null) {
            return node;
        }
        // look up Node value in the graph
        node = graph.getNode(dt, false, false);

        if (node == null) {
            node = dt;
        }

        vcache.put(dt, node);
        return node;

        //        if (dt.isBlank() && dt.getLabel().startsWith(Query.BPATH)) {
//                // blank generated for path node: do not store it
//                return local.getNode(dt, true, false);
//            } else {
//                node = local.getNode(dt, true, true);
//        }
    }

    public Object getValue(Node node) {
        return node.getValue();
    }

    @Override
    public Iterable<Entity> getNodes(Node gNode, List<Node> from, Node qNode,
            Environment env) {
        // TODO Auto-generated method stub
        return empty;
    }

    @Override
    public void init(int nbNodes, int nbEdges) {
        // TODO Auto-generated method stub
        graph.init();
    }

    @Override
    public void initPath(Edge edge, int index) {
        //graph.initPath();
    }

    @Override
    public boolean isBindable(Node node) {
        if (!Graph.valueOut) {
            return true;
        }
        IDatatype dt = (IDatatype) node.getValue();
        // 1 && 1.0 are not same Node: cannot bind (see kgram Eval)
        return !dt.isNumber();
    }

    @Override
    public boolean isGraphNode(Node gNode, List<Node> from, Environment env) {
        // TODO Auto-generated method stub
        Node node = env.getNode(gNode);
        if (!graph.isGraphNode(node)) {
            return false;
        }
        if (from.size() == 0) {
            return true;
        }

        return ei.isFrom(from, node);
    }

    @Override
    public Mappings map(List<Node> nodes, Object object) {
        // TODO Auto-generated method stub
        if (object instanceof IDatatype) {
            IDatatype dt = (IDatatype) object;
            if (dt.getObject() != null) {
                return map(nodes, dt.getObject());
            } else {
                return map(nodes, dt);
            }
        } else if (object instanceof SQLResult) {
            // sql()
            Mappings lMap = mapper.sql(nodes, (SQLResult) object);
            return lMap;
        } else if (object instanceof Mappings) {
            return map(nodes, (Mappings) object);
        }
        else if (object instanceof Collection){
            return map(nodes, (Collection<IDatatype>)object);
        }
        return new Mappings();
    }
    
    Mappings map(List<Node> lNodes, Collection<IDatatype> list) {
        Mappings map = new Mappings();
        for (IDatatype dt : list){
            Mapping m =  Mapping.create(lNodes.get(0), dt);
            map.add(m);
        }
        return map;
    }


    Mappings map(List<Node> lNodes, Mappings map) {
        map.setNodes(lNodes);
        return map;
    }

    Mappings map(List<Node> nodes, IDatatype dt) {
        Node[] qNodes = new Node[nodes.size()];
        int i = 0;
        for (Node qNode : nodes) {
            qNodes[i++] = qNode;
        }
        Mappings lMap = new Mappings();
        List<Node> lNode = toNodeList(dt);
        for (Node node : lNode) {
            Node[] tNodes = new Node[1];
            tNodes[0] = node;
            Mapping map = Mapping.create(qNodes, tNodes);
            lMap.add(map);
        }
        return lMap;
    }

    @Override
    public List<Node> toNodeList(Object obj) {
        IDatatype dt = (IDatatype) obj;
        List<Node> list = new ArrayList<Node>();
        if (dt.isArray()) {
            for (IDatatype dd : dt.getValues()) {
                if (dd.isXMLLiteral() && dd.getLabel().startsWith("http://")) {
                    // try an URI
                    dd = DatatypeMap.newResource(dd.getLabel());

                }
                list.add(getNode(dd));
            }
        } else {
            list.add(getNode(dt));
        }
        return list;
    }

    void filter(Environment env) {
        // KGRAM exp for current edge
        Exp exp = env.getExp();
        List<String> lVar = new ArrayList<String>();
        List<Node> lNode = new ArrayList<Node>();

        for (Filter f : exp.getFilters()) {
            // filters attached to current edge
            if (f.getExp().isExist()) {
                // skip exists { PAT }
                continue;
            }

            // function exp.bind(f) tests whether current edge binds all variable of filter f			
            for (String var : f.getVariables()) {
                if (!lVar.contains(var)) {
                    Node node = env.getNode(var);
                    if (node != null) {
                        lVar.add(var);
                        lNode.add(node);
                    }
                }
            }
        }

        System.out.print("bindings ");
        for (String var : lVar) {
            System.out.print(var + " ");
        }
        System.out.println("{(");
        for (Node node : lNode) {
            System.out.print(node + " ");
        }
        System.out.println(")}");

    }

    private boolean isSelfValue() {
        return selfValue;
    }

    public void setSelfValue(boolean selfValue) {
        this.selfValue = selfValue;
    }

    @Override
    /**
     * Overloading of graph ?g { } The value of ?g may an extended graph
     * producer
     */
    public boolean isProducer(Node node) {
        IDatatype dt = (IDatatype) node.getValue();
        if (dt.getObject() != null) {
            return toRDF.isGraphAble(dt.getObject());
        }
        // system named graph in a GraphStore
        return graph.getNamedGraph(node.getLabel()) != null;
    }

    @Override
    public Producer getProducer(Node node, Environment env) {
        IDatatype dt = (IDatatype) node.getValue();
        Object obj = dt.getObject();
        Graph g = null;

        if (obj == null) {
            g = graph.getNamedGraph(node.getLabel());
        } else {
            g = toRDF.getGraph(obj);
        }

        if (g == null) {
            g = Graph.create();
        }
        ProducerImpl p = ProducerImpl.create(g);
        p.setMode(EXTENSION);
        // producer remember the query that createt it
        // use case: templates may share this producer
        p.setQuery(env.getQuery());
        return p;
    }

    /**
     * @return the speedUp
     */
    public boolean isSpeedUp() {
        return speedUp;
    }

    /**
     * @param speedUp the speedUp to set
     */
    public void setSpeedUp(boolean speedUp) {
        this.speedUp = speedUp;
    }

    /**
     * @return the index
     */
    public int getIndex() {
        return index;
    }

    /**
     * @param index the index to set
     */
    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public void setGraphNode(Node n) {
        graphNode = n;
    }

    @Override
    public Node getGraphNode() {
        return graphNode;
    }

    /**
     * IProducer for QP. F. Song 2014
     *
     * @param qpgn QPG node
     * @param type
     * @return
     */
    @Override
    public int getCount(QPGNode qpgn, int type) {
        switch (type) {
            case PREDICATE:
                // case 1: unbound
                if (qpgn.getExpNode(PREDICATE).isVariable()) {
                    return getSize(PREDICATE);
                }

                // case 2: bound
                Iterator<Node> it = this.graph.getProperties().iterator();
                while (it.hasNext()) {
                    Node predNode = it.next();
                    if (qpgn.getExpNode(PREDICATE).getLabel().equalsIgnoreCase(predNode.getLabel())) {
                        return this.graph.size(predNode);
                    }
                }

                //case 3: not found
                return 0;
            //to do for SUBJECT | OBJECT
            //for the moment, cannot get these values directly
            case SUBJECT:
            case OBJECT:
            case TRIPLE:
            default:
                return NA;
        }

    }

    @Override
    public int getSize(int type) {
        switch (type) {
            case ALL:
                return this.graph.size();
            case PREDICATE:
                return this.graph.getIndex().size();
            //to do for SUBJECT | OBJECT
            //for the moment, cannot get these values directIProducerQP      
            case SUBJECT:
            case OBJECT:
            default:
                return NA;
        }
    }

    /**
     * @return the query
     */
    public Query getQuery() {
        return query;
    }

    /**
     * @param query the query to set
     */
    public void setQuery(Query query) {
        this.query = query;
    }
    
}
