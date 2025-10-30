package fr.inria.corese.command.programs;

import fr.inria.corese.command.exceptions.CoreseCommandException;
import fr.inria.corese.command.exceptions.SparqlExecutionException;
import fr.inria.corese.command.utils.exporter.OutputFormat;
import fr.inria.corese.command.utils.exporter.sparql.SparqlOutputFormatCandidates;
import fr.inria.corese.command.utils.exporter.sparql.SparqlOutputFormatConverter;
import fr.inria.corese.command.utils.exporter.sparql.SparqlResultExporter;
import fr.inria.corese.command.utils.loader.rdf.RdfLoader;
import fr.inria.corese.command.utils.loader.rdf.RdfInputFormat;
import fr.inria.corese.command.utils.loader.rdf.RdfInputFormatCandidates;
import fr.inria.corese.command.utils.loader.rdf.RdfInputFormatConverter;
import fr.inria.corese.command.utils.loader.sparql.SparqlQueryLoader;
import fr.inria.corese.command.utils.wrapper.CoreseRdfGraph;
import fr.inria.corese.command.utils.wrapper.CoreseSparqlQuery;
import fr.inria.corese.command.utils.wrapper.CoreseSparqlResult;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "query", description = "Run a SPARQL query.", mixinStandardHelpOptions = true)
public class Query extends AbstractInputCommand {

    @Option(
            names = {"-f", "-if", "--input-format"},
            description =
                    "Specifies the RDF serialization format of the input file. Possible values are:"
                            + " :@|fg(magenta) ${COMPLETION-CANDIDATES}|@.",
            converter = RdfInputFormatConverter.class,
            completionCandidates = RdfInputFormatCandidates.class)
    private RdfInputFormat inputFormat = null;

    @Option(
            names = {"-r", "-of", "--result-format"},
            description =
                    "Specifies the format of the result file. Possible values are: :@|fg(magenta)"
                            + " ${COMPLETION-CANDIDATES}|@.",
            converter = SparqlOutputFormatConverter.class,
            completionCandidates = SparqlOutputFormatCandidates.class)
    private OutputFormat resultFormat = null;

    @Option(
            names = {"-q", "--query"},
            description =
                    "Specifies the SPARQL query string or the path/URL to a .rq file containing the"
                            + " query.",
            required = true)
    private String queryUrlOrFile;

    @Override
    public Integer call() {

        super.call();

        try {
            // Create data graph
            CoreseRdfGraph graph = new CoreseRdfGraph();

            // Load the data file(s)
            RdfLoader loader = new RdfLoader(this.spec, this.verbose, graph);
            loader.load(this.inputsRdfData, this.inputFormat, this.recursive);

            // Load the query
            SparqlQueryLoader queryLoader = new SparqlQueryLoader(this.spec, this.verbose);
            String query = queryLoader.load(this.queryUrlOrFile);

            // Execute the query
            CoreseSparqlResult mappings = this.execute(graph, query);

            // Export the result
            SparqlResultExporter exporter =
                    new SparqlResultExporter(this.spec, this.verbose, this.output);
            exporter.export(mappings, graph, this.resultFormat);

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
     * Execute SPARQL query on the given graph.
     * 
     * @param graph The RDF graph
     * @param query The SPARQL query string
     * @return The SPARQL result
     * @throws SparqlExecutionException if an error occurs during query execution
     */
    private CoreseSparqlResult execute(CoreseRdfGraph graph, String query) {

        if (this.verbose) {
            this.spec.commandLine().getErr().println("Query: " + query);
            this.spec.commandLine().getErr().println("Executing query...");
        }

        try {
            CoreseSparqlQuery sparqlQuery = new CoreseSparqlQuery(query);
            return sparqlQuery.execute(graph);
        } catch (Exception e) {
            throw new SparqlExecutionException(
                    "Error when executing SPARQL query : " + e.getMessage(), e);
        }
    }
}
