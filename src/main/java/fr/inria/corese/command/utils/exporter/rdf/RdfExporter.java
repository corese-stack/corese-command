package fr.inria.corese.command.utils.exporter.rdf;

import fr.inria.corese.command.utils.exporter.AbstractExporter;
import fr.inria.corese.command.utils.exporter.OutputFormat;
import fr.inria.corese.command.utils.wrapper.CoreseRdfGraph;

import picocli.CommandLine.Model.CommandSpec;

import java.nio.file.Path;

/** Utility class to export RDF graphs. */
public class RdfExporter extends AbstractExporter {

    // ===== Constructor ===== //

    /**
     * Constructor.
     *
     * @param spec Command specification.
     * @param verbose If true, print information about the exported file.
     * @param output Output file path. If not provided, the result will be written to standard
     *     output.
     */
    public RdfExporter(CommandSpec spec, boolean verbose, Path output) {
        super(spec, verbose, output);
    }

    // ===== Public methods ===== //

    /**
     * Export an RDF graph to a file or standard output.
     *
     * @param graph RDF graph to export.
     * @param format Serialization format.
     */
    public void export(CoreseRdfGraph graph, OutputFormat format) {

        if (this.outputIsDefined) {
            Path path =
                    this.needToAppendExtension
                            ? Path.of(this.output + "." + format.getExtension())
                            : this.output;
            exportToFile(path, format, graph);
        } else {
            exportToStdout(format, graph);
        }
    }
}
