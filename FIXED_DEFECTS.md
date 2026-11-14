# openflexo-core — Fixed defects

Defects of openflexo-core that are fixed: entries moved from [`KNOWN_DEFECTS.md`](./KNOWN_DEFECTS.md) once their status is `DONE`,
with what was verified, the cause and the fix. Identifiers are those of `KNOWN_DEFECTS.md`, never reused.

---

## FML matching engine

### CORE-D-2 — `end match … unmatched: method()` runs the first action scheme instead  ·  `DONE`

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

**Fix (2026-11-12, with CORE-D-57).**
1. `EndMatchActionNode` decomposes `unmatched: name(args)` into a behaviour of the matched concept and its arguments, resolved in
   `finalizeDeserialization` (a deletion scheme other than the default one is called this way); an unknown name, or a call that is not
   a behaviour of the concept (`a.b()`), raises a semantic issue and runs nothing.
2. `FinalizeMatching.getFlexoBehaviour()` no longer falls back on the first action scheme: without a behaviour nothing runs. This made
   the `unmatched:` clause optional in the grammar (`end match C in set;`, CORE-D-57).
Two tests passed only through the fallback, which happened to pick the behaviour written: `TestUpdateWithReferences` (its workaround
setting the behaviour on the parsed action is gone) and `TestMatchFlexoConceptInstances` (`unmatched: performDelete()`). Tests:
`TestCoreD57` (fml-parser).

### CORE-D-41 — Validation rejects `container.new X(...)` as the `unmatched:` clause of a `match`  ·  `DONE`

**Symptom.** In the behaviour of a concept, `match X in set from container where (...) unmatched: new X(...)` validates but
creates nothing at run time (a bare `new` in another concept's behaviour has no container), while the working spelling
`unmatched: container.new X(...)` was reported `'new instance' binding must instantiate a new instance`.

**Cause.** `MatchFlexoConceptInstance.NewInstanceBindingIsRequiredAndMustBeValid` required the instantiation at the ROOT of the
binding path; in `container.new X(...)` it is the last element.

**Fix (2026-11-08).** The rule checks the last element of the path. Proven by `TestFMLClassDiagramVirtualModel`
(fml-diagramming-extension): the class diagram creates one `FlexoPropertyGR` per property, none twice, with 0 validation error.

**Second case (2026-11-08).** The same rule failed with a `NullPointerException` ("Unexpected exception" in the validation
report) on `unmatched: new Foo()` when `Foo` declares no creation scheme: the path element has no scheme, and the rule read the
concept from it. It now reads the concept from the type of the path element. Proven by `TestFMLEditor23` (fml-technologyadapter-ui,
UI test, fixture `TestCreationScheme.fml`), red before.

---

## FML control structures and edition actions

### CORE-D-6 — A `do … while` loop executes its body once and ignores its condition  ·  `DONE`

**Fixed (2026-11-11).** A `do … while` is a `WhileAction` with `evaluateConditionAfterCycle`, printed back as `do { … } while (…);` (see `CORE-D-46`).

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

### CORE-D-7 — A compound assignment statement ignores its operator  ·  `DONE`

**Fixed (2026-11-11).** `AssignationAction` has an `assignmentOperator` (enum `AssignmentOperator`). As in Java, `x op= v` reads `x` first, applies the FML binary operator, and converts the result to the type of `x` (`n /= 2` on an int gives an int; `+=` on a String concatenates anything, `null` included); validation accepts what Java accepts. Printed back with its operator (see `CORE-D-46`).

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

### CORE-D-8 — An invalid condition is evaluated as true by `if` and `while`  ·  `DONE`

**Fixed (2026-11-13).** `ConditionalAction`, `WhileAction` and `ExpressionIterationAction` evaluate their condition strictly: a condition that is not set (a `for` without condition apart, which is true) or not valid, or whose evaluation raises an error other than a null reference, throws an `FMLExecutionException`; `null` and a null reference stay false. Test: `ExecutionFailures/InvalidCondition.fmlscript` run by `TestExecutionFailures` (fml-cli-test).

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

### CORE-D-9 — A `for (init; ; update)` loop without condition makes its behaviour disappear  ·  `DONE`

**Fixed (2026-11-11).** The node is built without a condition, a missing condition is true (as in Java), the update is optional; the validation rules require neither (see `CORE-D-46`).

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

### CORE-D-18 — An increment or decrement used as a statement makes its behaviour return null  ·  `DONE`

**Fixed (2026-11-11).** `++` and `--` statements are built as expression actions, as the update of a `for` already was (see `CORE-D-46`).

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

### CORE-D-42 — The FML technology adapter could not be used from textual FML  ·  `DONE`

**Symptom.** Found while migrating `FMLClassDiagram` (CORE-F-8), its only user: assigning a VirtualModel to an
`FMLModelSlot` failed at run time with `ClassCastException: VirtualModel cannot be cast to FMLCompilationUnit`, and
`FML::CreateFlexoConcept(conceptName=…, container=…)` reported `Unknown property conceptName`.

**Cause.** `FMLModelSlot.getType()` declared `VirtualModel` while the slot holds the compilation unit (`makeActorReference`,
`FMLModelSlotInstance`, `getTypeDescription()` all use `FMLCompilationUnit`); `CreateFlexoConcept` carried no `@FMLAttribute`.

**Fix (2026-11-08).** `getType()` returns `FMLCompilationUnit`; `conceptName` and `container` of `CreateFlexoConcept` are FML
attributes. **Not done**: the other actions of the FML technology adapter (`CreateTopLevelVirtualModel`,
`CreateContainedVirtualModel`, `CreatePrimitiveRole`, `CreateFlexoConceptInstanceRole`, `CreateFlexoBehaviour`) still have no FML
attribute — nothing uses them yet.

### CORE-D-43 — The FML editor did not follow most references edited in the text  ·  `DONE`

**Symptom.** In the FML editor, retargeting a reference in the text (the type of a role, `extends`, the concept of a `begin match` /
`match` / `end match`, the property of a `match` criterion, the event of a listener) left the model on the former target, or with no
target at all, until the model was reloaded.

**Verified (2026-11-09).** `TestUpdateWithReferences` (flexo-foundation-test) merges with `updateWith()`, as the editor does, a text
in which one more reference is retargeted at each step. On the baseline: the type of a role became null; `extends` and the whole match
family kept their former concept; a criterion and a listener's event pointed to objects of the parsed compilation unit (thrown away
after the merge). Only `select` followed (its type was already a translated `Type` property). The deprecated URI strings, merged too,
did not repair any of them.

**Cause.** A reference is merged by PAMELA through the property holding it: a property holding a model object without `@Updater`
brings the object of the parsed compilation unit (or merges two different objects into each other), and the match family, the parents
of a concept and the event of a listener carried their reference outside any PAMELA property, or without translation.

**Fix (2026-11-09, CORE-F-8 phase 4).** Concepts are carried by `FlexoConceptInstanceType` properties with a translating `@Updater`;
behaviours, parameters and properties by `FMLNamedReference` (compared by name, resolved by the holder). All eight steps pass. The
behaviour of `end match ... unmatched: someMethod(...)` is parsed since CORE-D-2 was fixed (2026-11-12): the test no longer sets it.

### CORE-D-45 — Validating or printing a VirtualModel marks its resource modified  ·  `DONE`

**Symptom.** A compilation unit nobody edited is reported modified, so the application would offer to save it. Measured on
2026-11-09 on the 22 VirtualModels of `integration-tests` (all loaded, then validated, then pretty-printed): none is modified after
loading, 9 are after validation, 11 after printing.

