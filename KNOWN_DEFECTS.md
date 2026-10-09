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

---

## Resources: unloading and deletion

`CORE-D-21`, `CORE-D-22` and `CORE-D-23` were found and fixed together; `CORE-D-24` is what the same measurement left open. `CORE-D-27`
was met while closing a project, in the same code; it predates `CORE-D-21`. The
regression test of the first four is `flexo-foundation-test`, `TestUnloadDoesNotDelete`, over the fixtures `FML/UnloadLedger.fml` and
`FML/UnloadProbe.fml` of `flexo-test-resources`: an `Item` whose deletion scheme increments a counter of a ledger held in another
resource, and whose renderer reads that ledger.

### CORE-D-21 — Closing a project runs the deletion schemes of every loaded instance  ·  `DONE`

**Symptom.** Closing a project, or removing any resource center, executes the FML deletion scheme of every loaded concept instance
of that resource center. Deletion schemes are business logic that change other models; unloading must not run them. In Formose,
closing the project runs the `deleteWithBItem()` rules of the B methodology, which fail with `NullReferenceException while
evaluating container.context.componentResource.bComponent.removeFromSets(bSet)`.

**Reproduction (verified 2026-09-21 by execution).** `formod`, module `formod-module`, uiTest `TestBMethology.testReloadProject`;
reduced in `TestUnloadDoesNotDelete.testClosingProjectDoesNotRunDeletionSchemes`, which fails as soon as the fix is reverted.

**Mechanism — verified by execution.**
1. `FlexoProjectImpl.close()` removes the project's resource center.
2. `DefaultResourceCenterService.removeFromResourceCenters()` called `resource.unloadResourceData(true)` on every loaded resource.
   That `true` came with 964643eba4 (2017-04-26), after the parameter itself (187e4894f, 2015, "Improved unload support with/without
   deletion"); nothing says why deleting was wanted there.
3. `PamelaResourceImpl.unloadResourceData(true)` therefore deleted the resource data: `VirtualModelInstance.delete()`, then PAMELA's
   cascade over the embedded instances, each `FlexoConceptInstanceImpl.delete()` running its concept's deletion scheme.

The likely reason for `true` — releasing the unloaded objects — does not hold: measured, `delete()` released almost nothing either.
And `unloadResourceData(false)` released nothing at all. After unloading a probe instance, the following still referenced its items:
- the index of the resource itself, `PamelaResourceImpl.objects` (FlexoID → object), never emptied — its reset in `indexResource()`
  is commented out;
- listeners on the ledger, another resource, growing at each load/unload cycle (10, 15, 24, 25 in the measurement): the
  `rendererChangeListener` of `FlexoConceptInstanceImpl`, never stopped, even by `delete()`; and the listener the renderer's
  `DataBinding` caches per evaluation context;
- that renderer `DataBinding`, which belongs to the VirtualModel: its `cachedValues` and `cachedBindingValueChangeListeners` are
  strong maps keyed by the instance. Connie had no way to drop a context from them (`clearCacheForBindingEvaluationContext()` only
  drops the value), and `BindingPathChangeListener.delete()` did not unregister the listener;
- the `BindingPath`s of any expression evaluated on an item (`CORE-D-23`).

PAMELA offers no disposal protocol distinct from deletion: `destroy()` only empties the properties and makes the object unusable;
the registrations are Connie's and flexo-foundation's, which PAMELA does not see. So the fix is not in PAMELA.

**Fix.**
1. `removeFromResourceCenters()` unloads with `false` — except the data of a `FlexoProjectResource`, the `FlexoProject` itself, still
   deleted. That deletion runs no behaviour, and it is what reopening a project relies on: `ProjectLoader` keeps the project resource,
   and the resource center it delegates to, across a close; deleting the `FlexoProject` (a `ResourceRepository`) detaches the old
   resources from that resource center, so that reopening builds and registers new ones. Measured: unloading it with `false` too,
   every `testReloadProject` finds the reopened project's repositories empty (`TestPopulateVirtualModelInstance`,
   `TestFlexoRoleCardinality`, `city-mapping`…). This is probably why `true` was chosen in 2017; dereferencing the resources of a
   removed resource center properly — the `TODO` left in `removeFromResourceCenters()` — would remove the need for it.
