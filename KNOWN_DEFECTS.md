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

---

## FML properties

### CORE-D-3 — The parameter name declared in a `set(...)` block is ignored  ·  `TODO`

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

### CORE-D-50 — The default values of inherited properties are not set on a new instance  ·  `TODO`

**Symptom.** A property declared with a default value in a parent concept (`int legs = 4;`) is null on a new instance of a child
concept that has its own creation scheme. The child's own default values are set.

**Reproduction (verified 2026-11-11 by execution, test `KnownDefects/CORE-D-50.fmlscript`, fixture `KDInheritance`).** With
`Animal { int legs = 4; create() { } }` and `OwnDog extends Animal { int tail = 1; create() { } }`, `vmi.new OwnDog()` gives
`tail == 1` and `legs == null`.

**Mechanism — verified in the code.** `FlexoConceptInstance` initializes default values by iterating
`getFlexoConcept().getDeclaredProperties(FlexoRole.class)`: declared roles only, not inherited ones (and roles only, not
`FMLVariable`s). Found by the external audit of the FML language of 2026-10-23 (its §3.6).

**Workaround.** Assign the inherited property in the child's creation scheme.

### CORE-D-51 — Initializers and cardinalities written in a property declaration are lost  ·  `TODO`

**Symptom.** The parser accepts these declarations, but what they say does not reach the model:
- `Person first = new Person();` — the initializer of a concept-typed property is dropped;
- `Person selected = select unique Person from container;` — an edition action as initializer is dropped, on every kind of
  property, although §6.2 of the spec allows it;
- `Object[0,*] objects;` — the property is built with cardinality `ZeroOne`.

Primitive and `String` properties keep both their initializer and their cardinality.

**Reproduction (verified 2026-11-11 by execution, test `KnownDefects/CORE-D-51.fmlscript`, fixture `KDPropertyInit`).** The
cardinality of `objects` is `ZeroOne` (first assert); the script also asserts the initializer of `first` and the value of
`selected`, not reached once the first assert fails.

**Mechanism — verified in the code.** `FlexoPropertyFactory` routes a concept-typed declaration to `FlexoRolePropertyNode` and a
non-primitive Java type to `FMLVariableNode`. `FlexoRolePropertyNode.buildModelObjectFromAST` never reads the initializer;
`ObjectNode` returns null for an `AInitializerFmlActionVariableDeclarator` (`getInitializerFMLAction` has no caller);
`FMLVariableNode.buildModelObjectFromAST` never sets the cardinality, and `FlexoProperty.getCardinality()` defaults to `ZeroOne`.
Found by the external audit of the FML language of 2026-10-23 (its §3.6).

**Workaround.** Initialize such properties in the creation scheme; type multi-valued Java properties with a primitive or
`String` element when possible.

---

## FML behaviours

### CORE-D-4 — A parameter default value does not allow omitting the argument in `new`  ·  `TODO`

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

---

## FML control structures and edition actions

### CORE-D-10 — Deleting a concept instance leaves it in the multiple roles referencing it  ·  `TODO`

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

### CORE-D-48 — A local variable outlives its block and hides the property of the same name  ·  `TODO`

**Symptom.** A local declared inside a block (`if`, loop body, `{ }`) is still visible after it; a later read of a property of the
same name returns the local's value, although the analysis resolved that name to the property. Validation reports nothing.

**Reproduction (verified 2026-11-11 by execution, test `KnownDefects/CORE-D-48.fmlscript`, fixture `KDScope`).** With a property
`name` set to `"property"`, `blockScope()` returns `"local"`:

```fml
public String blockScope() {
	if (true) {
		String name = "local";
	}
	return name;
}
```

**Mechanism — verified in the code.** `FlexoBehaviourAction` keeps the locals of an execution in one `Hashtable<String, Object>
variables`; `getValue(BindingVariable)` looks the name up there first, whatever the kind of the binding variable, before going to
the instance. `Sequence` and `ConditionalAction` push no frame and `DeclarationAction` never removes its variable; only iteration
variables are removed at the end of a loop. Found by the external audit of the FML language of 2026-10-23 (its §3.4).

**Workaround.** Do not reuse the name of a property for a local, nor the name of an outer local inside a block.

