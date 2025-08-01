<!-- markdownlint-disable MD024 -->
# Corese Changelog

## Version 4.6.2 - 2025-08-01

### Changed

- Remove `module-info.java` from the `corese-command` module to avoid issues with Java 21 and later versions. This file is not needed for command-line applications and its removal simplifies the build process.
- Update JDK version to 21 in GitHub workflows.
- Update Corese-Core dependency to version `4.6.4`.

### Fixed

- Fixed bugs in installation scripts for Linux, macOS, and Windows.

## Version 4.6.1 – 2025-06-17

### Added

- Improved Linux, MacOS and Windows installation scripts:
  - Pre-releases and drafts are now excluded from the available version list.
  - User input is now validated in interactive mode: if an invalid version number is entered, the prompt will repeat until a valid choice is made.
  - Updated java version to 21 in the installation scripts.

### Changed

- Updated to require Java 21.
- Renamed `query-remote` command to `query-endpoint` for clarity and consistency with the `query` command.

### Fixed

- Fixed bug where one-line inline SPARQL queries containing URIs (e.g. `PREFIX ex: <http://example.org/> SELECT * WHERE { ex:Alice a ex:Person }`) were incorrectly detected as file paths due to slashes or dots in the query string.

## Version 4.6.0 – 2025-04-11

### Added

- Added script installers for Linux, macOS, and Windows platforms.
- Added verbose query visualization in `query-remote` command.
- Added deployment of the documentation site at [corese-stack.github.io/corese-command](https://corese-stack.github.io/corese-command).
- Added new `canonicalize` command to canonicalize RDF files in formats like `rdfc-1.0`.

### Changed

- Renamed commands for consistency:
  - `sparql` → `query`
  - `shacl` → `validate`
  - `remote-sparql` → `query-remote`
- Updated completion candidate colors from pale pink to magenta for better visibility on white backgrounds.
- Updated Corese-Core dependency to version `4.6.3`.
- Changed `--no-owl-import` to `--owl-import` across all commands for clarity.
  - The option now explicitly enables `owl:imports` (default is `false`).

### Fixed

- Fixed Windows bug related to incorrect line endings.
- Fixed issue with missing `.` when automatically adding file extensions on export.
- Fixed JSON-LD serializer bug on Windows.

### Removed

- Removed redirection options from the `query-remote` command.

## Version 4.5.0 – 2023-12-14

### Added

- Added new sub-command `shacl` to validate RDF graphs against SHACL shapes.
- Added new sub-command `remote-sparql` to execute SPARQL queries on remote endpoints (see [issue #135](https://github.com/Wimmics/corese/issues/135)).
- Added verbose option.
- Added support for property files.
- Added `-no-owl-import` option (see [issue #134](https://github.com/Wimmics/corese/issues/134)).
- Added output formats `N-Triples` and `N-Quads` to the `convert` sub-command.

### Changed

- Moved hint messages to the standard error stream.
- Moved error messages to the standard error stream (see [issues #141](https://github.com/Wimmics/corese/issues/141) and [#142](https://github.com/Wimmics/corese/issues/142)).

### Fixed

- Fixed Trig serialization to escape special characters (see [issue #151](https://github.com/Wimmics/corese/issues/151)).
- Fixed federated queries with `PREFIX` statements failing under certain conditions (see [issue #140](https://github.com/Wimmics/corese/issues/140)).

## Version 4.4.1 – 2023-07-25

### Added

- Added URL support as an input file for `convert` and `sparql` sub-commands.
- Added standard input support as an input file for `sparql` and `convert` sub-commands.
- Added standard output support as an output file for `sparql` and `convert` sub-commands.
- Added multiple files support as input for the `sparql` sub-command.
- Added directory and recursive directory support as input for the `sparql` sub-command.
- Added support for all types of queries (SELECT, CONSTRUCT, ASK, DESCRIBE, INSERT, DELETE, INSERT WHERE, DELETE WHERE) in the `sparql` sub-command.
- Added user choice for result format in the `sparql` sub-command.
- Added Markdown output format for the `sparql` sub-command.
- Added MIME type support as a format name.
- Added configuration to disable `owl:imports` auto-import.

### Changed

- Refactored `convert` and `sparql` sub-commands.
- Renamed format names for more consistency.

### Removed

- Removed `owlProfile` and `ldscript` sub-commands (to be reintroduced in a future release after refactoring).

### Fixed

- Fixed warning: `sun.reflect.Reflection.getCallerClass is not supported. This will impact performance.`

### Security

- Updated `json` from `20180813` to `20230227` in `/sparql` (see [Pull Request #123](https://github.com/Wimmics/corese/pull/123)).
- Updated `json` from `20180813` to `20230227` in `/corese-test` (see [Pull Request #124](https://github.com/Wimmics/corese/pull/124)).
- Updated `guava` from `31.1-jre` to `32.0.0-jre` in `/corese-jena` (see [Pull Request #128](https://github.com/Wimmics/corese/pull/128)).

## Version 4.4.0 – 2023-03-30

### Added

- **Corese-Command**: Initial beta release of the Corese-Command line application.  
  > **Note**: The interface and commands are subject to change in future versions.

- **Options**:
  - `-h`, `--help`: Display help message and exit.
  - `-V`, `--version`: Print version information and exit.

- **Commands**:
  - `convert`: Allows conversion of RDF files between different serialization formats (e.g., Turtle, RDF/XML, JSON-LD).
  - `sparql`: Enables execution of SPARQL queries directly from the command line.
  - `owlProfile`: Checks RDF data against OWL profiles to ensure compatibility.
