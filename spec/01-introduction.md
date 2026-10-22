# 1. Introduction

## 1.1 What FML is

FML (*Flexo Modeling Language*) is the language in which an Openflexo **virtual model** is described:
the concepts it defines, what their instances know, what they can do, and which external resources
they federate. FML is object-oriented in shape: a *concept* plays the part of a class, its *properties*
are attributes, its *behaviours* are methods. It differs from a general-purpose object-oriented language
in two respects, which the rest of this specification keeps returning to.

1. **Its types reach outside the language.** Alongside the primitive types and the Java types, a unit
   may name types contributed by *technology adapters*: a spreadsheet cell, an XML element, a class of an
   EMF metamodel. A path expression goes through such an object exactly as it goes through an instance of
   a concept; each step is resolved against the type of the previous one (§4.8, §9.6).
2. **Its instructions act on external resources.** Technology adapters contribute *edition actions*,
   written `Adapter::Action(…)`, which are composed with ordinary control structures to read, create,
   update and delete data in the resources a model federates (§10.10).

FML is executed, not compiled to a stand-alone artefact. A `.fml` file is parsed into an object form (the
*FML meta-model*), validated, and interpreted by the Openflexo runtime, which creates *instances* of the
declared concepts and runs their behaviours (chapter 13). The object form can also be
written back as FML text (the *pretty-print*), which is how a model edited in a graphical editor is saved;
the text written back is equivalent to, but not always identical to, the text that was parsed (`CORE-D-33`).

## 1.2 Two textual surfaces, one grammar

Two kinds of text are written in FML.

* **FML proper** (`.fml` files) *defines* a model. A file is a compilation unit that declares exactly one
  top-level `model` (chapters 3 to 11).
* **FML-script** (`.fmlscript` files, and the interactive shell) *drives* the runtime: it loads
  resources, creates instances, calls behaviours and checks values (chapter 12).

Both are parsed by the same grammar, `fml.sablecc`. The grammar has four *entry points*, each selecting the
lexical state in which the input starts (§2.3):

| Entry point | Lexical state at start | Parses | Used for |
|---|---|---|---|
| `compilation_unit` | `normal` | `fml_compilation_unit` | a `.fml` file |
| `binding` | `binding` | one `expression` | a data binding embedded in a user interface, an inspector or a model |
| `command` | `command` | one `command` | a line typed in the interactive shell |
| `script` | `script` | a sequence of `command ;` | a `.fmlscript` file |

The two surfaces share expressions, types, `new … with (…)`, `assert` and path syntax. They differ in
the top level and in the set of reserved words (§2.5, §12.8).

The grammar file `fml-cli/src/main/sablecc/fml-cli.sablecc` is a leftover that is not used; the shell and
the scripts are parsed by `fml.sablecc`.

## 1.3 The platform around the language

A reader needs four words from the platform; they are defined where they first matter.

| Term | Meaning | Defined in |
|---|---|---|
| *virtual model* | the unit that groups concepts, declared by `model`; itself a concept | §5.1 |
| *concept* (*flexo concept*) | a declared type with properties and behaviours, declared by `concept` | §5.2 |
| *instance* | a run-time object of a concept (a *concept instance*) or of a virtual model (a *virtual model instance*) | §13.1 |
| *technology adapter* | a plug-in that makes a kind of resource (and its types, roles, actions) available to FML | §4.7, §6.5, §10.10 |

A virtual model *is* a concept: it can do everything a concept can, and is also the unit that holds other
concepts and that a program instantiates to start using them. Instantiating a virtual model gives a
*virtual model instance*, which is a concept instance too.

## 1.4 Source of truth and status

This is a specification of the language as it is implemented in release 3.0. Three sources are
authoritative, in this order:

1. the grammar `fml-parser/src/main/sablecc/fml.sablecc` for everything about syntax;
2. the semantic analyzers of `fml-parser` (`org.openflexo.foundation.fml.parser.fmlnodes`) and the FML
   meta-model of `flexo-foundation` (`org.openflexo.foundation.fml`) for the meaning of an accepted
   program;
3. the executable examples of the platform, whose assertions are checked by its automated tests.

Where the implementation does not behave as the language is meant to, the departure is recorded as a
*deviation* (Appendix B). A deviation is a defect of the implementation, not a feature of the language,
and a program should not depend on it.

An FML file may also exist in an older XML serialization (`.fml.xml`). That serialization is deprecated,
is not described here, and a directory that still holds one must not be used as a reference for the
syntax.

## 1.5 Notation

### Grammar notation

Syntax is given in an EBNF that follows the productions of `fml.sablecc`:

| Notation | Meaning |
|---|---|
| `name` | a nonterminal |
| `` `;` `` or `` `kw_model` `` | a terminal: a punctuation or operator token, or a keyword written in monospace |
| `a b` | sequence |
| `a \| b` | alternative |
| `a?` `a*` `a+` | optional, zero or more, one or more |
| `( … )` | grouping |
| `[x]:name` | in the source grammar, a named occurrence; not used here except where it clarifies |

Keywords are written as the text they match (`model`, `concept`), not as the token names of the lexer. The
alternative labels of the source grammar (`{java}`, `{fml}`, …) are quoted in prose when a rule needs to
refer to one. The complete grammar is in [Appendix A](appendix-a-grammar.md).

### Normative language

*Must*, *must not*, *may* and *need not* are normative. A construct that the grammar accepts and that this
text says "must not" be used is, unless stated otherwise, rejected or reported by validation, not by the
parser.

### Diagnostics

Three levels of problem are distinguished.

* A **parse error** is a violation of the grammar. The parser reports it and the unit is not built; the
  resulting compilation unit is empty.
* A **semantic issue** is detected while the parse tree is turned into the FML meta-model: a name that
  cannot be resolved, a container concept that is not found. It is reported with the position of the
  fragment concerned.
* A **validation problem** is reported by the validation of a built unit, for instance an invalid
  binding. Validation can be run at will and is the way to find the errors that execution hides (§13.8).

A **warning** is a semantic issue that does not prevent the unit from being built (for instance a
cardinality that is read as another one, §4.6).

## 1.6 Conformance

An implementation conforms to this specification if

* it accepts every text that `fml.sablecc` accepts at the four entry points, and no other;
* it builds from an accepted `compilation_unit` the meta-model described in chapters 3 to 11;
* it executes behaviours as chapter 13 describes, within the deviations listed in Appendix B.

A *program* conforms if every unit it contains is accepted by the grammar and validates without error.

## 1.7 Organisation

Chapters 2 to 4 give what is common to every construct: tokens, the layout of a file, types. Chapters 5 to
7 describe the declarations (models and concepts, properties, behaviours). Chapters 8 to 10 describe what
a behaviour body is made of: statements, expressions, edition actions. Chapter 11 covers annotations.
Chapter 12 describes the script surface. Chapter 13 gives the execution model, which the previous
chapters refer to for the run-time meaning of each construct.
