# Corese-Command

[![License: CECILL-C](https://img.shields.io/badge/License-CECILL--C-blue.svg)](https://cecill.info/licences/Licence_CeCILL-C_V1-en.html) [![Discussions](https://img.shields.io/badge/Discussions-GitHub-blue)](https://github.com/orgs/corese-stack/discussions)

Corese-Command is the command-line interface (CLI) tool for the Corese platform. It allows users to interact with Corese's RDF processing, SPARQL querying, and reasoning capabilities via the terminal.

## Features

- Convert RDF data formats (Turtle, RDF/XML, N-Triples, etc.).
- Execute SPARQL on a file.
- Validate RDF data with SHACL.
- Execute SPARQL on endpoints.
- Canonicalize RDF data.

## Getting Started

### Download and Install

You can run Corese-Command using Flathub or by downloading and executing the JAR file.

**Flathub:**

To install Corese-Command via Flathub, use the following command:

``` bash
flatpak install flathub fr.inria.corese.CoreseCommand
```

To run the application:

``` bash
flatpak run fr.inria.corese.CoreseCommand
```

For more information, visit the [Flathub page](https://flathub.org/apps/details/fr.inria.corese.CoreseCommand).

**JAR File:**

Download the latest version of the Corese-Command JAR from the [releases page](https://github.com/corese-stack/corese-command/releases) and run it with the following command:

``` bash
java -jar corese-command-4.5.0.jar
```

This will provide you with access to the Corese-Command interface.

## Usage

For a full list of available commands and options, use:

``` bash
java -jar corese-command-4.5.0.jar --help
```

> Create an alias for the JAR file to simplify the command `echo 'alias corese="java -jar path/to/corese-command-4.5.0.jar"' >> ~/.bashrc`
> After that, you can directly use the `corese` command to run Corese-Command.
> For example, `corese --help`.

## Documentation

For more information on using Corese-Command, please refer to the following resources:

- [Getting Started Guide](https://corese-stack.github.io/corese-command/v4.5.0/getting_started/getting_started_with_corese-command.html)
- [API Documentation](https://corese-stack.github.io/corese-command/v4.5.0/java_api/library_root.html)

## Contributions and Community

We welcome contributions to improve Corese-Command! Here’s how you can get involved:

- **Discussions:** If you have questions, ideas, or suggestions, please participate in our [discussion forum](https://github.com/orgs/corese-stack/discussions).
- **Issue Tracker:** Found a bug or want to request a new feature? Use our [issue tracker](https://github.com/corese-stack/corese-command/issues).
- **Pull Requests:** We accept pull requests. You can submit your changes [here](https://github.com/corese-stack/corese-command/pulls).

## Useful Links

- [Corese Official Website](https://corese-stack.github.io/corese-command/v4.5.0/index.html)
- **Mailing List:** <corese-users@inria.fr>
- **Join the Mailing List:** Send an email to <corese-users-request@inria.fr> with the subject: `subscribe`
