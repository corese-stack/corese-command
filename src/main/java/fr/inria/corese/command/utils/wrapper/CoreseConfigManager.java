package fr.inria.corese.command.utils.wrapper;

import fr.inria.corese.core.util.Property;

import picocli.CommandLine.Model.CommandSpec;

import java.nio.file.Path;

/**
 * Configuration manager for Corese commands.
 *
 * <p>This class handles loading configuration from files or using default settings, and provides
 * methods to configure various Corese properties. It supports both file-based configuration and
 * programmatic property settings.
 *
 * @author Corese Team
 */
public class CoreseConfigManager {

    // ===== Fields =====

    private CommandSpec spec;
    private boolean verbose;

    private Path configFilePath;

    // ===== Constructors =====

    /**
     * Constructs a ConfigManager with the specified command specification and verbosity setting.
     *
     * <p>This constructor initializes the ConfigManager without a configuration file path, which
     * means that {@link #loadConfig()} will use default configuration settings.
     *
     * @param spec the command specification used for output and error messages
     * @param verbose if {@code true}, enables verbose logging of configuration operations
     */
    public CoreseConfigManager(CommandSpec spec, boolean verbose) {
        this.spec = spec;
        this.verbose = verbose;
        this.configFilePath = null;
    }

    /**
     * Constructs a ConfigManager with the specified command specification, verbosity setting, and
     * configuration file path.
     *
     * <p>This constructor initializes the ConfigManager with a specific configuration file path,
     * which will be used by {@link #loadConfig()} to load settings from the file.
     *
     * @param spec the command specification used for output and error messages
     * @param verbose if {@code true}, enables verbose logging of configuration operations
     * @param configFilePath the path to the configuration file to be loaded
     */
    public CoreseConfigManager(CommandSpec spec, boolean verbose, Path configFilePath) {
        this(spec, verbose);
        this.configFilePath = configFilePath;
    }

    // ===== Public Methods =====

    /**
     * Loads the configuration settings.
     *
     * <p>If a configuration file path was provided during construction, this method loads settings
     * from that file. Otherwise, it uses default configuration settings.
     */
    public void loadConfig() {
        if (this.configFilePath == null) {
            loadDefaultConfig();
        } else {
            loadFromFile(this.configFilePath);
        }
    }

    /**
     * Sets the OWL auto-import property.
     *
     * <p>This method configures whether OWL ontologies should be automatically imported during
     * processing. When enabled, the system will automatically resolve and import referenced OWL
     * ontologies.
     *
     * @param value {@code true} to enable OWL auto-imports, {@code false} to disable
     */
    public void setOwlAutoImports(boolean value) {
        Property.set(Property.Value.OWL_AUTO_IMPORT, value);
    }

    // ===== Private Methods =====

    /**
     * Loads configuration properties from the specified file.
     *
     * <p>This method uses the Corese utilities to load properties from the given file path. If
     * verbose mode is enabled, it prints a confirmation message to the error stream.
     *
     * @param path the path to the configuration file to load
     */
    private void loadFromFile(Path path) {
        try {
            Property.load(path.toString());
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to open config file: " + path, e);
        }

        if (verbose) {
            spec.commandLine().getErr().println("Loaded config file: " + path.toString());
        }
    }

    /**
     * Loads the default configuration settings.
     *
     * <p>This method is called when no configuration file path is specified. If verbose mode is
     * enabled, it prints a confirmation message to the error stream. The default configuration uses
     * built-in Corese property values.
     */
    private void loadDefaultConfig() {
        if (this.verbose) {
            this.spec.commandLine().getErr().println("Loaded default config");
        }
    }
}
