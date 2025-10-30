package fr.inria.corese.command.utils.exporter;

import picocli.CommandLine.Model.CommandSpec;

import java.nio.file.Path;

import fr.inria.corese.command.utils.wrapper.CoreseExporter;
import fr.inria.corese.command.utils.wrapper.CoreseRdfGraph;
import fr.inria.corese.command.utils.wrapper.CoreseSparqlResult;

/** Utility class to export SPARQL query results and RDF graphs. */
public abstract class AbstractExporter {

    public static final String DEFAULT_OUTPUT = "./output";
    private static final Path DEFAULT_OUTPUT_PATH = Path.of(DEFAULT_OUTPUT);

    // Command specification
    private CommandSpec spec;
    private boolean verbose;

    // Output
    protected Path output;
    protected boolean outputIsDefined;
    protected boolean needToAppendExtension;
    private boolean outputToFileIsDefault;

    // ===== Constructor ===== //

    /**
     * Constructor.
     *
     * @param spec Command specification.
     * @param verbose If true, print information about the exported file.
     * @param output Output file path. If not provided, the result will be written to standard
     *     output.
     */
    protected AbstractExporter(CommandSpec spec, boolean verbose, Path output)
            throws IllegalArgumentException {
        // Command specification
        this.spec = spec;
        this.verbose = verbose;

        // Output
        this.outputIsDefined = output != null;
        this.outputToFileIsDefault = outputIsDefined && DEFAULT_OUTPUT_PATH.equals(this.output);
        this.output = outputToFileIsDefault ? DEFAULT_OUTPUT_PATH : output;
        this.needToAppendExtension = outputIsDefined && !hasExtension(this.output);
    }

    // ===== Protected methods ===== //

    /**
     * Export the result to a file.
     *
     * @param path Path of the file to export to.
     * @param exportFormat Export format.
     * @param graph Graph to export.
     */
    protected void exportToFile(Path path, OutputFormat exportFormat, CoreseRdfGraph graph) {
        CoreseExporter resultFormater = new CoreseExporter(graph);
        exportToFile(path, exportFormat, resultFormater);
    }

    /**
     * Export the result to standard output.
     *
     * @param exportFormat Export format.
     * @param graph Graph to export.
     */
    protected void exportToStdout(
            OutputFormat exportFormat, CoreseRdfGraph graph) {
        CoreseExporter resultFormater = new CoreseExporter(graph);
        exportToStdout(exportFormat, resultFormater);
    }

    /**
     * Export the result to a file.
     *
     * @param path Path of the file to export to.
     * @param exportFormat Export format.
     * @param sparqlResult SPARQL result to export.
     */
    protected void exportToFile(
            Path path, OutputFormat exportFormat, CoreseSparqlResult sparqlResult) {
        CoreseExporter resultFormater = new CoreseExporter(sparqlResult);
        exportToFile(path, exportFormat, resultFormater);
    }

    /**
     * Export the result to standard output.
     *
     * @param exportFormat Export format.
     * @param sparqlResult SPARQL result to export.
     */
    protected void exportToStdout(
            OutputFormat exportFormat, CoreseSparqlResult sparqlResult) {
        CoreseExporter resultFormater = new CoreseExporter(sparqlResult);
        exportToStdout(exportFormat, resultFormater);
    }

    // ===== Private methods ===== //

    /**
     * Export the result to a file.
     *
     * @param path Path of the file to export to.
     * @param exportFormat Export format.
     * @param ResultFormat Result formater.
     */
    private void exportToFile(
            Path path, OutputFormat exportFormat, CoreseExporter resultFormater) {

        resultFormater.setSelectFormat(exportFormat);
        resultFormater.setConstructFormat(exportFormat);

        resultFormater.write(path.toString());

        if (this.verbose) {
            this.spec
                    .commandLine()
                    .getErr()
                    .println(
                            "Exported result in file: "
                                    + path.toString()
                                    + " with format: "
                                    + exportFormat.getName());
        }
    }

    /**
     * Export the result to standard output.
     *
     * @param exportFormat Export format.
     * @param ResultFormat Result formater.
     */
    private void exportToStdout(
            OutputFormat exportFormat, CoreseExporter resultFormater) {

        // Configure the result formater
        resultFormater.setSelectFormat(exportFormat);
        resultFormater.setConstructFormat(exportFormat);

        // Write the result to standard output
        try {
            String str = resultFormater.toString();
            spec.commandLine().getOut().println(str);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to write to standard output", e);
        }

        // Print information about the exported file
        if (verbose) {
            spec.commandLine()
                    .getErr()
                    .println("Exported result to standard output with format: " + exportFormat.getName());
        }
    }

    /**
     * Determine if the given path has an extension.
     *
     * @param path Path to check.
     * @return True if the path has an extension, false otherwise.
     */
    private boolean hasExtension(Path path) {
        String fileName = path.getFileName().toString();
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex > 0 && dotIndex < fileName.length() - 1;
    }
}