### CORE-D-56 — A statement written without braces is printed back with braces and a broken indentation  ·  `TODO`

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

### CORE-D-15 — `new Child()` creates an instance of the parent when the child virtual model only inherits its creation scheme  ·  `TODO`

**Symptom.** A virtual model that `extends` another and declares no creation scheme of its own, while its parent declares one, is
instantiated as the **parent**: none of the members declared by the child resolve on the result (`unresolved path element hello()`),
although the members inherited from the parent do.

**Reproduction (verified 2026-09-21 by execution, in `flexo-test-resources`).** Two files:

```fml
@URI("http://openflexo.org/test/TestResourceCenter/Parent.fml")
public model Parent {
	create() {
	}
}
```

```fml
import ["http://openflexo.org/test/TestResourceCenter/Parent.fml"];

@URI("http://openflexo.org/test/TestResourceCenter/Son.fml")
public model Son extends Parent {
	public String hello() {
		return "hello";
	}
}
```

```
virtualModel = load -r ["http://openflexo.org/test/TestResourceCenter/Son.fml"];
service ResourceCenterService add_temp_rc;
c = new Son() with (name="x");
assert c.hello() == "hello";
```

The assert fails (with `log "…" + c.hello()` the line is reported as an invalid binding, `unresolved path element hello()`), and
`c.flexoConcept.name` gives `Parent`. Isolating the trigger, each measured on its own: a parent with no creation scheme, with a property
only, with a behaviour only, or with a concept only works; a parent with a `create()` fails, with or without a property; a `Son` that
declares its own `create() { }` works; the base and the child loaded in either order, and the alias or the plain address in the
import, make no difference. The same shape between two **concepts** of one model (`concept Animal { create() { } }`,
`concept Dog extends Animal { … }`, instantiated by `new Dog()` from a behaviour of the model) works: only inheritance between
virtual models is affected.

**Mechanism — read in the code, consistent with every observation, not confirmed with a debugger.**
`AbstractAddFlexoConceptInstanceImpl.getFlexoConceptType()` returns `getCreationScheme().getFlexoConcept()` as soon as a creation
scheme is set, i.e. the concept that **declares** the scheme; `AbstractAddVirtualModelInstance` derives both its result type
(`FlexoConceptInstanceType.getFlexoConceptInstanceType(getFlexoConceptType())`) and `getVirtualModelType()` from it. A scheme reached
by inheritance is declared by the parent. Not read: how the scheme gets chosen at parse time (`AddVirtualModelInstanceNode`).

**Impact.** Silent: no error at load or at execution of the `new`; the wrong type shows up later, as unresolved paths or `null`.

**Concepts too — verified 2026-11-11 by execution (test `KnownDefects/CORE-D-15.fmlscript`, fixture `KDInheritance`).** The
observation above that concepts work holds only for a `new Dog()` written in a behaviour of the model. From a script,
`vmi.new Dog()` (a `CreationSchemePathElement`) on a concept `Dog extends Animal` that inherits `create()` gives an instance whose
`flexoConcept.name` is `Animal`: `AbstractCreationSchemeAction.getFlexoConceptBeingCreated()` returns the concept that declares
the scheme. Found by the external audit of the FML language of 2026-10-23 (its §3.6).

**Workaround.** Declare a creation scheme in the child, even an empty `create() { }`.

### CORE-D-73 — A creation run by a technology action, or through a technology factory, still hides a failing creation scheme  ·  `TODO`

**Symptom.** Since `CORE-D-47`, a failing creation scheme fails the `new` that runs it and leaves no partly built instance. Two
creation paths do not follow that rule yet: a creation through a technology action built on `AbstractAddFlexoConceptInstance`
(for instance `CreateHbnObject` of the JDBC adapter) goes on after the scheme failed, and an instance created through the
VirtualModelInstance factory of an adapter that overrides `makeNewFlexoConceptInstance` (Excel, XML, Hibernate, REST) stays in its
container when its scheme fails.

