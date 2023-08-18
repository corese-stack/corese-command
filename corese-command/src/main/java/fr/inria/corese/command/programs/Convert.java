package fr.inria.corese.command.programs;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.Callable;

import fr.inria.corese.command.App;
import fr.inria.corese.command.utils.GraphUtils;
import fr.inria.corese.command.utils.format.EnumInputFormat;
import fr.inria.corese.command.utils.format.EnumOutputFormat;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.util.Property;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

@Command(name = "convert", version = App.version, description = "Convert an RDF file from one serialization format to another.", mixinStandardHelpOptions = true)
public class Convert implements Callable<Integer> {

    private static final String DEFAULT_OUTPUT_FILE_NAME = "output";
    private static final int ERROR_EXIT_CODE_SUCCESS = 0;
    private static final int ERROR_EXIT_CODE_ERROR = 1;

    @Spec
    CommandSpec spec;

    @Option(names = { "-i", "--input-data" }, description = "Path or URL of the file that needs to be converted.")
    private String input;

    @Option(names = { "-f", "-if",
            "--input-format" }, description = "RDF serialization format of the input file. Possible values: ${COMPLETION-CANDIDATES}.")
    private EnumInputFormat inputFormat = null;

    @Option(names = { "-o",
            "--output-data" }, description = "Output file path. If not provided, the result will be written to standard output.", arity = "0..1", fallbackValue = DEFAULT_OUTPUT_FILE_NAME)
    private Path output;

    @Option(names = { "-r", "-of",
            "--output-format" }, required = true, description = "Serialization format to which the input file should be converted. Possible values: ${COMPLETION-CANDIDATES}.")
    private EnumOutputFormat outputFormat;

    @Option(names = { "-v",
            "--verbose" }, description = "Prints more information about the execution of the command.")
    private boolean verbose = false;

    @Option(names = { "-c", "--config",
            "--init" }, description = "Path to a configuration file. If not provided, the default configuration file will be used.", required = false)
    private String configFilePath;

    private Graph graph;

    private boolean outputFormatIsDefined = false;
    private boolean isDefaultOutputName = false;

    public Convert() {
    }

    @Override
    public Integer call() {

        try {

            // Load configuration file
            if (configFilePath != null) {
                Property.load(configFilePath);
                if (this.verbose) {
                    spec.commandLine().getOut().println("Loaded configuration file: " + configFilePath);
                }
            } else if (this.verbose) {
                spec.commandLine().getOut().println("No configuration file provided. Using default configuration.");
            }

            this.outputFormatIsDefined = this.output != null;
            this.isDefaultOutputName = this.output != null && DEFAULT_OUTPUT_FILE_NAME.equals(this.output.toString());
            checkInputValues();
            loadInputFile();
            exportGraph();
            return ERROR_EXIT_CODE_SUCCESS;
        } catch (IllegalArgumentException | IOException e) {
            spec.commandLine().getErr().println("\u001B[31mError: " + e.getMessage() + "\u001B[0m");
            return ERROR_EXIT_CODE_ERROR;
        }
    }

    /**
     * Check if the input values are correct.
     * 
     * @throws IllegalArgumentException if input path is same as output path.
     */
    private void checkInputValues() throws IllegalArgumentException {
        if (input != null && output != null && input.equals(output.toString())) {
            throw new IllegalArgumentException("Input path cannot be the same as output path.");
        }
    }

    /**
     * Load the input file.
     * 
     * @throws IllegalArgumentException if the input format is not supported.
     * @throws IOException              if an I/O error occurs while loading the
     *                                  input file.
     */
    private void loadInputFile() throws IllegalArgumentException, IOException {
        if (input == null) {
            // if input is null, load from stdin
            this.graph = GraphUtils.load(System.in, inputFormat);
            if (verbose) {
                spec.commandLine().getOut().println("Loaded file: stdin");
            }
        } else {
            this.graph = GraphUtils.load(input, inputFormat);
            if (verbose) {
                spec.commandLine().getOut().println("Loaded file: " + input);
            }
        }
    }

    /**
     * Export the graph.
     * 
     * @throws IOException if an I/O error occurs while exporting the graph.
     */
    private void exportGraph() throws IOException {

        if (verbose) {
            spec.commandLine().getOut().println("Converting file to " + outputFormat + " format...");
        }

        Path outputFileName;

        // Set output file name
        if (this.outputFormatIsDefined && !this.isDefaultOutputName) {
            outputFileName = this.output;
        } else {
            outputFileName = Path.of(DEFAULT_OUTPUT_FILE_NAME + "." + this.outputFormat.getExtention());
        }

        // Export the graph
        if (output == null) {
            // if output is null, print to stdout
            GraphUtils.exportToStdout(graph, outputFormat, spec);
            if (verbose) {
                spec.commandLine().getOut().println("Results exported to standard output.");
            }
        } else {
            GraphUtils.exportToFile(graph, outputFormat, outputFileName);
            if (verbose) {
                spec.commandLine().getOut().println("Results exported to file: " + outputFileName);
            }
        }
    }

}
