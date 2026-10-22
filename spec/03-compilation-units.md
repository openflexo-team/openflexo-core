# 3. Compilation units

A `.fml` file is a *compilation unit*. It declares exactly one top-level model, preceded by four optional
groups of declarations that set up the names the model can use.

## 3.1 Syntax

```ebnf
fml_compilation_unit = namespace_decl*
                       use_decl*
                       import_decl*
                       type_decl*
                       model_decl

namespace_decl = "namespace" lit_string "as" identifier ";"
use_decl       = "use" composite_tident "as" cidentifier ";"
import_decl    = "import" composite_tident ";"                              (* Java import *)
               | "import" composite_tident "as" identifier ";"              (* named Java import *)
               | "import" reference_by_uri ";"                              (* URI import *)
               | "import" type_decl_type identifier "from" reference_by_uri ";"   (* named URI import *)
type_decl      = "typedef" type_decl_type "as" uidentifier ";"
```

The four groups appear **in this order**, and each may be empty or repeated. A `use` after an `import`, or a
`namespace` after a `use`, does not parse. A file without a `model` declaration does not parse; neither
does a file with two.

An empty unit is not valid. The empty compilation unit that results from a parse error (§1.5) is a
fallback of the implementation, not a legal program; it validates without error and must not be mistaken for
a correct one.

## 3.2 Files, resources and addresses

An FML resource is stored as a **directory** named after the model, with the extension `.fml`, which holds a
file of the same name. The model `People` is stored in `People.fml/People.fml`. The directory is the *container*
of the unit: the localized dictionary (`Localized/`), the user-interface components (`.fib`) and the
inspectors (`.inspector`) that belong to the model's concepts live beside the source in the same directory.
A tool that looks for the sources of a model searches for the file, not the directory (a file-system query for
`*.fml` returns both).

A model has an **address** (a URI) by which other units refer to it. The address of a top-level model is given
by the annotation `@URI("…")` (§11.3), or, when absent, derived from the address of the resource center that
holds the file and the path of the file in it.

A model can contain other models (§5.1.4). A contained model is stored inside the directory of its container
(`Library.fml/Catalog.fml/Catalog.fml`) and needs no `@URI`: its address is computed from the container's
by appending the name of the contained unit.

> **Implementation note.** The address of a model must be unique in the set of resource centers loaded by
> the platform. A resource center that declares an address that is already registered is skipped without
> an error.

## 3.3 Namespace declarations

A namespace gives a short name to the beginning of an address.

```fml
namespace "http://openflexo.org/test/TestResourceCenter/" as TRC;

import [TRC+"People.fml"];

public model Reception {
}
```

The alias is an `identifier` of any of the three classes. It is usable in *URI expressions*: the expressions
written between the brackets of an `import` and of the other forms of `reference_by_uri` (§3.5.3). A URI
expression is a `+`-separated sequence of string literals and identifiers; an identifier that names a namespace
alias stands for the string it abbreviates.

> **Implementation note.** An alias is resolved in URI expressions only. In an annotation, such as
> `@URI(TRC+"x.fml")`, it is not resolved, and the model is silently registered under another address
> (`CORE-D-16`).

## 3.4 Use declarations

```ebnf
use_decl = "use" composite_tident "as" cidentifier ";"
```

A `use` declaration binds a **technology-adapter alias** to a model-slot class. The `composite_tident` is the
fully qualified Java name of the model-slot class that the technology adapter provides; the alias, a
`cidentifier` by convention capitalised, is the name by which the unit refers to what that model slot offers:

```fml
use org.openflexo.foundation.fml.rt.FMLRTModelSlot as FMLRT;
use org.openflexo.technologyadapter.diagram.TypedDiagramModelSlot as DIAGRAM;

model Mixed {
}
```

The alias prefixes the roles, the behaviours and the edition actions of the technology adapter:
`Shape shape with DIAGRAM::ShapeRole(…)` (§6.5), `DIAGRAM::AddShape(…)` (§10.10). It also brings the
adapter's types into the typing space of the unit (§4.8).