**What is verified (recorder of `setIsModified` on resource data, temporary, removed).**
- Validation: the 9 `DiagramSpecification.fml` of the old view points. Their expressions are parsed again
  (`DataBinding.setExpression` → `notifiedBindingChanged` → `setIsModified`, from `FMLControlGraph`).
- Printing: `FMLCompilationUnit` printing visits the concepts, and `FlexoConcept.handleRequiredImports` calls `ensureUse`, which adds a
  use declaration (BPMNModel, FMLClassDiagram).
- Printing a `DiagramSpecification` loads its example diagrams and palettes from XML, and the diagram-ta objects mark themselves
  modified while being deserialized (`DiagramImpl.setIsModified` / `DiagramPaletteImpl.setIsModified` from `XMLSaxDeserializer`).

**To reproduce.** Load every `CompilationUnitResource` of a resource center, check `isModified()`, validate the VirtualModels, check
again, call `getFMLPrettyPrint()`, check again.

**Expected.** Neither validation nor printing changes the model, so neither marks it modified; a binding parsed again to the same
expression, and a use declaration that printing needs, are not edits. Found while measuring phase 6 of CORE-F-8.

**Done (2026-11-11), decisions taken with the user.** Reproduced first by `TestNoSpuriousModification` (integration-tests): every
resource of every resource center is loaded, then validated, then printed, and none may be loaded and modified. It failed as measured
(9 after validation, 11 after printing) and, once widened beyond the compilation units, showed 22 diagram specifications, diagrams and
palettes modified after loading. Each cause was fixed at its source, one at a time, with no global guard:
1. Validation (9 `DiagramSpecification.fml`): a binding whose parse had been postponed at load (no binding factory yet) is parsed by its
   first `isValid()`. That is not an edit: `DataBinding.isValid()` sets the expression without notifying the owner. The parser's
   throw-away binding that carries an expression back (`FMLExpressionParser`) no longer notifies the owner either
   (`ExpressionFactory.makeDetachedExpression`, `DataBindingNode.isDetached()`): that one marked the owner even when the binding was
   already parsed.
2. Printing, `use` declarations: `manageImports()` now also runs when a unit is finalized after loading (decision: declared at load, not
   skipped at print), so the unit is complete in memory and the declaration is not marked as an edit. Printing still calls it, for a unit
   edited in memory. Found on the way, in the same family:
   - the pretty-print delegate of an object created in memory (a `use` or Java import added by `manageImports()`) is linked when
     printing: `FMLObjectNode` links it through the new `FlexoObject.runWithoutMarkingModified()` (a PAMELA setter of a persistent
     property marks the object after the implementation returns, so only the caller can suppress it);
   - `FMLObject.updateFMLPropertyValues()` builds the property values when printing; `FMLPropertyValue.setIsModified()` no longer
     propagates (a property value mirrors a property of its object), and `FMLObject.setChanged()` honours the ignore flag;
   - printing a property valued by a resource declares an element import (`FMLSimplePropertyValueNode`), which also loads the
     resource: the declaration is not marked as an edit. Declaring it at load time was tried and abandoned: it loads other units while
     a unit is loading, and an unit whose data is not yet available is finalized (`NullPointerException`, the CORE-D-14 situation).
3. Diagram specification (openflexo-diagram): linking the example diagrams and palettes to their specification, when they are first
   asked for (`loadExampleDiagramsWhenUnloaded`, `loadDiagramPalettesWhenUnloaded`, `DiagramPaletteResourceImpl.performLoad`), marked the
   diagram, the palette and the specification: now done through `runWithoutMarkingModified()`. The marks raised while
   `XMLSaxDeserializer` builds a diagram are cleared by the resource at the end of its loading, as for any resource: they were not a
   cause.

Declaring the `use` at load made `formod-module` (`FormodUnitsRoundTripTest`) fail: the resource did not list the technologies so
added (`FMLRTModelSlot`). The resource now adds them to its used model slots right after `manageImports()` (the model factory is not
rebuilt in the middle of loading).

**Verified.** The 28 usual suites give the baseline (flexo-foundation-test 552 tests, 547 successes, 5 skipped; integration-tests 41,
`TestNoSpuriousModification` included; formod-module 17/17), and the five touched modules compile with ECJ.

**Left.** `runWithoutMarkingModified()` is a local, explicit exception for derived bookkeeping, used at five places; a new derived link
built on demand must use it too.

### CORE-D-46 — A statement the control-graph builder does not handle is dropped, or flattened, without a message  ·  `DONE`

**Fixed (2026-11-11).** Every statement alternative of the grammar now has a node in `ControlGraphFactory`: the three `*_no_short_if` forms (`ConditionalNode`, `WhileActionNode`, `ExpressionIterationActionNode` accept them), the empty statement (an `EmptyControlGraph`), `do … while`, `++`/`--` statements. A statement that still produces no node, or a block that cannot be completed, is reported as a semantic issue instead of being dropped. Tests: `AutomatedTests/TestStatementLowering.fmlscript` (fixture `StatementLowering`, which replaces `KDControlFlow`) and `TestStatementLowering` in fml-parser (no issue, the normalized and the parsed text parse again, an unbuildable `for` is reported).

**Symptom.** An accepted behaviour becomes another program, with no parse error and no validation error. This is the common root
of `CORE-D-6`, `CORE-D-7`, `CORE-D-9` and `CORE-D-18` (its symptoms, fixed with it), and of the following ones:
- an unbraced `if/else` or `while` nested as the *then* arm of an `if/else` is flattened: only a statement inside it is kept;
- an empty statement `;` makes the enclosing block abort, and the behaviour is rebuilt as a single expression.

**Reproduction (verified 2026-11-11 by execution, test `KnownDefects/CORE-D-46.fmlscript`, fixture `KDControlFlow`).**
`nestedIf()` returns 1 instead of 2; the braced `nestedIfBraced()` returns 2. The fixture also holds `whileArm()` (expected 3) and
`emptyStatement()` (expected 7), not reached by the script once the first assert fails.

```fml
public int nestedIf() {
	if (true)
		if (false) return 1; else return 2;
	else return 3;
	return 4;
}
```

**Mechanism — verified in the code.** `ConditionalNode` builds the *then* arm of an `if/else` from a `statement_no_short_if`
(`ControlGraphFactory.makeControlGraphNode(PStatementNoShortIf…)`). `ControlGraphFactory` has no handler for the alternatives
`if_then_else_statement_no_short_if`, `while_statement_no_short_if` and `for_statement_no_short_if`, nor for `++`/`--` statements;
`do_statement` and `empty_statement` have empty handlers. The depth-first walk then keeps the first node a descendant pushes.
`BlockSequenceInfo.finalizeBlockStatements` gives up a block when it has fewer nodes than statements (only a `logger.warning`
"Expecting to find N statements … Aborting"), and when nothing was pushed `makeControlGraphNode` falls back to an
`ExpressionActionNode` over the whole fragment. Found by the external audit of the FML language of 2026-10-23 (its §3.1).

**To fix.** A handler for every statement alternative of the grammar (an empty statement as an explicit no-op), and a semantic
error instead of the fallback whenever a statement produces no node.

**Workaround.** Brace every nested statement; write no empty statement.

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

---

## Resources: unloading and deletion

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

### CORE-D-38 — A repository built for a project, not for its delegate, loses the URI of its resources when the project is closed  ·  `DONE`