**Mechanism — read in the code, not run.** `AbstractAddFlexoConceptInstance._performExecuteCreationScheme` runs a
`CreationSchemeAction` and returns `hasActionExecutionSucceeded()`, which its callers do not turn into a failure. The core factory
discards the partly built instance (`FMLRTVirtualModelInstanceModelFactory.discardPartlyBuiltInstance`), but
`XLSVirtualModelInstanceModelFactory`, `XMLVirtualModelInstanceModelFactory`, `HbnVirtualModelInstanceModelFactory` (and the REST one)
build and register their instances themselves. Found by the review of `CORE-D-47` (2026-11-13).

**To fix.** Throw the failure of the creation action in `_performExecuteCreationScheme` (only `hasActionExecutionFailed()`: a
disabled action is not a failure), and discard the partly built instance in the adapters' factories, as the core factory does.

**Workaround.** None.

---

## FML compilation unit loading

### CORE-D-16 — A namespace alias is not resolved in an annotation: `@URI(NS+"…")` silently registers the model under another address  ·  `TODO`

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

**Symptom.** In a concept with several parents, `super.<behaviour>()` evaluates to null, so a `return "x" + super.hello();` returns null
(a `+` with a null operand gives null). No error is reported at validation or at execution. With a single parent, the same call works,
over several levels.

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

---

## FML expressions

### CORE-D-19 — A method call on a string literal gives null  ·  `TODO`

**Symptom.** A Java method called directly on a string literal evaluates to null, without any message: `"hello".length()` is null
instead of 5. The same methods work on a variable or a parameter holding the same string.

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

## FML-script

### CORE-D-20 — Reassigning a script variable makes every later read of it give null  ·  `TODO`

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

## Resources: unloading and deletion

`CORE-D-21`, `CORE-D-22` and `CORE-D-23` were found and fixed together; `CORE-D-24` is what the same measurement left open. `CORE-D-27`
was met while closing a project, in the same code; it predates `CORE-D-21`. The
regression test of the first four is `flexo-foundation-test`, `TestUnloadDoesNotDelete`, over the fixtures `FML/UnloadLedger.fml` and
`FML/UnloadProbe.fml` of `flexo-test-resources`: an `Item` whose deletion scheme increments a counter of a ledger held in another
resource, and whose renderer reads that ledger.

### CORE-D-24 — Bindings of the metamodel keep every context they were evaluated in  ·  `TODO`

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

## Noise and fidelity found by mapping the warnings of loading a Formose project (2026-10-09)

Mapped on `formod-app` (583 warnings at the start of the application, 4 of them from opening a project) and on the headless tests (the same families, hundreds of times). Fixed since: CORE-D-31 (technologies forgotten at load), CORE-D-32 (stale "not implemented yet"), and the OWL adapter ones (`openflexo-owl/KNOWN_DEFECTS.md`). What is left is below, with what was measured and what was not.

### CORE-D-34 — Loading units that import each other logs transient "Unresolved type" and "still invalid" warnings  ·  `IN PROGRESS`

**Symptom.** At the start of formod-app, about 135 warnings: `Unresolved type: DefaultDataType | DomainModel | DocumentAnnotationMethodology` (32), `Preventing StackOverflow while loading resource CompilationUnitResource…/BMethodology | DomainModelMethodology | DomainModel | DocumentAnnotationMethodology` (32), `DataBinding … still invalid at the end of process, reason: BindingVariable X does not exist` (70, X being a constant imported from a diagram specification: `C_CONTAINER`, `GOAL_DIAGRAM`…, or `unresolved path element new DefaultDataType(…)`).

**What is verified.** The units import each other in a cycle — `FormoseCore` imports `SysMLKaosMethodology`, `DomainModelMethodology` and `BMethodology`, which import `FormoseCore` — which `FlexoResourceImpl.getResourceData` stops with a guard (the "StackOverflow" warning). All 19 model validations of `formod-test` pass and every scenario executes, so the bindings are valid in the end: these are transient states, logged as warnings. The three sources: `FMLCompilationUnitSemanticsAnalyzer.finalizeDeserialization`, `FlexoResourceImpl.getResourceData`, `FMLSemanticsAnalyzer.attemptToFixInvalidBindings` (it logs at the end of the FIRST pass, a second one resolves them: see CORE-D-26).

**Not verified.** Whether a cycle can leave a binding really invalid (none was found in formod); the order in which the units are loaded.

