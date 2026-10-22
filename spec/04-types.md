# 4. Types

FML is statically typed: every property, parameter, local variable and behaviour has a type. Types are
named in declarations; an expression has a type that is computed from the types of its parts. This chapter
gives the syntax of types, the notation that qualifies them (version, cardinality), and the rules by which a
name in a type position is resolved.

## 4.1 Syntax

```ebnf
type              = primitive_type
                  | "void"
                  | reference_type at_version?
                  | technology_specific_type at_version?

primitive_type    = "int" | "short" | "long" | "float" | "double" | "char" | "byte" | "boolean"

reference_type    = composite_tident type_arguments?
composite_tident  = identifier_prefix* uidentifier ( "$" uidentifier )*
identifier_prefix = lidentifier "." | escaped_identifier "."

technology_specific_type = uidentifier fml_parameters

type_arguments    = "<" ">"
                  | "<" type_argument ( "," type_argument )* ">"
type_argument     = reference_type
                  | "?" ( ( "extends" | "super" ) reference_type )?

at_version        = "@" "(" version ")"
version           = lit_integer | lit_float | lit_float lit_float lidentifier?
```

`void` and the other keywords of the first group are the only types that are not `uidentifier`-based.

## 4.2 Primitive types

The eight primitive types are `boolean`, `char`, `byte`, `short`, `int`, `long`, `float` and `double`. They are
used for properties, parameters, locals and return types.

**A primitive type does not give a property a default value.** A property declared `int count;` holds `null`
in a new instance, not `0`; `boolean flag;` holds `null`, not `false`. A value must be given by an initial value
(§6.2) or by a creation scheme.

`void` is the type of a behaviour that returns nothing; it is also the meaning of an omitted return type
(§7.1).

## 4.3 Reference types

A reference type is a possibly qualified name that ends in a `uidentifier`:

```fml-member
List<Task> tasks;
java.util.Date created;
Person guest;
Library$Shelf shelf;
```

The name may denote

* a Java class or interface (`List`, `java.util.Date`), when the unit imports it (§3.5.1), or when it is one
  that every unit knows (`String`, and the language types `ConceptInstance` and `MatchingSet`);
* a **concept** declared in the unit, in another model the unit imports, or inherited through `extends`; the
  type then denotes the instances of that concept and of the concepts that extend it;
* a **model**, which denotes its instances (virtual model instances, §13.1);
* an **enum** (§5.4);
* a name introduced by `typedef` (§3.6) or by a named import (§3.5.4).

The `$` separator names a concept that is declared inside another: `Library$Shelf` is the concept `Shelf`
declared in `Library`, and `Shelf$Book` the concept `Book` declared in `Shelf`. It is also the notation of
the address of a nested concept (`…/Library.fml#Shelf#Book`). Inside a unit, a nested concept is named by its
simple name from anywhere in the unit; the `$` form is for naming a concept of another unit
(`select AnOtherModel$AConcept from …`, §10.2).

A `lidentifier.`-prefixed name is a package path (`java.util.List`). An escaped-identifier prefix `{"…"}.` may
replace a segment whose text is not a valid identifier.

A type name must end in a `uidentifier`: the all-capitals rule of §2.4 applies, so a concept cannot be
named `UML`.

## 4.4 Generic types

A reference type may have type arguments, as in Java: `List<Task>`, `Map<String,List<String>>`. A wildcard
`?`, optionally bounded by `extends` or `super`, is accepted as an argument. `<>` is the empty (diamond)
argument list, usable after `new`.

```fml-member
List<String> names;
Map<String,List<String>> byGroup;
List<? extends Person> people;
List<List<List<Task>>> deep;
```

The lexer produces `>>` and `>>>` as single tokens. The grammar has special alternatives for them so that
the closing brackets of nested type arguments parse; no space between the brackets is ever required.

Type arguments are checked at the level of the binding model (§9.6): the element type of `List<Task>` lets the
path `tasks.get(0).label` be resolved. They are not enforced at run time.

Arrays are not a type of FML. The brackets `[]` appear only in a cast and in an `instanceof` test
(`(Foo[]) x`, `x instanceof Foo[]`), where the grammar accepts them after the type.

## 4.5 Versions

A type may be followed by a version in the form `@(version)`:

```fml
typedef String as Foo;

model Versioned {
	Foo@(1) foo1;
	Foo@(1.2) foo2;
	Foo@(1.3.4) foo3;
	Foo@(1.3.4beta) foo4;
}
```

A version is an integer (`1`), a decimal (`1.2`), or two decimals optionally followed by an identifier
(`1.3.4`, `1.3.4beta`). The latter lexes as the floating-point literal `1.3`, the floating-point literal `.4`,
then the identifier `beta`, which is why only that shape is accepted for three-part versions; a fourth
numeric part is not.

