package fr.inria.corese.command.programs;

import fr.inria.corese.command.exceptions.CoreseCommandException;
import fr.inria.corese.command.exceptions.ValidationException;
import fr.inria.corese.command.utils.exporter.OutputFormat;
import fr.inria.corese.command.utils.exporter.rdf.RdfExporter;
import fr.inria.corese.command.utils.exporter.rdf.RdfOutputFormatCandidates;
import fr.inria.corese.command.utils.exporter.rdf.RdfOutputFormatConverter;
import fr.inria.corese.command.utils.loader.rdf.RdfLoader;
import fr.inria.corese.command.utils.loader.rdf.RdfInputFormat;
import fr.inria.corese.command.utils.loader.rdf.RdfInputFormatCandidates;
import fr.inria.corese.command.utils.loader.rdf.RdfInputFormatConverter;
import fr.inria.corese.command.utils.wrapper.CoreseRdfGraph;
import fr.inria.corese.command.utils.wrapper.CoreseShaclValidator;

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
                            + " :@|fg(magenta) ${COMPLETION-CANDIDATES}|@.",
            converter = RdfInputFormatConverter.class,
            completionCandidates = RdfInputFormatCandidates.class)
    private RdfInputFormat inputFormat = null;

    @Option(
            names = {"-a", "-sf", "--shapes-format"},
            description =
                    "Specifies the serialization format of the SHACL shapes. Possible values are:"
                            + " :@|fg(magenta) ${COMPLETION-CANDIDATES}|@.",
            converter = RdfInputFormatConverter.class,
            completionCandidates = RdfInputFormatCandidates.class)
    private RdfInputFormat reportFormat = null;

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
            defaultValue = "TURTLE",
            converter = RdfOutputFormatConverter.class,
            completionCandidates = RdfOutputFormatCandidates.class)
    private OutputFormat outputFormat = null;

    @Override
    public Integer call() {

        super.call();

        try {
            // Create data graph
            CoreseRdfGraph dataGraph = new CoreseRdfGraph();

            // Load data file(s)
            RdfLoader dataLoader = new RdfLoader(this.spec, this.verbose, dataGraph);
            dataLoader.load(this.inputsRdfData, this.inputFormat, this.recursive);

            // Load shapes file(s)
            CoreseRdfGraph shapesGraph = new CoreseRdfGraph();
            RdfLoader shapesLoader = new RdfLoader(this.spec, this.verbose, shapesGraph);
            shapesLoader.load(this.shaclShapes, this.reportFormat, false);

            // Check if shapes graph contains SHACL shapes
            if (!CoreseShaclValidator.containsShaclShapes(shapesGraph)) {
                throw new ValidationException("No SHACL shapes found in the input file(s).");
            }

            // Evaluation of SHACL shapes
            CoreseRdfGraph reportGraph = this.evaluateSHACLShapes(dataGraph, shapesGraph);

            // Export the report graph
            RdfExporter rdfExporter = new RdfExporter(this.spec, this.verbose, this.output);
            rdfExporter.export(reportGraph, this.outputFormat);

            return AbstractCommand.ERROR_EXIT_CODE_SUCCESS;
        } catch (CoreseCommandException e) {
            this.spec.commandLine().getErr().println("Error: " + e.getMessage());
            if (this.verbose && e.getCause() != null) {
                e.printStackTrace(this.spec.commandLine().getErr());
            }
            return AbstractCommand.ERROR_EXIT_CODE_ERROR;
        }
    }

    /**
     * Evaluate SHACL shapes.
     *
     * @param dataGraph The data graph.
     * @param shapesGraph The shapes graph.
     * @return The report graph.
     * @throws ValidationException If an error occurs while evaluating SHACL shapes.
     */
    private CoreseRdfGraph evaluateSHACLShapes(CoreseRdfGraph dataGraph, CoreseRdfGraph shapesGraph) {

        if (this.verbose) {
            this.spec.commandLine().getErr().println("Evaluating SHACL shapes...");
        }

        CoreseShaclValidator shacl = new CoreseShaclValidator(dataGraph);
        try {
            return shacl.eval(shapesGraph);
        } catch (Exception e) {
            throw new ValidationException(
                    "Error while evaluating SHACL shapes: " + e.getMessage(), e);
        }
    }
}