2. A release path distinct from deletion: `ReleasableObject.release()` undoes the registrations an object made outside of its
   resource data, and runs no behaviour, changes no property, notifies no deletion. `PamelaResourceImpl.unloadResourceData(false)`
   releases the resource data, which releases the objects it contains; with or without deletion it then empties the index.
   `FlexoConceptInstance` releases its `rendererChangeListener` and what the renderer of its concept caches for it;
   `VirtualModelInstance` releases its concept instances and its indexes (`FlexoConceptInstanceIndex.release()`). Deleting an
   instance releases it too. The release must NOT walk PAMELA's embedding closure: computing it calls getters such as
   `VirtualModelInstance.getVirtualModelInstances()`, which load the contained virtual model instances — unloading a view then
   loaded its children again, depending on the order of `getAllResources()` (measured: an intermittent `testReloadProject` red).
   `FMLRTVirtualModelInstanceResourceImpl` still removes the instance from the run-time engine, as before.
3. Connie: `DataBinding.releaseEvaluationContext(context)` drops the cached value and deletes the listener for a context;
   `BindingPathChangeListener.delete()` stops observing first; `DataBinding.delete()` and a change of caching strategy release every
   context.

Asserted directly: after an unload, no listener of an item is left on the ledger and the item is garbage collected. In formod,
`TestBMethology.testReloadProject` no longer runs any deletion scheme on close (40 `NullReferenceException`s before, none after); it
stays red for a reason of its own — expected 8 contained instances, 5 on reload, as before the fix — because `instantiateBMethodology`
aborts its save loop on the jar resource center's `DocumentLibrary` (`SaveResourcePermissionDeniedException`), leaving 3 instances
unsaved.

**Not covered.** The undo history keeps the edits of the actions that created objects, hence those objects, after they are unloaded.
Resources that are not `PamelaResource`s (technology adapters over `FlexoResourceImpl`) get no release: an adapter whose objects
register listeners elsewhere must release them in its own `unloadResourceData()`.

### CORE-D-22 — Deleting a virtual model instance runs the deletion schemes of its instances without their container  ·  `DONE`

**Symptom.** Deleting a virtual model instance — in particular deleting its resource — runs the deletion scheme of each instance it
holds with `container` already null: a scheme that navigates through its container fails with `NullReferenceException while
evaluating BindingPath container…: null occured when evaluating container`, and its side effects are lost.

**Reproduction (verified 2026-09-24 by execution).** `TestUnloadDoesNotDelete.testDeletingTheResourceRunsDeletionSchemesWithTheirContainer`:
deleting the probe resource with two items must add 2 to the ledger; with the fix disabled it adds nothing (expected 3, was 1).
The same failure showed while measuring `CORE-D-21`, on `unloadResourceData(true)`.

**Mechanism — verified by execution.** `FlexoConceptInstanceImpl.deleteWithScheme()` ran the scheme of the instance being deleted,
then detached it and called `performSuperDelete()`: PAMELA nullifies the properties first, the inverse `owningVirtualModelInstance`
/ `containerFlexoConceptInstance` of the embedded instances included, and deletes those embedded instances afterwards — running
their schemes with no container left.

**Fix.** `deleteWithScheme()` runs the scheme, then deletes the contained instances itself, each with its own default deletion
scheme and while it is still attached, and only then detaches and deletes the instance (`deleteContainedFlexoConceptInstances()`;
a `VirtualModelInstance` deletes its root instances, each deleting the instances it contains).

### CORE-D-23 — Every evaluation of an expression leaves listeners on its owner  ·  `DONE`

**Symptom.** `FlexoConceptInstance.execute(String)` — and more generally every `DataBinding` built from a string in an FML context —
leaves listeners registered on the owner of the binding and on its binding variables, for good: two more on the instance at each
call. They keep the binding and its owner alive, and are notified at every change.

**Reproduction (verified 2026-09-24 by execution).** `TestUnloadDoesNotDelete.testExecuteLeavesNoListener`: the number of listeners
of the ledger after 5 calls of `ledger.execute("this.deletions")`.

**Mechanism — verified by execution.** Three causes, the first one also able to unregister the WRONG listener:
1. `DataBinding` registered itself on its owner, binding model and binding variables, and `BindingPath` itself on its binding
   variable. Both override `equals()` to compare their text, and a `PropertyChangeSupport` removes the first listener equal to the
   one it is given: unregistering a binding could remove another binding of the same text, and leave itself registered.
