package fr.inria.corese.command.utils.exporter.sparql;

import fr.inria.corese.command.utils.coresecorowrapper.CoreseMappingType;
import fr.inria.corese.command.utils.coresecorowrapper.CoreseRdfGraph;
import fr.inria.corese.command.utils.exporter.AbstractExporter;
import fr.inria.corese.core.kgram.core.Mappings;

import picocli.CommandLine.Model.CommandSpec;

import java.nio.file.Path;

/** Utility class to export SPARQL query results and SPARQL graphs results. */
public class SparqlResultExporter extends AbstractExporter {

    // Default output
    private static final EnumResultFormat DEFAULT_GRAPH_OUTPUT = EnumResultFormat.TURTLE;
    private static final EnumResultFormat DEFAULT_MAPPING_OUTPUT = EnumResultFormat.MARKDOWN;

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
     * @param map SPARQL query result to export.
     */
    public void export(Mappings map, CoreseRdfGraph graph, EnumResultFormat format) {
        CoreseMappingType mappingType = new CoreseMappingType(map);

        EnumResultFormat outputFormat = determineOutputFormat(format, mappingType);
        validateFormatCompatibility(outputFormat, mappingType);
        prepareOutputPath(outputFormat);
        performExport(map, graph, outputFormat, mappingType.isUpdate());
    }

    // ===== Private methods ===== //

    /**
     * Determine the output format based on the query type.
     *
     * @param format The requested format (may be null).
     * @param mappingType The type of SPARQL query result.
     * @return The determined output format.
     */
    private EnumResultFormat determineOutputFormat(
            EnumResultFormat format, CoreseMappingType mappingType) {
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
    private void validateFormatCompatibility(
            EnumResultFormat format, CoreseMappingType mappingType) {
        boolean isGraphQuery = mappingType.isUpdate() || mappingType.isConstruct();
        boolean isMappingQuery =
                mappingType.isSelect() || mappingType.isAsk() || mappingType.isDescribe();

        if (isGraphQuery && format.isMappingFormat()) {
            throw new IllegalArgumentException(
                    String.format(
                            "Error: %s is not a valid output format for insert, delete, describe or"
                                + " construct requests. Use one of the following RDF formats: %s",
                            format, EnumResultFormat.getRdfFormats()));
        }

        if (isMappingQuery && format.isRdfGraphFormat()) {
            throw new IllegalArgumentException(
                    String.format(
                            "Error: %s is not a valid output format for select or ask requests. Use"
                                    + " one of the following mapping formats: %s",
                            format, EnumResultFormat.getMappingFormats()));
        }
    }

    /**
     * Prepare the output file path based on the format.
     *
     * @param format The output format.
     */
    private void prepareOutputPath(EnumResultFormat format) {
        if (!this.outputIsDefined) {
            this.output = Path.of(DEFAULT_OUTPUT + format.getExtention());
        } else if (this.needToAppendExtension) {
            this.output = Path.of(this.output + format.getExtention());
        }
    }

    /**
     * Perform the actual export operation.
     *
     * @param map The SPARQL query result mappings.
     * @param graph The SPARQL graph.
     * @param format The output format.
     * @param isUpdate Whether this is an update query.
     */
    private void performExport(
            Mappings map, CoreseRdfGraph graph, EnumResultFormat format, boolean isUpdate) {
        if (isUpdate) {
            exportGraph(graph, format);
        } else {
            exportMappings(map, format);
        }
    }

    /**
     * Export a graph to file or stdout.
     *
     * @param graph The graph to export.
     * @param format The output format.
     */
    private void exportGraph(CoreseRdfGraph graph, EnumResultFormat format) {
        if (this.outputIsDefined) {
            exportToFile(this.output, format.getCoreseFormat(), format.toString(), graph);
        } else {
            exportToStdout(format.getCoreseFormat(), format.toString(), graph);
        }
    }

    /**
     * Export mappings to file or stdout.
     *
     * @param map The mappings to export.
     * @param format The output format.
     */
    private void exportMappings(Mappings map, EnumResultFormat format) {
        if (this.outputIsDefined) {
            exportToFile(this.output, format.getCoreseFormat(), format.toString(), map);
        } else {
            exportToStdout(format.getCoreseFormat(), format.toString(), map);
        }
    }
}
