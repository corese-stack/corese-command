package fr.inria.corese.command.utils.exporter;

import fr.inria.corese.command.utils.coresecorowrapper.CoreseRdfGraph;
import fr.inria.corese.command.utils.coresecorowrapper.CoreseResultFormat;
import fr.inria.corese.core.kgram.core.Mappings;
import fr.inria.corese.core.sparql.api.ResultFormatDef;

import picocli.CommandLine.Model.CommandSpec;

import java.nio.file.Path;

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
     * @param coreseFormat Corese format.
     * @param formatName Name of the format.
     * @param graph Graph to export.
     */
    protected void exportToFile(
            Path path,
            ResultFormatDef.format coreseFormat,
            String formatName,
            CoreseRdfGraph graph) {
        CoreseResultFormat resultFormater = new CoreseResultFormat(graph);
        exportToFile(path, coreseFormat, formatName, resultFormater);
    }

    /**
     * Export the result to standard output.
     *
     * @param coreseFormat Corese format.
     * @param formatName Name of the format.
     * @param graph Graph to export.
     */
    protected void exportToStdout(
            ResultFormatDef.format coreseFormat, String formatName, CoreseRdfGraph graph) {
        CoreseResultFormat resultFormater = new CoreseResultFormat(graph);
        exportToStdout(coreseFormat, formatName, resultFormater);
    }

    /**
     * Export the result to a file.
     *
     * @param path Path of the file to export to.
     * @param coreseFormat Corese format.
     * @param formatName Name of the format.
     * @param mappings Mappings to export.
     */
    protected void exportToFile(
            Path path, ResultFormatDef.format coreseFormat, String formatName, Mappings mappings) {
        CoreseResultFormat resultFormater = new CoreseResultFormat(mappings);
        exportToFile(path, coreseFormat, formatName, resultFormater);
    }

    /**
     * Export the result to standard output.
     *
     * @param coreseFormat Corese format.
     * @param formatName Name of the format.
     * @param mappings Mappings to export.
     */
    protected void exportToStdout(
            ResultFormatDef.format coreseFormat, String formatName, Mappings mappings) {
        CoreseResultFormat resultFormater = new CoreseResultFormat(mappings);
        exportToStdout(coreseFormat, formatName, resultFormater);
    }

    // ===== Private methods ===== //

    /**
     * Export the result to a file.
     *
     * @param path Path of the file to export to.
     * @param coreseFormat Corese format.
     * @param formatName Name of the format.
     * @param ResultFormat Result formater.
     */
    private void exportToFile(
            Path path,
            ResultFormatDef.format coreseFormat,
            String formatName,
            CoreseResultFormat resultFormater) {

        resultFormater.setSelectFormat(coreseFormat);
        resultFormater.setConstructFormat(coreseFormat);

        resultFormater.write(path.toString());

        if (this.verbose) {
            this.spec
                    .commandLine()
                    .getErr()
                    .println(
                            "Exported result in file: "
                                    + path.toString()
                                    + " with format: "
                                    + formatName);
        }
    }

    /**
     * Export the result to standard output.
     *
     * @param coreseFormat Corese format.
     * @param formatName Name of the format.
     * @param ResultFormat Result formater.
     */
    private void exportToStdout(
            ResultFormatDef.format coreseFormat,
            String formatName,
            CoreseResultFormat resultFormater) {

        // Configure the result formater
        resultFormater.setSelectFormat(coreseFormat);
        resultFormater.setConstructFormat(coreseFormat);

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
                    .println("Exported result to standard output with format: " + formatName);
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