**Fixed for the cycle part (2026-10-13), `IN PROGRESS` for the rest.** Four causes, all of the same kind: a unit being loaded higher in the stack (units importing each other) was used or judged as if it were complete.
- `ElementImportDeclaration.buildReferencedObject` forced `getResourceData()` on a unit being loaded: it hit the guard of `FlexoResourceImpl.getResourceData` (warning + `Thread.dumpStack()`). It now takes the partial data of a unit that `isLoading()`, as `PamelaResourceWithPotentialCrossReferencesImpl.performLoadResourceData` already did.
- `FMLTypingSpaceDuringParsing.resolveType` looked for a concept inside an imported unit that was "loaded" but still loading; it now uses the `VirtualModelInfo` path for it (the type is resolved later).
- `FMLCompilationUnit.getContainedVirtualModelNamed` threw a `NullPointerException` (printed and swallowed, 10 per `formod-test` run) on a sibling unit without data yet; it now returns null for it.
- A parent concept declared in a unit that is not complete yet was reported "not found" by the second pass of the unit extending it (`SysMLKaosMethodology` extends `Methodology`, which imports `FormoseCore`, which imports `SysMLKaosMethodology`). `AbstractFlexoConceptNode.resolveParentConcepts` now postpones it, and so do the "Unresolved type" and "still invalid" reports, while `CompilationUnitResource.isWaitingForReferencedUnits()` is true; `FMLCompilationUnitSemanticsAnalyzer.retryPendingResolutions()` retries when a referenced unit is finalized (the CORE-D-14 hook in `CompilationUnitResourceImpl.finalizeLoadResourceData`), and what is left is reported then.

Measured over the printed output of the test runs, same tests before/after, all green (`formod-test` 32/32, `formod-module` 17/17): `Preventing StackOverflow` 213 → 0 and 110 → 0; `Unresolved type` 182 → 0 and 100 → 0 (the `DefaultDataType` and `DomainModel` ones came from the cycle too: they are resolved on the retry); `Parent concept … not found` 0 → 0 and 2 → 0 (`DocumentLibrary`); `NullPointerException` 10 → 0; `still invalid` 618 → 574 and 334 → 310. On a real formod-app start (2026-10-13): `Preventing StackOverflow` 0, `Unresolved type` 0, `Parent concept` 0, `still invalid` 62 (70 before), then 0 after the fixes below (second start, same day). Guarded by `TestCrossImportsLoading` in `fml-cli-test`.

**"still invalid": two families (measured 2026-10-13 with a temporary diagnostic of the owner of each binding).**
- *False positives, fixed.* 573 of the 574 of `formod-test`, 62 of 62 at the formod-app start, 8 of 8 in `xml-ta-test`: the owner is an `FMLSimplePropertyValue`, an annotation argument naming a named import (`ShapeRole(metamodelElement=C_CONTAINER)`, `XMLModelSlot(metaModel=OTAWA_PIPELINE)`). `FMLSimplePropertyValueNode` built a `DataBinding` whose owner was the property value, whose binding model (the one of the decorated object) has no named-import variable, then resolved the value by hand against the imports and dropped the binding, which stayed registered as "to revalidate" and was reported. The binding is now built with the compilation unit as bindable (its binding model has the named imports, as for the resource references of the imports) when the property is not a `DataBinding`. Chaining the unit binding model under the VirtualModel's was rejected: import names would validate in every expression while the run-time context of an instance cannot evaluate them.
- *True positives, kept.* Owner `ExpressionAction`: real mistakes in FML (`csv-ta-test-rc/.../TestCSVVM.fml`: `row.getCellAt(0)` where only `parameters.row` exists, `this.neighbors()` without its argument, `this.logData.row_3`), and invalid fixtures of `fml-cli-test` on purpose.

After the fix: `xml-ta-test` 8 -> 0, `formod-test` 574 -> 1 (then 0, see below), `formod-module` 310 -> 0, `csv-ta-test` 8 -> 8 (all real, fixed afterwards: see below); `fml-cli-test`, `flexo-foundation-test`, `formod-test`, `formod-module`, `xml-ta-test` green. No test of `openflexo-core` guards it: no FML property of the core is typed by an object (they all need a technology adapter), the guard is `xml-ta-test` / `formod-test`.