2. `DataBinding.delete()` did not unregister from its owner (registered by `setOwner()`); `BindingPath.clear()`, called by its
   `delete()`, dropped its binding variable without unregistering from it.
3. `FMLExpressionParser.parse()` builds a `DataBinding` on the bindable only to return its expression; that vehicle stayed
   registered on the bindable.

**Fix.** `DataBinding` and `BindingPath` register a private listener whose equality is identity (a clone of a `BindingPath` gets its
own); `DataBinding.stopListening()` unregisters from everything a binding listens to without touching its expression, and is used
by `delete()` and by `FMLExpressionParser.parse()` on its vehicle; `BindingPath.clear()` unregisters from its binding variable.

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

### CORE-D-25 — Two threads creating the same repository: the loser keeps an orphan one  ·  `DONE`

**Symptom.** Intermittently, a repository obtained from a resource center (`project.getVirtualModelInstanceRepository()`, or any
`XxxTechnologyAdapter.getXxxRepository(rc)`) is not the one the resource center keeps: resources created afterwards are registered in
another repository, and whoever listens to the first one — a browser, a test — is never told. The log shows `Repository already
registered: … for …`. `TestResourceLoadedStateIsNotified` failed that way in 5 out of 8 complete runs of `flexo-foundation-test` after
`CORE-D-21` shifted the timing, and 0 out of 3 before — the defect itself predates it.

**Reproduction (verified 2026-09-25 by execution).** `flexo-foundation-test`, `rm/TestConcurrentRepositoryCreation`: two threads ask a
fresh resource center for its virtual model instance repository at the same time, and must both get the registered one. With the old
callers it fails on the first attempt.

**Mechanism — verified by execution** (thread dump captured when the test failed within the complete suite).
1. Every repository accessor of a technology adapter does, unsynchronized: `retrieveRepository()`; if null, `instanciateNewRepository()`,
   then `registerRepository()`. `registerRepository()` kept the first one registered, logged a warning for the other — and the caller
   of the other one returned it all the same.
2. Such accessors run in several threads: `TechnologyAdapter.updateRepository()` posts `notifyRepositoryStructureChanged()` to the Swing
   EDT, even in headless tests, and that notification creates the missing repositories (`getAllRepositories()` →
   `getRegistedRepositories(ta, true)` → `ensureAllRepositoriesAreCreated()`). Here the EDT and the test thread created the repository
   of the same new project together. The `DirectoryWatcher` is another such thread.
3. `getRegistedRepositories()` also returned a live view of a map that another thread could change during the iteration.

**Fix.** `FlexoResourceCenter.registerRepository()` is atomic and returns the repository actually registered — supplied one, or the one
another thread registered first — and callers use what it returns. `FileSystemBasedResourceCenter` and `JarResourceCenter` guard their
repository map with a lock (the repository itself is built outside of it, and notifications are sent outside of it);
`getRegistedRepositories()` returns a copy. `FMLTechnologyAdapter` and `FMLRTTechnologyAdapter` use the returned repository.

**Technology adapters.** Every live accessor of the other technology adapters was changed the same way (30 call sites, 23 files):
pdf, docx, mcp, diagram, xml, markdown, owl, capella, pptx, http, emf, opc-ua, json, odt, java, gina, xlsx, csv, oslc, rhapsody,
`openflexo-technology-adapters` (dsl, xx) and formod's b-ta. The call sites left unchanged are in commented-out code.
`openflexo-http` is not in the `openflexo-dev` composite: its change was not compiled.

### CORE-D-27 — Closing a reloaded project fails on a compilation unit that was never loaded  ·  `DONE`

**Symptom.** `FlexoProject.close()` threw a `ModelExecutionException` caused by a `NullPointerException` in
`CompilationUnitResourceImpl.computeDefaultURI()` (`flexo-foundation-rm`), raised from
`DefaultResourceCenterService.removeFromResourceCenters()`. The project was left half-closed: `closed` never set, and the
resource center notification interrupted for the remaining technology adapters.

**Reproduction (verified 2026-09-28 by execution).** `flexo-foundation-test`, `TestCloseReloadedProject`: a project holding
a virtual model, loaded again in a NEW service manager (nothing loaded), then closed. Also free-modelling-editor, uiTests
`TestCreateFreeModel` and `TestCreateFreeModelWithInstances` (`tearDownClass`, after they reload their project).

