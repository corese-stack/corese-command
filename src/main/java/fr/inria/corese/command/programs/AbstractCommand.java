package fr.inria.corese.command.programs;

import fr.inria.corese.command.utils.exporter.AbstractExporter;
import fr.inria.corese.command.utils.wrapper.CoreseConfigManager;
import fr.inria.corese.command.utils.wrapper.CoreseVersionProvider;

import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

import java.nio.file.Path;
import java.util.concurrent.Callable;

/**
 * Abstract class for all commands.
 *
 * <p>This class provides common options and methods for all commands.
 */
@Command(versionProvider = CoreseVersionProvider.class)
public abstract class AbstractCommand implements Callable<Integer> {

    // ===== Constants =====

    // Exit codes
    protected static final int ERROR_EXIT_CODE_SUCCESS = 0;
    protected static final int ERROR_EXIT_CODE_ERROR = 1;

    // ===== Options =====

    @Option(
            names = {"-o", "--output-data"},
            description =
                    "Specifies the output file path. If not provided, the result will be written to"
                            + " standard output.",
            arity = "0..1",
            fallbackValue = AbstractExporter.DEFAULT_OUTPUT)
    protected Path output;

    @Option(
            names = {"-c", "--config", "--init"},
            description =
                    "Specifies the path to a configuration file. If not provided, the default"
                            + " configuration file will be used.",
            required = false)
    private Path configFilePath;

    @Option(
            names = {"-v", "--verbose"},
            description =
                    "Enables verbose mode, printing more information about the execution of the"
                            + " command.",
            negatable = true)
    protected boolean verbose = false;

    @Option(
            names = {"-w", "--owl-import"},
            description =
                    "Enables the automatic importation of ontologies specified in 'owl:imports'"
                            + " statements. When this flag is set, the application will fetch and"
                            + " include referenced ontologies. Default is '${DEFAULT-VALUE}'.",
            required = false,
            defaultValue = "false")
    private boolean owlImport;

    // ===== Properties =====

    // Command specification
    @Spec protected CommandSpec spec;

    // Output
    protected Boolean outputToFileIsDefault = false;

    // ===== Methods =====

    @Override
    public Integer call() {

        // Load configuration
        CoreseConfigManager configManager =
                new CoreseConfigManager(this.spec, this.verbose, this.configFilePath);
        configManager.loadConfig();

        // Set owl import
        configManager.setOwlAutoImports(this.owlImport);

        return 0;
    }
}
