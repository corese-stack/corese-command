package fr.inria.corese.command.utils.wrapper;

import picocli.CommandLine;

/** Version provider for dynamic version information */
public class CoreseVersionProvider implements CommandLine.IVersionProvider {

    // Version of Corese-Command
    public static final String COMMAND_VERSION = "4.6.2";

    @Override
    public String[] getVersion() {
        return new String[] {
            COMMAND_VERSION, "Based on: " + getCoreVersion()
        };
    }

    /**
     * Get the version of the core library.
     * 
     * Format example: "Corese-Core 4.6.2"
     *
     * @return the version string of the core library
     */
    protected String getCoreVersion() {
        return "Corese-Core " + fr.inria.corese.core.util.CoreseInfo.getVersion();
    }

    /**
     * Get the version of Corese-Command
     *
     * @return the version of Corese-Command
     */
    public static String getCommandVersion() {
        return COMMAND_VERSION;
    }
}
