package fr.inria.corese.command.programs;

import fr.inria.corese.command.utils.coresecorowrapper.CoreseRdfGraph;
import fr.inria.corese.command.utils.coresecorowrapper.CoreseShacl;
import fr.inria.corese.command.utils.exporter.rdf.EnumRdfOutputFormat;
import fr.inria.corese.command.utils.exporter.rdf.RdfDataExporter;
import fr.inria.corese.command.utils.loader.rdf.EnumRdfInputFormat;
import fr.inria.corese.core.sparql.exceptions.EngineException;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
        name = "validate",
        description = "Run SHACL validation on a RDF dataset.",
        mixinStandardHelpOptions = true)
public class Validate extends AbstractInputCommand {

    @Option(
            names = {"-f", "-if", "--input-format"},
            description =
                    "Specifies the RDF serialization format of the input file. Possible values are:"
                            + " :@|fg(magenta) ${COMPLETION-CANDIDATES}|@.")
    private EnumRdfInputFormat inputFormat = null;

    @Option(
            names = {"-a", "-sf", "--shapes-format"},
            description =
                    "Specifies the serialization format of the SHACL shapes. Possible values are:"
                            + " :@|fg(magenta) ${COMPLETION-CANDIDATES}|@.)")
    private EnumRdfInputFormat reportFormat = null;

    @Option(
            names = {"-s", "--shapes"},
            description = "Specifies the path or URL of the file containing the SHACL shapes.",
            arity = "1...",
            required = true)
    private String[] shaclShapes;

    @Option(
            names = {"-r", "-of", "--output-format"},
            description =
                    "Specifies the serialization format of the validation report. Possible values"
                            + " are: :@|fg(magenta) ${COMPLETION-CANDIDATES}|@. Default value:"
                            + " ${DEFAULT-VALUE}.",
            defaultValue = "TURTLE")
    private EnumRdfOutputFormat outputFormat = null;

    @Override
    public Integer call() {

        super.call();

        try {
            // Load input file(s)
            CoreseRdfGraph dataGraph = new CoreseRdfGraph(this.spec, this.verbose);
            dataGraph.load(this.inputsRdfData, this.inputFormat, this.recursive);

            // Load shapes file(s)
            CoreseRdfGraph shapesGraph = new CoreseRdfGraph(this.spec, this.verbose);
            shapesGraph.load(this.shaclShapes, this.reportFormat, this.recursive);

            // Check if shapes graph contains SHACL shapes
            if (!shapesGraph.containsShaclShapes()) {
                throw new IllegalArgumentException("No SHACL shapes found in the input file(s).");
            }

            // Evaluation of SHACL shapes
            CoreseRdfGraph reportGraph = this.evaluateSHACLShapes(dataGraph, shapesGraph);

            // Export the report graph
            RdfDataExporter rdfExporter = new RdfDataExporter(this.spec, this.verbose, this.output);
            rdfExporter.export(reportGraph, this.outputFormat);

            return AbstractCommand.ERROR_EXIT_CODE_SUCCESS;
        } catch (Exception e) {
            this.spec.commandLine().getErr().println("Error: " + e.getMessage());
            return AbstractCommand.ERROR_EXIT_CODE_ERROR;
        }
    }

    /**
     * Evaluate SHACL shapes.
     *
     * @param dataGraph The data graph.
     * @param shapesGraph The shapes graph.
     * @return The report graph.
     * @throws Exception If an error occurs while evaluating SHACL shapes.
     */
    private CoreseRdfGraph evaluateSHACLShapes(CoreseRdfGraph dataGraph, CoreseRdfGraph shapesGraph)
            throws EngineException {

        if (this.verbose) {
            this.spec.commandLine().getErr().println("Evaluating SHACL shapes...");
        }

        CoreseShacl shacl = new CoreseShacl(dataGraph.getGraph(), shapesGraph.getGraph());
        try {
            return new CoreseRdfGraph(shacl.eval());
        } catch (EngineException e) {
            throw new EngineException("Error while evaluating SHACL shapes: " + e.getMessage(), e);
        }
    }
}
