# openflexo-core — Backlog

Features and evolutions to develop in openflexo-core, near to long term. Known bugs go to `KNOWN_DEFECTS.md`
(identifiers `CORE-D-<n>`), not here.

Identifiers are stable and never reused: `CORE-F-<n>`. Reference them from commits and from other entries.

Status legend: `TODO` · `IN PROGRESS` · `BLOCKED` · `DONE` · `DEFERRED`.

---

## FML language

### CORE-F-1 — Arbitrary min/max bounds on FlexoRole cardinality  ·  `TODO`

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

**Problem.** `FlexoConcept.getDefaultDeletionScheme()` returns the first accessible deletion scheme, so which scheme is "the default"
depends on declaration order (and inheritance), and its intended semantics are unclear. The notion is not meant to be kept as is.

**Where it is used.**
- `unmatched: delete()` in an `end match` (`EndMatchActionNode.finalizeDeserialization()`).
- `FlexoConceptInstance.delete()`, hence the pruning of a behaviour's default matching set (`CORE-D-1`).
- `DeleteFlexoConceptInstance` when no deletion scheme is given.

**Why it matters.** A concept closed by an `end match` that declares several deletion schemes silently gets the first one — possibly
one that deletes federated artifacts instead of only releasing references. `CORE-D-2` makes it worse: `unmatched: method()` falls back
to the first action scheme, which may be a deletion scheme.

**To decide.**
- Drop the notion: require an explicit deletion scheme wherever more than one is accessible (validation error), or
- Redefine it explicitly (e.g. an annotation marking the default deletion scheme), with a validation rule when it is ambiguous.
- What `FlexoConceptInstance.delete()` does when no scheme can be chosen.

**Related.** Fix together with `CORE-D-1` and `CORE-D-2` (see [`KNOWN_DEFECTS.md`](./KNOWN_DEFECTS.md)).

### CORE-F-5 — Single identifier token in the FML grammar  ·  `DEFERRED`

**Problem.** `fml.sablecc` lexes three identifier kinds (`lidentifier`, `uidentifier`, `cidentifier`) and relies on the case of the
first letter to stay LALR(1). The distinction is uncomfortable (a one-letter type lexes as a constant, `cidentifier` matches the empty
string, `_FOO` is a constant) and leaks into the Java factories. Wanted: one `ident` token and a grammar that stays unambiguous.

**Findings (2.99).** Feasible: a prototype generates with 0 SableCC conflicts and parses the workspace corpus (532 `.fml`/`.fmlscript`)
exactly like the current grammar. It needs nine grammar changes, each removing a type-vs-expression ambiguity, and four of them change
the language: no `Type.method()` as a dedicated form (static call resolved semantically), casts written as `( expression )` checked
semantically (no generic/array casts, `(Foo) -x` not a cast), no assignment in call arguments (kept in `where`), `x = Foo` parsed as an
expression instead of a type, escaped identifiers as a single token.

**Deferred to 3.0**, together with the Java adaptation (~17 files in `fml-parser`, plus `fml-cli`).

**Acceptance criteria.**
- `fml.sablecc` has a single identifier token; SableCC reports no conflict.
- The `.fml`/`.fmlscript` corpus and the fml-parser / foundation / modelers tests pass as before the change.
- Static calls, casts, and type-valued FML parameters are resolved by the semantic layer.

**Material.** Analysis, prototype grammar and experiment harness: [`fml-parser/doc/single-identifier/`](./fml-parser/doc/single-identifier/README.md).

## Container-based user interfaces

### CORE-F-4 — Renaming a concept orphans the components named after it  ·  `DONE`

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
