package fr.inria.corese.command.utils.coreseCoreWrapper;


import fr.inria.corese.core.Graph;
import fr.inria.corese.core.load.Load;

/** A class to gather all dependencies to Corese-core in one place
 * This one is for everything related to RDFLoading
 */
public class RDFLoaderWrapper {

    public static Load graphLoader(Graph graph) {
        return Load.create(graph);
    }

}