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
one that deletes federated artifacts instead of only releasing references. (`CORE-D-2`, fixed: `unmatched: method()`
no longer falls back on the first action scheme.)

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

### CORE-F-7 — Clabjects: a multi-level extension of FML  ·  `DEFERRED`

**Problem.** FML separates `FlexoConcept` from `FlexoConceptInstance`, so stacked type/instance levels (product model →
series → item, the two-level emulation of the FME, the MULTI process challenge) duplicate every notion. Wanted: a language,
an extension of FML sharing its grammar (FML itself unchanged), whose elements are clabjects (concept and instance at once).

**Current state.** Exploratory study only, nothing implemented. Directions taken: no mandatory potency (optional, decremented at each refinement
step), no declared levels;
progressive refinement with values inherited by delegation; `.fml.rt.xml` replaced by textual FML (instances only) as a
first milestone. Analysis, examples, hard points and proposed path: [`design/clabjects.md`](./design/clabjects.md).

**To decide.** Valuation operator (`:=`), own features and optional classification, conflict rule shared with behaviours,
`select`/`match` semantics, deletion of an intermediate clabject, interoperability with FML.

**Acceptance criteria.** To be written per milestone (textual FML@RT, then clabjects in the FME).

### CORE-F-10 — Decide the status of the specification against the implementation  ·  `TODO`

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

## Technical debt

### CORE-F-8 — Remove the XML serialization of VirtualModels and the deprecated FML API  ·  `DONE`

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

**Problem.** The concept-creation wizards had a "Define inspector" step that built a `FlexoConceptInspector` with an entry per chosen
property. That model had no FML syntax, so the inspector was lost at the first save, and it was removed with it (CORE-F-8 phase 5). The
inspector of a concept is now a `Xxx.inspector` GINA component in the `Xxx.fml/` container, written by fml-gina-extension's
`CreateInspector`, which core and fml-technologyadapter-ui cannot call (it depends on them).

**Options.** A post-creation hook that a UI module contributes (fml-gina-extension runs `CreateInspector` with the chosen properties);
or the wizard step moves to fml-gina-extension.

**Acceptance criteria.** Creating a concept (core, diagram-ta, jdbc-ta, gina-ta wizards) can produce its container `.inspector`, saved
with the model and shown in the running application.

