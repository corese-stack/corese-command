package fr.inria.corese.command.utils.coresecorowrapper;

import fr.inria.corese.core.kgram.core.Mappings;
import fr.inria.corese.core.print.ResultFormat;
import fr.inria.corese.core.sparql.api.ResultFormatDef;

import java.io.IOException;

public class CoreseResultFormat {

    protected ResultFormat format;

    public CoreseResultFormat(CoreseRdfGraph graph) {
        format = ResultFormat.create(graph.getGraph());
    }

    public CoreseResultFormat(Mappings mappings) {
        format = ResultFormat.create(mappings);
    }

    public void setSelectFormat(ResultFormatDef.format coreseFormat) {
        format.setSelectFormat(coreseFormat);
    }

    public void setConstructFormat(ResultFormatDef.format coreseFormat) {
        format.setConstructFormat(coreseFormat);
    }

    public void write(String path) {
        try {
            format.write(path);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to open export file: " + path, e);
        }
    }
}
