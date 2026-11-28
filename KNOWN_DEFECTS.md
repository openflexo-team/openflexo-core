# openflexo-core — Known defects

Bugs of openflexo-core that are known and not fixed yet. Each entry says what is verified, how to reproduce it, and how to work
around it. Features and evolutions go to [`BACKLOG.md`](./BACKLOG.md), not here.

Identifiers are stable and never reused: `CORE-D-<n>`. Reference them from commits and from other entries.

Status legend: `TODO` · `IN PROGRESS` · `BLOCKED` · `DONE` · `DEFERRED`.

Fixed defects are moved to [`FIXED_DEFECTS.md`](./FIXED_DEFECTS.md) once their status is `DONE`.

---

## FML matching engine

`CORE-D-1` and `CORE-D-2` are linked to each other and to [`CORE-F-2`](./BACKLOG.md) (the "default" deletion scheme): they are
meant to be fixed together, with a broad test run through the openflexo-dev composite, since fixing one changes what the others do.

### CORE-D-1 — A behaviour's default matching set is typed by its first standalone `match`  ·  `TODO`

**GitHub:** [openflexo-team/openflexo-core#31](https://github.com/openflexo-team/openflexo-core/issues/31)

**Symptom.** In a behaviour executing standalone `match` actions (no `in <matchingSet>`) on several concepts, only the first matched
concept is ever found again: every later concept is never matched, and a new instance is created on every execution.

**Reproduction (verified 2026-09 by execution).** Two standalone matches, on `MirrorA` then `MirrorB`, each over one source instance;
running the behaviour twice leaves 1 `MirrorA` and 2 `MirrorB`:

```fml
syncBoth() {
	for (SourceA sa : select SourceA from this) {
		match MirrorA from this where (a=sa) unmatched: new MirrorA::init(sa);
	}
	for (SourceB sb : select SourceB from this) {
		match MirrorB from this where (b=sb) unmatched: new MirrorB::init(sb);
	}
}
```

**Mechanism — verified in the code.**
1. `MatchFlexoConceptInstance.execute()` uses `FlexoBehaviourAction.initiateDefaultMatchingSet(this)` when no matching set is given.
2. `initiateDefaultMatchingSet()` creates ONE `MatchingSet` per behaviour execution and returns the same one afterwards.
3. `MatchingSet(MatchFlexoConceptInstance, …)` takes its concept type — hence the instances it holds — from that first match.
4. At the end of the control graph, `FlexoBehaviourAction.executeControlGraph()` calls `finalizeDefaultMatchingSet()`, which calls
   `delete()` on every unmatched instance of that set, i.e. runs the concept's default deletion scheme (see `CORE-F-2`).

**Impact of a fix.** Typing the default set per concept switches pruning ON (step 4) for the secondary concepts, everywhere standalone
matches are used.

**Workaround.** One explicit matching set per concept, threaded through every match and closed by an `end match`
(`begin match X from this` / `match X in set …` / `end match X in set unmatched: delete()`), as done in
`openflexo-integration-tests/city-mapping` (`Mapping.fml`, `synchronization()`).

### CORE-D-12 — The `where` clause of `begin match` is ignored  ·  `TODO`

**GitHub:** [openflexo-team/openflexo-core#32](https://github.com/openflexo-team/openflexo-core/issues/32)

**Symptom.** `begin match X from this where (condition)` parses without any warning, but the matching set holds every instance of `X`,
whatever the condition. An `end match … unmatched: delete()` then deletes instances the condition was meant to exclude.

**Reproduction (verified 2026-09-15 by execution).** With two `Mirror` instances labelled `"keep"` and `"drop"`, after
`pruneKeepTagged()` no `Mirror` is left, instead of the `"drop"` one:

```fml
public pruneKeepTagged() {
	MatchingSet<Mirror> mirrors = begin match Mirror from this where (selected.label == "keep");
	end match Mirror in mirrors unmatched: delete();
}
```

**Mechanism — verified in the code.** `BeginMatchActionNode.buildModelObjectFromAST()` (fml-parser) reads the concept name and the
`from` clause, but never the `where_clause` of the `begin_match_action` production: no `MatchCondition` is added to the
`InitiateMatching`, although `MatchingSet(InitiateMatching, …)` does filter the instances with those conditions.

**Consequence — verified 2026-11-11 by execution (test `KnownDefects/CORE-D-12.fmlscript`, fixture `KDMatching`).** Filtering
the *sources* does not protect the *targets*: with sources `a` (managed), `b` (unmanaged), `c` (managed) and targets `a`, `b`, a
reconciliation whose `begin match` and source loop both filter on `managed` creates `c` and **deletes the unmanaged target `b`**,
because no `match` claims it. The workaround formerly given here and in the spec ("filter in the matches") is therefore wrong; it
was withdrawn from §10.9.5 and Appendix B. Found by the external audit of the FML language of 2026-10-23 (its §3.2).

Side observation: when the matched concept declares no deletion scheme, `end match … unmatched: delete()` deletes nothing and
says nothing (the same test passes without the `delete() { }` of `Target`). Related to [`CORE-F-2`](./BACKLOG.md).

**Workaround.** Close with `end match … unmatched: delete()` only a set whose domain is every instance of the concept in that
container; for a narrower domain, omit the `end match` and delete the leftovers explicitly.

## FML properties

### CORE-D-3 — The parameter name declared in a `set(...)` block is ignored  ·  `TODO`

**GitHub:** [openflexo-team/openflexo-core#33](https://github.com/openflexo-team/openflexo-core/issues/33)

**Symptom.** In a get/set property, the value assigned through the `set` block is only reachable as `value`, whatever name is
declared in `set(Type name)`. Using the declared name gives an invalid binding (`BindingVariable <name> does not exist`, logged at
parse time) and, at run-time, the assignment silently does nothing. `parameters.<name>` does not work either.

**Reproduction (verified 2026-09 by execution).** After `setBoth("X")`, `a` is still null while `b` is `"X"`:

```fml
public concept Holder {
	String a;
	String b;
	String viaDeclaredName {
		String get() { return a; }
		set(String aName) { a = aName; }    // invalid binding: aName does not exist
	};
	String viaValue {
		String get() { return b; }
		set(String aName) { b = value; }    // works, although the parameter is named aName
	};
	public setBoth(String x) {
		viaDeclaredName = parameters.x;
		viaValue = parameters.x;
	}
}
```

**Mechanism — verified in the code.**
1. The variable exposed in the `set` block is a `SetValueBindingVariable`, named after `GetSetProperty.getValueVariableName()`
   (`ControlGraphBindingModel.handleSetValueBindingVariable()`), whose default value is `value`.
2. Nothing in fml-parser calls `GetSetProperty.setValueVariableName()`: the name declared in the `set_decl` production is never
   transferred to the model (see `GetSetPropertyNode`).

**Workaround.** Always declare the setter parameter as `value` (`set(String value) { label = value; }`), as done in
`flexo-test-resources` `FML/Library.fml`.

### CORE-D-11 — An expression property read once keeps its old value  ·  `TODO`

**GitHub:** [openflexo-team/openflexo-core#34](https://github.com/openflexo-team/openflexo-core/issues/34)

**Symptom.** Once an expression property (`values …`) has been read, a later read returns the same value although what it depends on
has changed.

**Reproduction (verified 2026-09-15 by execution).** With the following concept, from a script: after creating two items, reading
`box.itemCount` (2), then calling `box.removeOnly(a)`, `box.items.size` is 1 but `box.itemCount` is still 2. Without the first read,
`box.itemCount` is 1 after the removal. Deleting the item as well (`delete parameters.item;`) changes nothing.

```fml
concept Box {
	Item[0,*] items;
	int itemCount values items.size;
	public removeOnly(Item item) {
		items.remove(parameters.item);
	}
}
```

**Mechanism — not investigated yet.** Presumably a value cached on first evaluation and not invalidated when the role changes; where
the value is cached (the expression property, its binding path element, or the concept instance) is to be found.

**Workaround.** None at FML level for a value read from outside; inside a behaviour, compute the value directly (`items.size`).

### CORE-D-74 — The initializer of a property is lost on save once the property has a `with` clause  ·  `TODO`

**GitHub:** [openflexo-team/openflexo-core#35](https://github.com/openflexo-team/openflexo-core/issues/35)

**Symptom.** A property declared with an initializer in the Java style (`Part first = new Part();`, or
`Person selected = select unique Person from container;`) loses it on save as soon as its role is given a property value (in the
inspector, for instance its `container`): the text is written `Part first with ConceptInstance(container=this);`, without the
initializer, and the initializer is gone after the next load.

**Mechanism — verified by the review of `CORE-D-51` (probe, 2026-11-14).** The grammar has no place for an initializer in the `with`
form of a declaration (`{fml}` and `{fml_fully_qualified}` alternatives of `inner_concept_decl` in `fml.sablecc`), so
`FlexoRolePropertyNode` writes the initializer only when no `with` clause is required (`writesInitializer`, `hasInitializer()` with
`!requiresWithClause()`).

**Options.** Extend the grammar so that a `with` declaration may end with `= initializer` (then build and print it); or refuse to set a
property value on a role that has an initializer.

**Workaround.** Do not set a property of the role of a property that has an initializer; initialize it in the creation scheme instead.

---

## FML behaviours

### CORE-D-4 — A parameter default value does not allow omitting the argument in `new`  ·  `TODO`

**GitHub:** [openflexo-team/openflexo-core#36](https://github.com/openflexo-team/openflexo-core/issues/36)

**Symptom.** A creation scheme parameter declaring a default value (`int capacity=10`) must still be passed explicitly: a `new`
expression omitting it does not resolve, and evaluates silently to null at run-time. **Decided 2026-11-11:** a call may omit trailing
arguments that have a default value (spec §7.2 and §7.4 now say so); this entry stays a defect of the implementation.

**Reproduction (verified 2026-09 by execution).** With the following concept, `newShelf("Fiction")` returns null; the parser logs
`cannot find constructor null for type Shelf with arguments [parameters.label]`, then
`DataBinding new Shelf(parameters.label) still invalid at the end of process`. Passing both arguments
(`new Shelf(parameters.label, 20)`) works.

```fml
public model Library {
	public Shelf newShelf(String label) {
		return new Shelf(parameters.label);
	}
	public concept Shelf {
		String label;
		int capacity;
		create(required String label, int capacity=10) {
			label = parameters.label;
			capacity = parameters.capacity;
		}
	}
}
```

**Mechanism — not investigated yet.** The constructor lookup performed while resolving the `new` binding
(`CreationSchemePathElement`, fml-parser binding factory) apparently matches creation schemes on the number of supplied arguments,
without considering parameters with a default value; to be confirmed in the code.

**Workaround.** Pass every argument explicitly, or declare a named creation scheme with fewer parameters
(`create::withDefaultCapacity(String label)`, reached with `new Shelf::withDefaultCapacity(...)`), as done in `flexo-test-resources`
`FML/Library.fml`.

### CORE-D-49 — Behaviour signatures, overload selection and overriding follow inconsistent rules  ·  `TODO`

**GitHub:** [openflexo-team/openflexo-core#37](https://github.com/openflexo-team/openflexo-core/issues/37)

**Symptom.** Five related observations, each verified 2026-11-11 by execution (fixtures `KDOverloads`, `KDSignatures`; tests
`KnownDefects/CORE-D-49.fmlscript` and `KnownDefectsJavaTests.coreD49_*`):
1. `f(int x)` and `f(String x)` are reported as duplicates ("Duplicate behaviour f(x)"), while `g(int x)` and `g(int y)` are not.
2. With `choose(Object o)` declared before `choose(String s)`, `this.choose("x")` calls the `Object` one.
3. A child `String h()` overriding a parent `int h()` is accepted without error.
4. A concrete concept that inherits an abstract behaviour without implementing it is accepted without error.
5. Not covered by a test: `overrides(FlexoBehaviour)` requires equal parameter types, but dynamic dispatch
   (`getMostSpecializedBehaviour`) uses the assignable lookup, so a child `f(Object)` is dispatched in place of a parent `f(String)`.

**Mechanism — verified in the code.** `FlexoBehaviour.getSignature()` is built from the parameter *names*
(`getParameterListAsString`, the type-based version is commented out), and `BehaviourSignatureMustBeUnique` compares those strings.
`FlexoConcept.getFlexoBehaviour(name, types)` returns the first declared behaviour whose parameters are assignable, not the most
specific. No validation rule compares the return type of an override. `NonAbstractFlexoConceptMustImplementAllPropertiesAndbehaviours`
relies on `abstractRequired()`, which only looks at properties. Found by the external audit of the FML language of 2026-10-23 (its §3.5).

**Workaround.** Give overloads distinct names; declare the most specific overload first; keep override return types identical.

---

## FML types

### CORE-D-5 — A `FlexoConceptInstanceType` built with an empty URI is reported as resolved  ·  `TODO`

**GitHub:** [openflexo-team/openflexo-core#38](https://github.com/openflexo-team/openflexo-core/issues/38)

**Symptom.** A `FlexoConceptInstanceType` created with a null or empty concept URI and no concept (typically when the URI of the
concept could not be determined while parsing) is considered resolved, so it is never resolved later and stays an
`UndefinedFlexoConceptInstanceType(null)`. Every binding using that type stays invalid, and at run-time the corresponding expressions
silently evaluate to null.

**Known occurrence (fixed).** Imported VirtualModels whose URI is computed (no `@URI`) were typed with an empty URI by
`FMLTypingSpaceDuringParsing.resolveType` (fml-parser), so `new Catalog() with (name=...)` in `flexo-test-resources`
`FML/Library.fml` silently returned null. That cause was fixed by using the URI of the compilation unit resource; the rule below,
which turned it into a silent failure, remains.

**Mechanism — verified in the code** (`FlexoConceptInstanceType`, flexo-foundation).
1. `isResolved()` returns `flexoConcept != null || StringUtils.isEmpty(conceptURI)`: an empty URI counts as resolved.
2. `resolve()` only acts when `flexoConcept == null && StringUtils.isNotEmpty(conceptURI) && customTypeFactory != null`, so such a type
   is never resolved either.
3. Nothing reports the situation: the only visible trace is the unresolved binding logged by `attemptToFixInvalidBindings`.

**To decide as part of the fix.** Whether a type with neither concept nor URI must be reported as unresolved (and warned about), or
rejected at construction; check the callers relying on the current rule (e.g. the `UNDEFINED_FLEXO_CONCEPT_INSTANCE_TYPE` constant,
built with a null concept).

**Workaround.** None at FML level; when this symptom appears, look for the place where the type is built with an empty URI.

## FML control structures and edition actions

### CORE-D-10 — Deleting a concept instance leaves it in the multiple roles referencing it  ·  `TODO`

**GitHub:** [openflexo-team/openflexo-core#39](https://github.com/openflexo-team/openflexo-core/issues/39)

**Symptom.** After `delete x;`, the deleted instance is still an element of a multiple-cardinality role (`Book[0,*] books`) holding
it: the role keeps its size, and the element is a deleted instance whose concept is null.

**Reproduction (verified 2026-09-15 by execution).** With a concept `Box` declaring `Item[0,*] items` and two items `a` and `b`,
after `delete parameters.item;` called with `a`, `box.items.size` is still 2 and `box.items.contains(a)` is true. The deletion
scheme of `Item` did run.

**Mechanism — partially verified in the code.** `DeleteAction.execute()` calls `FlexoConceptInstance.delete()`, which runs the
deletion scheme (`deleteWithScheme()`), then removes the instance from its container and from its VirtualModelInstance only. Nothing
removes it from the roles of other instances; where such a clean-up should happen is not investigated yet.

**Workaround.** Remove the instance from the role before deleting it — `books.remove(parameters.book); delete parameters.book;`,
as done in `flexo-test-resources` `FML/Library.fml` (`Shelf.removeBook`).

### CORE-D-13 — `new Concept::behaviour()` resolves only when the concept declares no creation scheme  ·  `TODO`

**GitHub:** [openflexo-team/openflexo-core#40](https://github.com/openflexo-team/openflexo-core/issues/40)

**Symptom.** A call naming a behaviour whose parameters are supplied elsewhere — typically a diagram palette binding, where the drop
dialog asks the user for them — is reported as an invalid binding: `Invalid binding value: new FunctionalGoalGR::createFunctionalGoal()
reason: unresolved path element new FunctionalGoalGR::createFunctionalGoal()`. The very same call on a sibling concept validates.

**Reproduction (verified 2026-09-17 by execution).** In `formod-rc`, `SysMLKaos/SysMLKaos.fml/GoalModelingDiagram.fml`, nine palette
bindings call drop schemes with no argument. Eight validate, including `new ContributionGoalGR::createContributionGoal()` whose drop
scheme takes three parameters. The ninth, `new FunctionalGoalGR::createFunctionalGoal()`, does not — and `FunctionalGoalGR` is the
only one of the nine concepts that also declares creation schemes (`create::createTopFunctionalGoal`, `create::representTopFunctionalGoal`).

**Mechanism — verified in the code.** `CreationSchemePathElement.resolve()` asks `FMLBindingFactory.retrieveConstructor()`, which ends
in `FlexoConcept.getCreationScheme(name, arguments)`; that one requires `cs.getParameters().size() == arguments.length`, so a
parameterless call never matches a parameterized drop scheme. What saves the other eight is the fallback right below: when the concept
`!hasCreationScheme()`, `resolve()` sets `resolvedAsNoConstructorIsDefined` and the path element counts as resolved although no
behaviour was found. So the binding is not really resolved anywhere; `FMLDiagramPaletteElementBinding.getDropScheme()` then falls back
to "the first DropScheme declared by the bound concept", which is right by luck and wrong as soon as a concept has several.

**Workaround.** Pass the arguments explicitly in the call — `new FunctionalGoalGR::createFunctionalGoal("Goal", "", "")` — which makes
the arity match. They then act as the values the drop dialog starts from.

### CORE-D-44 — Looking up an unknown concept loops forever between compilation units that import each other  ·  `TODO`

**GitHub:** [openflexo-team/openflexo-core#41](https://github.com/openflexo-team/openflexo-core/issues/41)

**Symptom.** `StackOverflowError`, the stack made of `FMLCompilationUnit$FMLCompilationUnitImpl.getFlexoConcept(FMLCompilationUnit.java:814)`
only.

**Verified (2026-11-09).** Reached by a first version of CORE-F-8 phase 4, which looked the concept of a role up in its declaring
compilation unit by URI: loading Formose (`FormodContainerUIsTest.test0LoadFormose`, formod-module) then overflowed. That call was
removed (the role looks its concept up in the `VirtualModelLibrary`, as before); the defect itself is not fixed.

**Mechanism — read in the code.** The deprecated `FMLCompilationUnit.getFlexoConcept(String)` searches its VirtualModel, then calls
itself on each loaded imported compilation unit, skipping only itself: when two units import each other and neither declares the name,
the calls never end.

**Expected.** A lookup visits each compilation unit once (a set of visited units), or the method goes with the other deprecated API
(phase 5 of CORE-F-8).

### CORE-D-56 — A statement written without braces is printed back with braces and a broken indentation  ·  `TODO`

**GitHub:** [openflexo-team/openflexo-core#42](https://github.com/openflexo-team/openflexo-core/issues/42)

**Symptom.** When a compilation unit is printed back from its parsed text (what the FML editor saves), an `if`, `else`, `while`,
`for` or `do` whose body is a single statement written without braces gets braces, and the closing brace goes on a line of its own
with a wrong indentation: `if (n > 0) n = 1; else n = 2;` is saved as `if (n > 0) { n = 1;⏎} else { n = 2;⏎}`. The meaning is kept;
the file is rewritten at every save.

**Reproduction (verified 2026-11-12 by execution, `KnownDefectsRoundTripTest.coreD56_*` in fml-parser, run with
`OPENFLEXO_KNOWN_DEFECTS` set).** Parse a unit whose behaviour holds the statement, print it with `getFMLPrettyPrint()`, compare
with the source. Simple `if/else` and `while` fail as well as the nested and `do` forms.

**Mechanism — read in the code.** `ConditionalNode`, `WhileActionNode` and `ExpressionIterationActionNode` always append
`staticContents("{")` / `staticContents(LINE_SEPARATOR, "}", "")` around the body, with the brace fragment of the parsed text when it
exists; when the source has no brace there is no fragment, so the static text is inserted. Older than `CORE-D-46`; before that fix
the nested and `do` forms were not built at all, so they were kept word for word (with a wrong meaning). Found by the review of
`CORE-D-46`.

**Workaround.** Brace the bodies of control statements.

---

## Instantiation

### CORE-D-73 — A creation run by a technology action, or through a technology factory, still hides a failing creation scheme  ·  `IN PROGRESS`

**GitHub:** [openflexo-team/openflexo-core#43](https://github.com/openflexo-team/openflexo-core/issues/43)

**Symptom.** Since `CORE-D-47`, a failing creation scheme fails the `new` that runs it and leaves no partly built instance. Two
creation paths do not follow that rule yet: a creation through a technology action built on `AbstractAddFlexoConceptInstance`
(for instance `CreateHbnObject` of the JDBC adapter) goes on after the scheme failed, and an instance created through the
VirtualModelInstance factory of an adapter that overrides `makeNewFlexoConceptInstance` (Excel, XML, Hibernate, REST) stays in its
container when its scheme fails.

**Mechanism — read in the code, not run.** `AbstractAddFlexoConceptInstance._performExecuteCreationScheme` runs a
`CreationSchemeAction` and returns `hasActionExecutionSucceeded()`, which its callers do not turn into a failure. The core factory
discards the partly built instance (`FMLRTVirtualModelInstanceModelFactory.discardPartlyBuiltInstance`), but
`XLSVirtualModelInstanceModelFactory`, `XMLVirtualModelInstanceModelFactory`, `HbnVirtualModelInstanceModelFactory` (and the REST one)
build and register their instances themselves. Found by the review of `CORE-D-47` (2026-11-13). The same path also derives the
concept to instantiate from the creation scheme (`AbstractAddFlexoConceptInstance.getFlexoConceptType()`): a creation scheme inherited
from a parent creates an instance of the parent there, unlike `new` since `CORE-D-15` (review of `CORE-D-15`, 2026-11-14).

**To fix.** Throw the failure of the creation action in `_performExecuteCreationScheme` (only `hasActionExecutionFailed()`: a
disabled action is not a failure), and discard the partly built instance in the adapters' factories, as the core factory does.

**Workaround.** None.

**Progress (CORE-F-11 phase 6, 2026-11-15).**
- Fixed in the core: `AbstractAddFlexoConceptInstance` (base of the technology creation actions) discards the partly built instance
  (`FMLRTVirtualModelInstanceModelFactory.discardPartlyBuiltInstance`, now public) and rethrows the failure of its creation scheme, or its
  cancellation; a creation scheme that is not enabled gives null, as `new`, and leaves no instance (decided with the user). Run outside a
  behaviour, it is an `FMLTechnologyError`; a dynamic instantiation without a matching creation scheme an `FMLResolutionError`.
- `_performExecuteCreationScheme` did not give the new instance to its `CreationSchemeAction` (found by the review): outside an
  `FMLRTVirtualModelInstance` the action did nothing, so the creation scheme of jdbc `CreateHbnObject` never ran and could not fail; in an
  `FMLRTVirtualModelInstance` it ran on a second instance (`CORE-D-84`). The new instance is now given (decided with the user): the scheme of
  `CreateHbnObject` runs for the first time. Proven in openflexo-jdbc (`JDBC-F-1`, 2026-11-16): `TestHbnAnnotatedMapping` runs a
  `CreateHbnObject` whose scheme fails (the behaviour fails with the typed error, no instance is left) and one whose scheme sets a column.
- Found while reading, to check per repository before changing anything: the factories of the reflected adapters (Excel, XML, Hibernate,
  REST) never run a creation scheme (`makeNewFlexoConceptInstance` builds and registers the instance only), and
  `AbstractCreationSchemeAction.doAction` creates an instance only in an `FMLRTVirtualModelInstance` (it warns "Not implemented feature"
  otherwise). The "partly built instance left in its container" of the factory part may not exist as described.
- Checked in openflexo-xlsx (`XLSX-F-2`, 2026-11-16): the factory of reflected Excel instances runs no creation scheme, so there is no
  partly built instance to discard there. `new` of a reflected concept in FML gives null after the warning "Not implemented feature"
  (`AbstractCreationSchemeAction.doAction` creates only in an `FMLRTVirtualModelInstance`): left, recorded here (decided with the user).
- Left: the concept instantiated by `AbstractAddFlexoConceptInstance` is still derived from its creation scheme (an inherited creation
  scheme creates an instance of the parent).

---

## FML compilation unit loading

### CORE-D-16 — A namespace alias is not resolved in an annotation: `@URI(NS+"…")` silently registers the model under another address  ·  `TODO`

**GitHub:** [openflexo-team/openflexo-core#44](https://github.com/openflexo-team/openflexo-core/issues/44)

**Symptom.** A compilation unit that declares `namespace "…" as NS;` and uses the alias in its `@URI` annotation is loaded without any
error, but under a different address than the one it declares: `load -r ["<declared address>"]` then gives nothing, and any script
line using the result fails with `Invalid binding value: virtualModel reason: currentType is null`. The alias does resolve in an
`import [NS+"…"]` (the documented use, see `ElementImportDeclaration`). Whether other annotations are meant to accept it is a question
for the maintainers; what is certain is that the failure is silent.

**Reproduction (verified 2026-09-21 by execution, in `flexo-test-resources`).** One file, no import, no inheritance:

```fml
namespace "http://openflexo.org/test/TestResourceCenter/" as TRC;

@URI(TRC+"ReproNs.fml")
public model ReproNs {
	String s;
}
```

`resources;` in an FML-script lists the unit under its default address,
`http://openflexo.org/test/flexo-test-resources/TestResourceCenter/FML/ReproNs.fml` (resource center base URI + path), not under
`http://openflexo.org/test/TestResourceCenter/ReproNs.fml`; `virtualModel = load -r ["http://openflexo.org/test/TestResourceCenter/ReproNs.fml"];`
then gives no model. The same file with the literal `@URI("http://openflexo.org/test/TestResourceCenter/ReproNs.fml")` loads normally.
A lower-case alias (`as trc` … `trc+"…"`) behaves the same, and so does another annotation:
`@Description(TRC+"desc")` logs `DataBinding TRC + "desc" still invalid at the end of process, reason: Invalid binding value: TRC
reason: BindingVariable TRC does not exist` (`FMLSemanticsAnalyzer.attemptToFixInvalidBindings`).

**Mechanism — partly verified.** The namespaces are added to the compilation unit by `VirtualModelInfoExplorer.inANamespaceDecl()`, and
`ElementImportDeclaration` documents them as usable in its URI expressions; nothing was found that makes them a binding variable in an
annotation's expression. Not checked: at which point the `@URI` value is evaluated, and why its failure is not reported.

**Note.** `FMLParsingExamples/TestNamespaces.fml` uses `@URI(local+"MyModel.fml")` after declaring `LOCAL` (different case). That file is
only parsed by `TestFMLParser`, never loaded, so it does not exercise this.

**Workaround.** Write the address of `@URI` in full; keep the alias for `import`.

---

## Inheritance

### CORE-D-17 — `super.<behaviour>()` gives null when the concept extends several concepts  ·  `TODO`

**GitHub:** [openflexo-team/openflexo-core#45](https://github.com/openflexo-team/openflexo-core/issues/45)

**Symptom.** In a concept with several parents, `super.<behaviour>()` evaluates to null, so a `return "x" + super.hello();` returns null
(a `+` with a null operand gives null). No error is reported at validation or at execution. With a single parent, the same call works,
over several levels.

**Since `CORE-F-11` phase 3 (verified 2026-11-15 by execution).** No longer silent: the binding is invalid ("BindingVariable super does not exist"), and evaluating it is an `FMLResolutionError`.

**Reproduction (verified 2026-09-21 by execution, in `flexo-test-resources`).** One model, one behaviour called from a script:

```fml
@URI("http://openflexo.org/test/TestResourceCenter/Pairing.fml")
public model Pairing {

	public Left newLeft() {
		return new Left();
	}

	public concept Base {
		create() {
		}
		public String hello() {
			return "hello";
		}
	}

	public concept Other {
	}

	public concept Left extends Base, Other {
		create() {
			super();
		}
		public String hello() {
			return "left " + super.hello();
		}
	}
}
```

```
p = new Pairing() with (name="p");
log "left=" + p.newLeft().hello();
```

The log line prints `left=null`; expected `left=left hello`. Reversing the parents (`extends Other, Base`) gives null as well. With
`concept Left extends Base` alone the result is `left hello`, and `concept Guard extends Dog` where `Dog extends Creature` (two levels)
works too. `Other` declares nothing: the second parent is enough. The properties of both parents are inherited normally (a
`ServiceDog extends Dog, Trained` reads `name` and `trick`), and `super(...)` in `create` runs in the same concepts.

**Mechanism — not investigated.** The resolution of `super.<name>()` presumably picks one parent, or none when there are several.

**Workaround.** Put the logic to reuse in a separate behaviour of the parent and call it on `this`: with
`public String baseHello() { return "hello"; }` in `Base`, `return "left " + this.baseHello();` gives `left hello` (verified).

### CORE-D-99 — `super.<property>` gives null  ·  `TODO`

**GitHub:** [openflexo-team/openflexo-core#150](https://github.com/openflexo-team/openflexo-core/issues/150)

**Symptom (verified 2026-11-24 by execution, fixture `FML/TestLexicalContext.fml`, script `KnownDefects/CORE-D-99.fmlscript`).**
In a concept with one parent, `super.inIB`, where `inIB` is a property the concept inherits and does not redeclare, evaluates to
null inside a behaviour; so does `super.level` where the concept redeclares `level`. The binding is reported valid; at run time
the only trace is the warning `Please implement me, target=…SuperReference…`, and no error is raised although a behaviour
evaluates strictly (`CORE-F-11`). `super.toto()` on a behaviour works in the same concept (`AutomatedTests/TestLexicalContext.fmlscript`).

**Cause (read in the code, not fixed).** `super` evaluates to a `FlexoConceptInstanceImpl.SuperReference` (instance + parent
concept). `FlexoBehaviourPathElement.getBindingValue` unwraps it and sets the declared conceptual level;
`FlexoPropertyPathElement.getBindingValue` only handles a `FlexoConceptInstance` target and falls through to the warning and
`return null`.

**Open question.** What `super.level` should give when the concept redeclares `level` (the parent's declaration, its own initial
value, or the same value as `this.level`) is not decided; the script asserts only the inherited, non-redeclared case.

**Workaround.** Read the property with `this.inIB` (or the bare name).

## FML expressions

### CORE-D-19 — A method call on a string literal gives null  ·  `TODO`

**GitHub:** [openflexo-team/openflexo-core#46](https://github.com/openflexo-team/openflexo-core/issues/46)

**Symptom.** A Java method called directly on a string literal evaluates to null, without any message: `"hello".length()` is null
instead of 5. The same methods work on a variable or a parameter holding the same string.

**Since `CORE-F-11` phase 3 (verified 2026-11-15 by execution).** No longer silent: the binding is invalid ("unresolved path element length()"), and evaluating it is an `FMLResolutionError`.

**Reproduction (verified 2026-09-21 by execution, in `flexo-test-resources`).** Called from a script, the following behaviour
returns null instead of 5, and so do `"abc".toUpperCase()`, `"kiwi-banana".substring(5)`, `"EQ-12-3".replace("EQ-", "")` and
`"ab".equals("a" + "b")` written as `return` expressions:

```fml
public int lengthLiteral() {
	return "hello".length();
}
```

Measured in the same model: with the string in a local variable, `String s = "abc"; return s.toUpperCase();` gives `ABC` and
`String s = "EQ-12-3"; return s.replace("EQ-", "").replace("-", "");` gives `123`; with a parameter, `parameters.s.length()` gives 5.

**Mechanism — not investigated.**

**Workaround.** Put the literal in a local variable first.

---

### CORE-D-80 — A bare call to a behaviour of the same concept or model is not resolved  ·  `TODO`

**GitHub:** [openflexo-team/openflexo-core#47](https://github.com/openflexo-team/openflexo-core/issues/47)

**Symptom (verified 2026-11-23 by execution, fixture `KDBareCall`, script `KnownDefects/CORE-D-80.fmlscript`).** A behaviour cannot call another behaviour of the same concept or of the same model without writing `this.`: the bare call `foo()` is an invalid binding, `unresolved path element foo()`, and evaluating it is an `FMLResolutionError` (since `CORE-F-11` phase 3; before, a silent null).

Fails, bare call:
- in a concept, to a behaviour of the same concept: `bar() { return foo(); }`;
- in a concept, with an argument: `echo("x")`;
- in a sub-concept, to a behaviour inherited from its parent;
- in a model, to a behaviour of the same model: `return middle();` (original case, verified 2026-11-14, `ExecutionStack/NestedFailure.fmlscript`).

Works: the same calls written `this.foo()`; and the bare access to a property (`name` for `this.name`, fixture `NameLookup`).

An earlier version of this entry said that a bare call is resolved in a concept: that was wrong (`ErrorKinds/BareCall` already showed the failure). Not checked: where the resolution of a bare call looks for the behaviour (binding model of a concept versus a model), and the priority between a local variable, a property and a behaviour of the same name.

**Workaround.** Write `this.foo()`.

## FML-script

### CORE-D-20 — Reassigning a script variable makes every later read of it give null  ·  `TODO`

**GitHub:** [openflexo-team/openflexo-core#48](https://github.com/openflexo-team/openflexo-core/issues/48)

**Symptom.** In a `.fmlscript`, assigning a name that already holds a value (`x = "a"; x = "b";`) does not report
anything, but every read of that name after the second assignment gives `null` — not `"b"`, and not `"a"` either.
Reading the name **before** the second assignment gives the correct first value. The variable is not otherwise
broken: it can be assigned again, but it never reads back anything but `null` from then on.

**Reproduction (verified 2026-09-22 by execution, against `HelloWorld.fmlscript`'s resource center).** Three
independent scripts, run through the platform's script runner:

```
x = "a";
log "before=" + x;   // "a"
x = "b";
log "after=" + x;    // null, not "b"
```

```
n = 1;
n = n + 1;
log "n=" + n;         // null, not 2
```

A third assignment does not recover it either (`x = "c";` after the above still reads `null`), and the same
happens with `int`, not only `String`.

**Mechanism — not investigated; one lead.** `AbstractFMLAssignation.execute()` (fml-cli) has two branches: when the
assignment's binding `isValid()` (the name is already known), it calls `assignation.setBindingValue(…)`; when it
is a new declaration, it calls `getCommandInterpreter().declareVariable(…)` then `.setVariableValue(…)`. A first
assignment to a new name takes the second path; every later assignment to the same name takes the first. Whether
the two paths write to the same storage the later reads look at was not checked.

**Workaround.** Give every new value a name of its own; do not reassign a script variable, and do not accumulate
into one across several statements.

---

### CORE-D-83 — The output of every fml-cli command is one list, shared by all commands and all interpreters  ·  `TODO`

**GitHub:** [openflexo-team/openflexo-core#49](https://github.com/openflexo-team/openflexo-core/issues/49)

**Symptom (read, not run; found while doing CORE-F-11 phase 5, 2026-11-15).** `AbstractCommand` (fml-cli) declares
`List<String> output = new ArrayList<>();` in the interface: a field of an interface is `static final`, so every command of every
interpreter writes into the same list, and `AbstractCommandInterpreter.getOutput()` returns it. Each command clears it when it
starts. Two interpreters running at the same time (two REST terminal sessions of the server, the terminal of the application and a
script) read each other's output, or an empty one; a command run by `execute` clears the output of the directive that ran it.

**Fix direction.** Keep the output per command (a field of `AbstractCommandImpl`, PAMELA-transient) or per interpreter.

---

## Resources: unloading and deletion

`CORE-D-21`, `CORE-D-22` and `CORE-D-23` were found and fixed together; `CORE-D-24` is what the same measurement left open. `CORE-D-27`
was met while closing a project, in the same code; it predates `CORE-D-21`. The
regression test of the first four is `flexo-foundation-test`, `TestUnloadDoesNotDelete`, over the fixtures `FML/UnloadLedger.fml` and
`FML/UnloadProbe.fml` of `flexo-test-resources`: an `Item` whose deletion scheme increments a counter of a ledger held in another
resource, and whose renderer reads that ledger.

### CORE-D-24 — Bindings of the metamodel keep every context they were evaluated in  ·  `TODO`

**GitHub:** [openflexo-team/openflexo-core#50](https://github.com/openflexo-team/openflexo-core/issues/50)

**Symptom.** A `DataBinding` owned by a VirtualModel — an expression of a behaviour, of a property — caches, per evaluation context,
the value and a listener registered on every object of the evaluated path. Nothing releases them: the VirtualModel keeps every
context such a binding was ever evaluated in (a behaviour action, a concept instance), and every object those contexts reach, for
as long as it is loaded; and those objects keep notifying the stale listeners.

**Reproduction (verified 2026-09-24 by execution, while measuring `CORE-D-21`).** After creating an `Item` with
`CreateFlexoConceptInstance`, the `CreationSchemeAction` stays referenced by the `cachedValues` of the `parameters.name` binding of
the creation scheme — reached from the compilation unit resource, which also keeps its parser and semantics analyzer
(`CompilationUnitResourceImpl.fmlParser` → `FMLCompilationUnitSemanticsAnalyzer.nodesForAST`) once loaded.

**Mechanism — verified in the code.** `DataBinding.getBindingValue()` caches in two strong `HashMap`s keyed by the context
(`OPTIMIST_CACHE`, or `PRAGMATIC_CACHE` for a cacheable binding); a context is only dropped when a notification invalidates it
(`LazyBindingPathChangeListener`), and the listener never. `CORE-D-21` releases the renderer only, the one binding evaluated with
an instance as context that an instance knows of.

**Options.** Weak keys, with a listener that does not reference its context strongly (it is itself held by the observed objects);
or no cache for bindings evaluated in transient contexts (behaviours); or a release hook on the contexts (actions, when they end).
Each changes the notification behaviour GINA relies on: to be decided and tested against the UI.

**Workaround.** None needed functionally; memory grows with the number of evaluations while a VirtualModel stays loaded.

---

## Resources: loading

### CORE-D-29 — A string addition on a binding path fails in a GINA component: `data.owner.name + ".prj"`  ·  `TODO`

**GitHub:** [openflexo-team/openflexo-core#51](https://github.com/openflexo-team/openflexo-core/issues/51)

**Symptom.** In a GINA component whose bindings are FML bindings, a label bound to `data.owner.name + ".prj"` stays empty and the
log reports, at every evaluation: `TypeMismatchException on operator addition : supplied types are STRING and STRING`. Both operands are
strings; the operator rejects them all the same. The same expression is in `fme-module` (`FMEProjectNaturePanel.fib`), and was in
`formod-module` (`FMSProjectNaturePanel.fib`, worked around by a getter on the component controller, 2026-10-07).

**Reproduction (2026-10-07).** `./gradlew :formod:formod-app:run` in `openflexo-dev`, create a project, give it the Formose nature, close
and reopen it: the big title label of the project view is empty. A stack trace taken at the catch in `Expression.evaluate` gives:

```
FMLArithmeticBinaryOperator$1.evaluate(FMLArithmeticBinaryOperator.java:126)   <- the final throw
ExpressionEvaluator.transformBinaryOperatorExpression(ExpressionEvaluator.java:84)
ExpressionEvaluator.performTransformation(ExpressionEvaluator.java:70)
JavaExpressionEvaluator.performTransformation(JavaExpressionEvaluator.java:83)
FMLBinaryOperatorExpression.transform(FMLBinaryOperatorExpression.java:75)
Expression.evaluate(Expression.java:75)
DataBinding.getBindingValue(DataBinding.java:1181)
BindingPathChangeListener.evaluateValue → ... FIBController.setDataObject
```

**What is measured, and what is not.** Measured: the expression is an `FMLBinaryOperatorExpression` (FML operator) transformed by a
`JavaExpressionEvaluator`; the throw is the last one of `FMLArithmeticBinaryOperator.ADDITION.evaluate`, i.e. neither branch
`leftArg instanceof StringConstant` (the FML `FMLConstant.StringConstant`) matched. Not measured: the class of the left constant. Reading
`JavaExpressionEvaluator.performTransformation` (line 74), a binding path is turned into `JavaConstant.makeConstant(value)`, a Java
constant, which the FML operator would not recognise — to be confirmed by printing the two operand classes before the throw (the
`System.out.println` already in place there is lost under `formod-app:run`: use the logger).

**Not covered by a test.** FML scripts evaluate with the FML evaluator and concatenate strings fine (T1 to T12).

### CORE-D-30 — The arguments of a palette binding are ignored when a shape is dropped  ·  `TODO`

**GitHub:** [openflexo-team/openflexo-core#52](https://github.com/openflexo-team/openflexo-core/issues/52)

**Symptom.** A palette binding written `PaletteElementBinding:(call=new FunctionalGoalGR::createFunctionalGoal("Goal", "", ""), paletteElementId=…)`
does not give the drop scheme the values "Goal", "" and "": the drop scheme runs with its parameters unset (log: `Found not initialized
parameter … name, type, description`).

**Verified by reading (2026-10-07).** `ContextualPalette.handleFMLControlledDrop` (`openflexo-diagram/diagram-ta-ui`) keeps only
`applicableBindings.get(0).getDropScheme()`, builds a `DropSchemeAction` from it and calls `doAction()`; `getCall()` is never evaluated. The
code carries the comment "Ce qui serait mieux : applicableBindings.get(0).getCall().getBindingValue(…)". The parameters then come from the
wizard, or from the parameter defaults when the wizard is skipped (`skipConfirmationPanel`, true by default, and every required parameter valid).

**Consequence.** The textual form `call=…(args)` is accepted and validated, but its arguments are decoration. A drop scheme whose parameters have
no default and are not `required` runs with nulls (formod, FORMOD-D-3). Not reproduced headless: the scripts call the scheme with explicit arguments.

**To decide.** Evaluate the call against the drop context and seed the action's parameters with its arguments (then a `call=` really is a default),
or reject arguments in `call=` where they cannot be honoured.

---

### CORE-D-82 — Reloading a project can deadlock between the project resource and an instance loaded in parallel  ·  `TODO`

**GitHub:** [openflexo-team/openflexo-core#53](https://github.com/openflexo-team/openflexo-core/issues/53)

**Symptom.** `TestBMethology.testReloadProject` (formod, `formod-module`, UI test) hung for 35 minutes; the test JVM had to be killed. Seen
once (2026-11-15, CORE-F-11 phase 4 run through the openflexo-dev composite); the 27 other UI tests of the module passed.

**Verified (thread dump, 2026-11-15).** The JVM reports a Java-level deadlock between two threads:
- `Test worker`: `OpenflexoProjectAtRunTimeTestCase.reloadProject` → `ProjectLoader.loadProject` → `FlexoResourceImpl.performLoadResourceData`
  (holds the lock of the `FlexoProjectResource`) → `PamelaResourceImpl.stopDeserializing` → `FlexoObject.clearIsModified`, waiting for the
  lock of the `FlexoProject`;
- a pool thread loading an instance in parallel: `VirtualModelInstance.addToVirtualModelInstances` → `getFlexoPropertyValue` →
  `getFlexoActor` → `ResourceBasedModelSlotInstance.getAccessedResourceData` → `FMLRTVirtualModelInstanceResourceImpl.loadResourceData`
  (line 145, `getFMLRunTimeEngine()`) → `FlexoConceptInstance.getEditor()` → `ProjectLoader.getEditorForProject` → a new editor →
  `FlexoProject.addToEditors` → `FlexoObject.setModified` (holds the lock of the `FlexoProject`) → `FlexoResourceImpl.notifyResourceModified`
  → `FlexoObservable.setChanged`, waiting for the lock of the `FlexoProjectResource`.

The two locks are taken in opposite orders. None of the frames belongs to the code of CORE-F-11. Not reproduced since; whether it is new
is not known (the editor created during loading goes through `FlexoConceptInstance.getEditor()`).

**How to reproduce.** Run `./gradlew :formod:formod-module:uiTest` from openflexo-dev several times; on a hang, `jstack` the
`Gradle Test Executor` JVM.

**To decide.** Do not create an editor (nor mark the project modified) while a resource of the project is being loaded, or take the two
locks in one order; adding an editor to a project probably should not mark it modified at all.

## Noise and fidelity found by mapping the warnings of loading a Formose project (2026-10-09)

Mapped on `formod-app` (583 warnings at the start of the application, 4 of them from opening a project) and on the headless tests (the same families, hundreds of times). Fixed since: CORE-D-31 (technologies forgotten at load), CORE-D-32 (stale "not implemented yet"), and the OWL adapter ones (`openflexo-owl/KNOWN_DEFECTS.md`). What is left is below, with what was measured and what was not.

### CORE-D-35 — `PasteAction.isEnabledForSelection` logs a warning for an ordinary state  ·  `TODO`

**GitHub:** [openflexo-team/openflexo-core#55](https://github.com/openflexo-team/openflexo-core/issues/55)

**Symptom.** `Unexpected null focused object in PASTE`, once at the start of formod-app. Read in `PasteAction.isEnabledForSelection`: it warns when the focused object is null, which is the normal state of a menu computed with nothing selected. The call that triggers it was not identified. Severity: noise.

### CORE-D-36 — `DirectoryWatcher.waitNextWatching()` can return before a file written just before the call is seen, and gives up silently  ·  `TODO`

**GitHub:** [openflexo-team/openflexo-core#56](https://github.com/openflexo-team/openflexo-core/issues/56)

**Symptom.** A test that writes files into a directory resource center, calls `getDirectoryWatcher().waitNextWatching()` once and then asks the center for the new resource gets null, intermittently: `Atelier B project SourceAtelierBProject was not registered` in `formod-test` (`T6`, `T7`, `T10`), which provisions two blank Atelier B projects before each script. Measured on 2026-10-10 on the same machine, formod-test: 3 green runs out of 4 on the state without the dependency changes of UI-D-1, 1 green out of 4 right after them; 6 out of 6 after the test-side fix below. `b-ta-test`: 131 of 132, the only red being FORMOD-D-1 (`testExpression7`), as before. Same family as `directory-watcher-flaky-tests` of the workspace memory.

**Mechanism — read in `openflexo-utils/flexo-utils/.../toolbox/DirectoryWatcher.java`, not reproduced in isolation.**
1. `waitNextWatching(timeout)` sets `waitNextWatchingRequested`, then waits for `waitNextWatchingDone`. `run()` sets the latter at the end of the watching **in progress**, even when that watching started before the call: a scan that began before the project was written ends, the wait returns, and the new project was not in what that scan listed.
2. After `timeout` (5 s by default) it throws an `InterruptedException`; the callers found in formod swallowed it, so the test went on without the resource.
3. `waitNextWatchingRequested`, `waitNextWatchingDone` and `isRunning` are plain fields read by the waiting thread: no visibility guarantee other than the `Thread.sleep` of the loop.

**Worked around in the tests (2026-10-10).** `FormodAutomatedTests.provisionAtelierBProject` and `TestCreateBlankAtelierBProject` wait again, up to 10 times, until the resource is registered.

**To decide.** Making `waitNextWatching` wait for a watching that STARTED after the call (a counter of started and completed runs, both `volatile`, waiting for `completed >= startedAtCall + 1`), and not turning the timeout into an `InterruptedException` the callers cannot tell from a real interruption. The default delay of 5 s must be checked against the period of the watcher before making the wait stricter.

---

## FML queries

### CORE-D-53 — `select` from a concept instance does not search the instances nested deeper than one level  ·  `TODO`

**GitHub:** [openflexo-team/openflexo-core#57](https://github.com/openflexo-team/openflexo-core/issues/57)

**Symptom.** `select X from c`, where `c` is a concept instance, finds only the instances directly embedded in `c`, whereas §10
of the spec states that the search covers what the source contains, nested instances included. From a virtual model instance the
search does cover every depth.

**Reproduction (verified 2026-11-11 by execution, test `KnownDefects/CORE-D-53.fmlscript`, fixture `KDQueries`).** `Outer`
contains a `Middle` that contains a `Leaf`; `select Leaf from this` in `Outer` finds 0 instances instead of 1.

**Mechanism — verified in the code.** `VirtualModelInstance` (fetch fallback, the indexed path is disabled by `isIndexable`
returning false) uses the type index of a virtual model instance source, but `getEmbeddedFlexoConceptInstances(type)` of a
concept instance source, which loops over the direct children only. Found by the external audit of the FML language of 2026-10-23 (its §3.7).

**Workaround.** Query from the virtual model instance and filter on the container, or walk the levels explicitly.

### CORE-D-54 — An unfiltered `select` returns the internal index of the instances  ·  `TODO`

**GitHub:** [openflexo-team/openflexo-core#58](https://github.com/openflexo-team/openflexo-core/issues/58)

**Symptom.** The list returned by `select X from vmi` without `where` is the type index of the virtual model instance itself:
modifying it (`clear()`) makes every later query miss instances that still exist. A filtered `select` returns a new list.

**Reproduction (verified 2026-11-11 by execution, test `KnownDefects/CORE-D-54.fmlscript`, fixture `KDQueries`).** After
`vmi.allItems().clear()`, `itemCount()` gives 0 although the `Item` was neither removed nor deleted.

**Mechanism — verified in the code.** `VirtualModelInstance.getFlexoConceptInstances(concept)` returns the list held by the index
map; `filterWithConditions` returns its argument unchanged when there is no condition, and `AbstractFetchRequest` returns the
computed values without copying. Found by the external audit of the FML language of 2026-10-23 (its §3.7).

**Decided 2026-11-11.** A `select` returns a snapshot (a new list) in every case.

**Workaround.** Do not modify the result of a `select`; copy it first.

---

## FML events

### CORE-D-55 — An inherited event listener is registered twice when instances are attached to the engine in bulk  ·  `TODO`

**GitHub:** [openflexo-team/openflexo-core#59](https://github.com/openflexo-team/openflexo-core/issues/59)

**Symptom.** When a virtual model instance is attached to the run-time engine with its existing instances — which is what loading
it does — an instance of a child concept that inherits a listener receives each event twice. Removing the instance afterwards
removes one registration only.

**Reproduction (verified 2026-11-11 by execution, test `KnownDefects/CORE-D-55.fmlscript`, fixture `KDListener`).** A `Child`
created after its model receives one event (control). After `removeFromExecutionContext` / `addToExecutionContext` — the bulk path
that loading takes — the next event is received twice (`received` goes from 1 to 3).

**Mechanism — verified in the code.** `DefaultFMLRunTimeEngine.addToExecutionContext` loops over every concept of the virtual
model, then over `vmi.getFlexoConceptInstances(concept)` (which includes the instances of sub-concepts), then over the listeners
accessible from that concept: a `Child` is met under `Base` and under `Child`. Each registration is a new `EventInstanceListener`
in a `HashSet`, without `equals`/`hashCode`; `stopEventListening` removes the first match only. Found by the external audit of the FML language of 2026-10-23 (its §3.8).

**Workaround.** None at FML level; declare the listener in the concrete concept rather than in a parent.

---

## Found by the p2pp harness on the FML parsing examples (2026-11-12)

Measured by `TestFMLPrettyPrintInvariants` (`flexo-foundation-test`), by execution, on the files of `FMLParsingExamples`. The mechanism of
each is NOT investigated. The harness reports only the first difference of a file: another defect may hide behind it.

### CORE-D-69 — A wrapper type of a behaviour parameter becomes the primitive type  ·  `TODO`

**GitHub:** [openflexo-team/openflexo-core#61](https://github.com/openflexo-team/openflexo-core/issues/61)

**Symptom.** `required Boolean aC2, Boolean aC3=true` (parameters of a behaviour) were printed `boolean`: the model holds the primitive type.
File: `UIAnnotations` (`FMLParsingExamples`). Seen in the pretty-print written from the model (2026-11-13); since CORE-D-33 the preserving
pretty-print of an untouched source keeps `Boolean`, but the model still says `boolean` (the normalized pretty-print shows it). Not checked:
where the type is changed (parser, typing space, parameter), and whether other wrappers are concerned.

### CORE-D-70 — The version of a type reference is lost  ·  `TODO`

**GitHub:** [openflexo-team/openflexo-core#62](https://github.com/openflexo-team/openflexo-core/issues/62)

**Symptom.** `Foo@(1) foo1;` (a type with a version, `typedef String as Foo`) was printed `Foo foo1;`: the version is not in the model.
File: `TestTypeVersions` (`FMLParsingExamples`). As for CORE-D-69, the preserving pretty-print of an untouched source now keeps `Foo@(1)`, the
model and the normalized pretty-print do not. Not checked: whether a version is meant to be kept by the model at all.

## FML exceptions

### CORE-D-85 — A failure caught by a try in a getter evaluated outside an action is still reported  ·  `TODO`

**GitHub:** [openflexo-team/openflexo-core#63](https://github.com/openflexo-team/openflexo-core/issues/63)

**Symptom (read, not run; found by the review of `CORE-F-11` phase 7, 2026-11-16).** A getter (or setter, or expression property) read
outside a behaviour — by a GINA binding, an fml-cli expression — runs its body with a `LocalRunTimeEvaluationContext`. A behaviour it
calls in a `try` block (`this.b()`) runs as an outermost, non-embedded action (`FlexoBehaviourPathElement`: the context is not a
`FlexoBehaviourAction`), so its failure is reported by the editor (console, and its edits compensated) before the `catch` clause handles
it; `FMLErrorReporter.failureHandled` finds no execution scope. The specification (§8.10) says a caught failure is not reported. Inside a
behaviour, the case of every test, the called behaviour is a nested action and nothing is reported.

**Options.** Open an execution scope for the evaluation of a property body outside an action; or let `FlexoBehaviourPathElement` defer
the report of an action started inside a `try` block.

**Workaround.** Write the `try` in a behaviour.

## FML pretty-print

### CORE-D-97 — Removing the last root concept of a VirtualModel damages the printed text: '}}', moved concept not reindented  ·  `TODO`

**GitHub:** [openflexo-team/openflexo-core#139](https://github.com/openflexo-team/openflexo-core/issues/139)

**Symptom (seen 2026-11-20 in the Maintainer, reproduced by a test, branch 3.0).** When the last root concept of a VirtualModel
leaves it, the FML text printed back (the print that keeps the parsed text) is damaged:
- the line break before the closing brace of the model is lost: the text ends with `}}` (the closing brace of the previous concept
  and that of the model on one line);
- when the concept is moved into another concept rather than deleted, it keeps the indentation and the blank lines of its former
  place instead of being indented at its new depth.

```
public model LastRoot {
	public concept Root1 {
		String r;
	}
	public concept Root2 {
		public concept Inner {
		}
	}
}
```

Deleting `Root2` prints `\tpublic concept Root1 {\n\t\tString r;\n\t}}`. Moving it into `Root1` (inspector field "container",
`setContainer`) prints `Root2` at column 0, inside `Root1`, followed by `\t}}`.

**Verified.** Deleting reproduces it, and deleting a concept existed before `CORE-F-19`, so the defect is probably older than that
refactoring. This is not demonstrated: the commits of `CORE-F-19` and those made since on the same files prevent a direct comparison.
The normalized print (`getNormalizedFML()`) is right; only the print that keeps the parsed text is affected.

**How to reproduce.** `KnownDefectsPrintAfterLastRootRemovalTest` (fml-parser), run with `OPENFLEXO_KNOWN_DEFECTS=1`: one test for the
deletion, one for the move.

**Hypothesis (not checked).** The pretty-print node of the VirtualModel prints its concepts with a separator before and after each
one. The separator after the last concept is attached to that concept's fragment, and goes with it. A moved concept keeps the text of
its fragment as parsed (`keepingParsedTextWhileUnchanged`), with the indentation of its former place.

**Workaround.** Reformat the unit (normalized print), or fix the text by hand in the FML editor before saving.
