package fr.inria.corese.command.utils.loader.rdf;

import fr.inria.corese.command.exceptions.InputFormatException;
import fr.inria.corese.command.exceptions.RdfLoadException;
import fr.inria.corese.command.utils.ConvertString;
import fr.inria.corese.command.utils.InputTypeDetector;
import fr.inria.corese.command.utils.InputTypeDetector.InputType;
import fr.inria.corese.command.utils.wrapper.CoreseRdfLoader;
import fr.inria.corese.command.utils.wrapper.CoreseRdfGraph;

import picocli.CommandLine.Model.CommandSpec;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;

/** Utility class to load RDF data into a Corese Graph. */
public class RdfLoader {

    // ===== Fields =====

    private CoreseRdfGraph graph;

    private CommandSpec spec;
    private boolean verbose;

    // ===== Constructor =====

    /**
     * Constructor.
     *
     * @param spec Command specification.
     * @param verbose If true, print information about the loaded files.
     */
    public RdfLoader(CommandSpec spec, boolean verbose, CoreseRdfGraph graph) {
        this.graph = graph;
        this.spec = spec;
        this.verbose = verbose;
    }

    // ===== Public methods =====

    /**
     * Load RDF data into a Corese Graph.
     *
     * <p>Load from standard input if no input is provided. Load from URL or file if input is a
     * valid URL or file path.
     *
     * @param inputs Paths or URLs of the files to load.
     * @param inputFormat Input file serialization format.
     * @param recursive If true, load RDF data from subdirectories.
     * @throws RdfLoadException if an error occurs while loading RDF data
     */
    public void load(String[] inputs, RdfInputFormat inputFormat, boolean recursive) {

        // If no input is provided, load from standard input
        if (inputs == null || inputs.length == 0) {
            this.loadFromStdin(inputFormat);
        }

        for (String input : inputs) {
            InputType type = InputTypeDetector.detect(input);

            switch (type) {
                case URL:
                    this.loadFromURL(ConvertString.toUrlOrThrow(input), inputFormat);
                    break;

                case FILE_PATH:
                    Path path = ConvertString.toPathOrThrow(input);
                    File file = path.toFile();

                    if (file.isDirectory()) {
                        this.loadFromDirectory(path, inputFormat, recursive);
                    } else {
                        this.loadFromFile(path, inputFormat);
                    }
                    break;

                default:
                    throw new RdfLoadException("Invalid input: " + input);
            }
        }
    }

    // ==== Private methods =====

    /**
     * Load RDF data from standard input into a Corese Graph.
     *
     * @param inputFormat Input file serialization format.
     * @return The Corese Graph containing the RDF data.
     */
    private void loadFromStdin(RdfInputFormat inputFormat) {

        this.loadFromInputStream(System.in, inputFormat);

        if (this.verbose) {
            this.spec.commandLine().getErr().println("Loaded file: standard input");
        }
    }

    /**
     * Load RDF data from a path or URL into a Corese Graph.
     *
     * @param url URL of the file to load.
     * @param inputFormat Input file serialization format.
     * @throws RdfLoadException if an error occurs while loading from URL
     */
    private void loadFromURL(URL url, RdfInputFormat inputFormat) {

        // If the input format is not provided, try to determine it from the file
        if (inputFormat == null) {
            inputFormat = this.guessInputFormat(url.toString());
        }

        // Load RDF data from URL
        InputStream inputStream;
        try {
            inputStream = url.openStream();
        } catch (IOException e) {
            throw new RdfLoadException("Failed to open URL: " + url.toString(), e);
        }
        this.loadFromInputStream(inputStream, inputFormat);

        if (this.verbose) {
            this.spec.commandLine().getErr().println("Loaded file: " + url.toString());
        }
    }

