package fr.inria.corese.command.programs;

import fr.inria.corese.command.utils.coreseCoreWrapper.CoreseGraph;
import fr.inria.corese.command.utils.exporter.rdf.EnumCanonicAlgo;
import fr.inria.corese.command.utils.exporter.rdf.RdfDataCanonicalizer;
import fr.inria.corese.command.utils.loader.rdf.EnumRdfInputFormat;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "canonicalize", description = "Canonicalize an RDF file to a specific format.", mixinStandardHelpOptions = true)
public class Canonicalize extends AbstractInputCommand {

    @Option(names = { "-f", "-if",
            "--input-format" }, description = "Specifies the RDF serialization format of the input file. Available options are: :@|fg(magenta) ${COMPLETION-CANDIDATES}|@.")
    private EnumRdfInputFormat inputFormat;

    @Option(names = { "-a", "-ca", "-r", "-of",
            "--canonical-algo" }, required = true, description = "Specifies the canonicalization algorithm to be applied to the input file. Available options are: :@|fg(magenta) ${COMPLETION-CANDIDATES}|@. The default algorithm is ${DEFAULT-VALUE}.", defaultValue = "rdfc-1.0")
    private EnumCanonicAlgo canonicalAlgo;

    public Canonicalize() {
    }

    @Override
    public Integer call() {

        super.call();

        try {
            // Load the input file(s)
            CoreseGraph graph = new CoreseGraph(this.spec, this.verbose);
            graph.load(this.inputsRdfData, this.inputFormat, this.recursive);

            // Canonicalize and export the graph
            RdfDataCanonicalizer rdfCanonicalizer = new RdfDataCanonicalizer(this.spec, this.verbose, this.output);
            rdfCanonicalizer.export(graph, this.canonicalAlgo);

            return this.ERROR_EXIT_CODE_SUCCESS;
        } catch (IllegalArgumentException e) {
            this.spec.commandLine().getErr().println("Error: " + e.getMessage());
            return this.ERROR_EXIT_CODE_ERROR;
        }
    }

}
