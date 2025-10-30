package fr.inria.corese.command.utils.exporter.sparql;

import fr.inria.corese.command.utils.exporter.AbstractExporter;
import fr.inria.corese.command.utils.exporter.OutputFormat;
import fr.inria.corese.command.utils.wrapper.CoreseRdfGraph;
import fr.inria.corese.command.utils.wrapper.CoreseSparqlResult;

import picocli.CommandLine.Model.CommandSpec;

import java.nio.file.Path;

/** Utility class to export SPARQL query results and SPARQL graphs results. */
public class SparqlResultExporter extends AbstractExporter {

    // Default output
    private static final OutputFormat DEFAULT_GRAPH_OUTPUT = OutputFormat.TURTLE;
    private static final OutputFormat DEFAULT_MAPPING_OUTPUT = OutputFormat.MARKDOWN;

    // ===== Constructor ===== //

    /**
     * Constructor.
     *
     * @param spec Command specification.
     * @param verbose If true, print information about the exported file.
     * @param output Output file path. If not provided, the result will be written to standard
     *     output.
     */
    public SparqlResultExporter(CommandSpec spec, boolean verbose, Path output) {
        super(spec, verbose, output);
    }

    // ===== Public methods ===== //

    /**
     * Export a SPARQL query result to a file or standard output.
     *
     * @param format Serialization format.
     * @param graph SPARQL graph to export.
     * @param sparqlResult SPARQL query result to export.
     */
    public void export(CoreseSparqlResult sparqlResult, CoreseRdfGraph graph, OutputFormat format) {

        OutputFormat outputFormat = determineOutputFormat(format, sparqlResult);
        validateFormatCompatibility(outputFormat, sparqlResult);
        prepareOutputPath(outputFormat);
        performExport(sparqlResult, graph, outputFormat, sparqlResult.isUpdate());
    }

    // ===== Private methods ===== //

    /**
     * Determine the output format based on the query type.
     *
     * @param format The requested format (may be null).
     * @param mappingType The type of SPARQL query result.
     * @return The determined output format.
     */
    private OutputFormat determineOutputFormat(OutputFormat format, CoreseSparqlResult mappingType) {
        if (format != null) {
            return format;
        }

        if (mappingType.isUpdate() || mappingType.isConstruct()) {
            return DEFAULT_GRAPH_OUTPUT;
        }
        return DEFAULT_MAPPING_OUTPUT;
    }

    /**
     * Validate that the output format is compatible with the query type.
     *
     * @param format The output format.
     * @param mappingType The type of SPARQL query result.
     * @throws IllegalArgumentException if the format is incompatible.
     */
    private void validateFormatCompatibility(OutputFormat format, CoreseSparqlResult mappingType) {
        boolean isGraphQuery = mappingType.isUpdate() || mappingType.isConstruct();
        boolean isMappingQuery =
                mappingType.isSelect() || mappingType.isAsk() || mappingType.isDescribe();

        if (isGraphQuery && format.isMappingFormat()) {
            throw new IllegalArgumentException(
                    String.format(
                            "Error: %s is not a valid output format for insert, delete, describe or"
                                + " construct requests. Use one of the following RDF formats: %s",
                            format, OutputFormat.rdfAliases()));
        }

        if (isMappingQuery && format.isRdfFormat()) {
            throw new IllegalArgumentException(
                    String.format(
                            "Error: %s is not a valid output format for select or ask requests. Use"
                                    + " one of the following mapping formats: %s",
                            format, OutputFormat.sparqlAliases()));
        }
    }

    /**
     * Prepare the output file path based on the format.
     *
     * @param format The output format.
     */
    private void prepareOutputPath(OutputFormat format) {
        if (!this.outputIsDefined) {
            this.output = Path.of(DEFAULT_OUTPUT + format.getExtension());
        } else if (this.needToAppendExtension) {
            this.output = Path.of(this.output + format.getExtension());
        }
    }

    /**
     * Perform the actual export operation.
     *
     * @param sparqlResult The SPARQL query result mappings.
     * @param graph The SPARQL graph.
     * @param format The output format.
     * @param isUpdate Whether this is an update query.
     */
    private void performExport(
            CoreseSparqlResult sparqlResult, CoreseRdfGraph graph, OutputFormat format, boolean isUpdate) {
        if (isUpdate) {
            exportGraph(graph, format);
        } else {
            exportMappings(sparqlResult, format);
        }
    }

    /**
     * Export a graph to file or stdout.
     *
     * @param graph The graph to export.
     * @param format The output format.
     */
    private void exportGraph(CoreseRdfGraph graph, OutputFormat format) {
        if (this.outputIsDefined) {
            exportToFile(this.output, format, graph);
        } else {
            exportToStdout(format, graph);
        }
    }

    /**
     * Export mappings to file or stdout.
     *
     * @param sparqlResult The mappings to export.
     * @param format The output format.
     */
    private void exportMappings(CoreseSparqlResult sparqlResult, OutputFormat format) {
        if (this.outputIsDefined) {
            exportToFile(this.output, format, sparqlResult);
        } else {
            exportToStdout(format, sparqlResult);
        }
    }
}