**Mechanism — verified by execution.**
- `removeFromResourceCenters()` unloads the loaded resources, and DELETES the data of the project resource, the
  `FlexoProject` itself: this detaches the resources of the project (reopening it relies on it), and detaches the project
  from its delegate resource center. Then it notifies `ResourceCenterRemoved`, on which each technology adapter unregisters
  its resources.
- `ResourceRepositoryImpl.unregisterResource()` removed a resource from its map BY URI: `resources.remove(r.getURI())`. A
  compilation unit never loaded has no URI of its own and computes it from its resource center, the detached project, whose
  default URI is then null.
- This order predates `CORE-D-21`: before `49602a019` every resource was deleted before the notification, the project
  included.

**Why not reordering.** Deleting the project after the notification was tried and measured: the technology adapters empty
the repositories first, the deletion then detaches nothing, and a project reopened in the same service manager finds its old
resource objects again, unregistered — its repositories stay empty (three `testReloadProject` of flexo-foundation-test
failed). The two needs conflict: unregistering wants the project attached, deleting it wants the repositories full.

**Fix.**
1. `ResourceRepositoryImpl.unregisterResource()` removes the resource by identity; the URI is only a fast path, used when it
   still maps to that very resource. This also covers a resource whose URI changed since it was registered, which left an
   orphan entry.
2. `CompilationUnitResourceImpl.computeDefaultURI()` answers null instead of failing when its resource center gives no
   default URI; `FlexoResourceImpl.getURI()` then falls back on the serialization artefact.

Test: `TestCloseReloadedProject` — reopening in the same service manager keeps the virtual model in the project's
repository; closing a project reloaded in a new service manager does not fail.

---

### CORE-D-28 — Reloading a project leaves two generations of the same resource, and references straddle them  ·  `DONE`

**Symptom.** After a project is closed and reloaded, two distinct object graphs of the same `.fml.rt` resource are alive at once, so
the same conceptual instance exists twice. An FML expression comparing identities then fails for some objects and succeeds for
others, intermittently: `select unique SysMLKaosMethodology from container.container where (selected.declaringElement == this)`
returns null although the methodology is there and its `declaringElement` is restored.

**Reproduction (verified 2026-10-07 by execution).** `formod`, module `formod-module`, uiTest `TestBMethology.testReloadProject`,
which fails about one run in three (the measurement: 2 green, 1 red; with probes in place, four reds in a row — the timing matters,
as for a race). Probing the reloaded model shows one resource object and several data objects:

```
projectElement     : Element id=343616650 in FormoseCore object 1703832508
DocumentAnnotation : declaringElement id=343616650  -> same object
SysMLKaos/DomainModel/B : declaringElement id=545990162 in FormoseCore object 5700829
both                 : resource 751724127, .../FormoseView.fml.rt/FormoseVMI.fml.rt
```

Which methodologies fall on which side changes from run to run. At the moment of the assertion,
`resource.getLoadedResourceData()` is null: the resource is not loaded any more, while objects of its graphs are still referenced
by the reloaded view.

**Mechanism — verified by execution.** Instrumenting `PamelaResourceImpl` (load, unload), `FlexoResourceImpl.setResourceData` and
`FlexoObjectReference.getObject(boolean)` on that one resource, all on the test thread, with no `DirectoryWatcher` involvement:

1. `reloadProject` → `FlexoProjectImpl.close()` → `DefaultResourceCenterService.removeFromResourceCenters()` unloads the resource
   without deleting its data (CORE-D-21): generation 1 stays alive wherever it is still referenced;
2. during the test, `FMLRTModelSlotInstance.getAccessedResourceData()` loads the resource again: generation 2;
3. `FlexoObjectReference.getObject(boolean)` caches the object it resolved in `modelObject` and resolves only when that field is
   null. **Nothing invalidates it when the resource is unloaded.** An `ActorReference` that resolved before the project was closed
   therefore keeps pointing at a generation 1 object for ever, while one resolving for the first time after the reload gets
   generation 2. Measured on the failing run: 36 cache hits against 9 resolutions, the resolutions coming from the earlier
   validation of the project (`FlexoObject.getEmbeddedValidableObjects` → `ActorReference.getModellingElement`). Which references
   had already resolved depends on what the earlier test steps touched, which is why the failure moves from one assertion to
   another;
