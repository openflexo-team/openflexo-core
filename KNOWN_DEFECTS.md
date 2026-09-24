# openflexo-core — Known defects

Bugs of openflexo-core that are known and not fixed yet. Each entry says what is verified, how to reproduce it, and how to work
around it. Features and evolutions go to [`BACKLOG.md`](./BACKLOG.md), not here.

Identifiers are stable and never reused: `CORE-D-<n>`. Reference them from commits and from other entries.

Status legend: `TODO` · `IN PROGRESS` · `BLOCKED` · `DONE` · `DEFERRED`.

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

### CORE-D-2 — `end match … unmatched: method()` runs the first action scheme instead  ·  `TODO`

**Symptom.** `end match X in set unmatched: someMethod();` parses, logs a warning, and then runs on each leftover instance NOT
`someMethod()` but the first accessible action scheme of `X` — a deletion scheme when one is declared first. The leftovers are then
deleted although the FML asks for something else.

**Reproduction (verified 2026-09 by execution).** With `Stale` declaring `delete::_delete()` before `markStale()`, and one `Stale`
instance: after `pruneWithMethod()` the instance is gone, `_delete` ran, `markStale` never ran.

```fml
pruneWithMethod() {
	MatchingSet<Stale> staleMatching = begin match Stale from this;
	end match Stale in staleMatching unmatched: markStale();
}
```

**Mechanism — verified in the code.**
1. `EndMatchActionNode.buildModelObjectFromAST()`, `AComplexEndMatchActionClause` branch (fml-parser): the method invocation is not
   decomposed into behaviour name + arguments; the behaviour is left unresolved and the warning
   `'end match ... unmatched: <method invocation>' is not yet supported by the parser` is logged.
2. `FinalizeMatching.getFlexoBehaviour()` (flexo-foundation): when the behaviour is null, it lazily falls back to the first entry of
   `getAvailableFlexoBehaviours()`, i.e. `getFlexoConceptType().getAccessibleAbstractActionSchemes()`.
3. `DeletionScheme` extends `AbstractActionScheme`, so that first entry may be a deletion scheme; `FinalizeMatching.execute()` then
   runs it on each unmatched instance.

**Workaround.** Only use `unmatched: delete()`, on a concept declaring exactly ONE deletion scheme (see `CORE-F-2`).

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

**Workaround.** Filter in the matches instead: iterate over the relevant sources only, and match on criteria.

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

---

## FML behaviours

### CORE-D-4 — A parameter default value does not allow omitting the argument in `new`  ·  `TODO`

**Symptom.** A creation scheme parameter declaring a default value (`int capacity=10`) must still be passed explicitly: a `new`
expression omitting it does not resolve, and evaluates silently to null at run-time. Whether default values are meant to allow
omitting arguments, or only to pre-fill the parameters user interface, has to be decided as part of the fix.

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

---

## FML control structures and edition actions

### CORE-D-6 — A `do … while` loop executes its body once and ignores its condition  ·  `TODO`

**Symptom.** `do { … } while (condition);` parses without any warning, but the body is executed exactly once, whatever the condition.

**Reproduction (verified 2026-09-15 by execution).** The following behaviour returns 1 instead of 3:

```fml
public int doWhileCount() {
	int n = 0;
	do {
		n = n + 1;
	} while (n < 3);
	return n;
}
```

**Mechanism — verified in the code.**
1. `ControlGraphFactory.inADoStatementStatementWithoutTrailingSubstatement()` (fml-parser) builds no node for the `do_statement`
   production: only its inner statement ends up in the control graph, and the condition is dropped.
2. `WhileAction` supports the post-condition form (`setEvaluateConditionAfterCycle(true)`), but nothing in fml-parser sets it.

**Workaround.** Use a `while` loop.

### CORE-D-7 — A compound assignment statement ignores its operator  ·  `TODO`

**Symptom.** In a statement `x += value;` (and likewise `-=`, `*=`, `/=`…), the operator is ignored: `x` receives `value`.

**Reproduction (verified 2026-09-15 by execution).** The following behaviour returns 2 instead of 7:

```fml
public int plusAssign() {
	int n = 5;
	n += 2;
	return n;
}
```

**Mechanism — verified in the code.** `AssignationActionNode.buildModelObjectFromAST()` (fml-parser) builds a plain
`AssignationAction` from the left-hand side and the right-hand side; the `assignment_operator` is only used to locate a source
fragment, and the pretty-print always writes `=` (`AssignationAction` carries `// TODO: manage assignment operator`). The operator
inside an expression, as in `i = i += 2`, is evaluated by Connie and is not affected.

**Workaround.** Write the full expression: `n = n + 2;`.

### CORE-D-8 — An invalid condition is evaluated as true by `if` and `while`  ·  `TODO`

**Symptom.** When the condition of an `if` is invalid (unresolved binding), its `then` branch is executed; a `while` with an invalid
condition loops forever. Nothing is reported at run-time.

**Reproduction (verified 2026-09-15 by execution for `if`).** The following behaviour returns `"then"`:

```fml
public String invalidCondition() {
	String r = "none";
	if (zzUnknownVariable.foo) {
		r = "then";
	}
	else {
		r = "else";
	}
	return r;
}
```

**Mechanism — verified in the code.**
1. `ConditionalActionImpl.evaluateCondition()` returns `true` when the condition is not set or not valid, and when its evaluation
   throws a `TypeMismatchException` or a `ReflectiveOperationException`; it returns `false` for a null value or a
   `NullReferenceException`.
2. `WhileActionImpl.evaluateCondition()` has the same code, except that a `NullReferenceException` also yields `true`. The `while`
   case was read in the code, not executed (it would not terminate).

