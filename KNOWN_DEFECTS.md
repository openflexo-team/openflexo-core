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