**Symptom.** Intermittently, a project reopened in the same service manager no longer finds one of its resources by its URI, or finds
the resource of the closed project: `TestUnloadDoesNotDelete` (`NullPointerException` on `probeResource()`) and `TestReloadedResourceReferences` (a role answering
`null`, `RESOURCE_NOT_FOUND`) failed in 8 builds of `openflexo-core-2.99` out of 12 on 2026-10-13 (#151-#162), never locally. The log shows, when the project is closed, a virtual model instance resource unregistered under a
`file:` URI (`hop on desenregistre …file:/tmp/….prj/Probe.fml.rt/Probe.fml.rt.xml`).

**Mechanism — verified by execution** (diagnostics captured on Jenkins, build #162, then reproduced locally).
1. A resource of a project stores no URI: it computes one from the resource center of the repository it is registered in.
2. That repository is built by whoever asks for it first (CORE-D-25 keeps the first one registered). The test thread asks the project,
   which goes to its delegate resource center: the repository belongs to the delegate. But
   `FMLRTTechnologyAdapter.notifyRepositoryStructureChanged()`, posted to the Swing event thread, also calls
   `getVirtualModelInstanceRepositories()`, which asks the technology adapter for the repository of every resource center — the
   `FlexoProject` among them. When the project joins the list between the two loops of that method, and before the test thread asked,
   the repository is built for the project itself: its resources get the project as resource center.
3. Closing the project deletes its data, so the project has no URI any more: the resources fall back to their `file:` URI. The
   reopened project keeps the same delegate resource center, with the stale resources still indexed in it, attached to the closed
   project — and does not find them by their URI.

The race depends on thread scheduling only, which is why the same revision went green or red from one build to the next (green when
`openflexo-core-3.0` happened to build at the same time, red otherwise, on the 14 builds looked at, #149-#162 — presumably through the
load it puts on the agent, not measured).

**Fixed (2026-10-13).** `FlexoProjectImpl.registerRepository()` gives a repository built for the project to its delegate resource center
before registering it, whichever technology adapter built it: a repository of a project always belongs to the delegate, as it already
did when the test thread won.

**Tests.** `flexo-foundation-test`, `rm/TestProjectRepositoryResourceCenter`: holds the event thread back while the project is
created and asks the technology adapter for the repository of the project, then checks that the repository and the resources belong
to the delegate, and that the reopened project finds the resource by its URI. Red without the fix (3/3, the resource answering its
`file:` URI), green with it. Non-regression: `flexo-foundation-test` 528 (523 successes, 5 skipped), `fml-cli-test` 99/99, all other
modules of openflexo-core green.

---

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

### CORE-D-31 — A technology used only through edition actions is forgotten while a unit is loaded; its actions are then written `null`  ·  `DONE`

**Symptom.** In the FML text rebuilt from the model (what the FML editor of the Modeller shows and saves), the actions of a technology that the unit `use`s but has no model slot role of are written without their name: `XLS::AddExcelSheet(sheetName="Goal Model")` becomes `XLS::null(sheetName="Goal Model")`, `B::CreateBPredicateFromString(…)` becomes `B::null()`, and `with BPredicateRole()` disappears. The log carries one `Cannot find FMLEntity for interface …` per action. The Modeller then fails to parse its own text (`ParserException token:null`). Measured in formod: `BMethodology` and `GoalModelingDiagram`, the only two units declaring three `use`.

**Reproduction (verified 2026-10-09 by execution).** `formod-module`, `FormodUnitsRoundTripTest`: after loading, `BMethodology` declares `BModelSlot` but the used model slots of its resource are `[AtelierBProjectModelSlot, FMLRTModelSlot]`, and `getFMLPrettyPrint()` contains `B::null()`.

**Mechanism — verified by tracing the two places that write the list.** The list of the resource is right after `setUsedModelSlots(String)` (3) and after the parser (3). During `finalizeDeserialization`, each model slot role calls `VirtualModel.declareUse(…)` (`ModelSlot.finalizeDeserialization`), the deprecated one: it builds the new list from `VirtualModel.getUseDeclarations()` — the declarations of the XML serialization, empty for a textual FML, whose `use` live in the compilation unit — plus the class of that role, and `CompilationUnitResource.updateFMLModelFactory` replaces both the list and the model factory with it. Only the model slots that have a role survive (`[FMLRT]`, then `[FMLRT, TypedDiagram]`); a technology used by its actions alone is never put back. The factory then does not know its entities, hence the `null`. It also added duplicate declarations to the VirtualModel.

**Fixed (2026-10-09).** `ModelSlot.finalizeDeserialization` asks the compilation unit (`uses` / `declareUse`, which build the list from the unit's own declarations) and only falls back on the VirtualModel's when there is none. Platform tests unchanged: `flexo-foundation-test` 521 (0 failure, 5 skipped), `fml-cli-test` 97, `integration-tests` 37; formod 32 + 15 + 56, and `Cannot find FMLEntity` is gone from their logs (it was in the thousands).

**Not fixed, noted.** `VirtualModel.uses/declareUse/getUseDeclarations` are still deprecated and still used by other callers (`CreateModelSlot`, the XML serialization): the same trap exists wherever they feed `updateFMLModelFactory`.

### CORE-D-32 — "DeclarationAction with EditionAction not implemented yet" is logged for declarations that are built and executed  ·  `DONE`

**Symptom.** One warning per declaration of the form `Type v = <edition action>` (`ExcelRow row = XLS::AddExcelRow(...)`, `Goal g = select unique Goal from ... where ...`, `MatchingSet m = begin match ...`): 81 at the start of formod-app, about 700 per test run. A `System.out.println("On obtient ...")` of debugging was left next to it.

**Verified by tests (2026-10-09) that the case is implemented, not only that it logs.** The declaration is built from the grammar production `initializer_fml_action` (not from the expression one) and `DeclarationActionNode` sets its assignable action and its child node *before* logging:
- `TestDeclarationWithEditionAction` (`flexo-foundation-test`, fixture `TestResourceCenter/FML/TestDeclarationWithAction.fml`): `select unique` and `select ... where` declarations hold their action (type, condition), the node of the declaration holds the node of the action, and the text printed from the model gives the declaration back. `TestMatchingVM` already asserted the `begin match` form (`InitiateMatching`).
- `TestDeclarationWithAction.fmlscript` (`AutomatedTests`, 98 tests): the declared variable is used by the next statement and gives the right values (`nameOfTheOneWithValue`, `countWithValue`).
- `FormodDeclarationsTest` (`formod-module`): every declaration of the 16 units of the Formose resource center holds its action, and the forms met include `AddExcelRow`, `CreateBExpressionFromString`, `select`, `select unique`, `begin match`.

**Fixed.** Warning and `println` removed from `DeclarationActionNode`. Platform tests: `flexo-foundation-test` 525 (0 failure, 5 skipped), `fml-cli-test` 98; formod 32 + 17 + 56; the message is gone from every log.

### CORE-D-39 — A compilation unit found half-copied by the directory watcher is never completed  ·  `DONE`

**Symptom.** Intermittently, a unit copied into a watched resource center is found by its URI but loads incomplete — a concept of a
contained VirtualModel is missing — or is not found at all. Seen on Jenkins: `TestBehavioursInheritance.testReloadViewPoint`
(`FlexoConceptA` missing, build #163) and, most probably, `TestLocalizedDictionaryResource.test6DictionaryIsRediscoveredOnReload`
(builds #149, #153, #154). The log shows, in the watcher thread, `FileNotFoundException: …/Xxx.fml/Xxx.fml (No such file or
directory)` and `Cannot retrieve info from …/Xxx.fml`, then `File MODIFIED Xxx.fml … resource=null`.

**Mechanism — verified by execution** (diagnostics of build #163, then reproduced locally).
1. A new `DirectoryResourceCenter` schedules its `DirectoryWatcher` with no initial delay: it runs at once, in its own thread
   (`pool-N-thread-1`), alongside the caller.
2. The tests made the directory `Xxx.fml` in the resource center, then copied its files into it. When the watcher passes in between,
   `CompilationUnitResourceFactory.isValidArtefact()` accepts the directory although `Xxx.fml/Xxx.fml` is not there yet: the resource is
   built without its info, and its contained units are not explored.
3. The next watching sees the directory modified, but `FileSystemBasedResourceCenterImpl.fileModified()` finds no resource registered
   for it and does nothing: the incomplete resource is never completed.

Reproduction: a copy of `TestBehavioursInheritance` running `performDirectoryWatchingNow()` between `mkdirs()` and the copy fails every
time on `FlexoConceptA`, with the same two messages.

**Not a test-only defect.** Copying a unit into a watched resource center while Openflexo runs can end the same way.

**Worked around in the tests (2026-10-13).** `OpenflexoTestCase.copyIntoResourceCenter()` copies the directory aside, next to the
resource center, and moves it in at once (`ATOMIC_MOVE`) before asking for a watching: the watcher can no longer find it half-copied.
Used by `reloadResourceCenter()` and by the seven tests that copied a unit into a resource center (`TestBehavioursInheritance`,
`TestCreateVirtualModel`, `TestFlexoConceptInheritance`, `TestFlexoConceptInstanceType`, `TestFMLBindingModelManagement3`,
`TestLocalizedDictionaryResource`, `TestURIManagement`). `flexo-foundation-test` 528 (523 successes, 5 skipped).

**Fixed in the platform (2026-10-14).**
1. A directory artefact needs its main file: `isValidArtefact()` of `CompilationUnitResourceFactory` (`Xxx.fml/Xxx.fml`, or
   `Xxx.fml.xml` in the deprecated XML serialization), `FMLRTVirtualModelInstanceResourceFactory` (`Xxx.fml.rt/Xxx.fml.rt.xml`) and
   `FlexoProjectResourceFactory` (`Xxx.prj/ProjectData.xml`, the check that had been left commented out). New
   `IFlexoResourceFactory.isIncompleteArtefact()`: the form of an artefact, its main file missing.
2. `TechnologyAdapter.contentsAdded()` takes an incomplete artefact neither for a resource nor for a folder, and tells the resource center
   (`FlexoResourceCenter.incompleteArtefactFound()`).
3. `FileSystemBasedResourceCenterImpl` keeps those artefacts in a set of its own and looks them up again when they change, or when a
   file is added in them (the date of a directory does not always change). Not in `addedFilesToBeRenotified`: the watcher empties that
   list after each watching, whatever the outcome.
4. New `TechnologyAdapter.contentsChangedInResourceCenter()`: `FMLTechnologyAdapter` makes the nearest registered unit enclosing a change
   explore its directory again (`CompilationUnitResourceFactory.exploreContainedResources()`), skipping what is already registered — but
   only when the contained unit has a main file: resources without a serialization artefact (units in the XML serialization, in a jar)
   are all registered under `null`.

**Tests.** `flexo-foundation-test`, `rm/TestIncompleteArtefactDiscovery`: a unit seen before its main file, a contained unit copied after
its container, and the rule for `.fml.rt` and `.prj`. Red without the fix (3 of 4), green with it; the reproduction of
`TestBehavioursInheritance` likewise. Non-regression: every non-UI test of openflexo-core green (`flexo-foundation-test` 532, 527
successes, 5 skipped); through `openflexo-dev`, integration-tests 37/37, b-ta-test 144/144, bpmn/uml/statecharts modelers 10/6/5,
xml-ta-test 30 (1 skipped), xlsx-ta-test 34 (4 skipped), xx-ta-test 6/6. `formod-test` 31/32, the same failure without the fix (a
`StackOverflowError` in `BSerializationIdentifiers.pathTo`, b-ta).

**Not covered.** A main file found while it is itself being written is still read as it is. The watcher keeps unsynchronized,
non-`volatile` state (`isRunning`, `waitNextWatching*`; see CORE-D-36).

### CORE-D-40 — The directory watcher and the initialization of a technology explore a resource center at the same time, registering a unit twice  ·  `DONE`

**Symptom.** `modelers-2.99` #260 to #264 (2026-10-14) UNSTABLE, #247 SUCCESS on the very same revision of `modelers`: in `bpmn-modeler`,
`TestBPMNModeler.test1BPMNEditor` fails on `assertVirtualModelIsValid` with two errors, `Types are not compatible in assignation :
FlowNodeGR is not assignable from FlowNodeGR` (the two `fromNodeGR` / `toNodeGR` assignations of `ProcessDiagram.fml`). Locally the same
test is green (3 runs of 10 tests, and the published snapshots).

**Mechanism — read in the output of the failing test, not reproduced.**
1. `DirectoryResourceCenter` starts its watcher with no initial delay (see CORE-D-39): its first watching runs in `pool-1-thread-1` while
   the `Test worker` thread activates the technologies and explores the same resource center.
2. The watcher finds directories modified since its snapshot (`File MODIFIED Models`, `File MODIFIED BPMNEditor.fml`,
   `File MODIFIED ProcessDiagram.fml … resource=null`) and explores them through `TechnologyAdapter.contentsAdded()` /
   `FMLTechnologyAdapter.contentsChangedInResourceCenter()` (the latter added by CORE-D-39).
3. Both threads do the same unsynchronized look-up then register: `DuplicateURIException: a resource with that URI … already declared`
   for the diagram specification, its palette, `ExampleDiagram.diagram` and `ProcessDiagram.fml`, the warnings alternating between the
   two thread names. `ProcessDiagram` ends with two `CompilationUnitResource` (`@521e7c50`, `@6213104f`), hence two concepts named
   `FlowNodeGR`, one not assignable from the other.

Timing dependent: it follows the speed of the agent, as CORE-D-36 and CORE-D-38 do. Jenkins keeps ~1 KB of the output of a passing suite,
so #247 cannot be compared.

**Fix (2026-10-14, commit `0c64bec8b`).** The explorations of a resource center are serialized on the resource center:
`TechnologyAdapter.initializeResourceCenter()`, `TechnologyAdapter.contentsAdded()` and `FMLTechnologyAdapter.contentsChangedInResourceCenter()`
are `synchronized (resourceCenter)`.

**Measured.** `flexo-foundation-test`: `TestIncompleteArtefactDiscovery`, `TestLocalizedDictionaryResource`, `TestCreateVirtualModel`,
`TestResourceLoadedStateIsNotified`, `TestURIManagement` green (23). `bpmn-modeler` through the composite, 3 runs: 10/10. Neither shows
anything about the race, which the local runs never triggered.

**Validated on Jenkins (2026-10-14).** `openflexo-core-2.99` #181 (the fix) green, then `modelers-2.99` #265 and two later builds green
(bpmn 10/10, owl 3/3, statecharts 5/5, uml 6/6), where #260-#264 were red on the same revision of `modelers`.

**Not checked.** Another lock held by a thread that waits for the resource center (the explorations call back into repositories,
`repositoriesLock`): no deadlock seen, none ruled out by reading.

---

## Noise and fidelity found by mapping the warnings of loading a Formose project (2026-10-09)

### CORE-D-33 — The text rebuilt from the model is not the text that was parsed  ·  `DONE`

**Symptom.** `FMLCompilationUnit.getFMLPrettyPrint()` — what the FML editor of the Modeller shows and saves — differs from the source file on the 10 Formose units compared (2026-10-09). With the whitespace, the comments and the `java.lang.String` imports left out, the differences measured are:
- **Imports added**: `import java.lang.String;` (and `ExcelCell`, `FlexoEnumInstance` once) appears where the source has none. **Intended behaviour, not a defect** (confirmed 2026-11-12): the parser adds the imports that the types of a unit need. The p2pp tests do not compare Java imports.
- **Arguments reformatted**: the space after a comma is dropped (`createRequirement(a,b)`) and wrapped lines are joined into one very long line.
- **Parentheses**: `in (container.diagram)` is written `in container.diagram` (123 times in `DomainModelDiagram`), and an extra pair appears around two string concatenations.
- **`with ConceptInstance()` added** to a role declared without it (`Requirement requirement;` in an event of `FormoseCore`; 4 times in `SysMLKaosModel`). Whether this is neutral (it is the default role of a concept-typed property) was NOT verified.
- `Cannot find P2PPNode for object FMLTypePropertyValue[…/targetType=GoalModelingDiagram,required=false]` logged twice (`ChildrenContents.handlePreludeAndPoslude` and `initializePrettyPrint`) for `with DropScheme(targetType=…)`. The printed text of the unit keeps the attribute: only the log was measured.

**Not a defect, measured:** `@Renderer("x")` is written `@Renderer(x)`. The source was wrong, the printer is right (FORMOD-D-7).

**Impact.** Saving a unit from the Modeller rewrites it entirely (large diffs, noise in review). No loss of meaning was found once CORE-D-31 was fixed (every `B::` and `XLS::` action of `BMethodology` and `GoalModelingDiagram` is printed, 40/40 and 10/10), but the neutrality of `with ConceptInstance()` is not proven (the added imports are intended).

**To decide.** Whether the printer must give back the parsed text for what the user did not edit, or only normalise; a round-trip test (print, parse again, compare the models) over the units of the test resource center would settle the neutrality.

**Measured again by the p2pp harness (2026-11-12).** `TestFMLPrettyPrintInvariants` (`flexo-foundation-test`) checks that the preserving
pretty-print of an UNTOUCHED source is that source, on the 103 FML files of `FMLParsingExamples` and of the `TestFMLPrettyPrintN`
directories: **28 differ**, Java imports not compared (the added imports are intended). Besides the families above, the expressions are written again from the model: `1+2` becomes `1 + 2`, `i>0` becomes
`i > 0`, `a+b*c-9` becomes `a + b * c - 9`, `ns+"x"` becomes `ns + "x"`, parentheses go (`assert (i != -1)`, `from (this)`), and a qualified type
is shortened (`java.lang.String aString` becomes `String aString`). The files and their family are in
`flexo-foundation-test/src/test/resources/fml-invariants-known-defects.txt`. Four more differences are not cosmetic, see CORE-D-63 to CORE-D-66.

**Expressions and types fixed (2026-11-13).** The preserving pretty-print no longer writes an expression again from the model when it did
not change: it is printed through the p2pp nodes of the expression (one per operator, operand and path element, `fml-parser/.../fmlnodes/expr`),
whose text is the parsed fragment where the operands are replaced by their own print, and whose leaves (constants, names, the head of a call,
types) keep their parsed text while their value is the one captured once the unit was built (P2PP-F-2). An FML node prints a binding with
`binding.getPrettyPrint()` (CONNIE-F-3: the `DataBindingNode` of a binding is its pretty-print delegate) instead of `binding.toString()`,
which stays normalized, as the normalized pretty-print. A binding typed in an inspector keeps its syntax too: its text is parsed with p2pp
nodes over it. Kept as written: spacing (`1+2`, `i>0`), parentheses (`assert (i != -1)`,
`from (this)`), qualified types (`java.lang.String`, in expressions and in declarations), the separators of argument and `where` lists,
annotation values (`@URI(local+"x")`). An edited expression is written from the model, as before: a binding whose expression object was
replaced (its text edited), an operator or operand replaced in place, a path whose shape changed (another number of elements or of
arguments). Measured with `TestFMLPrettyPrintInvariants` (strict run): 24 files differed, 11 still did, none on an expression; with the
roles below, CORE-D-63, D-64, D-66 and D-68, none does (2026-11-13).

**Roles fixed (2026-11-13).** `with ConceptInstance()` was added to a concept-typed role declared without it (`ConceptB aConceptB;`, 5
files), and `Concept _concept with FlexoConceptInstanceRole;` was written `... with ConceptInstance();` (`MatchingVM`). `FlexoRolePropertyNode`
now writes the `with` clause of a declaration written in the Java style only once its role is no longer the default one (another kind of
role, or a property value), writes the parentheses of a clause written without them only when a property has a value, and keeps the name
of the role as written while it does not change. A role created by a model change is written as before. `TestCoreD60` checks the insertion
of the clause (and its spacing, CORE-D-60) once a property is given a value. The remaining rows of the invariants test are CORE-D-63,
CORE-D-64, CORE-D-66 and CORE-D-68.

**Regression found by review and fixed (2026-11-13).** The `with (...)` clause of an instantiation (`new X(...) with (name=p)`) was
kept verbatim by the preserving pretty-print, also after a property it names was renamed: the saved text kept the former name. The clause
is now a leaf of its path (`BindingPathNode`), kept while its values (written as `CreationSchemePathElement` writes them) do not change.
Test: `TestRenameInWithClause` (flexo-foundation-test).

**Masked, not fixed.** Since a declaration keeps the type written, two model losses no longer show in the preserving pretty-print of an
untouched source, but are still in the model (and in the normalized pretty-print): CORE-D-69 and CORE-D-70.

**Capture revised after review (2026-11-13), decided with the user.** The reference values are captured again when referenced units
complete (`CompilationUnitResourceImpl`), only if the unit was not modified since it was parsed. Not covered by a dedicated test.

### CORE-D-37 — A reference serializes the identifier the object had when it was assigned, not the one it has  ·  `DONE`

**Symptom.** A FML role pointing at a technology object is empty after a reload, its reference serialized as `<resource>#FLX_null`, although the object does give a serialization identifier at the time the project is saved.

**Cause (measured 2026-10-13 in formod, FORMOD-D-15).** `FlexoObjectReference.setObject` captures `resourceIdentifier`, `userIdentifier` and `objectIdentifier` when the reference is made, and `constructSerializationRepresentation` - what `FlexoObjectReferenceConverter` writes - rewrote those fields as they were. An identifier is not frozen, though: `notifySerializationIdHasChanged` exists for that, and a technology adapter may compute one from the place the object holds in its resource. A B predicate is identified by its containment path in its component, which it only takes once the model inserts it in a clause - after the role was assigned - so the reference kept the null it was given.

**Fixed (2026-10-13).** `constructSerializationRepresentation` now takes the identifiers from the live object again (`resource.getObjectIdentifier(modelObject)`) before writing, and only when the object still gives one: this refreshes an identifier, it never drops one.

**Blast radius, read in the code.** The only implementations of `getObjectIdentifier` in the workspace are `PamelaResourceImpl` (the `FlexoID`), `EMFModelResourceImpl` / `EMFMetaModelResourceImpl` (`EcoreUtil.getID`) and `BResourceImpl` (the path of the B object). The first three are stable, so the refresh is a no-op for them; the B one is what needed it.

**Tests.** No test in `openflexo-core`: the defect needs a resource whose identifiers depend on the place of the object, which only the B technology adapter has. It is guarded by `TestBMethology.testReloadProject` in `formod`, red before this change. Non-regression here: `flexo-foundation-test` 525 (520 successes, 5 skipped), `fml-cli-test` 99/99.

### CORE-D-57 — The normalized pretty-print of some units loses expressions and does not parse again  ·  `DONE`

**Symptom.** `getNormalizedFML()` (the text built from the model alone, used for objects created from scratch) dropped parts of
statements on 5 of the 32 `FMLParsingExamples` units, and the text it gave could not be parsed again (found by
`TestFMLReindent.aNormalizedPrettyPrintIsAFixedPoint`, P2PP-F-1).

**What it was (verified 2026-11-12 by execution, each construct rebuilt in a unit where every reference resolves).** Three defects of
the printer and the parser, and a family that is CORE-D-58:
1. `end match C in set unmatched: delete();` was printed without its clause, which the grammar required: the print specification of
   `EndMatchActionNode` had no contents for it (its former version, commented out, aimed at a syntax that no longer exists).
2. A statement in a `for` loop whose collection could not be built lost its `;`: `AssignableActionNode.requiresSemi()` took the child
   of index 0 of the loop for its collection.
3. `use ...ModelSlot as X` threw a `NullPointerException` while parsing (`FMLCompilationUnit.lookupClass`: the type argument of
   `ModelSlot` itself is a type variable, its raw type is null) — TestNamespaces.
4. Everything else (the collection of `for (X x : select X from (m))`, a role, `new MyVM::constructor1(...)`, `extends`,
   `begin match null`, `where (?=x)`) is lost because the reference does not resolve (an import of a unit that does not exist): the
   object is not built in the model. Rebuilt with references that resolve, each of them prints and parses again. Moved to CORE-D-58.

**Fix (2026-11-12).**
1. The `unmatched:` clause of `end match` is optional in the grammar (decided with the user): without it nothing runs on the
   unmatched instances. `EndMatchActionNode` prints `unmatched: delete(args)` for the default deletion scheme, `unmatched: name(args)`
   for another behaviour, the parsed text when no behaviour was resolved from it, and nothing without a behaviour. Making the clause
   optional required removing the fallback of `FinalizeMatching.getFlexoBehaviour()` on the first action scheme, hence CORE-D-2.
2. The collection of a loop is recognized by identity (`getIterationAction()`), as the update of a `for` already was (CORE-D-9).
3. `lookupClass` returns null for a type variable.

Tests: `TestCoreD57` (fml-parser). The 4 units still skipped by `TestFMLReindent` are CORE-D-58.

### CORE-D-58 — What refers to a type that does not resolve is dropped from the model  ·  `DONE`

**Symptom.** When a concept referred to does not resolve (an import of a unit that does not exist), the parser raised a semantic issue
and then dropped the object referring to it, so that the model lost it while the text printed from the source kept it. Seen in
`FMLParsingExamples` (verified 2026-11-12 by execution): the whole body of a behaviour holding `select AnOtherModel from (…)`
(TestSelect, printed `testSelectExternalVirtualModel();`), the collection of `for (Concept c : select Concept from (_model))`,
`new MyVM::constructor1(1,"foo") with (…)` printed empty (`myModel =;`, with NO issue), `begin match null`, `match null`,
`where (?=conceptA)`.

**Mechanism — verified in the code.** A type name that does not resolve becomes a connie `UnresolvedType`. Each node expecting a
concept tested `type instanceof FlexoConceptInstanceType` and gave up otherwise: `FetchRequestNode` (no select built, hence no
statement), `BeginMatchActionNode`, `MatchActionNode`, `EndMatchActionNode` (no matched type), `BindingPathFactory` for
`new X::ctor(...)` (no path element, an empty expression). `MatchActionNode` printed `?` for the property of a criterion that the
unresolved concept could not resolve, although the model keeps its name.

**Fix (2026-11-12, decided with the user: keep the object, typed by an unresolved type).** `TypeFactory.asFlexoConceptInstanceType`
turns an `UnresolvedType` into an unresolved `FlexoConceptInstanceType` keeping the name written, as
`makeVirtualModelInstanceType` already did for virtual models; the nodes above use it, still raise their issue, and build the object.
A criterion prints the name written when its property is not resolved. All 32 `FMLParsingExamples` print a normalized text that parses
again (`TestFMLReindent` no longer skips any). Tests: `TestCoreD58` (fml-parser).

**Left out.**
- `extends` of a concept that does not resolve: fixed separately (CORE-D-62).
- `Object o = …` is printed with the type of the right-hand side, even when everything resolves: not this defect (CORE-D-61).
- `MyVM _model with FMLRTVirtualModelInstanceModelSlot;` (MatchingVM) is dropped with `role_not_found`, whatever the type: an
  obsolete role syntax in the fixture (the current one is `with ModelInstance()`), not a defect.

### CORE-D-60 — A role declared without `with` is printed `nameXwith ConceptInstance()`, which does not parse  ·  `DONE`

**Symptom.** `public ConceptB myConceptB;` was printed back by `getFMLPrettyPrint()` (what the FML editor saves) as
`public ConceptB myConceptBwith ConceptInstance();`: the `with` clause added to the role (CORE-D-33) lost the space before it, and
the text no longer parsed (`ParseException: expecting: ';'`). Any unit holding a concept-typed role declared without `with` was saved
unreadable. `flexo-foundation-test` showed it: `TestBehaviourAvailability` (3 tests), `TestDerivedInspector.test4RoundTripsThroughTheFMLSource`,
`TestRendererUpdateFromText` (2 tests).

**Mechanism — verified in the code and by execution (2026-11-12).** A regression of p2pp, commit `af3231a` (fix of P2PP-D-6): when the
canonical postlude of a contents is whitespace that the source does not have, `PrettyPrintableContents.getPostludeFragment()` now
returns an empty fragment (the source "chose" no separator, a layout to keep) instead of null. `FlexoRolePropertyNode` writes the name
with postlude `" "` and then `with` with an empty prelude: `with` is inserted at the end of the name, postlude included, and the name
no longer writes its space since its postlude counts as present.

**Fix (2026-11-12, p2pp).** A contents inserted right after a parsed one whose whitespace postlude the source wrote empty starts with
that postlude (`PrettyPrintableContents.separatorMissingBeforeInsertion`, used by `StaticContents` and `DynamicContents`): only the
first contents inserted, and not when the inserted text starts with whitespace. As long as nothing is inserted, the layout of the
source is kept (P2PP-D-6 holds). Tests: `TestCoreD60` (fml-parser); the 6 tests above pass; p2pp-test, fml-cli-test and formod
(b-ta-test, formod-test) unchanged.

### CORE-D-61 — The declared type `Object` of a local variable is printed as the type of its value  ·  `DONE`

**Symptom.** `Object o = 1;` was printed `long o = 1;` and `Object o = select Item from (this);` was printed `List<Item> o = …;`, even
when everything resolves: the declared type `Object` (or `java.lang.Object`) of a local variable with an initializer was lost.
`Object o;` (no initializer), `String`, `Number` or `Integer` were kept.

**Mechanism — verified in the code and by execution (2026-11-12).** A trap of PAMELA: a setter changes nothing when the new value
equals the one the getter returns (`DefaultSinglePropertyImplementation.set` compares with `invokeGetter`, which calls the overridden
getter). `DeclarationAction.getDeclaredType()` falls back on the analyzed type of the right-hand side when no type is stored, and
`DeclarationActionNode` sets the declared type before the right-hand side: the analyzed type is then `Object`, so that
`setDeclaredType(Object.class)` was ignored; once the right-hand side was set, the getter returned its type (`long`).

**Fix (2026-11-12).** `DeclarationActionImpl.setDeclaredType` makes the getter return the stored value while it sets it, so that the
comparison is made against the stored value. What `getDeclaredType()` returns otherwise is unchanged. Tests: `TestCoreD61`
(fml-parser).

**Same pattern, not affected today.** `FlexoBehaviour.getDeclaredType()` falls back on the analyzed return type the same way; a return
type `Object` is kept (the analyzed type is not `Object` when it is set: tested), but a declared type equal to the analyzed one at
that moment would not be stored. `FlexoRole.getCardinality()` / `getCloningStrategy()`, `LogAction.getLogLevel()` and
`AbstractCreateResource.getRelativePath()` fall back on a constant: setting that constant is not stored, with no visible effect.

### CORE-D-62 — `extends` of a concept that does not resolve is dropped from the model  ·  `DONE`

**Symptom.** `model M extends Unknown` (or a concept), where `Unknown` comes from an import that does not resolve: the semantic issue
was raised and the parent dropped; the normalized pretty-print wrote `model M {`. With `extends A, Unknown`, only `A` was kept.

**Mechanism — verified in the code.** `AbstractFlexoConceptNode.buildParentConcepts` kept only `FlexoConceptInstanceType` parents (an
`UnresolvedType` was dropped at once), `resolveParentConcepts` removed a parent it could not resolve after reporting it, and
`extends` was printed from `getParentFlexoConcepts()`, which holds `FlexoConcept` objects only. The model already had what was needed:
`parentFlexoConceptTypes` and its pending types (CORE-F-8), but resolved them all or nothing.

**Fix (2026-11-12, plan agreed with the user; no new persistent property).**
- The parser keeps every declared parent (an unresolved one as an unresolved concept type, `TypeFactory.asFlexoConceptInstanceType`),
  still reports the one not found, and hands all of them to the concept (`setParentFlexoConceptTypes`).
- `FlexoConcept` attaches the parents that resolve and keeps the others pending, in the order written, trying them again at each read;
  a current parent is detached only when no declared type designates it.
- `extends` is printed from `getParentFlexoConceptTypes()` (the name of a resolved parent, the name written otherwise).
A parent whose unit appears later is attached at the next parse. Tests: `TestCoreD62` (fml-parser), including the merge of the FML
editor (`updateWith`).

---

## Instantiation

### CORE-D-47 — A creation scheme that fails does not fail the `new` that called it  ·  `DONE`

**Fixed (2026-11-13).** `CreationSchemePathElement` checks the status of the creation action (instance and virtual model instance) and throws its failure, as a behaviour call does; `FMLRTVirtualModelInstanceModelFactory.makeNewFlexoConceptInstance` takes the partly built instance, and what it contains, out of its container and of its virtual model instance before rethrowing (what the scheme did elsewhere is kept). A failing listener fails the `fire` once every listener of the event has run (`EventListenerFailedException` carried through the `PropertyChangeSupport`); a failing deletion scheme fails `delete x`, `DeletionSchemeAction` (contextual menu) and the deletion of diagram elements, and the instance is not deleted (`deleteWithScheme` / new `deleteWithDefaultScheme`; the generic `delete()`, used by release and for contained instances, still deletes with a warning; `delete` of a VirtualModelInstance keeps its own deletion); a failing behaviour on an unmatched instance fails the `end match`. Only an executed and failed action counts (new `FlexoAction.hasActionExecutionFailed()`): a disabled one (`@Enable`) is not a failure. The partly built instance is deleted with `performCoreDeletion` (no deletion scheme, notified as deleted). A virtual model instance whose creation fails is kept (decided). Not covered: `CORE-D-73`. Tests: the scripts of `ExecutionFailures/` (fixture `ExecutionFailures`, which replaces `KDConstruction`) run command by command by `TestExecutionFailures` (fml-cli-test).

**Symptom.** When the creation scheme run by `new C(…)` fails, the caller goes on as if it had succeeded: the behaviour that wrote
the `new` continues and returns normally, and the partly built instance stays registered in its container. The same failure in an
ordinary behaviour call fails its caller.

**Reproduction (verified 2026-11-11 by execution, test `KnownDefectsJavaTests.coreD47_failedCreationSchemeFailsTheCaller`,
script `KnownDefectsSupport/CORE-D-47.fmlscript`, fixture `KDConstruction`).** `outer()` returns 7 and sets `progress` to 1,
although `create()` of `Bomb` fails on `absent.substring(0)` (`absent` is null):

```fml
public int outer() {
	Bomb b = new Bomb();
	progress = 1;
	return 7;
}
concept Bomb {
	String absent;
	create() {
		String consumed = absent.substring(0);
	}
}
```

**Mechanism — verified in the code.** `CreationSchemePathElement.performExecuteCreationScheme` calls `doAction()` and returns
`getNewFlexoConceptInstance()` without looking at `hasActionExecutionSucceeded()` (the check is commented out); its
`FMLExecutionException` catch only prints the stack trace. `FMLRTVirtualModelInstanceModelFactory` registers the new instance in
its container and its virtual model instance before running the creation scheme, with no rollback. By contrast,
`FlexoBehaviourPathElement` propagates the thrown exception. Failures are swallowed the same way by event listeners
(`DefaultFMLRunTimeEngine`: `doAction()` without status check — observed while writing the test of `CORE-D-55`), by
`FinalizeMatching` (a warning) and by `DeleteFlexoConceptInstance` (deletes even when the deletion scheme failed).
Found by the external audit of the FML language of 2026-10-23 (its §3.3).

**Decided 2026-11-11.** On failure the partly built instance is removed from its container and from the indexes, then the
`FMLExecutionException` reaches the caller.

**Workaround.** None: validate the unit, and assert the effects of a creation in tests.

---

## Found by the p2pp harness on the FML parsing examples (2026-11-12)

### CORE-D-63 — A `use` of a class that does not resolve is rewritten as the generic `ModelSlot`  ·  `DONE`

**Symptom.** `use some.package.name.AModelSlot as MS;` is printed `use org.openflexo.foundation.technologyadapter.ModelSlot as MS;`, in the
preserving and in the normalized pretty-print. What the source said is lost. Files: `BasicDeclaration`, `TestNamespaces`, `UIAnnotations`,
`TestMatch` (`FMLParsingExamples`). Same family as CORE-D-58 (what refers to a type that does not resolve is dropped), for the `use` declaration.

**Fixed (2026-11-13), decided with the user: the name is kept by the model.** `UseModelSlotDeclaration` has a new property,
`unresolvedModelSlotClassName`, the class written, set by the parser when it does not resolve (the class stays null); both pretty-prints
write it. The parser reports a warning (`Cannot find model slot class ...`) instead of printing a stack trace. The four files are reproduced.

### CORE-D-64 — The `with (...)` of a `new X()` without a constructor name is lost  ·  `DONE`

**Symptom.** `myModel = new MyVM() with (name="toto");` is printed `myModel = new MyVM();`, in the preserving and in the normalized pretty-print:
the assignment of `name` is not in the model. The same clause is kept after `new MyVM::constructor1(1,"foo")` (`with (name="tutu")`).
File: `TestAddVirtualModelInstance` (`FMLParsingExamples`).

**What it was (verified 2026-11-13 by execution).** `MyVM` does not resolve there (its import names no reachable unit), and a `new MyVM()`
whose type does not resolve was built as the instantiation of a Java class (`NewClassInstanceNode`, `JavaNewInstanceMethodPathElement`),
which has no `with` clause; `new MyVM::ctor(...)` already took an unresolved name for a concept (CORE-D-58). **Fix:** the simple form does the
same when it has a `with` clause, which only a concept has (`BindingPathFactory`, `NewFlexoConceptInstanceNode`), and reports the
unresolved concept as the qualified form does. Without the clause an unresolved name is left as before: it may be a Java class
(`new a.MyA()`, `TestExpressionWithTypesParser`).

### CORE-D-65 — `log("x")` is printed `log"x"` by the preserving pretty-print  ·  `DONE`

**Symptom.** `log("Hello world!");` is printed `log"Hello world!";` by the preserving pretty-print and `log "Hello world!";` by the normalized one;
the source has parentheses and no such spacing. Not checked: whether `log"x"` parses again. Files: `MatchingVM`, `TestBehaviours2`,
`TestMiscellaneousActions` (`FMLParsingExamples`).

**Fixed (2026-11-13)** with CORE-D-33: the parentheses belong to the parsed text of the binding, which is kept while the binding does not change.
The three files are reproduced. The normalized pretty-print still writes `log "x"`, which parses back to the same model (invariant
`normalized-parse-back`).

### CORE-D-66 — The normalized pretty-print of a constructor call with arguments is not stable  ·  `DONE`

**Symptom.** `this.new MyVM(null,null,null,null)` (as the normalized pretty-print writes it) is parsed and normalized again as
`this.new MyVM(null,null,null,null,null,null,null,null)`: the arguments double at each round trip. File: `TestAddVirtualModelInstance`
(`FMLParsingExamples`), invariant `normalized-stable`.

**Fixed (2026-11-13) with CORE-D-64.** The arguments came from the Java instantiation that `this.new MyVM()` was built as (its type does not
resolve): taken for a concept, it has no argument, and the normalized pretty-print is stable. Not checked: whether `this.new X()` of a real
Java class doubles its arguments too.

### CORE-D-67 — A reference in an expression follows a first rename of its target, not a second one  ·  `DONE`

**Symptom.** In `FMLParsingExamples/TestAddFlexoConceptInstance.fml`, rename the constructor `ConceptB::constructor1` to `q`, print the unit
(`new ConceptB::constructor1()` is now written `new ConceptB::q()`, as expected), rename it back to `constructor1`, print again: the expression
still reads `new ConceptB::q()`. Same for a concept (`new ConceptA(name)` stays `new Qx(name)` after `Qx` is renamed back to `ConceptA`). On a
document that is NOT printed between the two renames, the text is right. A print in between changes what a later print gives.

**Verified** by execution (2026-11-13) with the edit matrix of the p2pp harness (`TestFMLEditMatrix`): the groups `rename-concept/*` and
`rename-behaviour/*` fail on the oracle "undo-after-print", on `TestAddFlexoConceptInstance` and `TestFMLPrettyPrint8/InitialModel` only. The
mechanism is NOT investigated: the text of an expression is probably kept by its binding once the first print has used it.

**What it was (verified 2026-11-13 by execution, `TestRenameInExpressions` in `flexo-foundation-test`).** Wider than the symptom: once
`toString()` had been called, the text of a binding (`DataBinding.toString()`, what the pretty-print used and what an inspector shows) did not
follow even a FIRST rename. `BindingPath.toString()` caches its text. A renamed constructor clears the cache of its path element and fires
`BINDING_PATH_ELEMENT_NAME_CHANGED` on the binding model of its concept, which only reaches the bindings whose owner has that binding model:
`new ConceptB::q()`, written in a behaviour of the model, never heard of it. `AbstractFMLFunctionPathElementImpl` overrode
`clearSerializationRepresentation()` without clearing the path that activated the element (connie's `FunctionPathElementImpl` does), a
`new X(...)` did not listen to its concept at all, and a renamed property did not clear its path either.

**Fix.** An element now clears the cache of the path that activated it when its text changes (a renamed constructor, behaviour or property;
`CreationSchemePathElement` listens to its concept while activated). In connie, a path caches its text only while every element is activated
by it, i.e. able to tell it (CONNIE-D-3). The preserving pretty-print does not depend on this cache any more (its leaves read the names from
the model). Tests: `TestRenameInExpressions` (4, toString and both pretty-prints, a constructor and a concept renamed twice) and the 7 groups
of `TestFMLEditMatrix`, which pass (one case of `rename-behaviour/only` needed CORE-D-71 too).

### CORE-D-68 — The parameter of a setter is written `value`, whatever its name  ·  `DONE`

**Symptom.** In a `get/set` property, `set(int aValue) { internalValue = aValue; }` is printed `set(int value) { internalValue = aValue; }`:
the body still uses `aValue`, which the printed text no longer declares. File: `TestProperties` (`FMLParsingExamples`), found by listing every
differing row of the invariants files (2026-11-13), hidden until then behind an earlier difference of the file. Not checked: whether the name
is lost by the model or only by the printer, and whether the printed text still parses and runs.

**What it was (verified 2026-11-13 by execution).** Lost by the model: the parser never set `GetSetProperty.valueVariableName` from the
source, so it kept its default `value`, which both pretty-prints write and which names the variable the body of the setter is analyzed
against (`SetValueBindingVariable`). Not checked: whether `aValue` resolved in the body before the fix.

**Fix.** `GetSetPropertyNode` sets the name written before building the body of the setter. Test: `TestCoreD68` (fml-parser: the name is
kept, `aValue` is valid in the body, both pretty-prints write it); `TestProperties` of the invariants is reproduced.

### CORE-D-71 — A call of a behaviour loses its behaviour when the behaviour is renamed  ·  `DONE`

**Symptom.** In `FMLParsingExamples/TestInvariants.fml`, rename `AConcept.incrementA` to `q` then back to `incrementA`: `this.incrementA();`
is printed `this.q();` (both pretty-prints, and `DataBinding.toString()`). Verified by execution (2026-11-13): after the first rename the
`FlexoBehaviourPathElement` of the call is unresolved (`getFunction()` null, text `q()`), so it no longer listens to the behaviour and the
second rename does not reach it. Same result on the code before CORE-D-33 / CORE-D-67 were fixed (test run alone).

**Order-dependent.** `TestFMLEditMatrix` (group `rename-behaviour/only`, oracle "undo") passed on this case in some runs of the whole module
and failed in others: something done by the tests that run before (in the same JVM) lets the call resolve again. Not investigated: why the
element is not resolved again against the renamed behaviour (its parsed name is set to the new one before the binding is invalidated).

**What it was (verified 2026-11-13 in the code, fixed).** At a rename, `FlexoBehaviourPathElement` sets its parsed name and has its binding
invalidated; `invalidate()` sets its function to null until the binding is validated again (lazily). It stayed registered on the behaviour,
but `propertyChange()` only handled the events of `getFlexoBehaviour()`, now null: the rename back was ignored, the parsed name stayed `q`,
and a later resolution looked for `q`. When something validated the binding between the two renames (other tests in the same JVM), the
element was resolved again in time: hence the order dependency. `stopListenToBehaviour()` could not unregister either, for the same reason.

**Fix.** The element keeps the behaviour it listens to (`listenedBehaviour`) apart from its function, follows its renames whether resolved
or not, and unregisters from it.

**Tests.** `TestRenameInExpressions.aBehaviourRenamedAndBackGivesTheSourceBack` (also alone), group `rename-behaviour/only` of
`TestFMLEditMatrix`; their known-defect marks are removed.

### CORE-D-72 — Under Eclipse, a rename test destroys the `TestContainerUI.fml` fixture  ·  `DONE`

**Symptom.** Run under Eclipse, every test of `TestContainerUIComponents` fails on `No compilation unit for
http://openflexo.org/test/TestResourceCenter/TestContainerUI.fml`; green under Gradle. Verified 2026-11-13: in
`flexo-test-resources/bin/main/TestResourceCenter/FML/`, `TestContainerUI.fml/` was gone and a `RenamedContainer.fml/` was there instead.

**What it was.** `TestRenameFlexoConcept.test2…` runs `RenameCompilationUnit` (new name `RenamedContainer`) on the shared fixture, and
expected the rename to be refused because Gradle reads `flexo-test-resources` from its packaged jar, which is read-only. Eclipse reads it
from the `bin/main` directory, which is writable: the rename succeeded on disk, and the fixture stayed missing for every later run until
Eclipse copied the resources again (Project > Clean).

**Fix.** `TestRenameFlexoConcept` unregisters the resource center of `flexo-test-resources` and works on a copy of the fixture alone, in a
temporary directory resource center; it now asserts the real rename (container and file renamed on disk, components still resolved
through the frozen annotations) and that the shared fixture is untouched.

**Tests.** `TestRenameFlexoConcept` and `TestContainerUIComponents`, run under Gradle (jar) and with `flexo-test-resources` as a
directory on the classpath (the Eclipse case): 26/26 both ways, the directory left unchanged.
