# openflexo-core

The model-federation infrastructure of [Openflexo](https://www.openflexo.org).

`openflexo-core` hosts **FML** (Flexo Modeling Language) — the language that connects
heterogeneous models through VirtualModels and FlexoConcepts — and the runtime object model
everything else in Openflexo builds on: resources and resource centers, VirtualModel instances,
the editing context, services, actions and validation.

It sits in the middle of the Openflexo stack:

```
connie → pamela → gina / diana → openflexo-core → openflexo-ui → technology adapters → applications
```

A lower layer never depends on a higher one: `openflexo-core` uses Connie (expression language),
PAMELA (modeling framework) and the GINA API, and is used by the UI, the technology adapters and
the applications.

## Modules

| Module | Content |
|---|---|
| `flexo-foundation` | Runtime core: services, resources and resource centers, the FML metamodel and its runtime (FML-RT), actions, validation, localization, technology-adapter contracts. Model objects are PAMELA annotated interfaces. |
| `fml-parser` | The FML grammar (`src/main/sablecc/fml.sablecc`), the generated SableCC parser, and the hand-written factories and semantic analysis that build FML models from text, plus the pretty-printer. |
| `flexo-foundation-rm` | Resource management on top of the foundation and the parser (FML resources read from their textual serialization). |
| `fml-cli` | FML command-line interpreter and `.fmlscript` runner (headless entry point: `org.openflexo.foundation.fml.cli.LaunchHeadlessFMLCLI`). Commands and scripts are parsed by the FML grammar through its `command` and `script` entry points. |
| `fml-lsp` | Language Server Protocol support for FML (lsp4j). |
| `flexo-ontology` | Generic ontology API (`IFlexoOntology*`) implemented by ontology-based technology adapters. |
| `flexo-documentation` | Generic document API (`FlexoDoc*`: documents, paragraphs, runs, styles) implemented by document-based technology adapters. |
| `bug-reporting` | Bug-report submission service (GitHub issues). |
| `flexo-test-resources` | Test resource centers and FML fixtures (`TestResourceCenter`, FML parsing and pretty-print examples). |
| `flexo-foundation-test` | Test infrastructure and foundation tests. |
| `fml-cli-test` | Tests of the FML command-line interpreter and of FML scripts. |

## Building

The build uses **Gradle** through the Openflexo build plugin (`openflexo-buildplugin`), which
fixes versions and repositories and adds global tasks. Sources are compiled and tested with a
**Java 17** toolchain, whatever JDK launches Gradle. The `pom.xml` files are not used.

The FML parser is generated from its SableCC grammar. Generation is **not** triggered by
`compile`: run it once before the first build (or before importing the project in an IDE), and
again after any change to `fml.sablecc`:

```bash
./gradlew sableccParser
```

Then:

```bash
./gradlew compile     # compile all modules
./gradlew test        # non-UI tests (headless)
./gradlew testAll     # all tests, including those needing a display
./gradlew clean
./gradlew dep         # dependency report across modules
```

Run a single test class:

```bash
./gradlew :fml-parser:test --tests '*TestAssociativity'
```

> **Note** — tests run with `ignoreFailures = true`: the build reports `BUILD SUCCESSFUL` even
> when tests fail. Read the `Results: … (N tests, … failures)` summary printed at the end.

Generated parser sources land in `build/generated-sources/`; they are never committed and must
not be edited by hand.

### Developing across several Openflexo repositories

A standalone build of `openflexo-core` resolves its dependencies (Connie, PAMELA, GINA…) from
published artifacts on [maven.openflexo.org](https://maven.openflexo.org). To work on several
repositories at once against local sources, use the
[`openflexo-dev`](https://github.com/openflexo/openflexo-dev) composite build, which includes
each repository with Gradle `includeBuild`.

## Writing FML

- The grammar in `fml-parser/src/main/sablecc/fml.sablecc` is the reference for the syntax of
  FML, of FML commands and of `.fmlscript` files.
- A VirtualModel `Foo` is stored as a directory `Foo.fml/` containing the textual file
  `Foo.fml`, possibly with its `.fib`, `.inspector` and a `Localized/` dictionary.
- Validated examples live in `flexo-test-resources/src/main/resources/` (`FMLExamples`,
  `FMLParsingExamples`, `TestResourceCenter`).

## Known defects and backlog

Work is tracked in this repository:

- [`KNOWN_DEFECTS.md`](KNOWN_DEFECTS.md) — known bugs not fixed yet (`CORE-D-<n>`)
- [`BACKLOG.md`](BACKLOG.md) — features and evolutions to develop (`CORE-F-<n>`)

## Contributing

See [`CONTRIBUTING.md`](CONTRIBUTING.md).

## License

Openflexo is dual-licensed under the
[European Union Public License](https://joinup.ec.europa.eu/software/page/eupl/licence-eupl)
(EUPL 1.1 or later) and the [GNU General Public License](https://www.gnu.org/licenses/gpl.html)
(GPL 3 or later); see [`LICENSE`](LICENSE) and the header of each source file.
