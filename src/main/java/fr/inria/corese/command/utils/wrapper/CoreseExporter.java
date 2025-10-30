package fr.inria.corese.command.utils.wrapper;

import fr.inria.corese.command.exceptions.RdfExportException;
import fr.inria.corese.command.utils.exporter.OutputFormat;
import fr.inria.corese.core.print.ResultFormat;
import fr.inria.corese.core.sparql.api.ResultFormatDef;

import java.io.IOException;

/**
 * Exporter class for RDF graphs and SPARQL results.
 *
 * <p>This class provides methods to set the output format and write the data to a specified
 * location. It supports exporting both RDF graphs and SPARQL query results in various formats.
 */
public class CoreseExporter {

    // ===== Fields =====

    protected ResultFormat formater;

    // ===== Constructors =====

    /**
     * Constructor for exporting RDF graphs.
     *
     * @param graph The RDF graph to export.
     */
    public CoreseExporter(CoreseRdfGraph graph) {
        formater = ResultFormat.create(graph.getGraph());
    }

    /**
     * Constructor for exporting SPARQL results.
     *
     * @param sparqlResult The SPARQL result to export.
     */
    public CoreseExporter(CoreseSparqlResult sparqlResult) {
        formater = ResultFormat.create(sparqlResult.getMappings());
    }

    // ===== Public methods =====

    /**
     * Set the output format for SELECT queries.
     *
     * @param format The desired output format.
     */
    public void setSelectFormat(OutputFormat format) {
        formater.setSelectFormat(toCoreseInternalFormat(format));
    }

    /**
     * Set the output format for CONSTRUCT queries.
     *
     * @param format The desired output format.
     */
    public void setConstructFormat(OutputFormat format) {
        formater.setConstructFormat(toCoreseInternalFormat(format));
    }

    /**
     * Write the exported data to the specified path.
     *
     * @param path The file path to write the data to.
     * @throws RdfExportException if an error occurs while writing to the file
     */
    public void write(String path) {
        try {
            formater.write(path);
        } catch (IOException e) {
            throw new RdfExportException("Failed to open export file: " + path, e);
        }
    }

    @Override
    public String toString() {
        return formater.toString();
    }

    // ===== Private methods =====

    /**
     * Convert OutputFormat to Corese internal ResultFormatDef.format.
     *
     * @param format The OutputFormat to convert.
     * @return The corresponding ResultFormatDef.format.
     */
    private static ResultFormatDef.format toCoreseInternalFormat(OutputFormat format) {
        switch (format) {
            case RDFXML:
                return ResultFormatDef.format.RDF_XML_FORMAT;
            case TURTLE:
                return ResultFormatDef.format.TURTLE_FORMAT;
            case TRIG:
                return ResultFormatDef.format.TRIG_FORMAT;
            case JSONLD:
                return ResultFormatDef.format.JSONLD_FORMAT;
            case NTRIPLES:
                return ResultFormatDef.format.NTRIPLES_FORMAT;
            case NQUADS:
                return ResultFormatDef.format.NQUADS_FORMAT;
            case RDFC10:
                return ResultFormatDef.format.RDFC10_FORMAT;
            case RDFC10SHA384:
                return ResultFormatDef.format.RDFC10_SHA384_FORMAT;
            case XML:
                return ResultFormatDef.format.XML_FORMAT;
            case JSON:
                return ResultFormatDef.format.JSON_FORMAT;
            case CSV:
                return ResultFormatDef.format.CSV_FORMAT;
            case TSV:
                return ResultFormatDef.format.TSV_FORMAT;
            case MARKDOWN:
                return ResultFormatDef.format.MARKDOWN_FORMAT;
            default:
                throw new RdfExportException("Unsupported export format: " + format);
        }
    }
}
