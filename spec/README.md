# FML Language Specification

**Flexo Modeling Language — release 3.0**

This directory holds the specification of FML, the language in which an Openflexo *virtual model* is
written. It is organised as a book: read the chapters in order the first time, then use them as a
reference.

The specification is derived from, and subordinate to, the SableCC grammar
[`fml-parser/src/main/sablecc/fml.sablecc`](../fml-parser/src/main/sablecc/fml.sablecc). Where this text
and the grammar disagree about *what is syntactically accepted*, the grammar is right and this text has
a bug. Where this text describes *what an accepted program means*, the sources are the semantic analyzers
of `fml-parser`, the FML meta-model of `flexo-foundation`, and the executable examples of the platform;
the cases in which the implementation does not do what the language intends are recorded in
[Appendix B](appendix-b-known-deviations.md), not hidden.

## Contents

| # | Chapter | What it defines |
|---|---|---|
| 1 | [Introduction](01-introduction.md) | Scope, the two surfaces of the language, notation, conformance |
| 2 | [Lexical structure](02-lexical-structure.md) | Characters, comments, lexical states, tokens, keywords, literals |
| 3 | [Compilation units](03-compilation-units.md) | Layout of a `.fml` file; namespaces, `use`, `import`, `typedef`; resources and addresses |
| 4 | [Types](04-types.md) | Primitive, reference, generic and technology-specific types; versions; cardinalities; the typing space |
| 5 | [Models, concepts, events and enums](05-models-and-concepts.md) | The declarations that structure a program; containment; inheritance; invariants |
| 6 | [Properties](06-properties.md) | Stored, role, expression, abstract and get/set properties |
| 7 | [Behaviours](07-behaviours.md) | Methods, creation and deletion schemes, listeners, technology-specific behaviours |
| 8 | [Statements](08-statements.md) | Blocks, declarations, conditions, loops, returns |
| 9 | [Expressions](09-expressions.md) | Operators, precedence, paths, calls, `new`, name resolution |
| 10 | [Edition actions](10-edition-actions.md) | `select`, `match`, `delete`, `fire`, `notify`, `connect`, `log`, technology actions |
| 11 | [Annotations](11-annotations.md) | Syntax and the annotations the platform interprets |
| 12 | [FML-script](12-fml-script.md) | The command and script surface |
| 13 | [Execution model](13-execution-model.md) | Instances, containment, evaluation, events, matching sets, failure |
| A | [Grammar summary](appendix-a-grammar.md) | The complete syntax, in EBNF |
| B | [Known deviations](appendix-b-known-deviations.md) | Where the implementation departs from the language |
| C | [A complete example](appendix-c-example.md) | One model and one script, annotated |

## Reading conventions

* `monospace` is source text. *Italic* introduces a term at its definition.
* "Must", "must not", "may" and "need not" are normative and are written in lowercase.
* An **Implementation note** states what the 3.0 implementation does; it is not part of the language
  when it conflicts with the rule above it, and it then cites a defect identifier (`CORE-D-n`) recorded in
  [`KNOWN_DEFECTS.md`](../KNOWN_DEFECTS.md).
* Every example in this specification parses: each one was submitted to the parser of the platform (the entry point
  that fits it: compilation unit, script, expression) when the text was written, and the few that are shown as
  *rejected* were checked to be rejected. Those that are quoted from the platform's test resources
  (`flexo-test-resources`; [Appendix C](appendix-c-example.md) in full) are also *run* by its automated tests, which
  assert their values. A fragment written to illustrate a rule has been checked against the grammar only; whether it
  resolves depends on the types and adapters around it.
