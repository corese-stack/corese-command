package fr.inria.corese.command.utils.exporter.rdf;

import fr.inria.corese.command.utils.exporter.OutputFormat;

import picocli.CommandLine.ITypeConverter;

/** Converter for RDF output formats. */
public class RdfOutputFormatConverter implements ITypeConverter<OutputFormat> {
    @Override
    public OutputFormat convert(String value) {
        return OutputFormat.fromRdf(value);
    }
}