The version is reserved for qualifying a type with the version of the metamodel that defines it.

> **Implementation note.** In 3.0 the version is accepted by the grammar and **ignored** by the semantic
> analysis: the type that is built is the type without version, and a version is not written back by the
> pretty-print. `Foo@(1)` and `Foo@(2)` denote the same type. A program must not rely on it.

## 4.6 Cardinalities

A *cardinality* follows the type of a property and states whether the property holds one value or a list:

```ebnf
cardinality = "[" lit_integer "," lit_integer "]"
            | "[" lit_integer "," "*" "]"
            | "[" "*" "," lit_integer "]"
            | "[" "*" "," "*" "]"
            | "..."
```

```fml-member
String name;                 // no cardinality: at most one value (zero or one)
String[1,1] projectName;     // exactly one
String[1,*] atLeastOne;      // one or many
String[0,*] anyNumber;       // zero or many
String... equivalent;        // same as [0,*]
Task[2,8] bounded;           // accepted; read as [0,*] with a warning
```

The meta-model records one of four cardinalities, and the parser maps what is written onto them:

| Written | Recorded | Notes |
|---|---|---|
| nothing | zero or one (`0..1`) | the property is single-valued and may be empty |
| `[0,1]` | zero or one | printed back without brackets |
| `[1,1]` | one (`1`) | single-valued, declared mandatory |
| `[0,*]`, `[*,*]`, `...` | zero or many (`0..*`) | multi-valued |
| `[1,*]` | one or many (`1..*`) | multi-valued, at least one |
| any other (`[2,8]`, `[0,5]`, `[*,3]`) | zero or many | a *warning* is reported: "Cardinality … is not supported, read as …" |

A multi-valued property is a list from the start: a new instance holds an empty list, not `null` (§6.1). The
lower bound of a cardinality records the author's intention; this specification attaches no run-time check
to it.

A cardinality applies to the properties declared with a type (§6.1, §6.5). It is not part of the type: it is
not written in a parameter, a local variable or a return type.

## 4.7 Technology-specific types

```ebnf
technology_specific_type = uidentifier fml_parameters
fml_parameters           = "(" qualified_argument_list? ")"
```

A technology adapter may contribute types that need parameters to be identified: a typed diagram, an XML
document with a given schema. They are written like a call, `Name(arg = value, …)`, with the *qualified
arguments* of §10.10.1.

```fml-member
FooType(a = 1) foo;
```

The same form is accepted in a `select` (`select Row(sheet = s) from …`, §10.2) and in a `typedef` (§3.6). The
types contributed by the adapters declared with `use` are resolved by the adapter itself (§4.8).

## 4.8 The typing space

The set of type names a unit can use is its **typing space**. It is made of

* the primitive types and the language types (`String`, `ConceptInstance`, `MatchingSet`);
* the Java types imported with `import`, generic ones included;
* the concepts of the unit, nested ones included;
* the concepts of the models the unit imports (§3.5.2) and of the models it extends;
* the types brought by the technology adapters the unit declares with `use` (§3.4): a spreadsheet cell, an XML
  element, a class of an EMF model;
* the aliases introduced by `typedef` and by named imports.

A bare type name (not qualified by a package path) is resolved in the following order; **the first match
wins**:

1. a concept, event, or enum declared in the compilation unit;
2. a class exposed by a model slot that the unit declares with `use`;
3. a language type (`ConceptInstance`, `MatchingSet`);
4. a `typedef` alias of the unit;
5. a plain Java type that the unit imports.

A name that matches nothing yields an unresolved type: a parse-time semantic issue for a declaration, a
validation error for a binding. A property with an unresolved type still exists and is serialized; paths that
go through it evaluate to `null` at run time (§13.6).

> **Implementation note.** A concept type whose address resolves to an empty URI is nevertheless
> reported as resolved (`CORE-D-5`).

The last item of the list above is what lets an expression go through an external resource. A path such as
`sheet.rows.get(0).name` is resolved step by step, each element being looked up in the type produced by the
previous one, whether that type is a concept of the program, a Java class, or a type of a technology adapter
(§9.6).

## 4.9 Subtyping

The relation used wherever a value is assigned or passed is the following.

* An instance of a concept is acceptable where one of its ancestors is expected (§5.3). A model that
  extends another can be used where the other is expected.
* For Java types, Java's assignability applies, including generics' covariance as far as the binding model
  models it.
* Mixed integer and floating-point arithmetic produces a floating-point value (§9.5); no value is ever
  converted implicitly **to or from** a string.

The null value belongs to every type that is not a primitive; a primitive-typed property that has never
been assigned also holds it (§4.2).
