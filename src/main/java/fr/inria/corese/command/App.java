package fr.inria.corese.command;

import fr.inria.corese.command.programs.Canonicalize;
import fr.inria.corese.command.programs.Convert;
import fr.inria.corese.command.programs.Query;
import fr.inria.corese.command.programs.QueryEndpoint;
import fr.inria.corese.command.programs.Validate;
import picocli.AutoComplete.GenerateCompletion;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Ansi.Style;
import picocli.CommandLine.Help.ColorScheme;

@Command(name = "Corese", versionProvider = VersionProvider.class, mixinStandardHelpOptions = true, subcommands = {
        Convert.class, Query.class, QueryEndpoint.class, Validate.class, Canonicalize.class, GenerateCompletion.class })
public final class App implements Runnable {

    public static void main(String[] args) {
        // Define the color scheme
        ColorScheme colorScheme = new ColorScheme.Builder()
                .commands(Style.bold) // Commands in blue
                .options(Style.fg_yellow) // Options in yellow
                .parameters(Style.fg_cyan, Style.bold) // Parameters in cyan and bold
                .optionParams(Style.italic, Style.fg_cyan) // Option parameters in italic
                .errors(Style.fg_red, Style.bold) // Errors in red and bold
                .stackTraces(Style.italic) // Stack traces in italic
                .applySystemProperties() // Apply system properties for colors
                .build();

        CommandLine commandLine = new CommandLine(new App()).setColorScheme(colorScheme);

        // Hide the generate-completion command
        CommandLine gen = commandLine.getSubcommands().get("generate-completion");
        gen.getCommandSpec().usageMessage().hidden(true);

        // Execute the command
        int exitCode = commandLine.execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        // Print usage
        CommandLine.usage(new App(), System.out);
    }
}