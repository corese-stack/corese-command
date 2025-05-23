package fr.inria.corese.command.programs;

import fr.inria.corese.command.utils.exporter.rdf.EnumRdfOutputFormat;
import fr.inria.corese.command.utils.exporter.rdf.RdfDataExporter;
import fr.inria.corese.command.utils.loader.rdf.EnumRdfInputFormat;
import fr.inria.corese.command.utils.loader.rdf.RdfDataLoader;
import fr.inria.corese.core.Graph;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "convert", description = "Convert an RDF file from one serialization format to another.", mixinStandardHelpOptions = true)
public class Convert extends AbstractInputCommand {

    @Option(names = { "-f", "-if",
            "--input-format" }, description = "Specifies the RDF serialization format of the input file. Possible values:@|fg(magenta) ${COMPLETION-CANDIDATES}|@.")
    private EnumRdfInputFormat inputFormat = null;

    @Option(names = { "-r", "-of",
            "--output-format" }, required = true, description = "Specifies the RDF serialization format of the output file. Possible values::@|fg(magenta)${COMPLETION-CANDIDATES}|@.")
    private EnumRdfOutputFormat outputFormat;

    public Convert() {
    }

    @Override
    public Integer call() {

        super.call();

        try {
            // Load the input file(s)
            RdfDataLoader loader = new RdfDataLoader(this.spec, this.verbose);
            Graph graph = loader.load(this.inputsRdfData, this.inputFormat, this.recursive);

            // Export the graph
            RdfDataExporter rdfExporter = new RdfDataExporter(this.spec, this.verbose, this.output);
            rdfExporter.export(graph, this.outputFormat);

            return this.ERROR_EXIT_CODE_SUCCESS;
        } catch (IllegalArgumentException e) {
            this.spec.commandLine().getErr().println("Error: " + e.getMessage());
            return this.ERROR_EXIT_CODE_ERROR;
        }
    }

}
