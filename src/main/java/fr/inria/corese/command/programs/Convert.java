package fr.inria.corese.command.programs;

import fr.inria.corese.command.exceptions.CoreseCommandException;
import fr.inria.corese.command.utils.exporter.OutputFormat;
import fr.inria.corese.command.utils.exporter.rdf.RdfExporter;
import fr.inria.corese.command.utils.exporter.rdf.RdfOutputFormatCandidates;
import fr.inria.corese.command.utils.exporter.rdf.RdfOutputFormatConverter;
import fr.inria.corese.command.utils.loader.rdf.RdfLoader;
import fr.inria.corese.command.utils.loader.rdf.RdfInputFormat;
import fr.inria.corese.command.utils.loader.rdf.RdfInputFormatCandidates;
import fr.inria.corese.command.utils.loader.rdf.RdfInputFormatConverter;
import fr.inria.corese.command.utils.wrapper.CoreseRdfGraph;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
        name = "convert",
        description = "Convert an RDF file from one serialization format to another.",
        mixinStandardHelpOptions = true)
public class Convert extends AbstractInputCommand {

    @Option(
            names = {"-f", "-if", "--input-format"},
            description =
                    "Specifies the RDF serialization format of the input file. Possible"
                            + " values:@|fg(magenta) ${COMPLETION-CANDIDATES}|@.",
            converter = RdfInputFormatConverter.class,
            completionCandidates = RdfInputFormatCandidates.class)
    private RdfInputFormat inputFormat = null;

    @Option(
            names = {"-r", "-of", "--output-format"},
            required = true,
            description =
                    "Specifies the RDF serialization format of the output file. Possible"
                            + " values::@|fg(magenta)${COMPLETION-CANDIDATES}|@.",
            converter = RdfOutputFormatConverter.class,
            completionCandidates = RdfOutputFormatCandidates.class)
    private OutputFormat outputFormat;

    @Override
    public Integer call() {

        super.call();

        try {
            // Create data graph
            CoreseRdfGraph graph = new CoreseRdfGraph();

            // Load the data file(s)
            RdfLoader loader = new RdfLoader(this.spec, this.verbose, graph);
            loader.load(this.inputsRdfData, this.inputFormat, this.recursive);

            // Export the graph
            RdfExporter rdfExporter = new RdfExporter(this.spec, this.verbose, this.output);
            rdfExporter.export(graph, this.outputFormat);

            return AbstractCommand.ERROR_EXIT_CODE_SUCCESS;
        } catch (CoreseCommandException e) {
            this.spec.commandLine().getErr().println("Error: " + e.getMessage());
            if (this.verbose && e.getCause() != null) {
                e.printStackTrace(this.spec.commandLine().getErr());
            }
            return AbstractCommand.ERROR_EXIT_CODE_ERROR;
        }
    }
}
