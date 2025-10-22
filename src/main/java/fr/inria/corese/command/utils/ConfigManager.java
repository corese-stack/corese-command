package fr.inria.corese.command.utils;

import java.nio.file.Path;

import fr.inria.corese.command.utils.coresecorowrapper.CoreseUtils;
import picocli.CommandLine.Model.CommandSpec;

/**
 * Utility class to manage configuration files.
 */
public class ConfigManager {

    private ConfigManager() {
        // Prevent instantiation
    }

    /**
     * Load a configuration file.
     *
     * @param path    Path of the file to load.
     * @param spec    Command specification.
     * @param verbose If true, print information about the loaded files.
     */
    public static void loadFromFile(Path path, CommandSpec spec, boolean verbose) {

        CoreseUtils.loadProperty(path.toString());

        if (verbose) {
            spec.commandLine().getErr().println("Loaded config file: " + path.toString());
        }
    }

    /**
     * Load the default configuration file.
     *
     * @param spec    Command specification.
     * @param verbose If true, print information about the loaded files.
     */
    public static void loadDefaultConfig(CommandSpec spec, boolean verbose) {
        if (verbose) {
            spec.commandLine().getErr().println("Loaded default config");
        }
    }

}
