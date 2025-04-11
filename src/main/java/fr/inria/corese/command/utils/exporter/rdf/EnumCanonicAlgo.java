package fr.inria.corese.command.utils.exporter.rdf;

import fr.inria.corese.core.sparql.api.ResultFormatDef;

/**
 * Enumeration of canonic algorithms.
 */
public enum EnumCanonicAlgo {

    // Rdfc-1.0-sha256
    RDFC_1_0("rdfc-1.0", ResultFormatDef.format.RDFC10_FORMAT, "nt"),
    RDFC_1_0_SHA256("rdfc-1.0-sha256", ResultFormatDef.format.RDFC10_FORMAT, "nt"),

    // Rdfc-1.0-sha384
    RDFC_1_0_SHA384("rdfc-1.0-sha384", ResultFormatDef.format.RDFC10_SHA384_FORMAT, "nt");

    private final String name;
    private final ResultFormatDef.format coreseFormat;
    private final String extention;

    /**
     * Constructor.
     * 
     * @param name         The name of the canonic algorithm.
     * @param coreseFormat The Corese format.
     * @param extention    The extension of the file format associated with the
     *                     canonic algorithm.
     */
    private EnumCanonicAlgo(String name, ResultFormatDef.format coreseFormat, String extention) {
        this.name = name;
        this.coreseFormat = coreseFormat;
        this.extention = extention;
    }

    /**
     * Get the Corese format.
     * 
     * @return The Corese format.
     */
    public ResultFormatDef.format getCoreseFormat() {
        return this.coreseFormat;
    }

    /**
     * Get the extension of the file format associated with the canonic algorithm.
     * 
     * @return The extension of the file format associated with the canonic
     *         algorithm.
     */
    public String getExtention() {
        return this.extention;
    }

    @Override
    public String toString() {
        return this.name;
    }

}