**Last case, fixed.** `selected.declaringElement == this` of a `FetchRequestCondition` (`FormoseCore`, the `select unique BMethodology …`): the revalidation of CORE-D-14 retried the bindings of each unit and its postponed parent concepts one unit after the other, so `FormoseCore` retried before `BMethodology` had attached its parent `Methodology` (which brings `declaringElement`), and was never retried. `CompilationUnitResourceImpl.finalizeLoadResourceData` now resolves what is postponed for all the units to revalidate first, then retries their bindings. `formod-test` 574 -> 0, `formod-module` 310 -> 0 (one earlier run of `formod-module` failed in the discovery of a technology adapter, before any finalization: not reproduced in 3 later runs, cause not established).

**True positives of `csv-ta-test-rc/.../TestCSVVM.fml`, fixed in the FML (2026-10-13).** `row.getCellAt(…)` -> `tmpRow.getCellAt(…)` (`row` is a parameter, only reachable through `parameters.row`, copied in `tmpRow`), `this.neighbors()` -> `this.neighbors(root)` for each root (as `PrinterModel.fml` does), `this.logData.row_3` -> `this.logData.getRowAt(3)` (`CSVDocument` has no `row_N` path). `csv-ta-test` 8 -> 0 `still invalid`, 117/117. None of the three methods is called by a test.

**Unrelated, seen in the same output.** `DiagramTechnologyAdapter.makeCreationSchemeAction` (l.570) prints `Qui passe ici ????` and a stack trace on every creation scheme of a diagram.

**Also in this log, fixed (2026-10-14):** `cannot find constructor null for type Association with arguments […]` (8 per formod-app start, 56 in the `formod-test` automated scenarios). Transient, as first thought, but for a reason that was not the one given (a first note said "resolved at run-time, `T8` passes", and a second one feared a creation scheme not run at run-time; neither was right). Measured: only the `new Association(…)` of `ConceptAttributeGR.dropAttributeGRonConceptGR` (`DomainModelDiagram.fml`, argument 5, `rangeDataType`) warned. It is analyzed by `ExpressionAction.finalizeDeserialization` (`getAssignableType()`), called by `stopDeserializing` at the END OF THE FIRST PASS of the diagram unit, which a nested load had started (the parse of `DomainModelling` asks for the compilation unit of an inner concept). At that time `DefaultDataType` has no parent concept (`getParentFlexoConcepts()` empty, `extends Concept` is attached by the second pass of `DomainModel`), so it is not assignable to the parameter `Concept` and the constructor is not found. The binding is then valid by itself by the second pass (`DataBindingNode.finalizeDeserialization`: `valid=true` for the 3 `new Association` of each of the 8 loads), so the creation scheme is found and run. `CreationSchemePathElement.resolve` no longer warns while `CompilationUnitResource.isWaitingForReferencedUnits()` is true: a constructor really missing is still reported when the binding is analyzed again, once the referenced units are complete (2 left in `fml-cli-test`, `new RootVirtualModel("Foo")`, an invalid fixture). `formod-test` 56 -> 0. No test of the core guards it: reproducing it needs a unit loaded in the middle of the parse of its container, as `DomainModelling` does with `DomainModel` and `DomainModelDiagram`. Same message as CORE-D-4, which has a different cause.

### CORE-D-35 — `PasteAction.isEnabledForSelection` logs a warning for an ordinary state  ·  `TODO`

**Symptom.** `Unexpected null focused object in PASTE`, once at the start of formod-app. Read in `PasteAction.isEnabledForSelection`: it warns when the focused object is null, which is the normal state of a menu computed with nothing selected. The call that triggers it was not identified. Severity: noise.

### CORE-D-36 — `DirectoryWatcher.waitNextWatching()` can return before a file written just before the call is seen, and gives up silently  ·  `TODO`

**Symptom.** A test that writes files into a directory resource center, calls `getDirectoryWatcher().waitNextWatching()` once and then asks the center for the new resource gets null, intermittently: `Atelier B project SourceAtelierBProject was not registered` in `formod-test` (`T6`, `T7`, `T10`), which provisions two blank Atelier B projects before each script. Measured on 2026-10-10 on the same machine, formod-test: 3 green runs out of 4 on the state without the dependency changes of UI-D-1, 1 green out of 4 right after them; 6 out of 6 after the test-side fix below. `b-ta-test`: 131 of 132, the only red being FORMOD-D-1 (`testExpression7`), as before. Same family as `directory-watcher-flaky-tests` of the workspace memory.

