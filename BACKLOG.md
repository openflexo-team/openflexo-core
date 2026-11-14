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


## FML runtime errors

### CORE-F-11 — Typed FML runtime errors, raised where detected, reported once with the FML location  ·  `IN PROGRESS`

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
FMLExecutionException (checked, a FlexoException) carries the FML stack (frames); the plain type is the untyped form, for the throw
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
   - The `catch (FlexoException)` sites fixed before `FMLExecutionException` moves.
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
  wraps one that has a stack starts with its frames. None of the ~200 `catch (FlexoException)` of the workspace catches a failure that used to pass
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
    property) into the checked `FMLHostError` / `FMLResolutionError` waits for phase 3: until the `catch (FlexoException) → null` of the
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
    The status of the callers is to be settled when phase 4 raises it.
  - The report goes to the FML console and the log. The running application shows nothing more until phase 4 (`InteractiveFlexoEditor`
    unchanged), and the former `System.err` pretty-print of the failing behaviour is gone.
  - An action started by an expression (fml-cli, GINA, server) is an outermost action: it is reported, and its caller still receives the
    failure. GINA prints it again until phase 4.
  - A `ConcurrentModificationException` raised in a behaviour called from an expression is now an `FMLInternalError`; Connie
    (`BindingPath`) used to turn it into null.
  - `DeleteAction` no longer swallows an unchecked exception: a deletion path of a technology adapter that threw one silently now fails
    (`XML-D-1`, `PPTX-D-6`). The other adapters were not run.

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
