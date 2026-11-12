# Appendix B. Known deviations

This appendix lists the places where the 3.0 implementation does not do what the language, as specified in the
chapters above, is meant to do. Each entry is a defect of the implementation: a conforming program does not depend
on it, and a future release may change it. The identifiers are those of
[`KNOWN_DEFECTS.md`](../KNOWN_DEFECTS.md), which holds the symptom, the reproduction and the analysis of each.

The common trait of these deviations is that **they fail silently**: the program parses, loads, runs, and gives a
wrong value. They are the reason why chapter 13 insists on validation and on asserting values.

## B.1 Statements and expressions

| Id | Deviation | Specified in | Write instead |
|---|---|---|---|
| `CORE-D-8` | an invalid condition in `if` / `while` is taken as true | §8.4 | validate the unit |
| `CORE-D-19` | a method call on a string literal gives `null` | §9.5.2 | put the literal in a variable |
| `CORE-D-48` | a local declared in a block stays visible after it, and hides a property of the same name | §8.2 | do not reuse the name of a property or of an outer local |

## B.2 Properties and behaviours

| Id | Deviation | Specified in | Write instead |
|---|---|---|---|
| `CORE-D-3` | in `set(T x) { … }` the value is always named `value`; the declared name is ignored | §6.7 | declare it `value` |
| `CORE-D-11` | an expression property that has been read can keep its earlier value | §6.4 | read the underlying value in a behaviour |
| `CORE-D-4` | a default value of an argument does not let a call omit it | §7.2 | pass every argument; add a named creation scheme |
| `CORE-D-13` | `new C::name()` with fewer arguments than the scheme declares does not resolve; a call must match in number | §7.4 | pass the arguments |
| `CORE-D-17` | `super.name()` gives `null` when the concept has several parents | §5.3.2 | a helper behaviour called on `this` |
| `CORE-D-10` | `delete x` does not remove `x` from the multi-valued properties that hold it | §10.4 | remove, then delete |
| `CORE-D-15` | `new Child()` creates a `Parent` when `Child` only inherits the creation scheme (virtual models; concepts when created by `vmi.new Child()`) | §5.1.3 | declare a creation scheme in the child |
| `CORE-D-47` | a creation scheme that fails does not fail the `new`: the caller goes on, the partial instance stays | §13.6 | validate; assert the effects of a creation |
| `CORE-D-49` | signatures compare parameter names, not types; the first assignable overload wins; override return types and inherited abstract behaviours are not checked | §5.3.1, §7.4 | distinct names for overloads; most specific first |
| `CORE-D-50` | default values of inherited properties are not set on a new instance | §6.2 | assign them in the creation scheme |
| `CORE-D-51` | the initializer of a concept-typed property, an edition action as initializer, and the cardinality of a Java-typed property are lost | §6.2, §6.3 | initialize in the creation scheme |
| `CORE-D-55` | an inherited event listener is called twice after its instance is loaded | §7.7 | declare the listener in the concrete concept |

## B.3 Matching and queries

| Id | Deviation | Specified in | Write instead |
|---|---|---|---|
| `CORE-D-1` | the implicit matching set of a behaviour is typed by its first `match` | §10.9.4 | one explicit set per concept |
| `CORE-D-12` | the `where` of `begin match` is ignored: `end match … delete()` deletes the instances it meant to leave out, even if the `match` loop skips them | §10.9.5 | `end match … delete()` only on every instance of the concept; otherwise no `end match`, explicit deletion |
| `CORE-D-53` | `select` from a concept instance misses the instances nested more than one level down | §10.2 | query from the virtual model instance |
| `CORE-D-54` | the list returned by an unfiltered `select` is the internal index: modifying it hides instances | §10.2 | never modify a `select` result; copy it |

## B.4 Types, names and loading

| Id | Deviation | Specified in | Write instead |
|---|---|---|---|
| `CORE-D-5` | a concept type with an empty URI is reported as resolved | §4.8 | — |
| `CORE-D-16` | a namespace alias is not resolved in an annotation | §3.3, §11.3 | write the full address in `@URI` |
| `CORE-D-34` | loading units that import each other logs transient *Unresolved type* warnings | §3.8 | none: ignore the transient ones |
| `CORE-D-33` | the text rebuilt from the model is not always the text that was parsed | §1.1 | — |
| `CORE-D-56` | a control statement whose body has no braces is saved with braces and a broken indentation | §8.1 | brace the bodies |
| `CORE-D-52` | a concept-typed property with an initializer is printed as text that does not parse again | §1.1 | no initializer on such properties |

## B.5 Scripts

| Id | Deviation | Specified in | Write instead |
|---|---|---|---|
| `CORE-D-20` | after a second assignment, a script variable reads as `null` | §12.7 | a new name for each value |

## B.6 Behaviours of the language that are not defects

Some behaviours surprise a reader who comes from Java but are what the language specifies. They are collected here
for convenience.

| Behaviour | Section |
|---|---|
| `&&` and `||` evaluate both operands | §9.3 |
| `/` and `%` give decimals, even between integers | §9.5.1 |
| a string is never converted to a number; `1 + "a"` is an error | §9.5.2 |
| a parameter is read as `parameters.x`; a bare name gives `null` | §7.2.1, §9.6.3 |
| a behaviour of the same instance is called on `this` | §7.4 |
| a bare `new` of a concept that is not nested in the current one gives `null` | §9.7 |
| a path that does not resolve evaluates to `null` | §9.8 |
| a missing `import` gives `null` and no error | §3.5.2 |
| an unassigned primitive property holds `null`, not `0` | §4.2 |
| `[2,8]` and other bounds are read as `[0,*]` with a warning | §4.6 |
| a type version `@(1.2)` is accepted and ignored | §4.5 |
| a concept instance exposes only its properties and behaviours to a path | §9.6.2 |
| an all-capitals word is a constant identifier, not a type name | §2.4 |
| a misspelled annotation is kept and does nothing | §11.2 |
