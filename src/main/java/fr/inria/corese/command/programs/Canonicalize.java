package fr.inria.corese.command.programs;

import fr.inria.corese.command.exceptions.CoreseCommandException;
import fr.inria.corese.command.utils.exporter.OutputFormat;
import fr.inria.corese.command.utils.exporter.canonical.CanonicalOutputFormatCandidates;
import fr.inria.corese.command.utils.exporter.canonical.CanonicalOutputFormatConverter;
import fr.inria.corese.command.utils.exporter.canonical.CanonicalExporter;
import fr.inria.corese.command.utils.loader.rdf.RdfLoader;
import fr.inria.corese.command.utils.loader.rdf.RdfInputFormat;
import fr.inria.corese.command.utils.loader.rdf.RdfInputFormatCandidates;
import fr.inria.corese.command.utils.loader.rdf.RdfInputFormatConverter;
import fr.inria.corese.command.utils.wrapper.CoreseRdfGraph;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
        name = "canonicalize",
        description = "Canonicalize an RDF file to a specific format.",
        mixinStandardHelpOptions = true)
public class Canonicalize extends AbstractInputCommand {

    @Option(
            names = {"-f", "-if", "--input-format"},
            description =
                    "Specifies the RDF serialization format of the input file. Available options"
                            + " are: :@|fg(magenta) ${COMPLETION-CANDIDATES}|@.",
            converter = RdfInputFormatConverter.class,
            completionCandidates = RdfInputFormatCandidates.class)
    private RdfInputFormat inputFormat;

    @Option(
            names = {"-a", "-ca", "-r", "-of", "--canonical-algo"},
            required = true,
            description =
                    "Specifies the canonicalization algorithm to be applied to the input file."
                        + " Available options are: :@|fg(magenta) ${COMPLETION-CANDIDATES}|@. The"
                        + " default algorithm is ${DEFAULT-VALUE}.",
            defaultValue = "rdfc-1.0",
            converter = CanonicalOutputFormatConverter.class,
            completionCandidates = CanonicalOutputFormatCandidates.class)
    private OutputFormat canonicalAlgo;

    @Override
    public Integer call() {

        super.call();

        try {
            // Create data graph
            CoreseRdfGraph graph = new CoreseRdfGraph();

            // Load the data file(s)
            RdfLoader loader = new RdfLoader(this.spec, this.verbose, graph);
            loader.load(this.inputsRdfData, this.inputFormat, this.recursive);

            // Canonicalize and export the graph
            CanonicalExporter rdfCanonicalizer =
                    new CanonicalExporter(this.spec, this.verbose, this.output);
            rdfCanonicalizer.export(graph, this.canonicalAlgo);

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
