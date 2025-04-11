<!-- markdownlint-disable MD033 -->
<!-- markdownlint-disable MD041 -->

<p align="center">
    <a href="https://project.inria.fr/corese/">
        <img src="docs/source/_static/logo/corese-command-logo.svg" width="200" alt="Corese-Command-logo">
    </a>
    <br>
    <strong>Command-line tool for the Semantic Web of Linked Data</strong>
</p>

[![License: CECILL-C](https://img.shields.io/badge/License-CECILL--C-blue.svg)](https://cecill.info/licences/Licence_CeCILL-C_V1-en.html) [![Discussions](https://img.shields.io/badge/Discussions-GitHub-blue)](https://github.com/orgs/corese-stack/discussions)

## ✨ Features

- Convert RDF data formats (Turtle, RDF/XML, N-Triples, etc.)
- Execute SPARQL queries on local files or remote endpoints
- Validate RDF graphs using SHACL
- Canonicalize RDF data

## 🚀 Getting Started

Install Corese-Command using your preferred platform:

### Linux

<a href='https://flathub.org/apps/fr.inria.corese.CoreseCommand'>
    <img width='140' alt='Get it on Flathub' src='https://flathub.org/api/badge?locale=en'/>
</a>

```shell
curl -fsSL https://raw.githubusercontent.com/corese-stack/corese-command/main/packaging/scripts/install-linux.sh -o /tmp/corese.sh && bash /tmp/corese.sh
```

### macOS

```shell
curl -fsSL https://raw.githubusercontent.com/corese-stack/corese-command/main/packaging/scripts/install-macos.sh -o /tmp/corese.sh && bash /tmp/corese.sh
```

### Windows (PowerShell)

```powershell
iwr -useb https://raw.githubusercontent.com/corese-stack/corese-command/main/packaging/scripts/install-windows.ps1 | iex
```

You’ll then be able to use Corese-Command via the corese command in your terminal.

### Manual Installation (cross-platform)

You can also use Corese-Command as a standalone `.jar` file or add it to a Java project via Maven.

> Requires Java 11 or higher.

- [🔗 GitHub Releases](https://github.com/corese-stack/corese-command/releases)
- [📦 Maven Central](https://central.sonatype.com/artifact/fr.inria.corese/corese-command)

Run manually with:

```shell
java -jar corese-command-standalone.jar
```

## ✅ Example Usage

```shell
# Run a simple SPARQL query on an RDF file
corese query -q 'SELECT * WHERE {?s ?p ?o}' -i data.ttl
```

```shell
# Convert RDF from Turtle to RDF/XML
corese convert -i data.ttl -of rdfxml
```

```shell
# Validate RDF data with SHACL shapes
corese validate -i data.ttl -s shapes.ttl
```

```shell
# Canonicalize RDF using RDFC 1.0 with SHA-256
corese canonicalize -i data.ttl -of rdfc-1.0-sha256
```

```shell
# Query a remote SPARQL endpoint
corese query-remote -q 'SELECT * WHERE {?s ?p ?o}' -e "https://dbpedia.org/sparql"
```

## 📖 Documentation

- [Getting Started Guide](https://corese-stack.github.io/corese-command/v4.6.0/user_guide.html)

## 🤝 Contributing

We welcome contributions! Here’s how to get involved:

- [GitHub Discussions](https://github.com/orgs/corese-stack/discussions)
- [Issue Tracker](https://github.com/corese-stack/corese-command/issues)
- [Pull Requests](https://github.com/corese-stack/corese-command/pulls)

## 🔗 Useful Links

- [Corese Website](https://corese-stack.github.io/corese-command/)
- Mailing List: <corese-users@inria.fr>
- Subscribe: Send an email to <corese-users-request@inria.fr> with the subject: `subscribe`
