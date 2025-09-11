package fr.inria.corese.command.utils.coreseCoreWrapper;

import java.io.InputStream;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.api.Loader;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.core.load.LoadFormat;

/** A class to gather all dependencies to Corese-core in one place
 * This one is for everything related to RDFLoading
 */
public class RDFLoaderWrapper {

    /**
     * Creates a Load instance for the given graph.
     *
     * @param graph the graph to load data into
     * @return a Load instance
     */
    public static Load graphLoader(Graph graph) {
        return Load.create(graph);
    }

    /**
     * Determines the load format from the input string.
     *
     * @param input the input string representing the format
     * @return the corresponding Loader.format
     */
    public static Loader.format getLoadFormat(String input) {
        return LoadFormat.getFormat(input);
    }

    /**
     * Parses RDF data from an InputStream into the given Load instance using the specified format.
     *
     * @param loader      the Load instance to use for parsing
     * @param inputStream the InputStream containing RDF data
     * @param inputFormat the format of the RDF data
     * @throws LoadException if an error occurs during parsing
     */
    public static void parse(Load loader, InputStream inputStream, Load.format inputFormat) throws LoadException {
        loader.parse(inputStream, inputFormat);
    }

}