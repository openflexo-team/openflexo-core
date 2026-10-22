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
| `CORE-D-6` | `do … while` runs its body once and ignores its condition | §8.5 | `while` |
| `CORE-D-7` | a compound assignment *statement* (`n += 2;`) assigns the right-hand side | §8.3 | `n = n + 2;` |
| `CORE-D-18` | `n++;` / `--n;` as a statement make the behaviour return `null` | §8.6 | `n = n + 1;` (the update of a `for` is fine) |
| `CORE-D-9` | `for (init; ; update)` without condition: the behaviour is silently not built | §8.5 | give the condition, or `while (true)` |
| `CORE-D-8` | an invalid condition in `if` / `while` is taken as true | §8.4 | validate the unit |
| `CORE-D-19` | a method call on a string literal gives `null` | §9.5.2 | put the literal in a variable |

## B.2 Properties and behaviours

| Id | Deviation | Specified in | Write instead |
|---|---|---|---|
| `CORE-D-3` | in `set(T x) { … }` the value is always named `value`; the declared name is ignored | §6.7 | declare it `value` |
| `CORE-D-11` | an expression property that has been read can keep its earlier value | §6.4 | read the underlying value in a behaviour |
| `CORE-D-4` | a default value of an argument does not let a call omit it | §7.2 | pass every argument; add a named creation scheme |
| `CORE-D-13` | `new C::name()` with fewer arguments than the scheme declares does not resolve; a call must match in number | §7.4 | pass the arguments |
| `CORE-D-17` | `super.name()` gives `null` when the concept has several parents | §5.3.2 | a helper behaviour called on `this` |
| `CORE-D-10` | `delete x` does not remove `x` from the multi-valued properties that hold it | §10.4 | remove, then delete |
| `CORE-D-15` | `new Child()` creates a `Parent` when `Child` only inherits the creation scheme | §5.1.3 | declare a creation scheme in the child |

## B.3 Matching

| Id | Deviation | Specified in | Write instead |
|---|---|---|---|
| `CORE-D-1` | the implicit matching set of a behaviour is typed by its first `match` | §10.9.4 | one explicit set per concept |
| `CORE-D-12` | the `where` of `begin match` is ignored | §10.9.5 | filter in the `match` instructions |
| `CORE-D-2` | `end match … unmatched: someBehaviour()` does not call that behaviour; the first behaviour of the concept runs | §10.9.5 | `unmatched: delete()` and a single deletion scheme |

## B.4 Types, names and loading

| Id | Deviation | Specified in | Write instead |
|---|---|---|---|
| `CORE-D-5` | a concept type with an empty URI is reported as resolved | §4.8 | — |
| `CORE-D-16` | a namespace alias is not resolved in an annotation | §3.3, §11.3 | write the full address in `@URI` |
| `CORE-D-34` | loading units that import each other logs transient *Unresolved type* warnings | §3.8 | none: ignore the transient ones |
| `CORE-D-33` | the text rebuilt from the model is not always the text that was parsed | §1.1 | — |

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