**Workaround.** Validate the compilation unit: the invalid condition is reported by the validation rule
`ConditionBindingIsRequiredAndMustBeValid`.

### CORE-D-9 — A `for (init; ; update)` loop without condition makes its behaviour disappear  ·  `TODO`

**Symptom.** A behaviour containing a classic `for` loop whose condition is omitted is not built: the compilation unit loads, but
the behaviour is missing, and any call to it is an unresolved binding (evaluated as null).

**Reproduction (verified 2026-09-15 by execution).** Calling `forWithoutCondition()` from a script gives
`Invalid binding value: vmi.forWithoutCondition()`; the log shows a `NullPointerException`.

```fml
public int forWithoutCondition() {
	int n = 0;
	for (int i=0; ; i++) {
		n = n + 1;
		if (i > 5) {
			return n;
		}
	}
	return n;
}
```

**Mechanism — verified in the code.**
1. `ExpressionIterationActionNode.buildModelObjectFromAST()` (fml-parser) passes the null condition of the `for_basic` production to
   `ExpressionFactory.makeDataBinding()`, which throws a `NullPointerException` (line 105).
2. Even if the node were built, `ExpressionIterationActionImpl.evaluateCondition()` returns `false` for an unset condition, so the
   body would never be executed — unlike Java, where a missing condition means `true`.

**Workaround.** Always write the condition; to loop until a `return`, use `while (true)`.

### CORE-D-18 — An increment or decrement used as a statement makes its behaviour return null  ·  `TODO`

**Symptom.** A behaviour containing `n++;`, `++n;`, `n--;` or `--n;` as a statement returns null, whatever it returns and whatever the
variable is (a local variable or a property). Nothing is reported. The same operator works in the update part of a `for`
(`for (int i = 1; i <= n; i++)`) and inside an expression (`int m = n++;`).

**Reproduction (verified 2026-09-21 by execution, in `flexo-test-resources`).** The following behaviour, called from a script,
returns null instead of 7:

```fml
public int constAfterInc() {
	int n = 1;
	n++;
	return 7;
}
```

Also measured: `n++; return n;`, `++n; return n;` and `n--; return n;` give null too, and so does `touched = 3; touched++; return touched;`
on a property. `int m = n++; return n;` gives 2.

**Mechanism — read in the code, not confirmed by execution.** `ControlGraphFactory` (fml-parser) builds a node for the alternatives
`assignment`, `new_instance`, `method_invocation` and the action expressions of `statement_expression`, and has no handler for
`pre_increment`, `pre_decrement`, `post_increment` and `post_decrement` (they only appear in a comment listing the production).
What happens to the behaviour instead is not established.

**Workaround.** Write the assignment: `n = n + 1;`.

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

---

## FML compilation unit loading

### CORE-D-14 — Compilation units importing each other load forever, or keep bindings analyzed against partial types  ·  `DONE`

**Symptom.** Two defects of the loading of compilation units which import each other — typically a core model whose members are
typed by the models built on it, while those models type their own members with concepts of the core model:
1. loading never ends. The thread spends its time in `P2PPNode.getTextualRepresentation()` / `DerivedRawSource`, called from
   `CreationSchemePathElement.resolve()` while `CompilationUnitResourceImpl.finalizeLoadResourceData()` analyzes the bindings;
2. once the first defect is fixed, a binding of the unit finalized first which reaches a member of the other one, not declared yet at
   that time — in particular a member inherited from a third unit — stays invalid (`unresolved path element declaringElement`).
   Validation does not report it; at run-time the binding evaluates to null, so `select unique X from … where
   (selected.declaringElement == this)` silently selects nothing.

**Reproduction (verified 2026-09-18 by execution).** `flexo-test-resources`, `FML/CrossImports.fml` (Core types two computed
properties with DerivedA and DerivedB, which extend Base, whose `declaringElement` is an element of Core), exercised by
`AutomatedTests/TestCrossImports.fmlscript` in `fml-cli-test`. Without the first fix the script never ends; without the second it
fails line 31, `root.applicableDerivedB[null] == derivedB`. First met in `formod-rc` (`FormoseCore` and the Formose methodologies).

**Mechanism — verified by execution.**
1. `PamelaResourceWithPotentialCrossReferencesImpl.performLoadResourceData()` ran the first loading pass on every cross-reference
   dependency not loaded yet, including one whose load was in progress higher in the stack: that unit was parsed a second time, and
   the pretty-print nodes of the objects shared by both parses were initialized twice. Their contents double at each level, so
   computing a textual representation is exponential.
2. When units import each other, one of them is necessarily finalized before a unit it references: its bindings are analyzed against
   the partial content of that unit, and a `DataBinding` found invalid is cached as such.

**Fix.**
1. A cross-reference dependency whose load is in progress (`isLoading()`) is left to that load, which runs both passes on it.
2. `CompilationUnitResourceImpl.finalizeLoadResourceData()` registers the unit with each referenced unit not finalized yet; when that
   one is, the bindings of the registered units still invalid at the end of their semantics analyzing are analyzed again
   (`FMLSemanticsAnalyzer.attemptToFixInvalidBindings()`). Rebuilding all their bindings (`FMLCompilationUnit.revalidateAllBindings()`)
   is not an option: re-parsing an expression out of its compilation unit loses the parameters of a cast to a technology specific
   type (`(EMFObjectIndividualType(eClass=TASK)) x` becomes an `EMFObjectIndividual`), and the typing space of a unit built before
   the unit knew its service manager makes that re-parse fail with a `NullPointerException` (`TypeFactory`, measured on
   `modelers/bpmn-modeler`).

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

**Workaround.** Declare a creation scheme in the child, even an empty `create() { }`.

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
