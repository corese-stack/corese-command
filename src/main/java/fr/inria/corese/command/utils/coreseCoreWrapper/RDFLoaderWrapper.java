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

    public static Load graphLoader(Graph graph) {
        return Load.create(graph);
    }

    public static Loader.format getLoadFormat(String input) {
        return LoadFormat.getFormat(input);
    }

    public static void parse(Load loader, InputStream inputStream, Load.format inputFormat) throws LoadException {
        loader.parse(inputStream, inputFormat);
    }

}