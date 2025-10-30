package fr.inria.corese.command.utils.loader.rdf;

import picocli.CommandLine.ITypeConverter;

public class RdfInputFormatConverter implements ITypeConverter<RdfInputFormat> {
    @Override
    public RdfInputFormat convert(String value) {
        return RdfInputFormat.from(value);
    }
}