4. at `tearDownClass`, the project is closed again and generation 2 is unloaded (harmless, after the assertions).

**Fix (2026-10-07).** Unloading a resource resets the references that cached one of its objects.
1. `FlexoObjectReference.releaseObject()` forgets the cached object (and removes the reference from the `referencers` of that object),
   forgets the cached `resource` too — reopening a project builds another resource object with the same URI — and puts the status back
   to `UNRESOLVED`. A deleted reference, or one in status `DELETED`, is left alone, so deletion notifications are untouched
   (`notifyObjectDeletion()` has no caller anyway).
2. `PamelaResourceImpl.unloadResourceData()` calls `releaseReferences()` once the data is released (or deleted) and before the index is
   emptied: it walks the indexed objects and releases their `referencers`. This is the same hook for both flavours of unload.
3. The walk relies on the index of the resource, which is built lazily. `FlexoObjectReference.setObject()` therefore registers its
   object in the index of its resource, so that a reference built on an object nobody has looked up yet is still found. Building
   the index at unload time instead was rejected: computing the embedding closure calls getters that load other resources.

*Where the walk belongs* — in the resource: it is the only party that knows both the objects it drops and the moment it drops them.
Listening to `isLoaded` from every reference would register each reference on its resource (a strong reference from the resource, and
a listener to remove on deletion) for no gain.
*What a reference answers while its resource is unloaded* — it resolves on read, which loads the resource: exactly what a reference
that never resolved did before, so no new behaviour. Answering null until someone loads it would make the answer depend on who
loaded what first, which is the intermittence this defect is about.

**Not the same mechanism as CORE-D-24.** Both are caches that no lifecycle event clears, but this one holds a *result* that the
object it points at knows how to reach (`referencers`), whereas the caches of CORE-D-24 are held by a binding of the VirtualModel,
keyed by evaluation contexts that no object of the unloaded resource points back to: the walk cannot find them. What D-24 can reuse
is the hook, not the mechanism — `release()` of the instances (CORE-D-21) is already where the renderer is let go of.

**Limit.** Only references to objects of a `PamelaResource` are reset. `FMLRTModelSlotInstance` (a role typed by a virtual model)
does not go through a `FlexoObjectReference` and was not touched.

Test: `flexo-foundation-test`, `TestReloadedResourceReferences` (fixture `FML/UnloadHolder.fml`): a model in a resource center that
stays loaded holds a role pointing at an `UnloadProbe.Item` of a project; the role is resolved, the project closed and reopened, and
the role must answer the item of the CURRENT data, the very one found through the reloaded probe. Red without the fix (it answers
the first generation), green with it. `formod`, `TestBMethology`: 6 runs in a row green (9/9), against 1 red in 3 before.

**Not the cause** (both excluded by measurement): no `where` condition is invalid at evaluation (so not the CORE-D-14 family), and
`declaringElement` is correctly restored from the `.fml.rt`. The `DirectoryWatcher` is not involved either: every event is on the
test thread.


## Resources: loading

### CORE-D-26 — Loading a compilation unit leaves it modified  ·  `DONE`

**Symptom.** A VirtualModel loaded from disk and never edited reports itself as modified, so the application offers to save it and
`ResourceManager.getUnsavedResources()` lists it. On a resource center read from a jar this cannot even be done: saving raises
`SaveResourcePermissionDeniedException`. Measured on `formod-rc`: every contained compilation unit came back modified right after
loading.

**Reproduction (verified 2026-09-25 by execution).** `flexo-foundation-test`, `TestLoadDoesNotModify`, over the `FML/PingPong.fml` and
`FML/CrossImports.fml` fixtures of `flexo-test-resources`: it loads a container and its contained units and asserts that none is
modified. First met in `formod`, uiTest `TestBMethology.instantiateBMethodology`, which saves every unsaved resource and tripped on
`DocumentLibrary`, a VirtualModel of a jar resource center.

**Mechanism — verified by execution (instrumenting the PAMELA modified flag).** Two contributors, both on read paths:

1. `FlexoConcept.getInspector()` created the deprecated `FlexoConceptInspector` on the fly and gave it a title. Setting that title
   marks the concept, hence its compilation unit, as modified — and the lazy creation fires from ordinary reads: binding analysis
   (`FMLBindingFactory._getAccessibleSimplePathElements`), the localization scan (`FMLCompilationUnit.searchNewEntriesForConcept`) and
   `ModuleInspectorController`.