A unit that uses an adapter by alias only through edition actions must still declare it.

> **Implementation note.** If the named class cannot be found on the class path, the declaration is built
> without a model-slot class and the references that go through it are reported unresolved. The text is kept.

## 3.5 Import declarations

An `import` puts something within reach of the unit.

### 3.5.1 Java imports

```fml-header
import java.util.List;
import java.util.Map;
import java.lang.Double as JDouble;
```

`import a.b.C;` makes the Java class `a.b.C` known by its simple name `C` (§4.8). The class must be nameable by
a `composite_tident`: dotted lowercase package segments, then an uppercase-initial class name, optionally
followed by nested classes `$Inner`. The second form gives the imported class a local name, which can be of any
identifier class.

`java.lang.*` is not implicit for static calls: `Double.parseDouble(x)` needs `import java.lang.Double;` (or the
fully qualified `java.lang.Double.parseDouble(x)`).

There is no wildcard import.

### 3.5.2 Importing an FML model

```fml-header
import ["http://openflexo.org/test/TestResourceCenter/People.fml"];
```

`import [uri];` makes the concepts of the model at that address available to the unit, as types and as targets
of `new`: after the import of `People.fml`, `Person` is a type of the importing unit. This is required for
**every** reference to a concept of another model, including through the inheritance clause
(`model Child extends Parent`, §5.1.3).

> **Implementation note.** A missing import is not an error at load time. The concept is simply not in the
> typing space, a path that goes through it does not resolve, and in a behaviour the path evaluates to `null`
> (§13.6). Validation reports it.

### 3.5.3 URI expressions and references

```ebnf
reference_by_uri    = "[" uri_expression "]"
                    | "[" uri_expression ":" uri_expression "]"
uri_expression      = uri_primary { "+" uri_primary }
uri_primary         = lidentifier | uidentifier | cidentifier | lit_string
```

The first form designates a **resource** by its address, the second an **object inside a resource** by the
address of the resource and the identifier of the object in it. `+` concatenates. The same syntax is used, in
scripts, to designate resources (`load -r [uri]`, §12.3).

### 3.5.4 Named imports

```fml-header
import VirtualModel AnOtherModel from ["http://some_uri/AnOtherModel.fml"];
import SomeObject AN_OBJECT from [ns+"AThirdModel.fml":"SomeURI"];
```

A named URI import gives a local name to the element it designates, with a declared type. The name is any
`identifier`; the type is a reference type or a technology-specific type (§4.1).

## 3.6 Type definitions

```ebnf
type_decl = "typedef" type_decl_type "as" uidentifier ";"
```

`typedef T as Name;` introduces `Name` as an alias of the type `T` in the whole unit, nested concepts
included.

```fml
typedef String as Label;

model Typed {
	Label title;
	Label@(1.2) legacyTitle;
}
```

The alias is a `uidentifier`, with the restriction of §2.4. A `typedef` can alias a reference type or a
technology-specific type (`typedef XMLType(…) as Doc;`). An alias hides nothing: it participates in the name
resolution of §4.8 after the concepts of the unit and the model-slot types, and before plain Java types.

## 3.7 The model declaration

The last element of a unit is the declaration of its model; see §5.1. Its annotations (`@URI`, `@Version`,
`@Description`, …) are the only place where the metadata of the whole unit is written.

## 3.8 Loading

Loading a unit is a two-phase process on which a few rules depend.

1. The unit is **parsed**. Namespaces, `use` declarations, imports and type definitions are processed first,
   in order, and build the typing space of the unit. Then the model is built.
2. **Bindings are resolved** against the typing space. Every expression in the unit is analysed; a binding
   that does not resolve is kept (with its text) and reported by validation.

Units may import each other. The loader resolves such cycles: units that import each other are loaded
together, and bindings of a unit whose imports are not finished are analysed again once they are (`CORE-D-14`).
During that window, transient warnings of the form *Unresolved type* may be logged for types that end up resolved
(`CORE-D-34`).

A unit that fails to parse is registered as an empty unit and reported; the other units are not affected.