    /**
     * Load RDF data from a path to a file into a Corese Graph.
     *
     * @param path Path of the file to load.
     * @param inputFormat Input file serialization format.
     * @throws RdfLoadException if an error occurs while loading from file
     */
    private void loadFromFile(Path path, RdfInputFormat inputFormat) {

        // If the input format is not provided, try to determine it from the file
        if (inputFormat == null) {
            inputFormat = this.guessInputFormat(path.toString());
        }

        // Load RDF data from file
        InputStream inputStream;
        try {
            inputStream = new FileInputStream(path.toFile());
        } catch (FileNotFoundException e) {
            throw new RdfLoadException(
                    "Failed to open RDF data file: " + path.toString(), e);
        }
        this.loadFromInputStream(inputStream, inputFormat);

        if (this.verbose) {
            this.spec.commandLine().getErr().println("Loaded file: " + path);
        }
    }

    /**
     * Load RDF data from a directory into a Corese Graph.
     *
     * @param path Path of the directory to load.
     * @param inputFormat Input file serialization format.
     * @param recursive If true, load RDF data from subdirectories.
     * @return The Corese Graph containing the RDF data.
     */
    private void loadFromDirectoryRecursive(
            Path path, RdfInputFormat inputFormat, boolean recursive, CoreseRdfGraph graph) {

        File[] files = path.toFile().listFiles();

        if (files != null) {
            for (File childFile : files) {

                if (childFile.isDirectory() && recursive) {
                    this.loadFromDirectoryRecursive(
                            childFile.toPath(), inputFormat, recursive, graph);
                } else if (childFile.isFile()) {
                    this.loadFromFile(childFile.toPath(), inputFormat);
                }
            }
        }
    }

    /**
     * Load RDF data from a directory into a Corese Graph.
     *
     * @param path Path of the directory to load.
     * @param inputFormat Input file serialization format.
     * @param recursive If true, load RDF data from subdirectories.
     * @return The Corese Graph containing the RDF data.
     */
    private CoreseRdfGraph loadFromDirectory(Path path, RdfInputFormat inputFormat, boolean recursive) {
        this.loadFromDirectoryRecursive(path, inputFormat, recursive, graph);

        if (this.verbose) {
            this.spec.commandLine().getErr().println("Loaded directory: " + path);
        }
        return graph;
    }

    /**
     * Load RDF data from an input stream into a Corese Graph.
     *
     * @param inputStream Input stream of the file to load.
     * @param inputFormat Input file serialization format.
     * @throws InputFormatException if the input format is not specified or cannot be determined
     * @throws RdfLoadException if an error occurs while parsing the RDF data
     */
    private void loadFromInputStream(InputStream inputStream, RdfInputFormat inputFormat) {

        CoreseRdfLoader loader = new CoreseRdfLoader(graph);

        if (inputFormat == null) {

            throw new InputFormatException(
                    "The input format cannot be automatically determined. Please specify the input"
                            + " format with the option -if.");
        } else {
            try {
                loader.parse(inputStream, inputFormat);
            } catch (Exception e) {
                throw new RdfLoadException(
                        "Failed to parse RDF file. Check if file is well-formed and that "
                                + "the input format is correct. "
                                + e.getMessage(),
                        e);
            }
        }
    }

    /**
     * Guess the input format from the file extension.
     *
     * @param input Input file path or URL.
     * @return The guessed input format, falling back to Turtle if the format cannot be determined.
     */
    private RdfInputFormat guessInputFormat(String input) {
        RdfInputFormat inputFormat;

        try {
            inputFormat = CoreseRdfLoader.detectFormat(input);

            if (this.verbose) {
                this.spec
                        .commandLine()
                        .getErr()
                        .println("Format not specified, detected input format: " + inputFormat);
            }
        } catch (IllegalArgumentException e) {
            inputFormat = RdfInputFormat.TURTLE;

            if (this.verbose) {
                this.spec
                        .commandLine()
                        .getErr()
                        .println("Failed to detect input format, defaulting to Turtle");
            }
        }

        return inputFormat;
    }
}