2. The renderer of a concept is stored as the `@Renderer` metadata, and the deprecated inspector was the only object able to hold its
   `DataBinding`. Analyzing that binding parses its expression, which notifies a change, which wrote the metadata back — marking the
   unit modified although nothing was edited.

**Fix.**
1. `getInspector()` never creates anything and returns null for a concept that declares no inspector; `getOrCreateInspector()` is the
   explicit form, used by the few places that edit one (the concept creation wizard, free-modelling-editor, diagram-ta's concept from a
   diagram element, jdbc-ta's mapping generator). Readers were made null-tolerant.
2. The renderer moved to `FlexoConcept` itself: `getRenderer()` / `setRenderer()` read and write the `@Renderer` metadata, and the
   binding is owned by a `FlexoConceptRenderer` exposing the rendered instance as `instance`
   (`FlexoConceptRendererBindingModel`). `FlexoConceptInspector.getRenderer()` delegates to it, so both can never disagree. The
   metadata is written back only when the expression really differs from the one it holds, not when the binding is merely parsed.
3. `CompilationUnitResourceImpl.finalizeLoadResourceData()` clears the modified flag after the second analysis pass when the unit was
   not modified before it, symmetrically to what the first pass already did.

**What is NOT this defect.** A compilation unit whose file lacks a `use` declaration it needs is completed at load
(`FlexoProperty.handleRequiredImports` → `FMLCompilationUnit.ensureUse`), and that legitimately marks it modified: the file on disk is
incomplete. Two fixtures were completed rather than the platform changed — `flexo-test-resources` `FML/CrossImports.fml` and formod's
`Methodology.fml`.

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

### CORE-D-31 — A technology used only through edition actions is forgotten while a unit is loaded; its actions are then written `null`  ·  `DONE`

**Symptom.** In the FML text rebuilt from the model (what the FML editor of the Modeller shows and saves), the actions of a technology that the unit `use`s but has no model slot role of are written without their name: `XLS::AddExcelSheet(sheetName="Goal Model")` becomes `XLS::null(sheetName="Goal Model")`, `B::CreateBPredicateFromString(…)` becomes `B::null()`, and `with BPredicateRole()` disappears. The log carries one `Cannot find FMLEntity for interface …` per action. The Modeller then fails to parse its own text (`ParserException token:null`). Measured in formod: `BMethodology` and `GoalModelingDiagram`, the only two units declaring three `use`.

**Reproduction (verified 2026-10-09 by execution).** `formod-module`, `FormodUnitsRoundTripTest`: after loading, `BMethodology` declares `BModelSlot` but the used model slots of its resource are `[AtelierBProjectModelSlot, FMLRTModelSlot]`, and `getFMLPrettyPrint()` contains `B::null()`.

**Mechanism — verified by tracing the two places that write the list.** The list of the resource is right after `setUsedModelSlots(String)` (3) and after the parser (3). During `finalizeDeserialization`, each model slot role calls `VirtualModel.declareUse(…)` (`ModelSlot.finalizeDeserialization`), the deprecated one: it builds the new list from `VirtualModel.getUseDeclarations()` — the declarations of the XML serialization, empty for a textual FML, whose `use` live in the compilation unit — plus the class of that role, and `CompilationUnitResource.updateFMLModelFactory` replaces both the list and the model factory with it. Only the model slots that have a role survive (`[FMLRT]`, then `[FMLRT, TypedDiagram]`); a technology used by its actions alone is never put back. The factory then does not know its entities, hence the `null`. It also added duplicate declarations to the VirtualModel.

**Fixed (2026-10-09).** `ModelSlot.finalizeDeserialization` asks the compilation unit (`uses` / `declareUse`, which build the list from the unit's own declarations) and only falls back on the VirtualModel's when there is none. Platform tests unchanged: `flexo-foundation-test` 521 (0 failure, 5 skipped), `fml-cli-test` 97, `integration-tests` 37; formod 32 + 15 + 56, and `Cannot find FMLEntity` is gone from their logs (it was in the thousands).

**Not fixed, noted.** `VirtualModel.uses/declareUse/getUseDeclarations` are still deprecated and still used by other callers (`CreateModelSlot`, the XML serialization): the same trap exists wherever they feed `updateFMLModelFactory`.
