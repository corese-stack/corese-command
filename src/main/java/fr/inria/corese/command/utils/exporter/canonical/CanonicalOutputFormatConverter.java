package fr.inria.corese.command.utils.exporter.canonical;

import fr.inria.corese.command.utils.exporter.OutputFormat;

import picocli.CommandLine.ITypeConverter;

/** Converter for canonical RDF output formats. */
public class CanonicalOutputFormatConverter implements ITypeConverter<OutputFormat> {
    @Override
    public OutputFormat convert(String value) {
        return OutputFormat.fromCanonical(value);
    }
}
