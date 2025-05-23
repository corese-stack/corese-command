package fr.inria.corese.command.programs;

import fr.inria.corese.command.utils.exporter.sparql.EnumResultFormat;
import fr.inria.corese.command.utils.exporter.sparql.SparqlResultExporter;
import fr.inria.corese.command.utils.loader.rdf.EnumRdfInputFormat;
import fr.inria.corese.command.utils.loader.rdf.RdfDataLoader;
import fr.inria.corese.command.utils.loader.sparql.SparqlQueryLoader;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.kgram.core.Mappings;
import fr.inria.corese.core.query.QueryProcess;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "query", description = "Run a SPARQL query.", mixinStandardHelpOptions = true)
public class Query extends AbstractInputCommand {

    @Option(names = { "-f", "-if",
            "--input-format" }, description = "Specifies the RDF serialization format of the input file. Possible values are: :@|fg(magenta) ${COMPLETION-CANDIDATES}|@.")
    private EnumRdfInputFormat inputFormat = null;

    @Option(names = { "-r", "-of",
            "--result-format" }, description = "Specifies the format of the result file. Possible values are: :@|fg(magenta) ${COMPLETION-CANDIDATES}|@.")
    private EnumResultFormat resultFormat = null;

    @Option(names = { "-q",
            "--query" }, description = "Specifies the SPARQL query string or the path/URL to a .rq file containing the query.", required = true)
    private String queryUrlOrFile;

    public Query() {
    }

    @Override
    public Integer call() {

        super.call();

        try {

            // Load the input file(s)
            RdfDataLoader loader = new RdfDataLoader(this.spec, this.verbose);
            Graph graph = loader.load(this.inputsRdfData, this.inputFormat, this.recursive);

            // Load the query
            SparqlQueryLoader queryLoader = new SparqlQueryLoader(this.spec, this.verbose);
            String query = queryLoader.load(this.queryUrlOrFile);

            // Execute the query
            Mappings mappings = this.execute(graph, query);

            // Export the result
            SparqlResultExporter exporter = new SparqlResultExporter(this.spec, this.verbose, this.output);
            exporter.export(mappings, graph, this.resultFormat);

            return this.ERROR_EXIT_CODE_SUCCESS;
        } catch (Exception e) {
            this.spec.commandLine().getErr().println("Error: " + e.getMessage());
            return this.ERROR_EXIT_CODE_ERROR;
        }
    }

    private Mappings execute(Graph graph, String query) throws Exception {
        QueryProcess exec = QueryProcess.create(graph);

        // Execute query
        try {

            if (this.verbose) {
                this.spec.commandLine().getErr().println("Query: " + query);
                this.spec.commandLine().getErr().println("Executing query...");
            }

            return exec.query(query);
        } catch (Exception e) {
            throw new Exception("Error when executing SPARQL query : " + e.getMessage(), e);
        }
    }

}
