package fr.inria.corese.command;

import fr.inria.corese.core.util.CoreseInfo;
import picocli.CommandLine;

/**
 * Custom version provider for dynamic version information
 */
public class VersionProvider implements CommandLine.IVersionProvider {

    // Version of Corese-Command
    public final static String commandVersion = "4.6.1";

    @Override
    public String[] getVersion() {
        return new String[] {
                commandVersion,
                "Based on Corese-Core Version: " + CoreseInfo.getVersion()
        };
    }

    /**
     * Get the version of Corese-Command
     * 
     * @return the version of Corese-Command
     */
    public static String getCommandVersion() {
        return commandVersion;
    }

}