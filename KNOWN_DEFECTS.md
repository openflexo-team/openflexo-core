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
