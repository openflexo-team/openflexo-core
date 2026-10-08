# Single identifier token in the FML grammar (CORE-F-5)

Status: **analysis done on the 2.99 grammar, implementation deferred to 3.0.** Nothing here is built:
the prototype lives outside `src/main/sablecc`.

## Problem
`fml.sablecc` lexes three identifier kinds — `lidentifier` (lowercase start: properties, methods),
`uidentifier` (uppercase start + a lowercase: types), `cidentifier` (only `A-Z0-9_`: constants, technology
adapter ids) — and uses them to keep the grammar LALR(1). Goal: a single `ident`.

## Result
Feasible. `fml-single-ident.sablecc` generates with **0 conflicts** (SableCC 3.7; LALR(1) conflict-free
implies unambiguous). Over the `.fml`/`.fmlscript` files of the workspace (532 at analysis time) the
baseline parsed 493 and the prototype 494: same failing set (legacy XML, `faq.fml`), the only difference
being `import aClass;` now accepted. Note the lexical quirks that disappear: `cidentifier` matched the empty
string, `A` or `X1` (a one-letter type!) lexed as constant, `_FOO` as constant.

Lexer: `ident = (lowercase_letter | uppercase_letter) letter_or_digit*;` and `identifier = ident`.

## The real ambiguities, and the resolution of each
Found by iterating "run SableCC, read the conflict state, fix, rerun". Nine, in the order met:

| # | Ambiguity (case was the only separator) | Resolution — language delta |
|---|---|---|
| 1 | `Foo.bar(x)` as `{class_method}` (type dot name) vs call on a composite_ident `a.b.c(x)`; shift/reduce on `.` | Drop `{class_method}`. `Foo.bar(x)` is a composite_ident call; the semantic layer decides it is static when the path prefix resolves to a type. `Foo.CONST` becomes legal. Generic receivers `A<B>.m()` lost. |
| 2 | `annotation_tag`: composite_tident vs composite_cident (reduce/reduce) | Keep composite_tident only. |
| 3 | Cast `(Foo) x` vs `(foo) x` (reduce/reduce on `)`) | Java's way: `( expression ) unary_exp_not_plus_minus` (semantic layer checks the expression is a type) + `( primitive_type dim* ) unary_exp`. `(Foo) -x` is no longer a cast. Generic/array/`@version` casts lost (none in corpus). |
| 4 | That cast vs a call on a parenthesed expression `(a)(b)` | New `callee_primary` = primary without `( expr )`, used by `method_invocation`. `(expr)(args)` no longer parses (unused). |
| 5 | `(a)` then `{`: cast operand `{"x"}` (escaped identifier) vs `listen T from (a) {` | `escaped_identifier` becomes one lexer token (`{ "..." }`, no comment/newline inside). Zero occurrence in the corpus. |
| 6 | `Foo(x=1) v;` (FML-typed declaration) vs `foo(x = 1)` (call with an assignment argument) at statement start | Call arguments are `conditional_exp` (no assignment). `where (...)` keeps assignments via a new `expression_list`. |
| 7 | `qualified_argument` `x = Foo`: `{type}` vs `{simple}` expression (reduce/reduce) | Drop `{type}`; `x = Foo` is an expression resolved to a type semantically. `x = A<B>` lost (none in corpus). |
| 8 | `(Foo(a=1)) o` (FML-typed cast, used in bpmn-modeler) vs `(foo())` | `{cast_fml}` requires a non-empty `qualified_argument_list`. |
| 9 | `assert (List<X> x : ...)` vs `assert (a < b)` (shift/reduce on `<`) | `iteration_type` = primitive or `composite_tident` (no type arguments). |

Common root of 1, 3, 7, 9: a *type* and an *expression* both start with a name; `<` (type arguments vs
less-than) and `(` are the tokens that cannot be decided with one lookahead.

## Java impact (not done)
~17 files, ~155 references to the generated node classes: `FMLFactory`, `FMLSemanticsAnalyzer`,
`ExpressionFactory`, `BindingPathFactory`, `TypeFactory`, `URIExpressionFactory`, `ObjectNode`,
`fmlnodes/*` (`FlexoBehaviourNode`, `AbstractFlexoConceptNode`, `AbstractRolePropertyNode`,
`FlexoEnumValueNode`, `expr/StaticMethodCallBindingPathElementNode`, `expr/FMLCastExpressionNode`, …) and
`fml-cli/.../Directive.java`. Real semantic work: (a) static call detection from a composite_ident call,
(b) cast type built from a parenthesed expression, (c) qualified argument that resolves to a type,
(d) removal of `AConstantCompositeIdent` / `ACidentifier*` handling, enum values and TA ids (`cidentifier`
→ ident, validated semantically).

## Re-running the experiment
```
fml-parser/doc/single-identifier/run-experiment.sh <grammar.sablecc> <files-list.txt>
```
Generates in a temp dir, prints SableCC conflicts, compiles with the real `CustomLexer`, parses the list
(`Harness.java`), prints `OK=n FAIL=m`. The file list is any list of absolute `.fml`/`.fmlscript` paths, e.g.
`find` over the workspace excluding `build/`, `bin/`. Compare the FAIL set with the one of the real grammar,
not the counts (they move with the workspace).

## Resuming against the 3.0 API
1. Re-base: diff `fml-single-ident.sablecc` against the 2.99 grammar it came from, replay the 9 changes on
   the 3.0 grammar (new constructs may add new type/expression ambiguities: rerun the loop).
2. Re-check the language deltas are still acceptable, and grep the corpus for them (casts, `Foo.bar(`,
   `x = Type`, generic usages, `{"…"}`).
3. Not covered by the corpus harness: the `binding` entry point (GINA `.fib` bindings) and `fml-cli` commands.
4. Implement the Java side, then `./gradlew sableccParser compile test` in the openflexo-dev composite and read
   the `Results:` summary (failures are hidden by `ignoreFailures`).
