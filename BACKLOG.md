# openflexo-core — Backlog

Features and evolutions to develop in openflexo-core, near to long term. Known bugs go to `KNOWN_DEFECTS.md`
(identifiers `CORE-D-<n>`), not here.

Identifiers are stable and never reused: `CORE-F-<n>`. Reference them from commits and from other entries.

Status legend: `TODO` · `IN PROGRESS` · `BLOCKED` · `DONE` · `DEFERRED`.

---

## FML language

### CORE-F-1 — Arbitrary min/max bounds on FlexoRole cardinality  ·  `TODO`

**GitHub:** [openflexo-team/openflexo-core#116](https://github.com/openflexo-team/openflexo-core/issues/116)

**Problem.** The FML grammar (`fml-parser/src/main/sablecc/fml.sablecc`, production `cardinality`) accepts any
bounds after a property type: `[2,8]`, `[2,*]`, `[*,5]`, `[1,5]`. `PropertyCardinality` (flexo-foundation) only
has four values — `ZeroOne`, `One`, `ZeroMany`, `OneMany` — so other bounds are read as `ZeroMany` (`OneMany`
for `[1,n]`) and lost in the model.

**Current state.** The parser raises a `SemanticAnalysisWarning` ("Cardinality [2,8] is not supported, read as
[0,*]"), and the syntax-preserving pretty-print keeps the original text; the normalized pretty-print writes the
degraded form.

**Existing FML using such bounds.**
- `flexo-test-resources/.../FMLParsingExamples/TestProperties.fml` — `String[2,8] auMoins2AuPlus8;`
- `flexo-test-resources/.../FMLParsingExamples/TestBasicTypes.fml` and
  `TestFMLPrettyPrint2/TestBasicTypesWithComments.fml` — `ConceptA[2,*] otherConceptsA;`

**To decide.**
- Metamodel shape: explicit lower/upper bound properties on `FlexoRole` (PAMELA `@Getter`/`@Setter`), or a
  richer cardinality value type replacing the `PropertyCardinality` enum.
- Compatibility: keep `PropertyCardinality` and `isMultipleCardinality()` working for existing callers, derived
  from the bounds.
- Parsing and pretty-printing: `ObjectNode.getCardinality` / `serializeCardinality` / `cardinalityContents`;
  drop the warning once bounds are representable.
- Runtime semantics: whether and where bounds are enforced (validation rules on instances, edition actions
  adding or removing values). How `One` / `OneMany` are enforced today must be checked first.
- Roles and model slots of technology adapters that carry a cardinality.

**Acceptance criteria.**
- `[2,8]`, `[2,*]`, `[*,5]` parse without warning and keep their bounds in the model.
- The normalized pretty-print writes the bounds and a round trip preserves them (extend `TestFMLPrettyPrint11`).
- `ZeroOne` / `One` / `ZeroMany` / `OneMany` behave as before; pretty-print tests and AutomatedTests stay green.

### CORE-F-3 — Reference an enumeration value in FML expressions  ·  `TODO`

**GitHub:** [openflexo-team/openflexo-core#117](https://github.com/openflexo-team/openflexo-core/issues/117)

**Problem.** FML can declare an enumeration (`enum Genre { NOVEL, POETRY }`) and a property typed with it (`Genre genre;`), but an
FML expression cannot designate one of its values, so such a property can neither be assigned nor compared from FML.

**Current state (verified 2026-09 by execution, on a throwaway fixture).**
- `genre = Genre.POETRY;` and `Genre.enumValues` do not parse: the grammar only accepts lower-case path prefixes
  (`identifier_prefix = lidentifier dot` in `fml.sablecc`), so a type name cannot start a path. The parse error empties the whole
  compilation unit.
- `genre = POETRY;` parses (`composite_ident`, `{constant}` form) but does not resolve (`BindingVariable POETRY does not exist`); the
  property stays null.
- `enumValues` is only offered on an expression already typed with the enumeration (`FMLBindingFactory`, `EnumValuesPathElement`), e.g.
  `genre.enumValues`, which requires to already hold a value.
- No test executes an enumeration: `TestEnum.fml` (flexo-test-resources) is only parsed.

**Existing machinery.** `FlexoEnum` / `FlexoEnumValue` (each value is a `FlexoConcept`), run-time `FlexoEnumInstance`s shared by the
whole application (`FlexoEnum.getInstances()`), `FlexoEnumType`, `FlexoEnumValueActorReference` persisting a value in an instance.

**To decide.**
- Syntax: qualified access (`Genre.POETRY`, which requires accepting a type name as path prefix), bare constant resolved against the
  expected type (`genre = POETRY`), or both.
- Binding resolution: a path element or binding variable resolving to the shared `FlexoEnumInstance`; typing as `FlexoEnumType`.
- Comparison semantics (`genre == POETRY`) and use in `select … where`.

**Acceptance criteria.**
- A value can be assigned to and compared with an enumeration-typed property from an FML behaviour, and read back from a script.
- The value is persisted and reloaded with the instance.
- An fmlscript in `AutomatedTests` exercises it; pretty-print round trip preserves the syntax.

### CORE-F-2 — Remove or redefine the "default" deletion scheme of a concept  ·  `TODO`

**GitHub:** [openflexo-team/openflexo-core#118](https://github.com/openflexo-team/openflexo-core/issues/118)

**Problem.** `FlexoConcept.getDefaultDeletionScheme()` returns the first accessible deletion scheme, so which scheme is "the default"
depends on declaration order (and inheritance), and its intended semantics are unclear. The notion is not meant to be kept as is.

**Where it is used.**
- `unmatched: delete()` in an `end match` (`EndMatchActionNode.finalizeDeserialization()`).
- `FlexoConceptInstance.delete()`, hence the pruning of a behaviour's default matching set (`CORE-D-1`).
- `DeleteFlexoConceptInstance` when no deletion scheme is given.

**Why it matters.** A concept closed by an `end match` that declares several deletion schemes silently gets the first one — possibly
one that deletes federated artifacts instead of only releasing references. (`CORE-D-2`, fixed: `unmatched: method()`
no longer falls back on the first action scheme.)

**To decide.**
- Drop the notion: require an explicit deletion scheme wherever more than one is accessible (validation error), or
- Redefine it explicitly (e.g. an annotation marking the default deletion scheme), with a validation rule when it is ambiguous.
- What `FlexoConceptInstance.delete()` does when no scheme can be chosen.

**Related.** Fix together with `CORE-D-1` and `CORE-D-2` (see [`KNOWN_DEFECTS.md`](./KNOWN_DEFECTS.md)).

### CORE-F-5 — Single identifier token in the FML grammar  ·  `DONE`

**GitHub:** [openflexo-team/openflexo-core#119](https://github.com/openflexo-team/openflexo-core/issues/119)

**Problem.** `fml.sablecc` lexes three identifier kinds (`lidentifier`, `uidentifier`, `cidentifier`) and relies on the case of the
first letter to stay LALR(1). The distinction is uncomfortable (a one-letter type lexes as a constant, `cidentifier` matches the empty
string, `_FOO` is a constant) and leaks into the Java factories. Wanted: one `ident` token and a grammar that stays unambiguous.

**Findings (2.99).** Feasible: a prototype generates with 0 SableCC conflicts and parses the workspace corpus (532 `.fml`/`.fmlscript`)
exactly like the current grammar. It needs nine grammar changes, each removing a type-vs-expression ambiguity, and four of them change
the language: no `Type.method()` as a dedicated form (static call resolved semantically), casts written as `( expression )` checked
semantically (no generic/array casts, `(Foo) -x` not a cast), no assignment in call arguments (kept in `where`), `x = Foo` parsed as an
expression instead of a type, escaped identifiers as a single token.

**Deferred to 3.0**, together with the Java adaptation (~17 files in `fml-parser`, plus `fml-cli`).

**Taken up again (2026-11-23)** for 3.0. **Decided (2026-11-25):** all the language changes above are accepted; existing `.fml` are
migrated in the workspace only (no migration tool), the changes documented in the specification and the changelog. The prototype is
rebased on the 3.0 grammar (exceptions, `throws`, `try`): still 0 conflicts, corpus of 616 files parsed as by the current grammar,
and no binding or fml-cli command accepted today is rejected.

**Done (2026-11-25).** `fml.sablecc` has one `ident` token (0 conflicts); the Java side is adapted (fml-parser, fml-cli, the cast
printing of flexo-foundation). Decided with the user on the way:
- A cast to a type with arguments or wildcards is written `x as List<String>` (precedence of `instanceof`, any type, not in scripts,
  like `instanceof`); `(T) x` stays for a name. The normalized print uses `as` only for a type written with `<` or `?`, and parenthesizes
  an operand that has an operator of its own.
- `Foo.bar()` follows the rule of Java: a visible variable hides a type of the same name, otherwise the call is static. The decision
  is taken where the variables are known: again at the end of the parse of a unit (`BindingPathNode.reconsiderStaticCalls`, which
  keeps the parsed text), and at each validation of the path (`FMLBindingPath`), which covers scripts (a variable exists once
  assigned) and late-loaded units. The validation rule `VariableShouldNotHideAType` warns about such a variable.
- A receiver that is neither a variable nor a type is an unresolved variable (it was a static call on an unresolved type). The
  decision is taken again the other way at each validation: once no variable of that name is visible and the name resolves to a type
  of the unit (an import added afterwards), the call becomes static (`FMLBindingPath.reconsiderCallOnUnresolvedVariable`). For that,
  the typing space of a loaded unit (`FMLTypingSpace`) now looks in its Java imports, as the one of the parser does. A switch made at
  validation changes the shape of the path: its text is then the normalized one (an import alias is printed as the class name).
- Also lost, not used in the workspace: `(a)(b)` as a call, a generic iteration type in `assert (List<X> x : …)`, `(Foo()) x`,
  `x = A<B>` in a qualified argument, an array cast to a reference type.

Verified: fml-parser (TestStaticCallOrVariable, TestExpressionWithTypesParser, the `casts()` behaviour of TestExpressions.fml),
fml-cli-test (TestStaticCallOrVariable.fmlscript), flexo-foundation-test, the four modelers, formod, integration tests and the test
modules of the technology adapters: 0 failure (capella: CAPELLA-D-1 only, known before). Not measured: the cost, at parse time, of
looking up the receiver of every call on a dotted name as a type.

**Acceptance criteria.**
- `fml.sablecc` has a single identifier token; SableCC reports no conflict.
- The `.fml`/`.fmlscript` corpus and the fml-parser / foundation / modelers tests pass as before the change.
- Static calls, casts, and type-valued FML parameters are resolved by the semantic layer.

**Material.** Analysis, prototype grammar and experiment harness: [`fml-parser/doc/single-identifier/`](./fml-parser/doc/single-identifier/README.md).

### CORE-F-7 — Clabjects: a multi-level extension of FML  ·  `DEFERRED`

**GitHub:** [openflexo-team/openflexo-core#120](https://github.com/openflexo-team/openflexo-core/issues/120)

**Problem.** FML separates `FlexoConcept` from `FlexoConceptInstance`, so stacked type/instance levels (product model →
series → item, the two-level emulation of the FME, the MULTI process challenge) duplicate every notion. Wanted: a language,
an extension of FML sharing its grammar (FML itself unchanged), whose elements are clabjects (concept and instance at once).

**Current state.** Exploratory study only, nothing implemented. Directions taken: no mandatory potency (optional, decremented at each refinement
step), no declared levels;
progressive refinement with values inherited by delegation; `.fml.rt.xml` replaced by textual FML (instances only) as a
prerequisite, tracked by `CORE-F-27`, which blocks this entry. Analysis, examples, hard points and proposed path: [`design/clabjects.md`](./design/clabjects.md).

**To decide.** Valuation operator (`:=`), own features and optional classification, conflict rule shared with behaviours,
`select`/`match` semantics, deletion of an intermediate clabject, interoperability with FML.

**Acceptance criteria.** To be written per milestone (clabjects in the FME first). Textual FML@RT is `CORE-F-27`.

### CORE-F-10 — Decide the status of the specification against the implementation  ·  `TODO`

**GitHub:** [openflexo-team/openflexo-core#121](https://github.com/openflexo-team/openflexo-core/issues/121)

**Problem.** The specification (`spec/`) is ambiguous about what defines the meaning of a program. §1.4 makes the semantic analyzers
of fml-parser and the meta-model of flexo-foundation authoritative "for the meaning of an accepted program", while Appendix B lists
the places where that same implementation departs from the intended meaning, and §1.6 defines conformance "within the deviations
listed in Appendix B". So the implementation is both the reference and the thing judged against it, and a defect can end up
justifying itself. Raised by the external audit of the FML language of 2026-10-23 (its §4); left open on 2026-11-11 while its
defects were recorded (CORE-D-46 to CORE-D-55).

**Options.**
- The chapters are normative; the implementation is authoritative only where they are silent; Appendix B lists the departures,
  and conformance is defined without them.
- The implementation stays the reference; the chapters describe it, and Appendix B becomes a list of intended changes.
- Three explicit layers: normative semantics, known deviations of the implementation, guarantees left to technology adapters
  (identity, notification, failure) — the shape the audit recommends.

**Acceptance criteria.** §1.4, §1.6 and the introduction of Appendix B state one consistent rule; every Appendix B entry reads
as a departure from a stated rule, not as the rule.

### CORE-F-15 — Parameters of a behaviour with a cardinality (`String[0,*] platform`)  ·  `TODO`

**GitHub:** [openflexo-team/openflexo-core#21](https://github.com/openflexo-team/openflexo-core/issues/21)

**Problem.** `create(String id, String[0,*] platform, boolean deprecated, …)` is a syntax error at the `[` (`Syntax error: [38,27] expecting: identifier`).

**Verified (2026-11-17, branch 3.0).** The grammar gives a cardinality to properties only (`type cardinality? variable_declarator`);
`formal_argument` has none (`fml.sablecc`). A parameter cannot be a list or an array in that notation.

**Options.** (1) Accept `cardinality` on `formal_argument` as on properties. (2) Document `List<String> platform` as the way to pass several values
(probable workaround, not verified). (3) Keep as is and make the message say what is allowed.

**Acceptance.** A behaviour receiving several values can be declared and called, in textual FML, with a documented notation, and the pretty-print round-trips it.

**Original report** by `hibahnaini`, 2024-10-24 (issue openflexo-core#21):

> ![image](https://github.com/user-attachments/assets/7ea23ee0-e813-4610-bff6-24395557c592)
>

### CORE-F-17 — Align the syntax of `match` and `select`  ·  `TODO`

**GitHub:** [openflexo-team/openflexo-core#130](https://github.com/openflexo-team/openflexo-core/issues/130)

**Problem.** The two statements that look for instances of a concept do not read alike:

```
select Wrapper from (this) where (selected.name = "x", selected.age > 3)
match Holder in ms from this where (w = wr) unmatched: new Holder(wr)
```

- `select … where (…)` takes boolean expressions evaluated on `selected` (`where_clause`, an `argument_list`); `match … where (…)` takes
  `property = value` pairs (`qualified_where_clause`, a `qualified_argument_list`), where the name on the left is a property of the matched
  concept, not an expression. `selected.w = wr` is a syntax error in a `match` ([`CORE-D-88`](./FIXED_DEFECTS.md), only the messages fixed).
- `in` and `from` do not follow the same shape (`select X from (e)` / `match X in ms from e`).
- The keywords differ in style: `begin match … ; match … unmatched: … ; end match …` against `select [unique] … from … where …`.

**Decisions (2026-11-18).**
- The `where` of `match` takes the forms of the `where` of `select`: expressions on `selected`, **any boolean expression**, not only
  equalities. The target notation is `where (selected.w == wr)`.
- The comparison is written with `==`, as in `select`: `=` is the assignment and reads wrongly as a test. The old `where (w = wr)` (and
  `selected.w = wr`) stays accepted, with a **deprecation warning** on the use of `=` in a `where`, in `match` and in `select`
  (a `SemanticAnalysisWarning`, with a fix that rewrites it to `==`). It is printed back as it was written until it is edited.
- Backward compatibility is kept, and **the existing `.fml` are migrated in parallel** (a script on the resource centers, run in dry mode
  by default, as `MigrateLegacyParametersNotation` for `CORE-F-14`: the model decides, the text is edited, no re-serialization).
- The `in` / `from` clauses and the keywords of the two statements are aligned too.

**To settle.**
- The matching engine compares the values of properties (a key per instance, `MatchingSet.matchFlexoConceptInstance(criterias)`). Evaluating
  any expression on each candidate replaces that comparison: what it costs on a large set, and whether `==` equalities are still recognised
  to keep an index.
- Ambiguity of the legacy `where (w = wr)`: the left-hand side is a property name, not a path; `select … where (selected.name = "x")` with
  `=` is already accepted today and must keep its meaning, with the warning.
- Impact: grammar (`fml.sablecc`), `MatchActionNode` and the pretty-print, `MatchingCriteria` (the property name and the validation rule
  added by `CORE-D-88`), `MatchFlexoConceptInstance`, the FML specification (`openflexo-core/spec`) and the website guide.

**Acceptance criteria.** The same `where (…)` reads and behaves the same in `select` and in `match`; the existing `.fml` of the
repositories still load and are migrated (no `=` left in a `where`); the use of `=` is reported as deprecated; round trip of the pretty-print; tests on the matching engine unchanged in behaviour.

### CORE-F-18 — `new` resolves its container by climbing the containment tree  ·  `DONE`

**GitHub:** [openflexo-team/openflexo-core#131](https://github.com/openflexo-team/openflexo-core/issues/131)

**Problem.** With a VirtualModel `VM` holding `ConceptA`, which holds `ConceptB`, an instance can only be created from a container whose
type is exactly the applicable container of the concept: `a.new ConceptB()`, with `a` typed `ConceptA`. A bare `new X()` has no container in
the model; at run time the container is `this`, and the validation accepts it only when the applicable container of `X` is assignable from the
current concept, or the other way round. So, in the context of an instance of `ConceptB`, `new ConceptA()` and `new ConceptB()` are
rejected (`cannot instantiate X in C: X is not nested in C, use container.new X(...)`), although the host is unambiguous in the containment
tree. `p.new X()` is as strict: the type of `p` must be compatible as it is.

**Current semantics** (`CreationSchemePathElement`, openflexo-core `flexo-foundation`).
- Bare `new`: no container in the model; `this` at run time (`getBindingValue`).
- Validation (`checkBindingPathIsValid`): explicit parent, parent being a VirtualModel, bare `new` — three separate rules, none climbs.
- Run time (`FMLRTVirtualModelInstanceModelFactory.makeNewFlexoConceptInstance`): the container must be the VirtualModelInstance, or an
  instance whose concept is assignable to the applicable container.
- Related: `CORE-D-89` (concept inherited from a parent VirtualModel, same validation), `CORE-D-41`, `CORE-D-15`, `CORE-D-64`.

**Decisions (2026-11-18).**
- Starting point: the object designated by the container path (`p` in `p.new X()`), or `this` when there is none. It is itself a candidate.
- The containment chain is climbed (instance, its container, …, the VirtualModelInstance, a containing VirtualModelInstance); the **nearest**
  compatible ancestor hosts the new instance, with no ambiguity error. `new ConceptB()` in a `ConceptB` creates a sibling.
- The same rule applies to an explicit container and to the implicit one, and to the instantiation of a VirtualModel (`new MyVM()`): the nearest
  VirtualModelInstance compatible with the container VirtualModel; with none, the current behaviour (`rc` / `path` / `folder`) is kept.
- No change of syntax, grammar or pretty-print: a bare `new` stays bare, `this` is never added.
- A program valid today keeps its meaning (the starting point is tried first).
- Specified in a dedicated page of the FML specification, `spec/14-instantiation-and-containment.md`; §9.7, §7.6, §13.2 and the chapter 5
  point to it.

**Settled while implementing (2026-11-18).**
- Static check on the declared type, run-time climb on the dynamic type: a sub-concept may match nearer, which is accepted; the static check is
  lenient for a behaviour of an ancestor (the model climbed is a parent of the declaring model, the concept of the starting object a parent of the
  container concept), and the run time decides.
- A virtual model that declares a container model needs a hosting model instance (`FMLResolutionError` otherwise): `rc` / `path` / `folder` apply
  only to a model with no container model, which does not climb.
- An event and an exception follow the rules of the other concepts: nested ones climb to an instance of their container concept, root ones to the nearest model instance that declares them, which is their source. Their instances are signals: they are not stored in their host.
- An `unmatched: new X()` is evaluated from `this`, not from the `from` container of the `match` (unchanged by this change).
- A nested concept created by a bare `new` from a behaviour of the model is now an invalid binding (it used to run, with no container).
- `CORE-D-89` is fixed by the same climb (`AutomatedTests/CORE-D-89.fmlscript`).
- Left out: `FMLRT::NewFlexoConceptInstance` / `AddFlexoConceptInstance` keep an exact container; a test with a declared type that differs from the
  run-time type; a test of a saved and reloaded instance.

**Acceptance criteria.** In the context of a `ConceptB`: `new ConceptA()` hosts under the VirtualModelInstance, `new ConceptB()` creates a
sibling, `p.new ConceptA()` with `p` typed `ConceptB` is valid; the nearest ancestor wins when a concept can contain itself; a concept with no
reachable container is rejected with a message naming the types climbed; round trip of the pretty-print unchanged; a saved and reloaded
instance keeps its container; the existing `.fml` and the tests of `fml-cli-test`, `flexo-foundation-test` and `fml-parser` unchanged in behaviour.

### CORE-F-20 — Lambda expressions in FML  ·  `TODO`

**GitHub:** [openflexo-team/openflexo-core#140](https://github.com/openflexo-team/openflexo-core/issues/140)

**Problem.** FML has no way to write an anonymous function. Behaviour that depends on a small piece of logic (a predicate, a key
extractor, a callback) must either be hard-wired into a dedicated construct such as `select … where`, or be written as a named behaviour
and referred to by name. The language therefore cannot express, at the point of use, "apply this computation to each element" or "call
this when it is done", and cannot pass logic as a value to a behaviour or to a technology-adapter action.

Verified on the current `3.0` branch: `fml.sablecc` has no lambda syntax and no function type, and no existing backlog entry covers it.

**Goal.** Integrate lambda expressions into FML: an anonymous function that can be written inline, typed, evaluated, and passed or
stored like any other value.

**Open questions.** Nothing is decided yet; the design is part of this entry.
- Scope: expression lambdas only (`x -> x.age > 18`), or also block lambdas with several statements and `return`? Must this live in FML
  only, or also in Connie expressions (in which case a linked `CONNIE-F-` entry is needed)?
- Use cases to cover first: predicates and mappers over collections (`select … where`, filter, map, sort), callbacks passed to
  behaviours or technology-adapter actions, function-typed variables and properties.
- Syntax: Java-like `(a, b) -> expr`, or another form. Must not clash with existing tokens of `fml.sablecc`.
- Typing: explicit or inferred parameter types; a function type in the FML type system, or reuse of Java functional interfaces with
  implicit conversion; inference of the return type.
- Capture: access to the variables, parameters and `this` of the enclosing behaviour; read-only or read/write.
- Runtime errors: a failure inside a lambda should follow the rules of `CORE-F-11` (FML stack frames, `FMLFailureTunnel` where a
  signature cannot declare the exception).
- Persistence: a PAMELA entity for the lambda, and a stable round trip parse → pretty-print → parse.
- Tooling: FML editor support (colouring, completion, validation) in this entry or a follow-up.

**Acceptance criteria** (to be refined once the design is chosen).
- The grammar parses lambdas and the generated parser is regenerated.
- A lambda evaluates correctly, with its captured variables.
- Parameter and return types are checked, and a binding with a type error is reported as invalid.
- The pretty-print round trip is stable.
- An `fmlscript` test proves a representative example by running it.
- Failures raised in a lambda carry their FML stack.

**Related.** `CORE-F-11` (typed FML runtime errors).

### CORE-F-21 — Anonymous element in the where clause of select, fetch requests and match  ·  `TODO`

**GitHub:** [openflexo-team/openflexo-core#141](https://github.com/openflexo-team/openflexo-core/issues/141)

**Problem.** In the `where` clause of a `select`, of a fetch request or of a `match`, the element being tested is designated by a predefined variable named `selected`:

    select Foo from (container) where (selected.name == "x")

The name is not discoverable. Nothing in the text introduces it: it denotes the lexical context of the candidate element being tested, and an author has to know it by heart. It reads as an ordinary variable while it is not one.

Verified on the current `3.0` branch: `selected` is a binding variable created by `FetchRequestConditionSelectedBindingVariable` and `MatchConditionSelectedBindingVariable` (flexo-foundation), typed by the fetched type or by the matched concept. The grammar of the clause is `where_clause = kw_where l_par argument_list? r_par` (`fml.sablecc`). The test and example `.fml` files use `where (selected.name = "toto")` throughout.

**Goal.** Let the author write the condition without naming the element: an anonymous way of referring to the candidate, so that nothing has to be guessed.

**Open questions.** Nothing is decided yet; the design is part of this entry.
- Syntax of the anonymous reference: a leading dot (`where (.name == "x")`), a bare property name (`where (name == "x")`, resolved on the candidate type), or another form. How is a leading dot disambiguated in the grammar, and how does a bare name interact with variables of the enclosing behaviour that have the same name?
- Compatibility: does `selected` remain valid, or is it deprecated with a migration message (as `=` is in `CORE-F-17`)? Are the existing `.fml` migrated?
- Scope: `select` and fetch requests only, or also `begin match … where`? The left-hand side of a `match` where is already a property of the matched concept (`where (w = wr)`), which this must not break.
- Nesting: how to designate the outer candidate when a `where` contains another `select` (keep `selected` as the explicit name, or bind the anonymous reference to the nearest one).
- Lexical context: `this`, parameters and local variables must stay accessible inside the clause.
- Tooling and documentation: completion of the candidate's properties after the dot in the FML editor, and the page of the FML specification that describes `where`, in this entry or a follow-up.

**Acceptance criteria** (to be refined once the design is chosen).
- The grammar parses the anonymous form; the generated parser is regenerated.
- The anonymous reference is resolved on the type of the candidate, and an unknown property is reported as an invalid binding.
- The existing form with `selected` keeps its meaning, or is deprecated with a documented migration, as decided.
- The pretty-print round trip is stable.
- An `fmlscript` test proves select, fetch request and match examples by running them.

**Related.** `CORE-F-17` (align the syntax of `match` and `select`), which changes the same `where` clause.

### CORE-F-22 — Evaluate edition actions as expressions of the FML grammar  ·  `TODO`

**GitHub:** [openflexo-team/openflexo-core#142](https://github.com/openflexo-team/openflexo-core/issues/142)

**Problem.** In the FML grammar, an edition action is not an expression. The actions (`TA::Action(...)`, `select`, `delete`, `connect`, `log`, `fire`, `notify`, `begin match` / `match` / `end match`) belong to a separate production, `fml_action_exp`, and are accepted only in a few places: as the right-hand side of an assignment, as the initializer of a variable, after `return`, as the iterated collection of `for (T x : …)`, and as a statement on their own. They cannot be composed with the rest of an expression, and cannot be an element of a binding path.

Verified on the current `3.0` branch: `fml.sablecc` declares `fml_action_exp` apart from `expression`. In the expression grammar, `new_instance` is accepted, but the alternative `{fml_action_expression} fml_action_exp` is present and commented out. The reason it was disabled has not been established.

**Goal.** Evaluate whether an edition action can be a valid expression, composable as a `BindingPathElement`, instead of being handled as a separate category, and decide whether to do it.

This is an evaluation entry: the expected result is a decision backed by a study, then, if the answer is yes, follow-up entries for the work. The potential impact is large.

**Open questions.** Nothing is decided yet.
- Motivation and use cases: what expressions should become writable (composing a result, passing an action as an argument, simplifying the model with one hierarchy instead of two). A before/after FML example is to be written.
- Scope: all actions, or only those that produce a value (technology adapter actions, `select`, `new`). Do the purely imperative ones (`log`, `fire`, `notify`, `delete`, `connect`, `match`) remain statements?
- Metamodel: does `EditionAction` become a `BindingPathElement` (a change that reaches flexo-foundation and Connie), or does only the grammar change while the model stays as it is?
- Evaluation semantics: an expression can be evaluated several times (listeners, validation, type checking) while an action has side effects (creation, deletion). How is an unwanted execution avoided?
- Impacts to inventory: LALR(1) conflicts in the grammar, the pretty-print, persistence, validation and type inference of an action, the FML editor, `fml-cli`, the technology adapters (about 66 actions), and the migration of existing `.fml`.
- Whether the change is breaking for existing FML.

**Acceptance criteria.**
- A report that states the feasibility, the options and a recommendation, with the list of impacts above filled in.
- A prototype grammar that generates with no SableCC conflict and parses the corpus of `.fml` and `.fmlscript` files of the workspace as before, or the reasons why it cannot.
- A decision recorded in this entry (go, no-go, or a scoped subset), with the follow-up entries created for the work.

### CORE-F-23 — Generics in FML (type parameters on concepts, models and behaviours)  ·  `TODO`

**GitHub:** [openflexo-team/openflexo-core#143](https://github.com/openflexo-team/openflexo-core/issues/143)

**Problem.** FML can use parameterized types that come from Java (for example `MatchingSet<Target>`), but it cannot declare type parameters of its own. A concept, a virtual model or a behaviour cannot be written once and reused for several types: a reusable container, a patron of modeling or a helper has to be duplicated for each type, or typed loosely.

Verified on the current `3.0` branch: the grammar reads type arguments in a type (`type_arguments`, `<…>`), but no production declares a type parameter on a concept, a model or a behaviour, and no existing backlog entry covers it.

**Goal.** Bring the notion of generics to FML, with a Java-like syntax, at three levels:
- a FlexoConcept: `concept Box<T> { T content; }`;
- a VirtualModel;
- a behaviour: `<T> T first(List<T> l)`.

Motivations: reusable typed containers and collections; a generic concept specialized by inheritance (`concept IntBox extends Box<Integer>`); typing of `select` and `match` over a type variable; reuse of modeling patterns.

The syntax must stay compatible: existing FML keeps its meaning and is not migrated.

**Open questions.** The design is part of this entry.
- Syntax details: bounds (`T extends Foo`), several parameters, wildcards, explicit type arguments at a call (`this.<String>first(l)`) versus inference only.
- Resolution: erasure as in Java, or a type that exists at run time. Is `Box<String>` a distinct concept or an instantiation of the same one? What does `new Box<String>()` create and how is it persisted?
- Typing: how Connie types a path through a type variable, inference of the type arguments, and the validation of a binding that uses `T`.
- Interaction with the existing constructs: inheritance (`extends Box<Integer>`), containment, `select … from`, `match`, behaviour overriding and overload selection, property cardinality (`T[0,*]`).
- Impacts to inventory: PAMELA metamodel of concepts and types, the pretty-print and its round trip, persistence, validation, the FML editor (completion, display), `fml-cli`, technology adapters, and the interaction with the grammar changes tracked in other entries.

**Acceptance criteria** (to be refined once the design is chosen).
- The grammar parses type parameters on a concept, a model and a behaviour, and existing FML parses as before.
- A generic concept can be instantiated with a type argument, specialized by inheritance, and its type variable is checked in bindings.
- A generic behaviour is called with inferred type arguments, and a type error is reported as an invalid binding.
- The pretty-print round trip is stable.
- A generic concept and its instances survive a save and a reload.
- `fmlscript` tests prove representative examples by running them.

### CORE-F-24 — Extend the listen behaviour to objects outside FML (resources, technology adapter objects)  ·  `TODO`

**GitHub:** [openflexo-team/openflexo-core#144](https://github.com/openflexo-team/openflexo-core/issues/144)

**Problem.** The `listen` behaviour only reacts to events of FML itself. `listen EventType from expression { … }` is implemented (`EventListener` in flexo-foundation, started by `FMLRunTimeEngine`, exercised by the `PingPong` model), but the expression after `from` must designate a `VirtualModelInstance`, and the events are `FlexoEvent`s declared in FML and raised with `fire`. FML cannot listen to a resource, nor to an object of a technology adapter, and therefore cannot react to a change that comes from outside FML.

Verified on the current `3.0` branch: `listen` is not only in the grammar, it runs between virtual model instances. The limit is the kind of object that can be listened to. No existing backlog entry covers it.

**Goal.** Extend `listen` so that a behaviour can listen to an object that is not a `VirtualModelInstance`, typically a resource or an object managed by a technology adapter, in order to track the modifications and events that come from that object, usually from outside FML.

**Open questions.** Nothing is decided yet; the design is part of this entry.
- Listened objects: which first (a resource and its life cycle, a technology adapter object such as a cell, an XML element or a diagram shape, a `FlexoConceptInstance`). A before/after FML example is to be written.
- Event mechanism: whether `listen` relies on the `PropertyChangeSupport` of the objects (`FlexoObservable` is to be deprecated). How the event is designated: a property name, an FML event type, or an event type declared by the technology adapter. Whether an adapter declares the events it emits.
- Syntax: whether `listen EventType from expression` stays valid for a non-FML object, and whether a generic "property of this object changed" event is needed. The syntax is left open.
- Life cycle: when listening starts and stops (registration of the instance, load or unload of the listened resource, release), and how leaks of listeners are avoided.
- Threading: events of an adapter often arrive from another thread (for example the directory watcher). On which thread the behaviour runs, and how events are serialized.
- Runtime errors: a failure inside a listener should follow the rules of `CORE-F-11` (`EventListenerFailedException`, reported once).
- Tooling: editor support and validation of the listened type and event.

**Acceptance criteria** (to be refined once the design is chosen).
- `listen` accepts an object that is not a virtual model instance, as decided, and existing `listen` over a virtual model instance keeps its behaviour.
- A change on the listened object, including one made outside FML, runs the behaviour with the received event.
- Listening stops when the instance or the listened object is released, and leaves no listener behind.
- A failure in the behaviour is reported as an FML error with its stack.
- The pretty-print round trip is stable.
- A technology adapter test proves a representative example by running it.

### CORE-F-25 — Declarative form of FML: inferred concepts maintained by rules  ·  `TODO`

**GitHub:** [openflexo-team/openflexo-core#145](https://github.com/openflexo-team/openflexo-core/issues/145)

**Problem.** To federate two models, FML requires writing the life cycle of the federated instances by hand, in an imperative behaviour. The two models need not be FML models: each can be a resource of any technology adapter. In the example below, `Model1` is a model of one technology adapter, whose elements of type `Foo1` can be fetched, and `Model2` a model of another technology adapter (possibly the same one), whose elements are of type `Foo2`:

    model FederatedModel {
        Model1 model1;      // model slot: a resource of a technology
                            // adapter, holding Foo1 elements
        Model2 model2;      // model slot: a resource of a technology
                            // adapter, holding Foo2 elements

        synchronize() {
            MatchingSet ms = begin match FederatedConcept from this;
            for (Foo1 foo1 : select Foo1 from model1) {
                …
            }
        }

        concept FederatedConcept {
            Foo1 foo1;      // an element of model1
            Foo2 foo2;      // an element of model2
        }
    }

The author has to say how the instances are created, matched, and deleted (`begin match`, `match … unmatched`, `end match`, called from `synchronize()`), although what is wanted is only a statement of when two elements correspond.

Verified on the current `3.0` branch: the grammar and the runtime have no declarative form. Matching exists only as the imperative statements `begin match` / `match` / `end match`, run by a behaviour called explicitly. No existing backlog entry covers it.

**Goal.** A declarative form of FML, in which the author writes rules, and the platform maintains the instances. The author does not manage the life cycle of the instances of the virtual model explicitly.

Intended shape (the syntax is a proposal, not decided), with the same `Model1` (elements `Foo1`) and `Model2` (elements `Foo2`):

    model FederatedModel {

        Model1 model1;      // a resource of a technology adapter,
                            // holding Foo1 elements
        Model2 model2;      // a resource of a technology adapter,
                            // holding Foo2 elements

        inferred concept FederatedConcept {
            Foo1 foo1;      // an element of model1
            Foo2 foo2;      // an element of model2
        }
        from (foo1 in model1, foo2 in model2)
        where (similarity(foo1, foo2) >= 0.95);

        double similarity(Foo1 t1, Foo2 t2) {
            return …;
        }
    }

An instance of `FederatedConcept` stands for a pair of elements, one `Foo1` taken in `model1` and one `Foo2` taken in `model2`, for which the rule holds. The roles `foo1` and `foo2` designate those elements.

**Open questions.** Nothing else is decided; the design is part of this entry.
- Keyword and syntax of the rule (`inferred` is the working name).
- Sources of the rule: the elements come from model slots of any technology adapter, not only from FML models. How `from (… in …)` enumerates them (the fetch requests of the adapter, as in `select Foo1 from model1`), and how the types `Foo1` and `Foo2` are resolved when the two models belong to different adapters.
- Semantics: one instance per tuple of the `from` clause that satisfies `where`. What happens when the condition stops holding or an element disappears (the instance disappears?).
- When rules apply: on demand, at each `synchronize()`, or reactively at each change of a source model. A reactive rule depends on the changes that an adapter reports (see `CORE-F-24`).
- Joins and ambiguity: an element with several candidates (similarity threshold), the choice rule (best candidate, one-to-one assignment), and elements with no match (partial instance, outer join, none).
- Scope beyond instance creation: derived properties, propagation of values between the two models, deletion rules.
- Relation with the existing engine: syntactic sugar compiled to `begin match` / `match` / `end match` and `synchronize`, or a new engine. Interaction with `CORE-F-2` (default deletion of a concept).
- Persistence: inferred instances saved with the virtual model instance, or recomputed at load.
- Expressions: the names introduced by `from (… in …)`, the call of behaviours of the model in `where`, and the alignment with the `where` of `select` and `match` (`CORE-F-17`, `CORE-F-21`).
- Impacts to inventory: grammar and pretty-print, PAMELA metamodel, validation, the FML editor, and runtime errors (`CORE-F-11`).

**Acceptance criteria** (to be refined once the design is chosen).
- The grammar parses the declarative rule, and existing FML parses as before.
- For two source models of technology adapters, the inferred instances are created, updated and deleted according to the rule, with no hand-written life cycle.
- A rule written declaratively gives the result of the equivalent imperative `begin match` code.
- The pretty-print round trip is stable.
- An `fmlscript` test proves a representative example by running it, including the disappearance of a source element.

### CORE-F-26 — Two serializations for an FML resource: single file or directory, promoted on demand  ·  `TODO`

**GitHub:** [openflexo-team/openflexo-core#146](https://github.com/openflexo-team/openflexo-core/issues/146)

**Problem.** An FML resource has a single serialization: a directory `xxx.fml/` that holds the file `xxx.fml` (and, when there is any, the resources it contains: `.fib`, `.inspector`, `Localized/`...). Even a model with nothing but its own text needs a directory and a file of the same name, which is heavy for the simplest models: more entries in the repositories, and a layout that is harder to read and to handle with ordinary tools.

Verified on the current `3.0` branch: `CompilationUnitResourceFactory` accepts as a valid artefact only a readable directory whose name ends with `.fml` and that holds its main file `xxx.fml` (`hasArtefactForm`, `hasMainFile`), and builds a directory-based IO delegate (`makeDirectoryBasedFlexoIODelegate`). A plain file `xxx.fml` is not recognized as a resource. No existing backlog entry covers it.

**Goal.** Allow an FML resource to be serialized in two forms:
- a directory `xxx.fml/` with the file `xxx.fml` and its possible content (the current form);
- a plain file `xxx.fml`, when the resource has nothing else.

The plain form can be extended to the directory form on demand, when the resource gets content of its own. The motivation is simplification.

**Open questions.** Nothing is decided yet; the design is part of this entry.
- Detection: a file and a directory `xxx.fml` cannot share a folder. The rule that tells the two forms apart, and what happens to a file `xxx.fml` found next to a directory `xxx.fml/` (conflict, priority, error).
- Promotion: when the plain form becomes a directory (at the creation of the first piece of content, by an explicit action, or both), and whether the conversion is automatic and transparent (the file moved to `xxx.fml/xxx.fml`).
- Demotion: whether a directory that has only its unit left goes back to a plain file, or the directory form is final once created.
- Default form of a new resource, and whether existing resources are migrated or left as they are.
- Impacts to inventory: the IO delegate and the detection of a valid artefact, the identity of the resource (its URI must not change), the loading of contained resources, saving and renaming, resources in a jar (`DirectoryBasedJarIODelegate`), the directory watcher, the tools (`fml-cli`, the editor, importers), the resources of technology adapters, and the tests.
- Compatibility: existing projects in directory form stay readable; whether a client that only knows the directory form must be able to open a project that uses the plain form.

**Acceptance criteria** (to be refined once the design is chosen).
- A plain file `xxx.fml` is loaded as an FML resource, with the same URI and the same behaviour as the directory form.
- Adding content to a plain-file resource turns it into the directory form without changing its identity or breaking references to it.
- Existing directory-form resources load and save as before.
- A resource of either form is correctly found, loaded, saved, renamed and unloaded from a directory resource center and from a jar.
- Tests cover both forms and the promotion.

### CORE-F-27 — Textual serialization of FML instances (FML@RT) in FML syntax, replacing .fml.rt.xml  ·  `TODO`

**GitHub:** [openflexo-team/openflexo-core#147](https://github.com/openflexo-team/openflexo-core/issues/147)

**Problem.** FML instances (a `VirtualModelInstance` and its `FlexoConceptInstance`s) are stored in `xxx.fml.rt/xxx.fml.rt.xml`, a PAMELA XML serialization (`FMLRTVirtualModelInstanceResource`, `FMLRTVirtualModelInstanceModelFactory`). It cannot be read, diffed or edited by hand: one element per `ActorReference`, instances known only by their `flexoID`, references written as `objectReference="…lineL2.fml.rt#FLX_2"`, and about twenty technology-specific reference kinds (`XMLObjectActorReference`, `XLSObjectActorReference`, `ModelObjectActorReference`…), each with its own attributes. Types are written in textual FML while their instances are not.

**Goal.** Serialize FML instances in FML syntax, in files that hold instances only (the types stay in the `.fml`). The same grammar (`fml-parser/src/main/sablecc/fml.sablecc`) is used, with only what is needed added: an entry point for an instance unit, an instance literal, and `:=` to value a property, a role or a model slot. It is independent of `CORE-F-7` (clabjects), which it blocks: an `instance` is a clabject at the leaf, so this entry must close no door to `CORE-F-7` without anticipating it beyond that. Sketch on `digital-twin-demo` (`INSTANCES/lineL2.fml.rt`):

```fml
instance lineL2 : ProductionLine {
    ligneXml    := XML::Model(uri="http://openflexo.org/digital-twin-demo/production-line.xml");
    maintenance := FMLRT::Reflected(uri="http://openflexo.org/digital-twin-demo/maintenance.xlsx");

    instance DEP01 : Equipment #FLX_6 {
        xmlElement    := XML::Ref(path="/0/1");
        assetRecord   := maintenance.AssetRecord[5];
        readings      := maintenance.Reading[2, 9, 15, 17, 24, 30, 32, 39, 45, 48];
        interventions := maintenance.Intervention[4];
    }
    instance link1 : MaterialLink #FLX_80 {
        source := EQ2;
        target := EQ3;
    }
}
```

**Proposed (to confirm in the design).**
- Literals and references only in an instance file: nothing is evaluated at load time.
- A readable name per instance for references, plus a stable identifier (`#FLX_6`) since other resources (diagrams, FME) point to FCIs by `flexoID` / URI. A name generated from a key property is only a label: a derived key may be known only once a model slot is bound.
- A generic reference form `Slot::Ref(args)`, parsed as is and handed over to the technology adapter, so that the grammar does not change for each adapter.
- A two-pass resolution for forward references, as the parser already does for types (`unresolvedTypes`).
- Files owned by the machine: formatting and comments are not preserved when the run time rewrites them.

**Open questions.**
- Inventory of what a `.fml.rt.xml` really holds: VMI, FCIs, all `ActorReference` kinds of all technology adapters, model slot instances, containment, enum values, multiple roles, `JavaActorReference` (how to write an arbitrary Java value as text).
- Grammar entry point (next to `CustomLexer.EntryPointKind` CompilationUnit / Binding / Command / Script), read and write paths, where each technology adapter contributes its reference literal.
- Coexistence with the XML form during the migration (read both, write text only?), and the converter of existing files, on the model of the `.fml.xml` → FML converter (which only runs in file-based resource centers, not in jars).
- Performance: parse and semantic analysis against the PAMELA XML reader, on a VMI with thousands of instances.

**Acceptance criteria** (to be refined once the design is chosen).
- Every `.fml.rt.xml` of the workspace (11 under `src/`, in `digital-twin-demo` and `multi-process-challenge` among others) converts to the textual form and back without loss.
- The tests of `digital-twin-demo` and `multi-process-challenge` stay green when reading the converted files.
- Other resources still resolve their references to FCIs after conversion.
- Saving an instance writes the textual form; a project in the XML form stays readable.
- A measure of load time on a large VMI is recorded, compared with the XML form.

**Related.** Blocks `CORE-F-7`; design and examples in [`design/clabjects.md`](./design/clabjects.md) §7.

### CORE-F-29 — Error-tolerant and incremental parsing and compilation of FML  ·  `TODO`

**GitHub:** [openflexo-team/openflexo-core#149](https://github.com/openflexo-team/openflexo-core/issues/149)

**Problem.** The FML parser analyses a whole file or nothing. A single syntax error aborts the analysis: the compilation unit that results is empty, and a unit with nothing in it reports no validation error at all. The rest of the file, which is correct, is lost for every tool that needs it (the editor, the completion, the language server). And each change makes the whole file to be parsed and compiled again, even when only one method has changed.

Verified on the current `3.0` branch: the parser is generated by SableCC from `fml.sablecc` and works on a whole file; a `ParserException` stops the analysis and leaves an empty compilation unit. `fml-parser` has `FMLCompilationUnitParser`, `FMLExpressionParser`, `DetachedBindingAnalyzer` (analysis of a binding on its own) and `FragmentManager` (pretty-print that keeps the text); the editor parses the text again and merges the result into the model with `updateWith()`. No existing backlog entry covers error-tolerant or incremental parsing.

**Vocabulary** (to settle the terms of this entry).
- Error-tolerant (resilient) parsing: produce a usable tree in spite of a syntax error, by resynchronizing after it.
- Incremental parsing: analyse again only the part of the text that changed.
- Incremental compilation: redo the semantic analysis (typing, binding resolution) only for what changed.
- Island parsing (structure first): recognize first the skeleton of a file (declarations, boundaries of the methods), then analyse the body of each piece on demand.

**Goal.** Parse quickly the structure of an FML file so that the compiler can be used on a single piece (a method, for example), and so that the parts of a file that are correct can still be used when another part has a syntax error. This also opens the way to an optimization of the compilation: only what has changed is compiled again.

**Open questions.** Nothing is decided yet; the design is part of this entry.
- Priorities: error tolerance (editor, completion, language server), compiling a single piece, or the optimization by partial recompilation; and in which order.
- Mechanism: error recovery in the SableCC grammar, a first pass for the structure and an analysis of each body on demand, a replacement of the parser generator, or another approach.
- Error model: whether a piece in error appears in the model (a node that holds its text and its messages) so that the rest stays usable, and what `updateWith()` does then.
- Impacts to inventory: grammar and generation, `FragmentManager` and the pretty-print, the PAMELA model (error nodes), validation (an error node must not be validated as correct code), `updateWith()`, the editor, the completion (`UI-F-5`), the language server `fml-lsp`, the loading of resources, and performance (to be measured before the work).

**Acceptance criteria** (to be refined once the design is chosen).
- A file with a syntax error in one method gives a model that still holds the other declarations, and the error is reported with its position.
- A single method can be parsed and compiled on its own.
- A change in one method does not compile the others again, and this is shown by a measure.
- A file without error gives the same model as before, and the pretty-print round trip is unchanged.
- Tests cover a file with errors at several places, and the editor and the language server keep working on it.

---

## Container-based user interfaces

### CORE-F-4 — Renaming a concept orphans the components named after it  ·  `DONE`

**GitHub:** [openflexo-team/openflexo-core#122](https://github.com/openflexo-team/openflexo-core/issues/122)

**Problem.** A concept finds its user interface and its inspector in the `Xxx.fml/` container of its VirtualModel by
naming convention: `<ConceptName>.fib`, `<ConceptName>.inspector` (`FlexoConcept.getUIComponentResource()` /
`getInspectorComponentResource()`). Renaming the concept renames neither file, so the concept silently loses both, and
the files stay in the container, driven by nothing. A component named by an explicit `@UI("…")` / `@Inspector("…")`
annotation survives the rename.

**Why it matters now.** The free modelling editor generates one `.inspector` per concept it creates, by convention
(`FMEInspectorGenerator`). FME itself offers no rename, but the FML editor does, on any concept.

**Resolution (2026-09-29).** The naming convention is kept only as a permanent, read-only fallback for components
created before this change; going forward, resolution is meant to rely on the annotation alone.
- `CreateFIBComponent` / `CreateInspector` (openflexo-ui/fml-gina-extension) now ALWAYS write an explicit
  `@UI`/`@Inspector` annotation for a component they create - the default variant when the wizard's "declare as a
  named variant" box is left unchecked, the chosen key otherwise - instead of only when that box was checked. A
  component created from now on can never be orphaned by a later rename.
- `FlexoConcept.freezeConventionalUIComponentNames()` (flexo-foundation) is the fixup for everything created
  before this change: called by both rename actions - `RenameFlexoConcept` (a nested concept) and
  `RenameCompilationUnit` (the VirtualModel itself, which IS a FlexoConcept and gets its own view the same way) -
  BEFORE the name actually changes, it writes an explicit annotation naming whatever the naming convention alone
  currently resolves, and is a no-op for a concept that already declares one. Covered by
  `TestContainerUIComponents.test10FreezeWritesAnAnnotationOnlyWhereTheConventionResolves` (the mechanism) and
  `TestRenameFlexoConcept` (end to end, both actions).
- **Known gap, out of scope:** `fml-lsp`'s `RenameProvider` renames by pure text substitution and never touches the
  model, so a rename issued through the LSP does not go through the freeze either.
- **Test note, not a defect:** `TestRenameFlexoConcept`'s `RenameCompilationUnit` case only asserts the freeze,
  not the action's overall success. `TestContainerUI.fml` is loaded from `flexo-test-resources`' packaged JAR
  (as documented in this repo's `CLAUDE.md`), and `InJarIODelegateImpl.hasWritePermission()` always answers
  false, so `FlexoResourceImpl.setName()` correctly refuses to rename it - `DirectoryBasedIODelegate.rename()`
  (where the earlier draft of this note wrongly placed the cause) is never even reached. The freeze already ran
  by then regardless, which is the only thing CORE-F-4 depends on.

**Acceptance criteria.**
- After renaming a concept that has a conventional `.fib` and `.inspector`, both still resolve, from memory and after
  reloading. — met via the freeze, for any rename that goes through `RenameFlexoConcept`/`RenameCompilationUnit`.
- No component is left in the container that nothing resolves to. — met the same way.

---

## User interface

### CORE-F-6 — Additive inspector inheritance along the FlexoConcept hierarchy  ·  `DONE`

**GitHub:** [openflexo-team/openflexo-core#123](https://github.com/openflexo-team/openflexo-core/issues/123)

**Problem.** The inspector of a concept is a `Xxx.inspector` component of its container. Resolution
(`FlexoConceptImpl.getContainedComponentResource`) kept ONE component — the concept's own, else its most specialized
parent's — so a concept with an inspector hid its ancestors', where the deprecated `FlexoConceptInspector` mechanism
(`appendInspectorEntries`) recursed on the parents first. In formod, 12 concepts declare an inspector while an ancestor does
too (`FunctionalGoalGR` lost the goal name and description of `GoalGR`).

**Decision (2026-10-08).** Additive: an instance shows the inspectors of its whole hierarchy, ancestors first, then its
own; the `index` of the widgets orders them, and the tabs of the same `name` are merged (see Known limits for the tab rules).

**Done.**
- `FlexoConcept.getOwnInspectorComponentResource()` / `…FlexoResource()` (what a concept contributes by itself),
  `getInspectorComponentResources()` / `…FlexoResources()` / `getInspectorContributingConcepts()` (the hierarchy, most
  general first, each ancestor once, several parents walked depth first). `getInspectorComponentResource()` keeps its
  meaning ("the" component) and its Javadoc says how the two differ.
- `ModuleInspectorController.mergeContainerInspectors`: a single component is merged as before; several are composed into
  tabs by `FIBContainer.append`, each widget bound to the instance's concept; a widget named like an ancestor's
  replaces it. The cache (`containerComponentsOfInspectors`, `isStale`, `listenToContainerComponent`,
  `dropInspectorsBuiltFrom`) tracks the components of the whole hierarchy: saving an ancestor's `.inspector` drops its
  descendants' cached inspectors.
- GINA: `FIBContainer.append` did not honour its own documented policy. A widget with no index was put BEFORE the
  existing ones, and on equal positive indexes the appended widget came BEFORE the existing one — a descendant ended up
  before its ancestor. Now: no index = after the last unindexed/negative one; equal index = after the existing one.
  `TestAppendIndexOrdering` (gina-core).
- Index convention: label and widget share `depth * 100 + position`; documented in `migrate-fml-serialization` (Step 2e)
  and `write-gina-component`. `legacy_inspectors_to_container.py` writes it, `index_container_inspectors.py` retrofits
  existing files; the 52 inspectors of formod-rc were retrofitted.
- Tests: `TestContainerUIComponents` (resolution), `TestContainerInspectors` (composition, bindings, cache),
  `TestAppendIndexOrdering`, `FormodInspectorsTest`; fixture `Insp*` concepts in `TestContainerUI.fml`.

**Known limits.**
- Tabs: a component declaring SEVERAL tabs contributes each to the tab of the same `name` (merged, the most specialized
  `title` wins; a tab only one side has stays a tab of its own, ancestors' tabs first); a component declaring ONE tab or a
  plain panel (the generated inspectors) contributes to the FIRST tab, whatever its name. So a single-tab inspector that
  is meant to stay a separate tab next to its parent's cannot be expressed. Covered by `TestContainerInspectors` (21–25).
- Cloning a component rewrites the index of an unindexed widget that follows an indexed one in the file
  (`FIBContainer.updateComponentIndexForInsertionIndex`, an editing aid): write the widgets in sort order.
- `FIBContainer.reorderComponents` compares an index of 0 as equal to "no index", an inconsistent comparator.

**Verified** in the running application (formod, SysML/KAOS methodology, a FunctionalGoal selected), 2026-10-08.

## Technical debt

### CORE-F-8 — Remove the XML serialization of VirtualModels and the deprecated FML API  ·  `DONE`

**GitHub:** [openflexo-team/openflexo-core#124](https://github.com/openflexo-team/openflexo-core/issues/124)

**Problem.** 3.0 no longer supports the `.fml.xml` serialization of VirtualModels, but flexo-foundation still carries it:
the XML load/save path of `CompilationUnitResourceImpl` (`PersistencyStrategy`, default `XML2FML`), `XMLToFMLConverter`,
the edition actions only the XML could produce (`AddFlexoConceptInstance`, `AddToListAction`, `IncrementalIterationAction`…),
the `_get/_set` accessors holding cross-references as URI strings (`_getFlexoConceptTypeURI`, `_getParentFlexoConceptsList`…,
also in flexo-ontology), about 120 other `@Deprecated` members, `@FMLMigration`, and the `@XMLElement`/`@XMLAttribute`
annotations of the definition classes (in core and in every technology adapter). `fml/rt` keeps its XML: `.fml.rt` instances
are still serialized that way.

**Phases.** 1. test fixtures and migration tests; 2. cut the XML path; 3. remove the XML-only edition actions and their UI;
4. remove the URI-string accessors; 5. other `@Deprecated`, family by family (renames last); 6. remove the XML annotations of
the definition classes; 7. remove `@FMLMigration`. Each phase compiles in the openflexo-dev composite and keeps the tests green.

**Phase 1 (2026-11-08).** No `.fml.xml` fixture was worth migrating. Deleted: the migration tests (`TestFMLMigration` in core
and diagram-ta, `TestMigrateEditionActions`, fixture `MigrationVM`), the `*XMLVersion` tests and fixture of diagram-ta, the
duplicates `TestControlledDiagramViewPoint2` and `TestViewPointA` (with `TestFMLEditorWithDiagramTA` and the two "FromJar"
steps of `TestDiagramFeaturesBindingModelManagement`: their jar dependency is commented out), and the docx fixtures
`TestLibraryViewPoint`/`TestLibraryViewPoint2`, which no longer loaded at all. Tests repointed to the textual models
`TestControlledDiagramViewPoint` and docx `FML/TestLibrary.fml` (that one revealed DOCX-D-2).

**`FMLClassDiagram` migrated (2026-11-08).** The model of fml-diagramming-extension (used by the Modeller) is textual FML; the
XML version loaded but carried 19 validation errors, so the feature could not work. Now proven by
`TestFMLClassDiagramVirtualModel` (load, 0 validation error, instantiation, representation of a concept and its properties,
creation of a concept from the palette). It needed CORE-D-41 and CORE-D-42; the role `concept` is now `flexoConcept`.

**Last `.fml.xml` removed (2026-11-08).** Deleted with their unparseable 2022 pretty-print, none being used by a test:
xlsx-ta-test-rc `RootVM`/`PersonListing`, the copy of `TestXXVM` in capella-ta-test-rc, `XXX`/`TestVM` of the unbuilt
`xxxmodule` (its `.fib` and `XXXCst` still name `http://openflexo.org/xxxmodule/XXX.fml`, to rewrite if the module is revived).
`TestXXVM` of the TA template xx-ta-test-rc is rewritten in textual FML and run by `AutomatedTests/TestXXVM.fmlscript`.
No `.fml.xml` is left in the workspace: phase 2 can start.

**Phase 2 (2026-11-08).** The XML path is cut: `CompilationUnitResourceImpl` reads and writes textual FML only
(`PersistencyStrategy`, the XML load/save, the metadata read from the XML root, `doesIODelegateExist` special case removed),
`XMLToFMLConverter`, `FML_XML_SUFFIX` and `FMLObjectImpl.IS_CONVERTING_FROM_XML` are deleted. A `Xxx.fml/` directory holding only a
`Xxx.fml.xml` gives no resource, and `CompilationUnitResourceFactory.isIncompleteArtefact` logs a warning naming it (not covered by a
test: no such fixture is left).

**Phase 3 (2026-11-08).** The edition actions only the XML could produce are deleted, with their factory methods, `@Import`s,
editor entries, icons, inspectors, documentation pages and validation key: `AddFlexoConceptInstance`, `AddVirtualModelInstance`,
`CreateTopLevelVirtualModelInstance`, `AbstractAddFMLRTVirtualModelInstance`, `ExecuteFML`, `FireEventAction`, `AddClassInstance`,
`AddToListAction`, `RemoveFromListAction`, `IncrementalIterationAction` (the textual equivalents are `new X(...)`, `fire`,
`list.add(x)` / `list.remove(x)` and `for (int i = ...; ...; ...)`). Decided with the user: the editor's "add to list" (creation
wizard and `AddToAction`) is kept and now writes `list.add(value)` (`AssignableAction.addToList`: an expression is rewritten in
place, any other action is first declared as a variable `valueToAdd`), proven by `TestControlGraph.testAddToList`; the REST routes
`add-to-list` / `remove-from-list` of http-server are removed (their tests were already commented out). The two diagram-ta
strategies mapping a shape or connector to a concept instance now write `<vmi>.new Concept()`. The fml-parser nodes still named
`AddFlexoConceptInstanceNode`, `AddVirtualModelInstanceNode`, `AddClassInstanceNode` build `CreationSchemePathElement`s: a rename
left for phase 5.

**Phase 4 (2026-11-09), core only.** The `_get/_set` URI-string accessors of flexo-foundation are removed. Decided with the user: a
reference the textual FML carries becomes a typed PAMELA property, and flexo-ontology / owl-ta keep theirs for now (there the string is
the storage, and `ClassRole.conceptURI` / `AbstractSelectIndividual.ontologyClassURI` are FML syntax).
- Measured first: when the FML editor merges the text it has just parsed (`updateWith()`), the strings did not carry the references;
  most of them were already lost or pointed into the parsed compilation unit (CORE-D-43). `TestUpdateWithReferences` now pins every one.
- A concept is carried by a `FlexoConceptInstanceType` property whose `@Updater` translates it into the current typing space: the type
  of a role (already so), of the `select`s (`fetchedType`, now also routed for `select` of VirtualModel instances), of `begin match` /
  `match` / `end match` (`matchedType`, now a PAMELA property), of a listener (`eventType`), the parents of a concept
  (`parentFlexoConceptTypes`; `parentFlexoConcepts` is now derived for the merge), and the instantiated type of `NewFlexoConceptInstance`
  and the parent of `CreateFlexoConcept`.
- A behaviour, a parameter or a property is carried by `FMLNamedReference` (new): an immutable reference by name, compared by name,
  resolved by its holder in its own context (the matched concept, the called behaviour), the `@Updater` keeping the name only. Used for
  the criteria of a `match`, the behaviour and the arguments of an `end match`, the creation scheme of `NewFlexoConceptInstance`, the
  deletion scheme of `DeleteFlexoConceptInstance`, and the parameter of the four argument classes.
- Left out: the deprecated creation scheme of `FlexoConceptInstanceRole` is a plain field (no FML syntax, for phase 5); the URI property
  of jdbc-ta `AbstractPerformSQLQuery` and those of flexo-ontology / owl-ta (to do with the ontology rework); the behaviour of
  `end match ... unmatched: someMethod(...)` is parsed since CORE-D-2 was fixed (2026-11-12): the test no longer sets it.
- For phase 6: PAMELA calls a property "serializable" only when it carries `@XMLAttribute`/`@XMLElement`
  (`ModelProperty.isSerializable()`), and that decides the list matching of `updateWith()` (`CompareAndMergeUtils.getDistance`) and
  the "modified" marking. The XML annotations cannot be removed before this definition changes.

**Phase 5 (2026-11-09), other `@Deprecated`.** Checked caller by caller across the workspace first: most of the deprecated API is
in fact alive. Decided with the user: remove the dead ones, the old GUI hints of a behaviour, the use declarations of `VirtualModel` and
the old inspector model; list the alive ones (below) for a decision item by item; renames last.
- Dead, removed: `RolePropertyValue`, the `CreateActionScheme` entity of `fml/ta` (never declared), `SynchronizationScheme`'s
  `synchronizedVirtualModel`, `NotifyPropertyChangedAction.staticPropertyName`, `ModelSlot.getFMLRepresentationForConformToStatement`,
  commented-out leftovers of fml-parser.
- GUI hints with no FML syntax (only the XML carried them), removed: `FlexoBehaviour.longRunningAction` / `stepsNumber` (and
  `LongRunningActionSchemeAction`), `definePopupDefaultSize` / `width` / `height`, `AbstractActionScheme.conditional` (superseded by
  `@Enable`), with their widgets and dictionary keys. A long-running action scheme, if wanted again, is an annotation to design (UI-F-2).
- `VirtualModel.getUseDeclarations` / `getAccessibleUseDeclarations` / `uses` / `declareUse` and the adder/remover removed: the use
  declarations live in `FMLCompilationUnit`; the three internal callers moved there.
- The old inspector model removed: `FlexoConceptInspector`, `InspectorEntry`, their binding models, `FlexoConcept.getInspector` /
  `getOrCreateInspector` / `getApplicableInspector`, the `CreateInspectorEntry` action and wizard, the "Define inspector" step of the
  concept-creation wizards (`defineInspector`, `performCreateInspectors`), the legacy tab of `ModuleInspectorController` (a concept
  without a container `.inspector` now shows the basic inspector only), the inspector nodes of the FML browsers. The entries that
  concept creation used to generate (core, diagram-ta, jdbc-ta, gina-ta) were never saved; creating an inspector at creation time
  again is CORE-F-9. fml-gina-extension's widget scaffolding is a detached `FlexoBehaviourParameter` instead of an `InspectorEntry`.
- Alive, kept `@Deprecated` until decided (callers outside the declaring file, measured 2026-11-09):

| API | Users | What it is |
|---|---|---|
| `FlexoObject.addActionForClass` / `removeActionFromClass` | ~150 files, every repository | the registration of FlexoActions on a class |
| `TechnologySpecificActionDefiningReceiver` (receiver) | ~90 files, every TA | the receiver of TA edition actions (`in (x)`) |
| `FlexoConceptInstance.getModelSlotInstance(...)` | ~60 files | access to the model slot instances of an instance |
| `AbstractCreateResource.relativePath` | ~50 files | static relative path of a created resource (`dynamicRelativePath` exists) |
| `CreationSchemePathElement.virtualModelInstanceTitle` | ~60 files | title of a created VirtualModel instance |
| `VirtualModel` / `FMLCompilationUnit.getVirtualModelNamed`, `FMLCompilationUnit.getFlexoConcept(String)`, `getObject` | ~45 files | lookup by name or URI |
| `VirtualModel.getCompilationUnitResource` | ~70 files | the resource of a VirtualModel |
| `FlexoConcept.getFlexoBehaviour(String)` | ~40 files | behaviour lookup by name |
| `SynchronizationScheme` (the class) | ~30 files | the `synchronize` behaviour of a VirtualModel |
| `ExecuteFlexoBehaviour`, `DeleteFlexoConceptInstance` (the classes) | FML syntax | `FMLRT::ExecuteFlexoBehaviour(...)`, `delete` |
| `VirtualModel.modelSlotNatureClass` | jdbc-ta | gives `CreateJDBCVirtualModel` an `HbnVirtualModelInstanceType`; not in FML text, lost on reload |
| `FlexoConceptInstanceRole.creationScheme` | core | plain field since phase 4, fallback of `NewFlexoConceptInstance` |
| 8 binding classes (`FlexoBehaviourParameter*PathElement`, `*ParameterBindingModel`, `FlexoConceptInstancePathElement`), `FMLBindingFactory` constants | core | the implementation of `parameters.x`, `parametersDefinitions`, `flexoConceptInstance` |
| `KVCFlexoObject`, `DefaultFlexoObject` | `FlexoObservable`, `RepositoryFolder`, `VirtualModelLibrary`, ontology, pdf-ta | base classes, to become PAMELA |
| `DirectoryContainerResource.get/setDirectory`, `ModelSlot.makeFlexoRole` / `getModelSlotName`, `CompilationUnitResource.setUsedModelSlots` | 5-10 files each | resource and model slot plumbing |
| fml-parser `getCompilationUnitAnalyzer` | 79 uses | the analyzer of the unit being parsed |
| flexo-ontology `isNamedClass` / `isRootConcept`, `AddIndividual` URI | ontology TAs | with the ontology rework |

**Phase 5 renames (2026-11-09).** The renames the `@FMLMigration` markers asked for, markers removed:
`FlexoBehaviourParameter` → `FlexoBehaviourArgument` (core, ui, and every TA using it; its inspector is now
`Inspectors/FML/FlexoBehaviourArgument/`), `JavaRole` → `FMLVariable` (its `@FML` keyword too; nothing in a `.fml` names it, a variable
is written `Type name;`), `JavaActorReference` → `FMLVariableActorReference` (still `.fml.rt` XML: `JavaActorReference` is kept as a
deprecated XML tag so older instances load), and the fml-parser nodes `Add{FlexoConcept,VirtualModel,Class}InstanceNode` →
`New…InstanceNode`. Kept as they are: the names derived from the parameters collection (`FlexoBehaviourParametersType`,
`FlexoBehaviourParameter*PathElement`, `ConfigureFlexoBehaviourParameters`, `createFlexoBehaviourParameter`…), the XML tag
`GenericBehaviourParameter`, and `FMLDataBindingRole`'s `@FML("JavaRole")`.

**Phase 6, measured before deciding (2026-11-09).** Candidate rule for FML definitions: a property counts for "modified" and for the
matching of `updateWith()` when it is neither derived nor inverse nor marked transient, instead of when it carries an XML annotation.
Measured with the rule behind a flag (temporary code, removed): 2590 definition properties are XML-marked, 964 others are neither
derived nor XML-marked. Over the 16 usual suites only one test changes: `TestBehaviourAvailability.test5` — `FMLObject.metaData` (the
annotations) now weighs in the matching, a reorder is attempted and PAMELA fails because the property has no `@Reindexer`. 48 properties
are set at least once under the rule; sorted:
- transient (print / parse helpers, caches): `prettyPrintDelegate` (128 entities), `FMLPropertyValues`, the `object` / `property` /
  `value` / `instances` / `enum` of the `FMLPropertyValue`s, `unresolvedPropertyName`, `flexoResource`, `accessedVirtualModel`,
  `roleName` (an alias of `name`), `serializationRepresentation` (to check);
- parent pointers without a declared inverse (to exclude like inverses): the `owner` of meta-data and of call arguments;
- data, so far ignored by "modified" and by the matching: `type`, `modelSlot`, `key`, `valueExpression`, `matchedType`,
  `fetchedType`, `metamodelElement`, `label`, `targetType`, `fromType` / `toType`, `eventType`, the typed references of phase 4
  (`behaviourReference`, `propertyReference`, `paramReference`, `creationSchemeReference`), `parentFlexoConceptTypes`, and the values
  of TA annotations set through `FMLModelContext$FMLProperty.set` (`ontologyClass`, `objectProperty`, `call`, `transparency`, …).
On loading, the rule marks nothing modified (the load resets the status); printing marks 2 more resources of 22 because of the
`FMLPropertyValue` helpers. Validation and printing already mark resources modified without the rule (CORE-D-45).

**The same rule on every PAMELA model, measured (2026-11-10).** Proposed by the user: persistent = not derived and not `@Transient`
for all PAMELA models, the XML annotations keeping only the mapping role. ("Not inverse" is not needed: PAMELA already makes the single
side of an inverse pair derived.) 28 suites (pamela, pamela-editor, gina, diana non-UI, core, ui, 7 TAs, formod, FME), all green
without the rule. With it: pamela, pamela-editor, gina, diana, pptx, owl, formod, FME, fml-parser, fml-cli, ui, http-server unchanged;
red: docx 25, xlsx 17, jdbc 12, flexo-foundation-test 5, xml 2. Cause verified for xlsx and jdbc: models built by a converter from a
native file (POI rows and cells, JDBC lines) are not deserialized by PAMELA, so every setter called while building them now marks them
modified, and `setIsModified` climbs `getResourceData()` through a parent not set yet (`ExcelCell.getExcelSheet()`,
`JDBCLine.getResultSet()`) -> NPE. In core: reading a localized dictionary marks it modified; `TestPopulateVirtualModelInstance` not
analyzed; docx and xml not root-caused. Census over the 933 entities loaded: 7765 XML-mapped properties, 609 derived, about 3100 neither
(core 950, gina 243, diana 60, pamela 47, TAs 1800). The properties set under the rule are mostly runtime links: the
`serviceManager` / `factory` / `resourceCenter` / `technologyAdapter` of every resource, the `flexo_resource` of IO delegates, the
native handles of TA models (`ExcelRow.row`, `ExcelCell.cell`), GINA's `controllerClass` / `customTypeManager` / `fibLibrary` /
`parent`, DIANA's mouse controls. Note: an `@XMLElement` on an entity does not mean it is saved in XML (many TA model entities carry one
and are rebuilt from their native file).

**Phase 6, batch 1 (2026-11-10): flexo-foundation.** Persistence no longer depends on the XML annotations (PAMELA-F-3, the rule is
the only one), so they are removed from the FML definitions. Which classes are definitions was decided by a probe, not by package:
the FML-RT meta-model contains nearly every definition (PAMELA follows `getFlexoConcept()` and the other references), so the criterion
is "reachable from the runtime roots (the VirtualModelInstance and the actor references of every model slot) through XML-mapped
properties only". In flexo-foundation that gives 98 definition interfaces (243 annotations removed) and leaves only `FlexoObject`
shared with the `.fml.rt`; the actor references and model-slot instances keep their mapping, wherever their package. PAMELA now only
requires distinct XML tags between XML-mapped entities (the tag of an unmapped one falls back on its simple name and may clash).
`FMLObject.getStringRepresentation()` defaults to "interface name + name" instead of an XML dump; the debug prints of
`DuplicateVirtualModel` and the XML dumps of definitions in the tests are gone (the tests print the FML instead). Batch 2: the
technology adapters, formod, flexo-ontology and flexo-documentation (their probe runs where their adapter is loaded).

**Phase 6, batch 2 (2026-11-10): the technology adapters.** The same probe, run in the test module of each of the 23 adapters with
every adapter on its classpath activated, roots extended to every instance, actor reference and model-slot instance type (the
reflected VirtualModelInstances of xlsx, xml and rest included): 272 definition files (model slots, roles, edition actions, fetch
requests, URI processors), plus 21 definition classes no meta-model reaches (dead roles and actions); 540 annotations removed, in
flexo-ontology, flexo-documentation, formod's b-ta and 21 adapter repositories. Kept: every actor reference, model-slot instance and
reflected instance, and the adapters' own models (none of them points to a definition through an XML-mapped property).

**Phase 7 (2026-11-11): `@FMLMigration` removed.** 31 markers were left, all on members kept for the XML format. Decided with the
user: remove the members, not only the marker.
- diagram-ta: `DropScheme`, `LinkScheme` and `DrawRectangleScheme` stored their targets twice, as the typed types the FML attributes
  show (`targetType`, `fromType` / `toType`, `childrenType`) and as URI strings (`target`, `fromTarget` / `toTarget`, `children`). The
  strings and their `_get/_set` accessors are gone; `getXxxTargetFlexoConcept()` / `getChildrenFlexoConcept()` are now plain
  conveniences derived from the type (no longer deprecated), their setters set the type, and changing the type fires the concept event the
  binding models listen to. `DrawRectangleScheme.isTopTarget()` read the string, so a scheme declaring a `targetType` still counted as
  drawing at top level: it is now "no target type". Unchecking "top level" in its inspector starts from the concept declaring the scheme.
  FME built behaviour bodies with the removed property names as variable names: it now uses the binding-model constants
  (`DropSchemeBindingModel.TARGET`, `LinkSchemeBindingModel.FROM_TARGET` / `TO_TARGET`).
- diagram-ta `ShapeRole.parentShapeRole` is not a leftover but the API the wizard and inspectors use over `containerElement`: kept,
  markers and `@Deprecated` removed, and it is no longer a PAMELA property (it was persistent under PAMELA-F-3, duplicating
  `containerElement`).
- flexo-ontology `AddIndividual._getOntologyClassURI` removed (the class is the typed `ontologyClass`, else the role's type).
- `FMLMigration` itself is deleted.

**Done (2026-11-11).** Checked against the criteria below: no `.fml.xml` left in the workspace, `XMLToFMLConverter`,
`PersistencyStrategy`, `FML_XML_SUFFIX` and `FMLMigration` gone; the touched modules compile with javac and with ECJ (pamela-core,
flexo-foundation, flexo-ontology, diagram-ta, diagram-ta-ui, fme-diagram: no error); the 28 usual suites and the 15 other adapter test
modules give the results they gave before (CAPELLA-D-1 apart). Still open beside it: CORE-D-45.

**Acceptance criteria.** No `.fml.xml` read or written by the platform; `XMLToFMLConverter`, `PersistencyStrategy`,
`FMLMigration` gone; core and touched repositories compile (javac and ECJ) with unchanged test results.

### CORE-F-9 — Create the inspector of a concept when creating the concept  ·  `TODO`

**GitHub:** [openflexo-team/openflexo-core#125](https://github.com/openflexo-team/openflexo-core/issues/125)

**Problem.** The concept-creation wizards had a "Define inspector" step that built a `FlexoConceptInspector` with an entry per chosen
property. That model had no FML syntax, so the inspector was lost at the first save, and it was removed with it (CORE-F-8 phase 5). The
inspector of a concept is now a `Xxx.inspector` GINA component in the `Xxx.fml/` container, written by fml-gina-extension's
`CreateInspector`, which core and fml-technologyadapter-ui cannot call (it depends on them).

**Options.** A post-creation hook that a UI module contributes (fml-gina-extension runs `CreateInspector` with the chosen properties);
or the wizard step moves to fml-gina-extension.

**Acceptance criteria.** Creating a concept (core, diagram-ta, jdbc-ta, gina-ta wizards) can produce its container `.inspector`, saved
with the model and shown in the running application.

### CORE-F-16 — `FlexoBehaviourAction` should not implement `RunTimeEvaluationContext`  ·  `TODO`

**GitHub:** [openflexo-team/openflexo-core#4](https://github.com/openflexo-team/openflexo-core/issues/4)

**Problem.** Analysed as a design issue (for example for the management of the DropScheme in the Diagram technology adapter).
`FlexoBehaviourAction` still declares `implements RunTimeEvaluationContext` (checked 2026-11-17, branch 3.0).

**Notes.** Since `CORE-F-11` the execution contexts are `STRICT` (`FlexoBehaviourAction`, `LocalRunTimeEvaluationContext`, the interpreter of fml-cli), which
is a dependency to take into account.

**Original report** by `sylvain-openflexo`, 2022-12-09 (issue openflexo-core#4):

> This has been analyzed as a design issue (for example for DropScheme management in Diagram TA) and should be refactored.

### CORE-F-19 — A single container/contents association for FlexoConcepts  ·  `DONE`

**GitHub:** [openflexo-team/openflexo-core#132](https://github.com/openflexo-team/openflexo-core/issues/132)

**Problem.** A nested FlexoConcept is held by two PAMELA compositions, both `@Embedded` and `@CloningStrategy(CLONE)`:

- `VirtualModel.flexoConcepts` ↔ `FlexoConcept.owner`: a flat list of all the concepts of a compilation unit, nested ones included.
  The parser fills it after the fact (`VirtualModelNode.bindEmbeddedFlexoConcepts`, itself marked deprecated), and
  `CreateFlexoConcept` adds every new concept to it.
- `FlexoConcept.embeddedFlexoConcepts` ↔ `FlexoConcept.containerFlexoConcept`: the nesting.

As a result:
- The PAMELA closure, cloning, deletion and notifications are doubled.
- Every adder and remover fires hand-written notifications for both sides (`allRootFlexoConcepts`, `InnerConceptsFacet`).
- `getAllRootFlexoConcepts()` and the pretty-print (`getAllEmbeddedFlexoConceptsDeclaringThisConceptAsContainer`) have to filter
  the flat list.
- The `inside X` clause (`AbstractFlexoConceptNode.finalizeDeserialization`) can set a container in another compilation unit
  (`hasExternalContainer`, `requiresExternalContainerDeclaration`), which makes the composition cross resources. No `.fml` of the
  workspace uses `inside`.

**Target (decided 2026-11-18).**
- One association, `FlexoConcept.container` ↔ `contents`. It is the only composition, and it is lexical: the container of a nested
  concept is its enclosing concept, and the container of a root concept is its VirtualModel.
- `getOwningVirtualModel()` / `getOwner()` climb the containers.
- `VirtualModel.getAllFlexoConcepts()` is derived (the whole tree) and notified. Each `getFlexoConcepts()` call site (~142, ~25
  repositories) is reviewed and moved to the direct contents or to all concepts.
- `getApplicableContainerFlexoConcept()` (the container inherited from parents) stays a derived computation, not a stored link
  (revised in step 4b: removed, the container is never inherited).
- The `inside` clause leaves the grammar.

**Not covered.** VirtualModel-in-VirtualModel containment stays resource-based (`getContainerVirtualModel`): a VirtualModel's
`container` is null. The same dual structure in the runtime model (`owningVirtualModelInstance` / `containerFlexoConceptInstance`)
is a possible follow-up.

**Plan.** The steps are phased, and each is reviewed before the next.
1. Characterisation tests first. They pin URIs and local URIs of nested concepts, root concepts, the owning VirtualModel, the
   pretty-print round trip, cloning, deletion of a container concept, the notifications on add and remove, and the FML editor
   browser.
2. Single storage. The old accessors (`getOwner`, `getContainerFlexoConcept`, `getEmbeddedFlexoConcepts`,
   `VirtualModel.getFlexoConcepts`...) become `@Deprecated` aliases that delegate. Remove `bindEmbeddedFlexoConcepts` and the
   doubled notifications.
3. Migrate callers repository by repository (openflexo-dev composite): Java first, then the `.fib` / `.inspector` bindings
   (`flexoConcepts`, `embeddedFlexoConcepts`, `containerFlexoConcept`, `owner`).
4. Remove `inside` from `fml.sablecc` and from the concept nodes, with `hasExternalContainer()`,
   `requiresExternalContainerDeclaration()` and the `hasExternalContainer` branch of `getAllRootFlexoConcepts()`.
   4b. Then the inherited container: a concept extending a nested one is declared in the container of its parent (`CORE-D-95`), so
   `getApplicableContainerFlexoConcept()` and `setApplicableContainerFlexoConcept()` only serve invalid models; the runtime that
   resolves the container of an instance (`FMLContainerResolver`, `AbstractAddFlexoConceptInstance`, `ContainerPathElement`) then
   reads the direct container. Only once `CORE-D-96` is fixed: until the validation reports every such model, a model it misses
   (a concept extending a root concept that extends a nested one) would change behaviour at run time without any message.
   `CORE-D-96` is fixed (2026-11-21): every ancestor is checked, so 4b is no longer blocked.
5. Remove the aliases.

**Acceptance.**
- A nested concept has exactly one composite parent.
- The tests of openflexo-core, openflexo-ui, the technology adapters, formod, FME, the modelers and the integration tests are as
  green as before.
- The pretty-print round trip of the FML corpus is unchanged.
- In the application, opening, editing (create, delete a nested concept), saving and reloading a VirtualModel with nested concepts
  behaves as before.

**Progress (2026-11-19).**
- Phase 1 done: `TestConceptContainment` (fml-parser) pins containers, inherited containers, URIs, lookup, round trip, add, move,
  delete, clone and notifications; `KnownDefectsConceptContainmentTest` reproduces `CORE-D-86`, expected to be fixed by phase 3. `CORE-D-91`
  (printing by the inherited container) was fixed on top of phase 2, with the containment rule of `CORE-D-95`.
- Phase 2 done: `container` ↔ `contents` is the only stored containment; `owner`, `containerFlexoConcept`, `embeddedFlexoConcepts` and
  `VirtualModel.flexoConcepts` are deprecated derived methods, still notified under their names; `VirtualModel.getAllFlexoConcepts()`
  is derived (a new list at each call); `bindEmbeddedFlexoConcepts` is gone; `inside X` with `X` in another unit is a parse issue; the
  paste handlers paste into `contents`. Only the outermost `setContainer` notifies: the re-entrant call PAMELA makes through the inverse
  property announced a null owner, which emptied the binding model of a concept moved inside its VirtualModel (found by the review).
- Semantics of the deprecated aliases, to keep in mind while migrating the callers: `removeFromFlexoConcepts` takes the concept out of
  its container at any depth; `addToFlexoConcepts` does nothing for a concept already declared in the VirtualModel; `moveFlexoConceptToIndex`
  only reorders root concepts; `getFlexoConcepts()` lists the root concepts first, then the nested ones depth first (for concepts created
  by actions, the order is no longer the order of creation); `owner` is notified for the whole subtree of a concept moved to another
  VirtualModel.
- For phase 3 (found in the application, 2026-11-19): the "container" field of `FlexoConcept.inspector` is bound to the deprecated
  `containerFlexoConcept`, so it shows nothing for a root concept. Bind it to `container`; the selector (`FlexoConceptSelector.fib`)
  must offer the VirtualModel itself (back to the root) and list nested concepts (its tree follows `childFlexoConcepts`, the
  inheritance, not `contents`).
- Checked in the Maintainer (2026-11-19): moving a root concept into another one with the inspector moves it with its nested concepts.
- Found in the Maintainer, not caused by CORE-F-19 (same result without it): copying a concept with a `void` behaviour fails
  (`PAMELA-D-7`); undoing the deletion of a concept leaves it deleted and its lists reversed (`PAMELA-D-8`). Not checked: dragging a
  concept in the bottom-left browser (embedding mode) does not even start; whether it worked before is not known.
- Checked in the Maintainer (2026-11-20), once `CORE-D-92`, `CORE-D-93`, `PAMELA-D-7` and `PAMELA-D-8` were fixed: re-parse, creation
  in a container, move with the inspector, copy and paste, deletion then undo, close and reopen, save and reload (`CORE-D-94` not seen
  again), and the containment rule of `CORE-D-95` (validation error). Found: the inner-concepts facet was created inside an undo by
  the notification of a contents change (fixed: only an existing facet is notified); `CORE-D-97` (printed text after the last root
  concept leaves the VirtualModel). Not checked: drag and drop in the browser, which does not start.
- Phase 3a done (2026-11-20), openflexo-core. Decided with the user: `getContainerFlexoConcept()` (the enclosing concept, null for a
  root concept and for a VirtualModel) and its inverse `getEmbeddedFlexoConcepts()` / `getEmbeddedFlexoConcept(String)` (empty for a
  VirtualModel) stay, as non-deprecated derived reads. Still deprecated, for phase 5: the writes (`setContainerFlexoConcept`,
  `add/removeFromEmbeddedFlexoConcepts`, `setEmbeddedFlexoConcepts`, `moveEmbeddedFlexoConceptToIndex`) and the whole
  `VirtualModel.flexoConcepts` family. Convention for the callers: `getFlexoConcepts()` becomes `getAllFlexoConcepts()`, or
  `getContents()` where the code wants the direct children; a creation adds to its container (`addToContents`), a move sets the
  container (`setContainer`). The parser attaches every nested declaration through one `attachToEnclosingConcept()`, which fixed
  `CORE-D-86`. `TestConceptContainment` keeps the deprecated sequences on purpose until phase 5.
- Phase 3b done (2026-11-20), openflexo-ui: no deprecated containment call or binding left; drag and drop moves through `setContainer`
  under one rule (`FMLFIBController.isAcceptableContainer`: its own VirtualModel or one of its concepts, never the concept itself nor
  one nested in it); the FlexoConcept inspector edits `container`; `FIBFlexoConceptSelector` has a container mode (tree on `contents`,
  reset = root concept); the creation wizard chooses the container in the focused VirtualModel. `VirtualModel` fires
  `allFlexoConcepts` for the bindings moved to it. Not checked in the application. Not decided: the selector does not apply the
  containment rule of `CORE-D-95`, so it may offer a move the validation then reports.
- Found by the review of phase 3b: after a move, `removeFromContents` no longer resets the container (a stale `scheduledSets` entry of
  PAMELA's `DefaultSinglePropertyImplementation.set` makes the inverse `set(null)` look like a loop); `delete()` of a moved concept is
  affected too. Recorded and fixed as `PAMELA-D-9` (2026-11-22): the deletion was not affected after all; detaching (`removeFromContents`,
  `setContainer(null)`) was.
- Phase 3c done (2026-11-20): openflexo-diagram, free-modelling-editor, openflexo-docx, openflexo-jdbc, openflexo-owl,
  openflexo-server, formod, modelers and openflexo-integration-tests no longer call the deprecated containment API; `.fib` bindings
  on `flexoConcepts` moved to `allFlexoConcepts` or `contents`. Found on the way: `DIAGRAM-D-5` (fixed). Outside openflexo-core, only
  the deprecated aliases themselves are left, for phase 5.
- Phase 4 done (2026-11-21). Decided with the user: the `inside` clause is removed completely (`kw_inside`, `inside_clause`,
  `FMLKeywords.Inside`, the fragments and pretty-print of the concept nodes, the keyword of the FML editor's token maker): `inside` is
  an ordinary identifier again, and containment is declared by nesting only. `hasExternalContainer`,
  `requiresExternalContainerDeclaration` and `getAllEmbeddedFlexoConceptsDeclaringThisConceptAsContainer` are gone;
  `getAllRootFlexoConcepts` lists the contents of the VirtualModel whose container is not inherited: a root concept whose container
  is inherited, from its unit or another one, is shown nowhere and cannot be instantiated at the root (decided: kept hidden; `CORE-D-95`
  reports it, except in the cases of `CORE-D-96`). Specification updated (chapters 2, 5, 13, appendix A) and website pages regenerated.
  Step 4b (the inherited container) is deferred until `CORE-D-96` is fixed (decided with the user).
- Step 4b done (2026-11-22). `getApplicableContainerFlexoConcept()`, `setApplicableContainerFlexoConcept()` and the
  `applicableContainerFlexoConcept` notification are removed (decided with the user: no deprecated alias). The runtime resolvers
  (`FMLContainerResolver`, `AbstractAddFlexoConceptInstance`, `NewFlexoConceptInstance`, `MatchFlexoConceptInstance`, the factory of
  instances), `ContainerPathElement`, `CreationSchemePathElement`, `FlexoConceptBindingModel` (the type of `container`) and the
  instance selector of openflexo-ui read the declared container. Decided with the user: a concept of an invalid model (declared at
  the root, extending a nested concept) is a root concept as declared, listed by `getAllRootFlexoConcepts` and instantiable in the
  VirtualModel instance; the validation error (`CORE-D-95`) is the only report. Specification §5.2.1 and chapter 14 updated.
- Phase 5 done (2026-11-22): the deprecated aliases are removed: `VirtualModel.getFlexoConcepts`, `setFlexoConcepts`,
  `addToFlexoConcepts`, `removeFromFlexoConcepts`, `moveFlexoConceptToIndex` and the `flexoConcepts` notification (`FLEXO_CONCEPTS_KEY`);
  `FlexoConcept.setOwner`, `setContainerFlexoConcept`, `setEmbeddedFlexoConcepts`, `add/removeFromEmbeddedFlexoConcepts`,
  `moveEmbeddedFlexoConceptToIndex`. No caller was left in the main sources of the workspace (only commented-out code, and methods
  of the same name on other types: `VirtualModelInfo`, the `CreateFlexoConcept` action, `InnerConceptsFacet`); `TestConceptContainment`
  now writes the containment with `setContainer` / `addToContents`. Kept, as decided: the derived reads `getOwner()`,
  `getContainerFlexoConcept()`, `getEmbeddedFlexoConcepts()`, `getEmbeddedFlexoConcept(String)`, read by many `.fib` bindings.
  The review of phase 5 proved the finding of phase 3b (`PAMELA-D-9`, fixed); `TestConceptContainment.aMovedConceptCanBeRemovedAndDeleted`
  covers it.
- Checked in the application by the user (2026-11-23, Maintainer): no defect left.

**Resolution (2026-11-23).** A concept has one composite parent: `container` ↔ `contents`, lexical (its enclosing concept, or its
VirtualModel for a root concept). The owner, the flat list (`VirtualModel.getAllFlexoConcepts()`), the enclosing concept and the
nested concepts are derived from it; the former duplicated API, the `inside` clause and the inherited container are removed.
Decided on the way: a concept extending a nested one is declared in the container of its parent (`CORE-D-95`, `CORE-D-96`, a
validation error otherwise, the only report); the derived reads `getOwner()`, `getContainerFlexoConcept()`,
`getEmbeddedFlexoConcepts()` stay. Found and fixed on the way: `CORE-D-86`, `CORE-D-91`..`CORE-D-93`, `CORE-D-95`, `CORE-D-96`,
`PAMELA-D-7`, `PAMELA-D-8`, `PAMELA-D-9`, `DIAGRAM-D-5`. Left: `UI-F-4` (openflexo-ui, the container choice does not apply the
containment rule), `CORE-D-97`; not covered, as planned: VirtualModel-in-VirtualModel containment and the runtime
`FlexoConceptInstance` model.

## Resources

### CORE-F-13 — Errors of a resource, detected at the resource layer without loading it  ·  `IN PROGRESS`

**GitHub:** [openflexo-team/openflexo-core#126](https://github.com/openflexo-team/openflexo-core/issues/126)

**Problem.** A `.fml.rt` whose VirtualModel cannot be found was announced by a modal dialog. More generally the resource layer
already computes, from the metadata of the resource (`VirtualModelInfo`, cached on disk), the dependencies and the technology
adapters a resource needs, but a missing VirtualModel, required `.fml` or technology adapter was never carried by the resource.

**Done.** `FlexoResource` carries `ResourceIssue`s (`getIssues`, `hasErrors`, `getIssuesMessage`, notified as `issues` /
`issuesMessage`), set when the resource is discovered: missing VirtualModel of a VMI, missing required resource, missing model slot
class or technology adapter. A pending dependency is resolved when the resource is registered (the issue goes, a VMI gets its
VirtualModel) and dropped when the waiting resource is unregistered. The browsers show the error icon and the issues as tooltip; the
status bar of the main window shows them for the selected resource; `Validate project` lists them
(`FlexoProject.AllResourcesMustBeFreeOfIssues`, own dictionary `ProjectValidation`).

**To decide / left.**
- Issues are computed once, at discovery: not recomputed on reload, re-parse, edited `import`/`use`, or a dependency that disappears.
- An `import` whose URI is not registered is an `ERROR` (`Severity.WARNING` exists, unused).
- A VMI declaring no VirtualModel gets the subject `?`; a VMI whose XML cannot be read gets no issue.
- `Validate project` covers the resources of the project only, not those of a global resource center.
- A `use` of a missing class is reported from the metadata computed at the first parse; a later save of the unit rewrites the cached
  list without it.

**Acceptance criteria.** Opening a project whose VMI names an absent VirtualModel shows the red cross, the tooltip and the status bar
with no dialog, and nothing is loaded; adding the resource center that holds the VirtualModel clears them; `TestResourceIssues`
passes, including the late resolution of a pending dependency, a missing dependency and a missing technology adapter (not covered yet).

### CORE-F-28 — Advanced search over a set of resources (FML or technology adapter)  ·  `TODO`

**GitHub:** [openflexo-team/openflexo-core#148](https://github.com/openflexo-team/openflexo-core/issues/148)

**Problem.** The platform has no way to search across resources. The only search tool is the text finder of the FML editor (`TextFinderPanel`, in `fml-technologyadapter-ui`), which looks for a text in the document being edited. The `select … from … where` of FML queries one model from FML code. There is nothing to find, across a set of resources, a text, an element of a given type, an element with a given property value, or the places that use an element, whether the resources are FML or belong to any technology adapter.

Verified on the current `3.0` branch: no search engine, index or search API was found in `openflexo-core`, and no existing backlog entry covers it.

**Goal.** An advanced search function that runs over a set of resources (FML resources and resources of any technology adapter). The scope is wide, from free text to advanced structured queries, and the entry covers both its design and its implementation.

**Open questions.** Nothing is decided yet; the design is part of this entry.
- Kinds of search: free text in the content of resources (names, values, FML text); structured queries by type of concept, by property, by value (`name == "x"`); search for the usages or references of an element; combinations of these.
- Targets: a resource center, a selection of resources, a project, a resource repository. Whether a result can mix objects of several technology adapters (an Excel cell, an XML element, an FML concept).
- Resources that are not loaded: search in loaded resources only, or also in unloaded ones (which requires an index or a loading on demand).
- Mechanism: traversal on demand, a persistent index, or an extension of the FML query language. Whether each technology adapter provides its own search provider (an extension point).
- Query language: how queries are written and typed, and whether it is related to the FML `select … from … where`.
- Result model: what a result is (object, location, matching text), how it is ordered, how it designates the found object across technology adapters.
- Interfaces: Java API, an `fml-cli` command, an FML statement, a graphical interface in `openflexo-ui` (possibly a separate entry).
- Impacts to inventory: performance and memory (loading resources), threading and progress of a long search (see skill `long-task-progress`), persistence and freshness of an index, localization, technology adapters, and tests.

**Acceptance criteria** (to be refined once the design is chosen).
- A design note records the kinds of search, the targets, the mechanism and the query language that are chosen, with the open questions answered.
- A search runs over a set of resources of at least two technology adapters, FML included, and returns results that designate the found objects.
- Free-text and structured searches are supported as decided.
- A long search reports its progress and can be cancelled.
- Tests prove representative searches by running them.

## FML runtime errors

### CORE-F-11 — Typed FML runtime errors, raised where detected, reported once with the FML location  ·  `DONE`

**GitHub:** [openflexo-team/openflexo-core#127](https://github.com/openflexo-team/openflexo-core/issues/127)

**Problem (audit of every execution primitive, 2026-11-13).** Errors raised while FML runs are handled ad hoc:
- **Errors become values.** About 60 blocks catch `TypeMismatchException` / `NullReferenceException` / `InvocationTargetException` /
  `ReflectiveOperationException` four at a time, print the stack and go on with `null`. They are found in receivers, containers,
  matching sets, behaviour references, behaviour arguments (`BehaviourCallArgument`), the get/set of properties
  (`FlexoConceptInstance`, getters, setters, expression properties), and `where` (`FetchRequestCondition`, gives `false`). A failing `enable` /
  `visible` condition counts as true (`AbstractActionScheme.evaluateCondition`). The fault shows up later as an NPE
  (`ExecuteFlexoBehaviour`, `MatchFlexoConceptInstance`, `FinalizeMatching`), an empty `select` or a null parameter. A null in the path
  of an `if` / `while` / `for` condition is silently `false`.
- **Inconsistent wrapping.** A failure in a behaviour called from an expression is wrapped as `FMLExecutionException` → `InvocationTargetException` →
  `InvocationTargetTransformException`, then each caller unwraps it in its own way (`getCause()` loses the message, some sites wrap
  twice). The stack and the whole pretty-print of the behaviour are printed again at every nesting level
  (`FlexoBehaviourAction.executeControlGraph`, `FMLRTVirtualModelInstanceModelFactory.executeCreationScheme`).
- **Two channels.** `DefaultFlexoEditor.performAction` and `FlexoAction.doAction` without an editor print the exception and keep it in
  `getThrownException()`; callers often test only the status. A `RuntimeException` escapes `doActionInContext` and leaves the status at
  `EXECUTING_CORE`.
- **`return` treated as an error.** A plain `return` fails in a creation scheme (factory), is printed in a deletion scheme
  (`deleteWithScheme`), and is an error at the top of an fml-cli script. `ReturnException` builds a full stack trace on every `return`.
- **No location.** No runtime error names the failing statement, its line, the behaviour or the instance, although the position is
  available (`getPrettyPrintDelegate().getStartLocation()`). In the application only `NotImplementedException` reaches the user (the
  initializers of the behaviour actions); the `FMLConsole` only receives `log` statements; GINA swallows binding and button errors
  (`FIBController.handleException` is never called). fml-cli prints `getMessage()` only, gives a line for `assert` only, and its interactive
  loop ends on a `RuntimeException`.
- **Connie and PAMELA (lower layers).** Connie is permissive: an invalid binding gives null on get and only warns on set. Other
  `TransformException`s, `IllegalArgumentException` / `IllegalAccessException` of a method call, and a `ConcurrentModificationException` become null.
  Several Connie exceptions lose their message or their cause (`CONNIE-F-4`); PAMELA `ModelExecutionException` loses its cause
  (`PAMELA-D-6`).
- **Technology adapters.** 66 edition actions of the technology adapters declare no exception and return null after a warning or a
  printed stack.

Measured in `flexo-foundation`, `org.openflexo.foundation.fml.**` (grep, 2026-11-13): 370 `printStackTrace`, 15 `Thread.dumpStack`, 181
`System.out/err`, 69 `return null` within 4 lines after a `catch`. The bugs found by the audit are `CORE-D-75` to `CORE-D-79`; `CORE-D-73`
is the creation-scheme part already recorded. Not verified (read, not run): `NewFlexoConceptInstance` may run its creation scheme twice,
in the factory and again in `doAction`.

**Decided with the user (2026-11-13).**
1. Foreseen errors are checked, under `FMLExecutionException`; a defect of the platform (`FMLInternalError`) is unchecked. Revised on
   2026-11-14, before the first commit: the first decision (an unchecked root) was reversed after weighing the two. A foreseen error is a
   condition the caller can recover from, which is what a checked exception is for. Every execution method already declares
   `FMLExecutionException` (81 in the core, 45 in the adapters), so the typed errors fit the existing contract. The compiler then points at
   every caller when phase 3 makes the helpers raise instead of returning null. Against it: the execution crosses interfaces whose signature
   is fixed (the path elements of Connie, `PropertyChangeListener`, GINA, PAMELA getters). There a failure travels wrapped in a single,
   normalized way, and is unwrapped by one helper that rethrows the same object.
2. A null dereference in a condition is an error, no longer `false`.
3. In the application an error is reported in the `FMLConsole` and in a dialog.
4. FML `throw` / `try … catch` come in a late phase.
5. Connie gets a strict evaluation mode; GINA stays lenient.

**Typology.** Two axes: who is at fault (the FML or its data, the user, the platform), and whether the semantics of the language foresee the case.

```
ReturnException (checked, without stack trace)   control flow, never an error; does not leave execute()
FMLExecutionException (checked, an OpenflexoRuntimeException) carries the FML stack (frames); the plain type is the untyped form, for the throw
│                                                sites not migrated yet
├── FMLCancellation                              the user cancelled (parameters dialog, OperationCancelledException,
│                                                ActionExecutionCancelledException): compensated (undo), never reported as an error
├── FMLError (abstract) — foreseen               the FML or its data break the semantics; catchable by FML try/catch (phase 7)
│   ├── FMLNullReferenceError                    null in a path, null receiver / container / target, null condition
│   ├── FMLTypeError                             type mismatch, invalid cast, argument of the wrong type
│   ├── FMLResolutionError                       invalid binding at runtime, unknown behaviour / property / concept,
│   │                                            unresolved technology action (validation should have reported it)
│   ├── FMLAssignmentError                       target not settable
│   ├── FMLArgumentError                         missing or invalid behaviour parameter (replaces InvalidParametersException there)
│   ├── FMLCardinalityError                      UNIQUE select with several results, required role null, [1,1] broken
│   ├── FMLTechnologyError                       foreseen failure of a technology operation (missing sheet, no common
│   │                                            ancestor, duplicate URI…), raised by the adapter with its own key
│   ├── FMLHostError                             exception of Java code called from FML (target of an ITE), kept as cause
│   └── FMLUserError                             raised by FML `throw` (phase 7); FMLAssertionError for `assert` in scripts

FMLInternalError (unchecked) — unforeseen        defect of the platform (NPE, ClassCastException, IllegalStateException,
                                                 ModelExecutionException, ConcurrentModificationException in OpenFlexo code):
                                                 wrapped once at the first statement it crosses, with the FML stack; not
                                                 catchable from FML; reported as a bug (bug report dialog)
```

Both carry the FML stack through the interface `FMLExecutionFailure`.

`FMLExecutionException` stays the checked root, so existing `throws` and `catch` clauses keep their meaning; it moved from
`org.openflexo.foundation.fml.rt` to `org.openflexo.foundation.fml.rt.error`, with its hierarchy (2026-11-14, about 100 files in some fifteen
repositories: only the import changes; `ReturnException`, control flow and not an error, stays in `fml.rt`). Its untyped `new
FMLExecutionException(...)` throw sites are replaced by typed ones site by site (phases 3 and 6).

An FML stack frame holds the behaviour, the concept, the instance (its renderer), the innermost statement, the binding text, and the
line and column given by `FMLPrettyPrintDelegate.getStartLocation()` (or the pretty-print of the statement when there is no source). `getMessage()`
gives the localized message and the innermost frame; `getFMLStackTrace()` gives the whole FML stack for the console.

**Levels: where an error is raised, enriched, caught and reported.**

| Level | Where | Rule |
|---|---|---|
| L0 detection | edition action, helper, Connie in strict mode | raises the typed error where the fault is detected; never `printStackTrace`, never `return null` on error |
| L1 statement | one helper `FMLExecution.executeStatement(cg, ctx)` used by `Sequence`, `ConditionalAction`, the loops | records the innermost statement if none is set yet; removes declared variables in a `finally`; catches nothing else |
| L1' evaluation | `FMLEvaluation.evaluate(binding, ctx, owner)`, `evaluateCondition`, `assign` | replaces the four-catch blocks: TME → Type, NRE → NullReference, invalid → Resolution, not settable → Assignment, ITE (or ITTE) whose target is an `FMLExecutionException` or an `FMLInternalError` → that target rethrown as is, other ITE → Host; null condition → NullReference |
| L2 behaviour | `FlexoBehaviourAction.executeControlGraph`, the creation scheme of the factory, `deleteWithScheme`, the listeners | pushes the behaviour frame; `ReturnException` → returned value, in every kind of scheme; unforeseen `RuntimeException` → `FMLInternalError`, once; logs nothing |
| L2' calls from an expression | `FlexoBehaviourPathElement`, `CreationSchemePathElement`, `ExecuteFlexoBehaviour`, `NewFlexoConceptInstance`, `FireEvent` | the one normalized tunnel: a checked failure crosses Connie as `InvocationTargetTransformException(InvocationTargetException(failure))`, and a listener as `EventListenerFailedException`; on the other side it is unwrapped and the same object is rethrown, never wrapped again. An `FMLInternalError` crosses unwrapped |
| L3 action | `FlexoAction.doActionInContext` | catches any `Throwable`, given by `getFailure()` (`getThrownException()` keeps the checked one); the status is always set (FAILED or CANCELLED); undo is compensated before reporting |
| L4 report, the only one | `FlexoEditor` → a new `FMLErrorReporter` per editor | a failing non-embedded action is reported once: a SEVERE `FMLConsole` record with the FML stack, and a log line. Interactive editor: an "FML error" dialog ("show in console", "open in the FML editor") for an `FMLError`, the bug report dialog for an `FMLInternalError`, nothing for an `FMLCancellation`. Headless `DefaultFlexoEditor`: console and log. The editor of the tests rethrows, so a test fails loudly |

Other entry points report to `FMLErrorReporter`, console only:
- listeners: `CORE-D-47` stays, so a failing listener fails the `fire`;
- synchronization at load;
- GINA actions: `FIBController.handleException` is revived and `FlexoFIBController` routes it;
- the diagram controls, which check the outcome.

The server answers JSON {kind, key, message, FML location}, 4xx for a foreseen error and 5xx otherwise. fml-cli reports the script line
and the FML stack; its interactive loop survives a `RuntimeException`; `ExecuteDirective` writes to err.

**Phases.** Each phase is a session of its own, with its tests and a review.
1. **Core foundations.**
   - The hierarchy, the frames, the helpers `FMLExecution` and `FMLEvaluation`.
   - L2 to L4, `FMLErrorReporter`, the SEVERE records, the test editor that rethrows.
   - `ReturnException` without stack trace, and handled in every scheme.
   - The `catch (OpenflexoRuntimeException)` sites fixed before `FMLExecutionException` moves.
   - The coding conventions: no `printStackTrace` / `System.out` / `return null` on error in `fml/**`, evaluation through
     `FMLEvaluation` only, reporting at L4 only.
2. **Connie and PAMELA** (independent repositories): `CONNIE-F-4`, `PAMELA-D-6`. The version bumps are decided at that time.
3. **The core primitives.**
   - The four-catch blocks move to `FMLEvaluation`.
   - Properties, `where` and `enable` / `visible` become strict, and so do conditions.
   - The double logs are removed.
   - `CORE-D-75` to `CORE-D-79` are fixed.
   - Strict null in conditions: run fml-cli-test, openflexo-integration-tests and formod-test, list here every FML that relied on
     `false`, and fix it with explicit null checks.
   - The counts above are measured again.
4. **User interface.**
   - The dialog, and navigation from the console to the FML editor through the location of the frame.
   - A default exception handler for the initializers of the behaviour actions (no more `NotImplementedException` special case).
   - GINA and the diagram controls.
5. **fml-cli and the server.**
6. **Technology adapters**, one repository at a time, each with its own entry: the silent actions raise `FMLTechnologyError` or go
   through the helpers; `CORE-D-73` is fixed here.
7. **FML language.**
   - `throw <expr>` and `try { … } catch (<ErrorType> e) { … }` in `fml.sablecc`, the pretty-print and the semantics.
   - The `FMLError` types become FML types; `FMLInternalError` and `FMLCancellation` cannot be caught.
   - The specification in `openflexo-core/spec` is updated and regenerated.

**Phase 1 done (2026-11-14), decided with the user.**
- The typed errors are in `org.openflexo.foundation.fml.rt.error`: `FMLError` and its subclasses, and `FMLCancellation`, all under the
  checked `FMLExecutionException`; `FMLInternalError`, unchecked. All carry an `FMLStack` (interface `FMLExecutionFailure`). A failure that
  wraps one that has a stack starts with its frames. None of the ~200 `catch (OpenflexoRuntimeException)` of the workspace catches a failure that used to pass
  through it unchecked.
- `FlexoAction.getFailure()` gives any failure, checked or not; `getThrownException()` gives the checked one, foreseen errors included. The
  status is now `FAILED_EXECUTION` for any failure (`HAS_BEEN_CANCELLED` for an `FMLCancellation`).
- Reporting is done once, for the outermost action run on a thread (`FMLErrorReporter.enterAction`, a `ThreadLocal`), by
  `DefaultFlexoEditor.performAction` and by `FlexoAction.doAction()` without an editor. A nested action leaves its failure to its caller.
  A nested checked failure that the caller went on after is reported when the outermost action succeeds ("failed, and its caller went
  on"). An unforeseen failure is reported and rethrown, as before; a foreseen one stays in the status of an outermost action. This is the
  behaviour of the test editor too (it extends `DefaultFlexoEditor`): unforeseen failures fail the test, foreseen ones are read from the
  status.
- L1 is `FMLExecution.executeStatement` (`Sequence`, `if`, the loops, `while`); L2 is `FMLExecution.executeBehaviourBody`
  (`FlexoBehaviourAction.executeControlGraph`, the creation scheme of the factory, `deleteWithScheme`). They print nothing, and an
  unforeseen `RuntimeException` becomes an `FMLInternalError` at the first statement it crosses. A `return` ends a creation or a
  deletion scheme without error, and builds no stack trace. `CORE-D-76` is fixed on the way.
- A listener failure reaches the `fire` unchanged when it is an `FMLExecutionException` or unchecked (another checked one is wrapped),
  and the other listeners still run; their failures go with the first one (suppressed exceptions, printed by the report).
- After review (decided with the user):
  - An unforeseen failure is recognized through the whole cause chain (`FMLErrorReporter.isForeseen`): a `RuntimeException` that is not under an
    `InvocationTargetException` (a failure of Java code called from FML, foreseen), or an `FMLInternalError`, is a defect, logged with its
    Java stack, even when wrapped into a checked exception.
  - `AbstractAssignationAction`, `AssignationAction` and `DeleteAction` no longer wrap an unchecked exception into an untyped
    `FMLExecutionException`, and `DeleteAction` no longer swallows one. `FMLExecution.convertUnchecked` converts it once, into an
    `FMLInternalError`: it stays unchecked and propagates as before. Turning the foreseen ones (a failing Java accessor, a missing Java
    property) into the checked `FMLHostError` / `FMLResolutionError` waits for phase 3: until the `catch (OpenflexoRuntimeException) → null` of the
    getters, setters and adapters are strict, they would swallow a failure that used to reach the caller.
  - The fml-cli commands no longer print the stack of a failure that the editor reported.
  - A valued `return` in a creation scheme is an error (`FMLTypeError`); a bare `return` ends the scheme.
  - A nested failure that is not part of the failure of the outermost action is reported too.
  - The callers that read the outcome of an action they ran call `rethrowUncheckedFailure()` first: `FlexoBehaviourPathElement`,
    `CreationSchemePathElement`, `ExecuteFlexoBehaviour`, `NewFlexoConceptInstance`, `DeleteFlexoConceptInstance`, `FinalizeMatching`,
    `CreateFMLRTVirtualModelInstance`. Since foreseen errors are checked, an unchecked failure always propagates out of `doAction()`, so
    this is only a guard. The callers of other repositories read `getThrownException()`, which gives every foreseen failure: they lose
    nothing.
  - The getter and the setter of a property run through `FMLExecution.executePropertyBody`, with a frame of their own.
  - A stack keeps no model object: a statement is described when it is recorded.
- Known limits of phase 1:
  - A cancellation (`FMLCancellation`, raised by nothing yet, phase 4) is a checked failure: the callers of a cancelled nested action
    rethrow it, wrapped, and their own action ends `FAILED_EXECUTION`. The report skips it, even wrapped (`FMLErrorReporter.isCancellation`).
    The status of the callers is to be settled when phase 4 raises it. Settled in phase 4: the callers end `HAS_BEEN_CANCELLED`.
  - The report goes to the FML console and the log. The running application shows nothing more until phase 4 (`InteractiveFlexoEditor`
    unchanged), and the former `System.err` pretty-print of the failing behaviour is gone.
  - An action started by an expression (fml-cli, GINA, server) is an outermost action: it is reported, and its caller still receives the
    failure. GINA prints it again until phase 4.
  - A `ConcurrentModificationException` raised in a behaviour called from an expression is now an `FMLInternalError`; Connie
    (`BindingPath`) used to turn it into null.
  - `DeleteAction` no longer swallows an unchecked exception: a deletion path of a technology adapter that threw one silently now fails
    (`XML-D-1`, `PPTX-D-6`). The other adapters were not run.

**Phase 2 done (2026-11-14), decided with the user.**
- Connie: `CONNIE-F-4` (strict evaluation policy, unchecked `BindingEvaluationException`, messages and causes kept).
- PAMELA: `PAMELA-D-6` (`ModelExecutionException` keeps its cause).
- FML contexts stay `LENIENT`: phase 3 switches them to `STRICT`, `FMLEvaluation` converting a `BindingEvaluationException` into the
  checked `FMLError` of its kind (invalid binding → `FMLResolutionError`, not settable → `FMLAssignmentError`, illegal argument →
  `FMLTypeError`, illegal access, incomplete evaluation, transformation failure → to decide then). The FML delegating contexts must pass
  the policy on.
- To check in phase 3, before switching FML to `STRICT` (found by the review of phase 2):
  - `INCOMPLETE_EVALUATION` is thrown where an expression does not reduce to a constant; the code says that happens "very often". List the
    valid FML expressions that do not reduce before making it an error.
  - `BindingPath.getBindingValue` without context throws `INVALID_BINDING` in strict mode: check that no FML caller evaluates with a context
    it has not built yet.
  - A `BindingEvaluationException` that escapes `FMLEvaluation` becomes an `FMLInternalError` at `executeStatement`: every evaluation
    site has to go through `FMLEvaluation`, or `convertUnchecked` has to map it.

**Phase 3 done (2026-11-15), decided with the user.**
- `FMLEvaluation` (`org.openflexo.foundation.fml.rt.error`): `evaluate`, `evaluateCondition`, `assign`, `execute`, and `convert` for the sites
  that call Connie themselves. Type mismatch and invalid cast → `FMLTypeError`, null reference → `FMLNullReferenceError`, not settable →
  `FMLAssignmentError`, invalid binding → `FMLResolutionError`; an invocation whose target is an FML failure → that very object (also through
  `InvocationTargetTransformException` and `FMLFailureTunnel`), another target → `FMLHostError`; a missing Java property → `FMLResolutionError`.
  The kinds of `BindingEvaluationException`: `INVALID_BINDING`, `INCOMPLETE_EVALUATION` → `FMLResolutionError`; `NOT_SETTABLE` →
  `FMLAssignmentError`; `ILLEGAL_ARGUMENT` → `FMLTypeError`; `ILLEGAL_ACCESS` → `FMLHostError`; `TRANSFORMATION_FAILURE` by its cause
  (invalid cast → type, not settable → assignment, other → resolution); `CONCURRENT_MODIFICATION` → `FMLInternalError` (unchecked). The failure
  is located at the FML object owning the binding unless a statement was recorded already.
- `FMLExecution.convertUnchecked` throws the checked error for `AccessorInvocationException` (host), `InvalidKeyValuePropertyException`
  (resolution), `BindingEvaluationException` (its kind) and `FMLFailureTunnel` (the failure carried); any other unchecked exception stays an
  `FMLInternalError`. `FMLExecution.rethrowFailure(action)` rethrows the failure of a nested action unchanged.
- `INCOMPLETE_EVALUATION`, checked first (to check 1): in FML it fires for a ternary whose condition is not a boolean constant (null), a cast to
  an unresolved type and an invalid nested path, all faults. A null ternary condition raises a null reference in a strict evaluation
  (`FMLConditionalExpression`). No FML caller evaluates without a context (to check 2): the only null-context evaluations are static ones of the
  compilation unit, which stay lenient. Every evaluation site of the core goes through `FMLEvaluation`, and `convertUnchecked` maps what
  escapes (to check 3).
- Policies: the execution contexts are `STRICT` (`FlexoBehaviourAction`, `LocalRunTimeEvaluationContext`, the interpreter of fml-cli); the
  delegating contexts pass the policy on (`DelegatedRunTimeEvaluationContext`, `CandidateEvaluationContext` for `where` and `match`). An
  instance used itself as a context stays `LENIENT` (user interface, renderer); `FlexoConceptInstance.newLocalEvaluationContext()` gives a
  strict one.
- Properties: `getFlexoPropertyValueOrFail` / `setFlexoPropertyValueOrFail` throw the failure of a getter, a setter or an expression property
  (which gets a frame of its own); `getFlexoPropertyValue` / `setFlexoPropertyValue` stay lenient for the user interface (null and one
  warning). The path element uses the strict ones in a strict evaluation and crosses Connie through `InvocationTargetTransformException`; a
  property read through its bare name (`BindingEvaluationContext.getValue`, which declares nothing) crosses through the new unchecked
  `FMLFailureTunnel`, unwrapped by `FMLEvaluation`. The former value read by a set does not fail the set. The strict methods are the entry
  points to override (`getFlexoPropertyValueOrFail`, `setFlexoPropertyValueOrFail`, `getValue(variable, policy)`): the instances of
  openflexo-rest (`HttpFlexoConceptInstance`, `RestFlexoConceptInstance`) and openflexo-jdbc (`HbnFlexoConceptInstance`) override them instead
  of the lenient ones (decided with the user after review), so FML and the user interface both reach their mapping.
- Strict: conditions (`if`, `while`, `for`, `where`, `match`), receivers, containers, arguments, matching criteria, `@Enable` / `@Visible`
  when a scheme runs (synchronization, listeners, navigation). In the user interface a failing `@Enable` shows the behaviour disabled, a
  failing `@Visible` shows it (FINE log). A null receiver, container or concept to instantiate is an `FMLNullReferenceError`; iterating over
  `null` too. The selection by index is not made strict: indexes are not used (`VirtualModelInstance.isIndexable()` is false); to do if they
  are enabled.
- `&&` and `||` short-circuit (`FMLBinaryOperatorExpression`), so that a null check can guard a condition; the specification is updated
  (chapters 3, 7, 8, 9, 12, 13, appendix B).
- An action about to run asks its factory `isEnabledForExecution` (`FlexoAction.doActionInContext`, `DefaultFlexoEditor.performAction`,
  `InteractiveFlexoEditor.performAction` of openflexo-ui, decided with the user after review): for
  a behaviour it evaluates `@Enable` strictly, so a failing `@Enable` fails the action (a listener fails the `fire`), while `isEnabled`, asked
  by the user interface, only answers. The expressions of a behaviour argument (default value, container, list) serve the parameters dialog
  too: they go through `FMLEvaluation` and give null with a warning on failure, also when the execution uses them (a limit: the
  specification says an error). `@Enable` / `@Visible` are evaluated on the instance the behaviour is called on (the instance of its
  factory), no longer on the focused object of the action, which is the virtual model instance for `x.doIt()`. Every kind of scheme is
  concerned, deletion schemes included.
- Other choices made while implementing, to keep in mind: the local context of a virtual model instance answers that instance as its
  virtual model instance (it used to answer its owner), and its default values are evaluated in that strict local context, no longer on the
  instance itself; an expression property is always evaluated strictly, the lenient reading wrapping the failure; a behaviour called outside
  a behaviour runs with `FlexoConceptInstance.getEditor()` (which falls back on the default editor); the strict interpreter of fml-cli is
  also the base of the terminal of the application and of the REST terminal of the server; in `FlexoBehaviourPathElement` and
  `CreationSchemePathElement`, the failure of an argument now reaches the caller in a lenient context too (it was printed and gave null);
  a JVM `Error` is never converted into an FML error.
- Double logs removed: Connie no longer prints the failure of Java code it propagates (`CONNIE-F-4` follow-up), the four-catch blocks are gone
  from `fml/editionaction`, `fml/rt/editionaction`, `fml/controlgraph`, `fml/binding`, `fml/rt/action`. Left for later: `fml/ta` (the FML
  technology adapter, phase 6), the static evaluations of `FMLCompilationUnit`, `md/`, `ElementImportDeclaration`, the renderer and the
  inspector of an instance (user interface, lenient), the unused `FlexoRole.getDefaultValue(context)` / `getContainer(context)`.
- `CORE-D-75`, `CORE-D-77`, `CORE-D-78`, `CORE-D-79` are fixed (`FIXED_DEFECTS.md`); `CORE-D-80` is not (binding resolution, not error handling).
- Technology adapters: `throws FMLExecutionException` added where the compiler required it, signature only (agreed with the user): capella,
  csv, diagram, docx, emf, jdbc, json, kafka, markdown, opc-ua, oslc, owl, pptx, technology-adapters (dsl, xx), xlsx, xml, formod (b-ta).
- FML that relied on a lenient null or on eager `&&` / `||` (found by the suites):
  - core fixtures, documenting the former behaviour: `Flow.fml` (`andOperands`, `orOperands`), `Depot.fml` (`bareTool`),
    `ReceptionWithoutImport.fml` (`guestName`), `NameLookup.fml` (`bareParameter`, `bareCall`), `ExecutionFailures.fml` (`nullCondition`): the
    scripts assert the new behaviour (`ErrorKinds/`);
  - openflexo-integration-tests, fixed with explicit null checks (agreed with the user): city-mapping `Mapping.fml`, `cityOfModel1Named` and
    `cityOfModel2Named` (`where (selected.cityInModel1 != null && …)`); multi-process-challenge `MetaModel.fml`, `getTaskType`
    (`taskType != null && …`). Not explained: the multiple role `taskTypes` holds a null element in `T6_AcmeFixture`. Same pattern, not
    exercised by a null so far: `Mapping.fml` lines 238, 242, 246;
  - formod, xml-ta-test, pptx-ta-test, integration-tests: nothing.
- Revealed, not fixed: `XLSX-D-2` (openflexo-xlsx): after an external edit and a refresh, the reflected Excel slot was rebuilt over a stale
  sheet; the `NullPointerException` used to be swallowed by the container of a `select`. Seen once (`ExternalWorkbookEditTest`,
  digital-twin-demo), not reproduced in the three later runs; recorded, not investigated (decided with the user).
- Not attributed to phase 3 (failures seen in the final run, outside the suites listed in the task): markdown-ta-test
  `TestMarkdownEditionActions` (an `UnresolvedTechnologySpecificAction`, whose throw is older than this phase), diagram-ta
  `TestFMLDiagramPrettyPrint3` (a pretty-print location), capella-ta-test `TestLoadXXDocuments` (a null resource center). Not compared with
  the state before phase 3.
- Counts in `flexo-foundation`, `org.openflexo.foundation.fml.**`, comment lines excluded (before phase 3 → after): `printStackTrace` 364 → 216,
  `Thread.dumpStack` 15 → 14, `System.out/err` 168 → 163, `return null` within 4 lines after a `catch` 67 → 45.
- Tests: `TestFMLErrorKinds` (fml-cli-test, scripts `ErrorKinds/`, behaviours appended to `ExecutionFailures.fml`): one script per kind
  (null in a condition path, null condition, null ternary condition, null iteration, failing loop initializer, failing getter by name and
  through `this.`, failing expression property, null in `where`, failing `@Enable`, invalid binding, bare `new`, missing import, bare
  parameter, bare call, failure of Java code), each asserting the type, the line and the behaviour frame, and that nothing is printed to
  stderr; `&&` / `||` laziness. `TestFMLEvaluation` (flexo-foundation-test): the conversions of `FMLEvaluation` and `convertUnchecked`.
  `TestFlexoRoleCardinality`: `CORE-D-77`. Not covered by a test: `FMLAssignmentError` from FML (a target that cannot be set is rejected by
  the analysis), `CORE-D-78`, `CORE-D-79`, `@Enable` in the application (`InteractiveFlexoEditor`) and on `x.doIt()`, the overrides of
  openflexo-rest (its tests need the network). `TestBehaviourAvailability` evaluates `@Enable` on a stub instance, not strictly.
- Reviewed by `openflexo-reviewer` (2026-11-15); its findings were fixed or are recorded here. openflexo-xlsx: the debug `log` of
  `TestLoadPersonListing1/2.fmlscript` called an unresolved method and is removed (decided with the user).


**Phase 4 done (2026-11-15), decided with the user.**
- `InteractiveFlexoEditor` (openflexo-ui) runs an action like `DefaultFlexoEditor`: the outermost action of a thread reports, a nested one
  leaves its failure to its caller; the edits of a failing action are undone before the report (`actionHasBeenPerformed(action, false)`,
  also for an unchecked failure, which used to leave the recording open); the exception handler of the initializer still comes first, for
  a nested action too; the finalizer runs once the action has left its execution scope. A long-running action reports in its task. The
  dialogs always come later on the event dispatch thread (`invokeLater`), once the failed action has left its scope: an action started
  from the dialog or while it is shown is an outermost one (found by the review). No more `System.out` / `printStackTrace`.
- Presentation (`InteractiveFMLErrorReporter`, `present()` hook of `FMLErrorReporter`): a foreseen failure opens the `FMLErrorDialog` (kind,
  action, message, the FML stack one row per frame, innermost selected; "Show in console", "Open in the FML editor" on the selected frame,
  "Close"; not modal, it stays open while the user goes from frame to frame (a double click opening one) or to the console); a defect of the platform proposes the bug report (the confirmation of the uncaught exception handler, then the issue dialog),
  and is still rethrown; a cancellation shows nothing. An action started by an expression or an event (`FlexoAction.isStartedFromExpression()`:
  set by `FlexoBehaviourPathElement`, `CreationSchemePathElement`, `ExecuteFlexoBehaviour` outside a behaviour, and the listeners) goes to
  the console only, a defect too (its Java stack in the log at SEVERE: a binding may be evaluated again and again, decided after review),
  and its failure still reaches the expression; a cancelled one aborts the expression silently.
- Reported once: `FMLErrorReporter` marks what it reported (`isReported`, by identity, through the cause chain); the uncaught exception
  handler of the application and `reportToConsole` (GINA, other entry points) skip a failure already reported.
- Localized kind titles (decided: one per type, the text built by the throw site stays English): `kindKey` / `localizedKind`, keys
  `fml_*_error`, `fml_not_implemented`, `fml_internal_error` in the `General` dictionary of the core; the dialog shows the translated kind,
  the console records and the logs the English one (`kind`), so that fml-cli and the server keep a stable text (decided after review). The
  texts of the dialog are in the dictionary of flexo-ui (`FlexoUILocalization`).
- Navigation: the console record keeps the FML stack (`FMLConsole.logFailure`, `FMLLogRecord.getFMLStack()`); `FMLSourceNavigator`
  (flexo-ui), implemented by `FMLTechnologyAdapterController`, shows the compilation unit of a frame in the FML perspective and its line in
  the FML editor; the button is disabled when the active module has no FML perspective, or the frame no URI or no line. The FML console
  gets the same action on a record.
- The initializers of the behaviour actions (action, navigation, synchronization schemes in openflexo-ui; drop, link, draw-rectangle schemes
  in openflexo-diagram) no longer special-case `NotImplementedException`: the editor reports it, with the kind "Not implemented".
- GINA: `FIBController.handleException` is called by the model invocations (button action, setting the data of a widget, including a failing
  Java setter, value-changed action, click / double-click / right-click actions, table and browser actions); the reads for display are
  unchanged. Its default logs the failure once with its stack. `FlexoFIBController` routes an FML failure to `reportToConsole` and skips one
  already reported; any other failure (Java code of a controller, a binding of the component) keeps the GINA log with its Java stack.
- The diagram controls check the outcome of their action (`DrawEdgeControl`, `ContextualPalette`, `FMLControlledDiagramFloatingPalette`)
  and stop instead of going on with a missing shape or connector.
- Cancellation (known limit of phase 1, settled with the user): a cancelled initializer of a behaviour action (its parameters dialog)
  cancels the action (`FlexoAction.cancelExecution(FMLCancellation)`), whose cancellation is rethrown by its caller; for any other action an
  initializer returning false keeps the former status, and its undo recording is closed with its edits kept
  (`FlexoUndoManager.actionHasNotBeenPerformed`, decided after review); `doActionInContext` sets
  `HAS_BEEN_CANCELLED` for a cancellation anywhere in the cause chain, so every caller ends cancelled; `CreationSchemePathElement` passes a
  cancelled creation on. The edits of the outermost action are undone, nothing is reported. A cancelled initializer of an outermost action
  also closes the undo recording, which it used to leave open.
- Tests: `TestFMLErrorReporting` (flexo-foundation-test) gains the marking, `reportToConsole`, the kinds, the stack of the record, the
  cancellation of the callers, the presentation hook; `TestInteractiveFMLErrorReporting` (flexo-ui-test, UI test) checks the undo, one
  record and one presentation per failure, console only from an expression, the cancellation; `FIBWidgetFailureHandlingTest` (gina-swing,
  UI test) checks that a failing button action and a failing data set reach `handleException`. Not covered by a test: the dialog itself,
  the navigation to the FML editor, the cancelled initializer of `InteractiveFlexoEditor` (it needs a module), the diagram controls.
- Found by the review, not changed: a behaviour called from a `where` / `match` clause runs as a non-embedded nested action, which takes
  over the recording of the outer action (`FlexoUndoManager.willDo` / `hasSuccessfullyDone`): the edits of the outer action are then not
  undone when it fails, and a failing nested one undoes the whole outer recording. The other initializers keeping a `NotImplementedException`
  case (`CreateBasicVirtualModelInstanceInitializer`, `CreateDiagramInitializer`...) are not behaviour actions and are left as they are.

**Phase 5 done (2026-11-15), decided with the user.**
- fml-cli: `FMLCommandExecutionException` knows the line of the failing command (set by `FMLScript.execute`, the innermost script
  first) and the script that holds it (set by the `execute` directive); `getFailure()` gives the FML failure it stands for (its cause),
  `describe()` the text for the user: location, English kind, message and FML stack (`FMLErrorReporter.describe`). A command that cannot
  run because it is invalid throws `FMLInvalidCommandException`, `AbstractCommandInterpreter.executeCommand` too (it used to print the
  reason and return). An unchecked failure of a command in a script is wrapped with its line. The expression commands (expression,
  assignment, `assert`, `return`) go through `FMLEvaluation.convert`, so their cause is the typed `FMLError` (a strict
  `BindingEvaluationException` no longer escapes). An `assert` on a value that is not a boolean is an `FMLTypeError`.
- `FMLAssertException`: a message for every assertion (`assert failed: false`), the values of the operands of a binary one, no printed
  stack.
- Top-level `return` (decided: it ends the script successfully, the value printed): `return` did not parse in a script, the
  `ReturnException` branch of `FMLActionCommand` could not be reached. `fml.sablecc` gains `{return} kw_return expression?` in
  `fml_command`, `kw_return` is reserved in the `command` and `script` states too (decided with the user); new `FMLReturnCommand`. The
  specification (§2.5.2, chapter 12) is updated, the website pages are not regenerated.
- The interactive loop goes through `AbstractCommandInterpreter.executeAndReport`: any failure, a `RuntimeException` included, is
  described on the error stream (a defect also logged at SEVERE with its Java stack), and the loop goes on. The terminal of the
  application (`FMLTerminal`, openflexo-ui) does the same.
- `ExecuteDirective` throws its failures (to the caller, which writes them to err), naming the script; it closes its stream. A missing
  or unparsable script is an invalid command.
- `LaunchHeadlessFMLCLI` (decided): a script mode `fml-cli [--rc path] script.fmlscript`; exit 0 success (end or `return`), 1 foreseen FML
  error or failed assertion, 2 invalid script, invalid command or wrong options, 3 internal error; interactive: 0 on quit, 3 if the
  interpreter dies. `runScript` and `parseOptions` are callable from tests.
- No `printStackTrace` / `System.out` left on the paths of a failing command: `CommandParser`, `FMLScriptParser`, `Directive.evaluate`
  (its failure becomes the reason why the `service` directive is invalid), `FMLAssignation`.
- Server (decided): a failing FML action answers JSON {`kind` (English), `key` (`fml_*`, for a client to translate), `message`,
  `location` (innermost frame: `uri`, `line`, `column`, `behaviour`, `concept`, `instance`, `statement`), `stack`}, 400 for a foreseen
  error, 500 for a defect (`GenericController.fmlError`, `JsonSerializer.fmlErrorSerializer`). `GenericController.performAction` runs an
  action and answers its failure; `ConceptInstancesController.add`, `VirtualModelInstancesController.add` and `executeBehaviour` then
  stop and save nothing. `TerminalController`: an FML failure or a failed assertion answers the same JSON; a parse error or an invalid
  command stays 422. `BehavioursController` (it edits the FML, runs none) is unchanged. The English kind is the one of phase 4: the
  console, the logs, fml-cli and the server give the same stable text.
- Tests: `TestScriptFailureReporting` (fml-cli-test, scripts of `ScriptFailures/`): line and kind of a failing command, FML stack of a
  failing behaviour, assertion message, `return` and bare `return`, `execute` of a failing and of a missing script, exit codes, options.
  `TestCLICommandsExceptionManagement`: assertion messages, `assert` on a non-boolean, invalid command, the loop surviving a
  `RuntimeException`. `AutomatedTests` fails on any failure with its description. `TestFMLErrorResponses` (http-server): a failing
  behaviour answers 400 with its frame and does not save its instance, the terminal answers 400 / 422, the JSON of a defect.
- After review (decided with the user): an action not executed because it is not enabled answers 409 (kind `Not enabled`, key
  `fml_not_enabled`); a `return` in a script run by `execute` ends that script only; the terminal of the application prints and logs a
  defect of a typed command, without the bug report (as decision 9 of phase 4). Fixed after review: a missing script run by `execute` is
  located at the `execute` command; a JVM `Error` of a script gives exit 3 and a `StackOverflowError` no longer ends the shell;
  `CommandParser` keeps the cause of an unexpected failure and logs it; the failures of `load`, `ls`, `enter`, `more` are invalid
  commands (exit 2, 422), no longer FML errors; a `ModelDefinitionException` under `execute` is a defect.
- Known limits:
  - The edits a failing action made before its failure stay in memory on the server (no undo manager there) and the next successful
    request saves them: `SERVER-D-1` (openflexo-server), to fix later (decided with the user).
  - A failure evaluating the argument of a `service` directive is found when the command is parsed: an invalid command (exit 2).
  - The REST terminal answers the FML failure of a command run in a script by `execute` without the name and line of that script.
  - The JSON keeps the `resource_type` field of the other answers of the server (`FMLError`).
  - An action started by a command (a behaviour called from a script, from the terminal, or by the server) is still reported by the
    editor (console and a WARNING log), and described again by fml-cli on err, or answered by the server: two texts for one failure.
  - Exit code 3 of a script and the 500 of an endpoint are not covered by a test (no FML reaches a defect on purpose).
  - The `AutomatedTests` of other repositories (formod, city-mapping, digital-twin-demo, multi-process-challenge) still catch only
    `FMLAssertException`; another failure fails them as an error, whose message now carries the line.
  - The directives that load resources (`cd`, `enter`, `more`, `open`, `load`) still print the stack of a loading failure: not an FML
    runtime error, left.
  - `ConceptInstancesController` still turns a failed save into a `RuntimeException`.
  - Found on the way: `CORE-D-83` (the output of the commands is one static list).

**Phase 6 done (2026-11-16), decided with the user.** Inventory of the adapters (edition actions, fetch requests, factories,
silent catches) made first; one entry per repository, done one at a time, in this order: the core (`fml/ta` and the shared bases), then
the adapters of `CORE-D-73` (xlsx, xml, jdbc, rest), formod (b-ta), docx, owl, csv, diagram, emf, json, markdown, pptx, opc-ua, capella,
technology-adapters (dsl, xx), kafka, oslc, gina, odt. pdf, java, rhapsody and free-modelling-editor have no edition action.
- Scope per repository: the paths that FML execution goes through (edition actions, fetch requests, factories and builders of instances,
  instances overriding the strict accessors). The `printStackTrace` of loading and I/O code are left, and counted in the entry.
- A failure raised where the signature cannot declare it (a lambda, an accessor called by `select`) travels in `FMLFailureTunnel`, unwrapped
  where a method can declare it.
- A technology action that needs a behaviour, run elsewhere (an fml-cli command), is an `FMLTechnologyError` ("can only be executed inside
  a behaviour").
- `select` (`AbstractFetchRequest.execute`): a request that cannot be applied (its `performExecute` gives null) is an `FMLResolutionError`; a
  select with no virtual model instance to select from (`AbstractSelectFlexoConceptInstance`, `AbstractSelectVirtualModelInstance`) an
  `FMLNullReferenceError`; a `select unique` that finds several instances an `FMLCardinalityError`. The specification said one of them was
  returned without error: §10.2.1 and the user guide page "Queries with select" are rewritten (decided with the user); the website
  specification is regenerated (only `edition-actions.md` changed).
- Core done: the FML technology adapter (`CreateFlexoConcept`, `CreateFlexoBehaviour`, `CreatePrimitiveRole`,
  `CreateFlexoConceptInstanceRole`, `CreateContainedVirtualModel`, `CreateTopLevelVirtualModel`) evaluates through `FMLEvaluation`, rethrows the
  failure of its nested action (`FMLExecution.rethrowFailure`), raises `FMLCancellation`, a null container as `FMLNullReferenceError`, a
  hierarchy or resource failure as `FMLTechnologyError` (a `ModelDefinitionException` as `FMLInternalError`); `AbstractCreateResource` raises on
  a missing adapter, a null name or a null resource center; the core part of `CORE-D-73` (`AbstractAddFlexoConceptInstance`, see the entry).
- FML that relied on the former `select`: fml-cli-test `TestStock` (the guide, now one match), `PropertyInitializers` (one person),
  `TestFetchFromNullContainer` (the null case moves to `TestFMLErrorKinds`).
- Tests: `TestFMLErrorKinds` gains `uniqueSelectWithSeveralResults`, `selectFromNullModel`, `fmlAdapterActionWithNullContainer` (fixture
  `FMLAdapterFailures.fml`, the first test of the FML technology adapter).
- After review (decided with the user): `_performExecuteCreationScheme` gives the new instance to its action (`CORE-D-73`), which revealed
  `CORE-D-84` (fixed); a select from a null source raises `FMLNullReferenceError` in every adapter (each repository's entry); an inconsistent
  hierarchy stays an `FMLTechnologyError`. The cardinality message names the first three results only. To do in the repositories: five
  `Create*Resource` actions (xlsx, xml, owl, kafka, jdbc) wrap the typed errors of `AbstractCreateResource` again into an untyped
  `FMLExecutionException` (they catch `OpenflexoRuntimeException`); the `performExecute` of emf, jdbc (`AbstractPerformSQLQuery`) and rest return null,
  those of json, markdown and pptx an empty list, for a null source (rest: its select of the virtual model instance returned null; done in
  `REST-F-1`).
- openflexo-xlsx: `XLSX-F-2` (its `BACKLOG.md`), done. For it, the core gains the strict entry point `FlexoConceptInstance.getFlexoActorOrFail`
  (by default `getFlexoActor`), called by the strict readings of a role (`getFlexoPropertyValueOrFail`, `FlexoPropertyBindingVariable`,
  `FlexoPropertyPathElement`): the path element read a role through `getFlexoActor`, so an override of `getFlexoPropertyValueOrFail` was
  never reached for a role. An instance reading its actors from a technology overrides it (kept after review, decided with the user:
  roles through `getFlexoActorOrFail`, other properties through `getFlexoPropertyValueOrFail`; jdbc and rest move their reading of roles
  there in their own entries).
- openflexo-xml: `XML-F-1` (its `BACKLOG.md`), done; `XML-D-2` recorded (`AddXMLIndividual` never succeeds with a model slot). For it, the ontology assertions of `flexo-ontology` (shared with emf and owl) are strict
  (decided with the user): a failing, invalid or null assertion condition is an error, no longer taken as true; their values evaluate
  through `FMLEvaluation`. emf and owl only adapted at signature level until their own entries; there, `AddEMFObjectIndividual` and
  `AddOWLIndividual` put the new object in the model before evaluating the assertions, which can now fail: a half-built object stays (to
  fix in their entries).
- openflexo-jdbc: `JDBC-F-1` (its `BACKLOG.md`). `HbnException` is an `FMLTechnologyError`; the creation scheme of `CreateHbnObject` runs, and
  its failure is proven (`CORE-D-73`).
- openflexo-rest: `REST-F-1` (its `BACKLOG.md`). `BadCredentialsException` is an `FMLTechnologyError`; the factory of REST instances is a
  stub (`CORE-D-73` not applicable). With xlsx, xml, jdbc and rest, the adapters of `CORE-D-73` are done.
- formod (b-ta): `FORMOD-F-5` (its `BACKLOG.md`). A text that is not B still gives null from the parsing actions, tested by `BMethodology.fml`
  (decided with the user; to revisit with phase 7).
- openflexo-docx: `DOCX-F-1` (its `BACKLOG.md`).
- The other adapters in one batch (asked by the user, 2026-11-16), with the rules above, then reviewed once: `OWL-F-1`, `CSV-F-1`,
  `DIAGRAM-F-1`, `EMF-F-1`, `JSON-F-1`, `MARKDOWN-F-1`, `PPTX-F-8`, `OPCUA-F-1`, `CAPELLA-F-1`, `TA-F-1` (dsl, xx; in `TA-BACKLOG.md`),
  `KAFKA-F-1`, `OSLC-F-1`, `GINATA-F-1` (openflexo-gina), `ODT-F-1`. Each entry lists what was chosen beyond the rules, to validate.
  Exceptions kept to avoid breaking existing behaviour: a `GraphicalAction` value computed from a representation that does not exist yet
  (headless diagrams) leaves the feature unchanged; a missing template of an FML-controlled FIB gives null; a text that is not B gives
  null (formod).
- Not covered by new tests: the batch adapters (the existing suites run green); kafka, oslc, gina, odt have no test module.
- Left for later: `CORE-D-73` stays open for `new` of a reflected concept (null after a warning) and the concept derived from an inherited
  creation scheme; `XML-D-2` (`AddXMLIndividual` never succeeds with a model slot); the `printStackTrace` of loading and conversion code
  of every adapter (out of the scope decided with the user).
- The three failures seen before phase 3 are unchanged (same messages): markdown `TestMarkdownEditionActions` (an
  `UnresolvedTechnologySpecificAction` at the creation of the model), capella `TestLoadXXDocuments` (`CAPELLA-D-1`), diagram
  `TestFMLDiagramPrettyPrint3` (a parser position): none goes through the code changed by phase 6.
- Not checked: the running application (a browser label or a renderer that selects from a slot not resolved yet during loading now meets
  an error, no longer an empty result); the 117 `select unique` of the workspace, beyond what the suites run.
- Suites (2026-11-15): flexo-foundation-test 735, fml-cli-test 153, fml-parser 361, openflexo-integration-tests (4 projects), formod, and
  the adapters' suites green; markdown `TestMarkdownEditionActions`, capella `TestLoadXXDocuments`, diagram `TestFMLDiagramPrettyPrint3`
  red as before phase 3 (not attributed yet). ECJ compiles flexo-foundation.

**Phase 7 done (2026-11-16), decided with the user.**
- Exceptions defined in FML (asked by the user before phase 7 started): a new specialization of `FlexoConcept`,
  `FlexoException` (keyword `exception`, declared like an event; the former Java base exception `FlexoException` is now `OpenflexoRuntimeException`), instances
  `FlexoExceptionInstance`, created by `new` outside any container, as an event. An exception extends only exceptions and a concept never
  extends one (validation `ExceptionHierarchyMustBeConsistent`). Its message is its `@Renderer`, otherwise its name and the values of its
  own properties.
- `throw <expr>;` (`ThrowStatement`, built as `ReturnStatement`): a new exception instance is raised as `FMLUserError` (an `FMLError`
  carrying the instance); a caught error (an exception instance or a built-in error) is rethrown unchanged, its FML stack kept; no
  `throw "text"`. Throwing null is an `FMLNullReferenceError`. A body ending with `throw` satisfies any declared type.
- `try { } catch (T e) { } … finally { }` (`TryStatement`, `CatchClause`, itself a control graph whose binding model adds the variable,
  visible in its block only): several clauses, the first that catches runs; optional `finally`, run on every exit; no multi-catch. A clause
  names `FMLError` or one of its subtypes (visible without import, `AbstractFMLTypingSpace`, no import written back) or an FML exception:
  a built-in type catches errors of that class (`FMLError` catches FML exceptions too, the variable being the error), an FML exception its
  sub exceptions (the variable being the instance). `FMLInternalError`, `FMLCancellation` and `ReturnException` are never caught; naming
  `FMLInternalError`, `FMLCancellation` or `FMLExecutionException` is a validation error, an unreachable clause a warning. Only the thrown
  object is matched: an untyped `FMLExecutionException` wrapping an `FMLError` is not caught.
- A caught error is handled: not reported (the failed nested actions it stands for are forgotten by the execution scope,
  `FMLErrorReporter.failureHandled`), the edits made before it are kept. A caught error exposes `message`, `kind` (English kind, or the
  name of the exception) and `fmlStackTrace` (`getKind` / `getFmlStackTrace` on `FMLExecutionException`, `ExceptionInfoPathElement` on an
  exception instance, unless it declares a property of that name). The kind of an `FMLUserError` is the name of its exception
  (`fml_user_error` for the server key).
- `assert` in scripts: the failure is an `FMLAssertionError` (kind "Assertion failed", key `fml_assertion_error`), the cause of
  `FMLAssertException`, which stays the command wrapper with the line (chosen while implementing, to report: the ~25 `AutomatedTests`
  of the workspace that catch `FMLAssertException` are unchanged). `describe()` gives `Line 3: Assertion failed: x[1] == 2`; the REST
  terminal answers the condition as message.
- Leniencies of phase 6 (decided with the user): formod `CreateBPredicateFromString` / `CreateBExpressionFromString` raise
  `FMLTechnologyError` for a text that is not B (`BMethodology.fml` catches it, six places, `FORMOD-F-5`); gina-ta
  `ConfigureGINAFIBComponent` raises `FMLTechnologyError` for a missing or empty template (`GINATA-F-1`); the diagram `GraphicalAction`
  value of a representation not built yet stays lenient.
- Grammar: `kw_exception`, `kw_throw`, `kw_try`, `kw_catch`, `kw_finally` (reserved in `normal`; no FML of the workspace used them as
  names); `exception_decl`, `throw` and `try_statement` in `statement_without_trailing_substatement`. Scripts have no blocks: `try` is
  written in behaviours. Pretty-print, preserving and normalized: `} catch (T e) {`, `} finally {`.
- Specification: §5.6 (exceptions, later sections renumbered), §8.1, §8.10, §2.5.1, §12.6, §13.6, Appendix A.
- Tests: `TestFMLExceptions` (fml-cli-test, scripts `Exceptions/`, fixtures `Exceptions.fml` and `ExceptionsDefect.fml` with
  `PlatformDefect`, a Java class of the tests raising an `FMLInternalError`): catch by exact type, by super exception, by `FMLError`, first
  matching clause, no matching clause (type and frames), built-in error caught, return through a try, finally on an error, nested try,
  rethrow of an exception and of a built-in error (frames of the first throw), message without renderer, stack of a caught exception,
  uncaught exception, a caught failure of a nested call not reported (nothing on stderr), an internal error crossing `catch (FMLError e)`
  with its finally run. `TestExceptionsValidation` (flexo-foundation-test, fixtures `Exceptions.fml`, `InvalidExceptions.fml`): structure,
  binding scope of the variable, no import written back, each invalid use reported. `FMLParsingExamples/TestTryCatch.fml` in the
  pretty-print invariants (layouts, comments, empty blocks, the example of §8.10). `TestScriptFailureReporting` and http-server
  `TestFMLErrorResponses` adapted to `FMLAssertionError`.
- After review (2026-11-16): a `throw` rethrows the same error only from the catch block handling it (`FMLErrorTypes.whileHandling` /
  `isBeingHandled`); an exception kept and thrown again later is a new `FMLUserError`, with its own stack, reported again (a kept
  built-in error is rethrown and unmarked as reported); the built-in error names resolve after the `typedef` aliases of the unit; an
  exception declared in a concept is embedded in it (it was dropped, as an event still is: `CORE-D-86`); deleting a catch clause removes
  it from its try statement; the French values of the new keys are escaped (`\uXXXX`). Kept: the failure is forgotten before the catch
  block runs (a failure of the block replaces it, as in Java); the five keywords cannot be names of FML objects (`FMLObject.setName`,
  FME names); a failure of the B parser that is unchecked is a technology error (phase 6). Recorded: `CORE-D-85` (a try in a getter
  evaluated outside an action still reports the failure it catches). `ExceptionsDefect.fml` sits in the shared test resource center but
  imports a class of fml-cli-test: it only resolves there (flexo-foundation-test loads it without failing).
- `throws` (asked by the user after the first commit of phase 7, decided with the user, 2026-11-16): Java semantics. A behaviour
  declares exceptions after its arguments (`throws_clause` in every alternative of `behaviour_decl`, listeners included; keyword
  `throws`, used nowhere as a name in the workspace); `FlexoBehaviour.thrownTypes` (a PAMELA list of `Type`, which needs
  `isStringConvertable`: without it the meta-model fails to build; its adder translates to the typing space of the unit). The exceptions
  defined in FML are checked, the built-in error types unchecked (allowed in `throws` as documentation). Validation errors
  (`CheckedExceptions`): `CheckedExceptionsMustBeCaughtOrDeclared` on every statement (a `throw`, the calls of behaviours and `new` in its
  bindings, `delete`, `fire` towards the listeners of the model of the statement) unless an enclosing try block catches it or its
  behaviour declares it; a getter, a setter declare nothing; `ExpressionMayNotRaiseCheckedExceptions`; `ThrowsClauseMustBeValid` (types,
  and an override declaring more checked exceptions than the overridden behaviour). Static analysis: a call is checked against the
  behaviour it resolves to; the bindings of a statement are found through its PAMELA getters of type `DataBinding` (and those of the
  objects it embeds); a property initializer or a default value is not checked. Nothing changes at run time; scripts are not checked.
  Specification §7.1, §8.10, §2.5.1, Appendix A. Tests: `InvalidExceptions.fml` gains one case per rule, `Exceptions.fml` declares
  what it lets go, `FireCaught.fmlscript` (a listener's exception caught by the caller of `fire`).
  After review (2026-11-16): a built-in type in `throws` covers no FML exception (`throws FMLError` let an undeclared one pass);
  `delete this` runs no deletion scheme and raises nothing; `end match … unmatched:` is checked against its behaviour; the listeners of a
  `fire` include the inherited ones; the override rule leaves out creation and deletion schemes and listeners (as Java constructors); the
  commands of an fml-cli script are not checked (their validation would reject a call of a behaviour declaring an exception); an unresolved
  type in `throws` is reported as such. Limits kept: the listeners of a `fire` are those of the model of the statement, whatever their
  `from` (listeners of other models are missed, a local one listening elsewhere is counted); no precise rethrow (`throw e` of a variable
  caught as `FMLError` raises nothing checked); property initializers, argument default values, `@Enable` / `@Visible`, `@Renderer` and the
  `from` of a listener are not checked; the analysis finds the bindings of a statement by reflection on its PAMELA getters (a binding
  held otherwise is missed) and its cost in the editor is not measured.
- Left to their own entries: `CORE-F-12` (the user interface of exceptions and try statements; the factories create try statements and
  catch clauses without empty blocks, unlike the other control structures). Not covered by a test: the normalized
  pretty-print of a try statement built without source (from the user interface), `ConfigureGINAFIBComponent` (gina-ta has no test module).

Deferred from phase 1:
- `FMLEvaluation` moves to phase 3: its conversions depend on the strict mode of Connie.
- Localized messages move to phase 4.
- `InteractiveFlexoEditor` (openflexo-ui) is unchanged until phase 4.

Found by the tests of phase 1: `CORE-D-80`. In the fixture, `this.middle()` is needed.

Tests:
- `TestFMLExecutionStack` (fml-cli-test, scripts of `ExecutionStack/`) checks the frames of a failure three behaviours deep and of a failing
  creation scheme, and a `return` in creation and deletion schemes.
- `TestExecutionFailures` checks that every failure has a located innermost frame, and gains `ValuedReturnInCreationScheme`.
- `TestFMLErrorReporting` (flexo-foundation-test) checks the branches of the editor, the outermost-action scope, the no-editor path and the
  classification of failures.

**Acceptance criteria.**
- The scripts of `ExecutionFailures/` (`TestExecutionFailures`, fml-cli-test) gain one script per kind of error. Each asserts the type,
  the line of the failing statement and the frame of the behaviour, and that nothing is printed to stderr.
- An error three behaviour calls deep (through an expression, `new` and `fire`) reaches the top unwrapped, with three frames, and is
  reported once.
- A `return` in a creation or a deletion scheme is not an error.
- In the application, a failing action scheme shows one dialog, the console holds the record, and its edits are undone.
- The full suites stay green once the FML that relied on a lenient null is fixed: core through the openflexo-dev composite,
  openflexo-integration-tests, formod. The `openflexo-reviewer` agent is run on every phase.

### CORE-F-12 — User interface of FML exceptions and try statements  ·  `TODO`

**GitHub:** [openflexo-team/openflexo-core#128](https://github.com/openflexo-team/openflexo-core/issues/128)

**Problem.** `CORE-F-11` phase 7 added exceptions (`FlexoException`, keyword `exception`), `throw` and `try … catch … finally`
(`ThrowStatement`, `TryStatement`, `CatchClause`) to the language. openflexo-ui knows none of them: they show with the icon and the
inspector of their parent class (`FlexoConcept`, `EditionAction`), and nothing creates them but the FML editor.

**Options.** Icons in `FMLIconLibrary`; inspectors (`FlexoException` as `FlexoEvent.inspector`; `CatchClause` with its type and
variable; `ThrowStatement` as `ReturnStatement.inspector`), added to `TestFMLInspectors`; a creation wizard of an exception as
`CreateFlexoEvent`; the insertion of a try statement in the control graph editor.

**Acceptance criteria.** The FML perspective shows an exception and the three statements with their own icon and inspector; an
exception can be created from the browser; the inspector tests cover the new inspectors.

### CORE-F-14 — Arguments of a behaviour designated by their name  ·  `DONE`

**GitHub:** [openflexo-team/openflexo-core#129](https://github.com/openflexo-team/openflexo-core/issues/129)

**Problem.** The argument `aValue` of a behaviour `foo(String aValue)` could only be read as `parameters.aValue`, unlike the local
variables of the same behaviour, which are designated by their name.

**Decision.** An argument is a variable of the binding model of its behaviour (`FlexoBehaviourArgumentBindingVariable`), readable and
assignable by its name, as in Java. It hides a property of the concept of the same name (`this.aValue` still reaches the property); a
local variable must not have the name of an argument (validation error). `parameters.aValue` stays supported, deprecated: the
validation reports it (`LegacyParametersNotationIsDeprecated`, in `FMLObject`) and its fix rewrites the binding.

**Migration.** `MigrateLegacyParametersNotation` (fml-cli, run by the `migrateLegacyParameters` task of `openflexo-headless`, dry run by
default) rewrites the `.fml` of resource centers. The model decides, the text is edited where it must be (no re-serialization):
`parameters.a` becomes `a`; when an argument has the name of a property (`label = parameters.label`, the usual constructor pattern),
the unqualified uses of that name in the body become `this.label`, because they meant the property. FML that is not migrated keeps
working, except in that collision case, where an unqualified name now designates the argument. The generators of FML (Java code)
write the new notation, with `this.` on the property they assign.

**Resolution (2026-11-18).** Done across the repositories (commit `368ad682a` in openflexo-core, and one commit per repository whose FML
or generator changed). Tests: openflexo-core, formod, modelers, the technology adapters, openflexo-integration-tests, fme and http-server
green; `TestFMLDiagramPrettyPrint3` (diagram-ta) and `TestMarkdownEditionActions` (markdown-ta-test) were red identically before this change
(stale fixtures, fixed separately).
Accepted consequence: FML of a third party that is not migrated, or generated before this change, and that assigns a property of the name of an
argument (`label = parameters.label;`) changes meaning without any diagnostic; a variable assigned to itself (`label = label;`) is reported.
Also decided: `this.role` is accepted as the target of a role in `role = TA::Action(...)`; the deprecation fix is not offered for a name that
hides a property. Known, not fixed: the deprecation warning is shown twice per object (a rule declared on `FMLObject` is reached twice through
diamond inheritance in PAMELA).

**Left.** `.fml` that are not resources of a resource center (parsing and pretty-print inputs: `FMLParsingExamples`,
`TestFMLPrettyPrint*`) keep the legacy notation on purpose, they exercise it; the `.inspector` and `.fmlscript` that use `parameters.`
are not rewritten.