**Mechanism — read in `openflexo-utils/flexo-utils/.../toolbox/DirectoryWatcher.java`, not reproduced in isolation.**
1. `waitNextWatching(timeout)` sets `waitNextWatchingRequested`, then waits for `waitNextWatchingDone`. `run()` sets the latter at the end of the watching **in progress**, even when that watching started before the call: a scan that began before the project was written ends, the wait returns, and the new project was not in what that scan listed.
2. After `timeout` (5 s by default) it throws an `InterruptedException`; the callers found in formod swallowed it, so the test went on without the resource.
3. `waitNextWatchingRequested`, `waitNextWatchingDone` and `isRunning` are plain fields read by the waiting thread: no visibility guarantee other than the `Thread.sleep` of the loop.

**Worked around in the tests (2026-10-10).** `FormodAutomatedTests.provisionAtelierBProject` and `TestCreateBlankAtelierBProject` wait again, up to 10 times, until the resource is registered.

**To decide.** Making `waitNextWatching` wait for a watching that STARTED after the call (a counter of started and completed runs, both `volatile`, waiting for `completed >= startedAtCall + 1`), and not turning the timeout into an `InterruptedException` the callers cannot tell from a real interruption. The default delay of 5 s must be checked against the period of the watcher before making the wait stricter.

### CORE-D-52 — The text printed for a concept-typed property with an initializer does not parse again  ·  `TODO`

**Symptom.** `Person first = new Person();` is printed `Person first with ConceptInstance()= new Person();`, which the parser
rejects (`ParseException: expecting: ';'`). A unit holding such a declaration cannot be saved from the Modeller and loaded again.
Unlike the rest of `CORE-D-33`, this is a loss of the unit, not a cosmetic difference.

**Reproduction (verified 2026-11-11 by execution, test `KnownDefectsRoundTripTest` in fml-parser).** Parse the declaration, print
the compilation unit, parse the printed text.

**Mechanism — verified in the code.** `FlexoRolePropertyNode.definePrettyPrint` always appends `with <role>(…)` when
`hasFMLProperties()`, even for a declaration written in the Java style; the `= …` comes from the original text kept around the
name. The `{fml}` form of the grammar has no slot for an initializer. Found by the external audit of the FML language of 2026-10-23 (its §3.6).

**Workaround.** Do not give an initializer to a concept-typed property (it is lost anyway, `CORE-D-51`).

**Partly fixed (2026-11-13) by CORE-D-33.** A declaration written in the Java style no longer gets a `with` clause while its role is the
default one: `Person first = new Person();` is printed as written and parses again (`KnownDefectsRoundTripTest`, run with
`OPENFLEXO_KNOWN_DEFECTS`). Still broken: once the role is given a property value, the clause is inserted after the name, before `= ...`.

---

## FML queries

### CORE-D-53 — `select` from a concept instance does not search the instances nested deeper than one level  ·  `TODO`

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

**Symptom.** `required Boolean aC2, Boolean aC3=true` (parameters of a behaviour) were printed `boolean`: the model holds the primitive type.
File: `UIAnnotations` (`FMLParsingExamples`). Seen in the pretty-print written from the model (2026-11-13); since CORE-D-33 the preserving
pretty-print of an untouched source keeps `Boolean`, but the model still says `boolean` (the normalized pretty-print shows it). Not checked:
where the type is changed (parser, typing space, parameter), and whether other wrappers are concerned.

### CORE-D-70 — The version of a type reference is lost  ·  `TODO`

**Symptom.** `Foo@(1) foo1;` (a type with a version, `typedef String as Foo`) was printed `Foo foo1;`: the version is not in the model.
File: `TestTypeVersions` (`FMLParsingExamples`). As for CORE-D-69, the preserving pretty-print of an untouched source now keeps `Foo@(1)`, the
model and the normalized pretty-print do not. Not checked: whether a version is meant to be kept by the model at all.
