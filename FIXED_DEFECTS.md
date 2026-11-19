# openflexo-core — Fixed defects

Defects of openflexo-core that are fixed: entries moved from [`KNOWN_DEFECTS.md`](./KNOWN_DEFECTS.md) once their status is `DONE`,
with what was verified, the cause and the fix. Identifiers are those of `KNOWN_DEFECTS.md`, never reused.

---

## FML matching engine

### CORE-D-2 — `end match … unmatched: method()` runs the first action scheme instead  ·  `DONE`

**GitHub:** [openflexo-team/openflexo-core#64](https://github.com/openflexo-team/openflexo-core/issues/64)

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

**GitHub:** [openflexo-team/openflexo-core#65](https://github.com/openflexo-team/openflexo-core/issues/65)

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

### CORE-D-88 — The criteria of a `match … where (…)` accept only `property = value`: `selected.x = …` is a cryptic syntax error and `selected = …` throws a NullPointerException  ·  `DONE`

**GitHub:** [openflexo-team/openflexo-core#20](https://github.com/openflexo-team/openflexo-core/issues/20)

**Symptom.** In `match Individual in matchingSet from this where (selected.individual = indiv) create::_create(indiv);` the editor reports
`Syntax error: [86,59] expecting: '='`; with `where (selected = indiv)` it reports `unexpected_exception_null`.

**Verified (2026-11-17, branch 3.0, fml-cli script).** Three variants of the same `match`, on a VirtualModel with `Holder` (property `w`) and `Wrapper`:
- `where (w = wr)`: works (control).
- `where (selected.w = wr)`: syntax error at the `.` (`ParserException token:.`): the grammar rule `qualified_argument` takes
  `property = value`, the left-hand side is the NAME of a property of the matched concept. The message `expecting '='` does not say so.
- `where (selected = wr)`: parses, then `Unexpected NullPointerException: Cannot invoke "Object.hashCode()" because "key" is null` at execution
  (`vmi.synchronize()`), `selected` being taken for a property name.

**Expected.** A clear message when the left-hand side is not a property of the concept (`selected.x`, `selected`): "a match criterion is
`property = expression`", at parsing or at validation, and no NullPointerException at execution. Whether `selected.x = value` should be supported
is a language question (see `CORE-D-1`, `CORE-D-12` for the other `where` problems of the matching engine).

**Cause.** `qualified_argument` takes a plain identifier on the left of `=`, and nothing checked that this name is a property of the
matched concept: `selected` was taken for one, `getFlexoProperty()` returned null, and `MatchFlexoConceptInstance.execute` put that null key
into its table.

**Fix (2026-11-18).** Where the language is not extended (a criterion stays `property = expression`):
- the parser message for a dotted left-hand side says what is expected (`ParserMessages`, used by the compilation-unit parser, the script
  parser and the command parser);
- the validation rule `criteria_must_address_a_property_of_the_matched_concept` reports a criterion that is not a property of the matched concept;
- the execution raises an `FMLResolutionError` instead of the `NullPointerException`.
Proven by `TestCoreD88` (fml-parser), `TestFMLErrorKinds.matchCriterionIsNotAProperty` (fml-cli-test, fixture `KDGh20c`) and
`TestMatchingCriteriaValidation` (flexo-foundation-test); the control case `where (w = wr)` is `AutomatedTests/TestMatchCriteria.fmlscript`. Supporting
`selected.x = value` would be a language evolution, not done.

**Reproduction.** Fixtures `KDGh20`, `KDGh20b`, `KDGh20c` (`TestResourceCenter/FML/KnownDefects`); script `ErrorKinds/MatchCriterionNotAProperty.fmlscript`.

**Original report** by `hibahnaini`, 2024-09-13 (issue openflexo-core#20):

> ![image](https://github.com/user-attachments/assets/c1c90477-0eb4-4b45-ab37-f6608c005268)
> Problem with the "=" when specifying the matching criteria and accessing the attribues of selected (selected.individual in this case).
> ![image](https://github.com/user-attachments/assets/d608d99d-2681-487b-b800-31e808373dcc)
> Without the attribute access (using only selected), a null exception is generated.

---

## FML control structures and edition actions

### CORE-D-6 — A `do … while` loop executes its body once and ignores its condition  ·  `DONE`

**GitHub:** [openflexo-team/openflexo-core#66](https://github.com/openflexo-team/openflexo-core/issues/66)

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

**GitHub:** [openflexo-team/openflexo-core#67](https://github.com/openflexo-team/openflexo-core/issues/67)

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

**GitHub:** [openflexo-team/openflexo-core#68](https://github.com/openflexo-team/openflexo-core/issues/68)

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

**GitHub:** [openflexo-team/openflexo-core#69](https://github.com/openflexo-team/openflexo-core/issues/69)

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

**GitHub:** [openflexo-team/openflexo-core#70](https://github.com/openflexo-team/openflexo-core/issues/70)

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

**GitHub:** [openflexo-team/openflexo-core#71](https://github.com/openflexo-team/openflexo-core/issues/71)

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

**GitHub:** [openflexo-team/openflexo-core#72](https://github.com/openflexo-team/openflexo-core/issues/72)

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

**GitHub:** [openflexo-team/openflexo-core#73](https://github.com/openflexo-team/openflexo-core/issues/73)

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

**GitHub:** [openflexo-team/openflexo-core#74](https://github.com/openflexo-team/openflexo-core/issues/74)

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

### CORE-D-48 — A local variable outlives its block and hides the property of the same name  ·  `DONE`

**GitHub:** [openflexo-team/openflexo-core#75](https://github.com/openflexo-team/openflexo-core/issues/75)

**Fixed (2026-11-14).** `FlexoBehaviourAction.getValue` reads the variables of the execution only for a local variable (`DeclarationBindingVariable` or `IterationActionBindingVariable`, new `isLocalVariable`); any other name, a property in particular, is resolved where the analysis bound it. The local context of `FlexoConceptInstance` (deletion schemes, get/set bodies, default values) follows the same rule. The other half (an inner local, or a loop variable, with the name of a visible local overwrote or erased it) is closed as in Java: such a redeclaration is invalid, reported by the new validation rule `LocalVariableMustNotRedeclareAVisibleLocal` (on `DeclarationAction` and `AbstractIterationAction`; decided 2026-11-14), so two visible locals never share a name and the single map of the execution is enough. Tests: `AutomatedTests/TestLocalScope.fmlscript` (fixture `LocalScope`, which replaces `KDScope`; a getter body included) and `TestLocalRedeclarationValidation` (fixture `LocalRedeclaration`) in fml-cli-test.

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

---

## FML compilation unit loading

### CORE-D-14 — Compilation units importing each other load forever, or keep bindings analyzed against partial types  ·  `DONE`

**GitHub:** [openflexo-team/openflexo-core#76](https://github.com/openflexo-team/openflexo-core/issues/76)

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

## Instantiation

### CORE-D-15 — `new Child()` creates an instance of the parent when the child virtual model only inherits its creation scheme  ·  `DONE`

**GitHub:** [openflexo-team/openflexo-core#77](https://github.com/openflexo-team/openflexo-core/issues/77)

**Fixed (2026-11-14).** `CreationSchemePathElement` keeps the type written in the `new` (`requestedType`) when the constructor found belongs to a parent, so the static type of `new Dog()` is `Dog`; the creation action allocates that concept (`AbstractCreationSchemeAction.setFlexoConceptBeingCreated`), and a virtual model is instantiated as the one written, not as the one declaring the scheme. Not covered: the edition-action form `AbstractAddFlexoConceptInstance` used by technology actions still derives the concept from the scheme. Test: `AutomatedTests/TestInheritedCreation.fmlscript` (fixtures `InheritedCreation`, `InheritedCreationParent`, `InheritedCreationChild`, which replace `KDInheritance`).

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

### CORE-D-47 — A creation scheme that fails does not fail the `new` that called it  ·  `DONE`

**GitHub:** [openflexo-team/openflexo-core#78](https://github.com/openflexo-team/openflexo-core/issues/78)

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

### CORE-D-84 — `FMLRT::NewInstance` runs its creation scheme three times, and leaves a second instance in the model  ·  `DONE`

**GitHub:** [openflexo-team/openflexo-core#79](https://github.com/openflexo-team/openflexo-core/issues/79)

**Symptom.** `FMLRT::NewInstance(conceptType=..., container=..., args=...)` (`NewFlexoConceptInstance`) ran the creation scheme it chose
first in the factory, without its arguments and in the context of the caller; then in its own `CreationSchemeAction`, with them; then
once more in the generic execution of `AbstractAddFlexoConceptInstance`, on another instance that the action built in the virtual model
instance and that stayed there. The side effects of a creation scheme happened up to three times and the model held an extra instance.

**Cause.** `NewFlexoConceptInstance.makeNewFlexoConceptInstance` passed the creation scheme to the factory and ran it again, and
`AbstractAddFlexoConceptInstance._performExecuteCreationScheme` built its `CreationSchemeAction` without the new instance
(`assignNewFlexoConceptInstance`), so the action made one of its own. Found by the review of `CORE-F-11` phase 6 (2026-11-15): making
the third run use the new instance (`CORE-D-73`) set the name of the fixture `TestFMLReflection` back to null.

**Fix (2026-11-16).** The factory builds the instance and its default values only; the creation scheme runs once, with its arguments, in
the action of `NewFlexoConceptInstance`, which no longer goes through the generic execution. A failing or cancelled scheme (an unchecked failure
too) discards the instance and fails or cancels the creation; a scheme not enabled gives null. The failures are typed
(`FMLNullReferenceError`, `FMLResolutionError`). `_performExecuteCreationScheme` gives the new instance to its action. Found by the second
review: the factory received the context of the caller, and assigned the new instance to it when the caller was itself a creation scheme,
so that scheme went on with `this` being the new instance; the factory now receives the action of the new instance.

**Test.** `TestFMLReflection.fmlscript` (fml-cli-test) asserts three instances after three creations, the argument reaching the
instance, and a `NewInstance` inside a creation scheme leaving that scheme on its own instance. Not proven against the former code by a
run (the review counts five instances at the former code, by reading).

---

## Resources: unloading and deletion

### CORE-D-21 — Closing a project runs the deletion schemes of every loaded instance  ·  `DONE`

**GitHub:** [openflexo-team/openflexo-core#80](https://github.com/openflexo-team/openflexo-core/issues/80)

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

**GitHub:** [openflexo-team/openflexo-core#81](https://github.com/openflexo-team/openflexo-core/issues/81)

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

**GitHub:** [openflexo-team/openflexo-core#82](https://github.com/openflexo-team/openflexo-core/issues/82)

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

**GitHub:** [openflexo-team/openflexo-core#83](https://github.com/openflexo-team/openflexo-core/issues/83)

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

**GitHub:** [openflexo-team/openflexo-core#84](https://github.com/openflexo-team/openflexo-core/issues/84)

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

**GitHub:** [openflexo-team/openflexo-core#85](https://github.com/openflexo-team/openflexo-core/issues/85)

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

**GitHub:** [openflexo-team/openflexo-core#86](https://github.com/openflexo-team/openflexo-core/issues/86)

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

### CORE-D-92 — Closing a project fails on a null root folder of the VirtualModel instance repository, leaving its resources open  ·  `DONE`

**GitHub:** [openflexo-team/openflexo-core#134](https://github.com/openflexo-team/openflexo-core/issues/134)

**Symptom (seen 2026-11-19 in the Maintainer, branch 3.0, with the uncommitted phase 2 of `CORE-F-19` in the working tree).** File >
Close project leaves the resources of the project and their views open. The log shows:

```
SEVERE Unexpected exception: ModelExecutionException … NullPointerException message: Cannot invoke
"org.openflexo.foundation.resource.RepositoryFolder.setRepositoryContext(String)" because the return value of
"org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstanceRepository.getRootFolder()" is null
	at org.openflexo.foundation.resource.FlexoResourceCenterService$$Pamela$2.removeFromResourceCenters(Unknown Source)
	at org.openflexo.foundation.project.FlexoProjectImpl.close(FlexoProjectImpl.java:565)
	at org.openflexo.foundation.project.ProjectLoader.closeProject(ProjectLoader.java:497)
	at org.openflexo.project.InteractiveProjectLoader.closeProject(InteractiveProjectLoader.java:195)
	at org.openflexo.view.menu.FileMenu$CloseProjectAction.actionPerformed(FileMenu.java:350)
```

Removing the project's resource center from the service updates the VirtualModel instance repository of the FML@runtime technology
adapter, whose root folder is null. The exception stops `close()` halfway: the project stays registered and its resources stay loaded.

**Context.** The project (`TestExceptions.prj`) holds a nested project (`Containment.prj`) and VirtualModel instances. Before closing,
it had been edited (a VirtualModel created, concepts edited, a deletion undone). The `RESOURCE UNLOADED not fully implemented` warnings
of `FMLTechnologyAdapterController.resourceUnloaded` precede the exception.

**Not known.** Whether a fresh project, closed right after opening, fails the same way. Whether this happens without `CORE-F-19`
(the failing code is resource-center and repository code, which `CORE-F-19` does not touch).

**How to reproduce.** Not scripted yet: open a project containing VirtualModel instances in the Maintainer, then File > Close project,
and read the log.

**Workaround.** Quit the application instead of closing the project.

**Cause and fix (2026-11-19, checked in the Maintainer).** The root folder of a repository is built by `setBaseArtefact`, from the
base artefact of the resource center. A project that is closing releases its resource data, so its base artefact
(`getProjectDirectory()`) is null and a repository built for it has no root folder: `instanciateNewRepository` failed on
`getRootFolder().setRepositoryContext(...)`, stopping `close()` halfway. `FMLRTTechnologyAdapter.getVirtualModelInstanceRepository` now
builds and registers no repository for a resource center without base artefact (it returns null), and `getVirtualModelInstanceRepositories`
skips it. Which resource center reached this code during the close (the project or the nested `Containment.prj`) was not established.
No automated test guards it.

### CORE-D-93 — Opening again a closed project shows its resources twice in the browser  ·  `DONE`

**GitHub:** [openflexo-team/openflexo-core#135](https://github.com/openflexo-team/openflexo-core/issues/135)

**Symptom (seen 2026-11-19 in the Maintainer, right after `CORE-D-92`).** After a failed close, opening the same project again shows
its resources twice in the browsers. The log shows the same null root folder:

```
SEVERE Unexpected exception: java.lang.NullPointerException: Cannot invoke
"org.openflexo.foundation.resource.RepositoryFolder.setRepositoryContext(String)" because the return value of
"org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstanceRepository.getRootFolder()" is null
	at org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstanceRepository.instanciateNewRepository(FMLRTVirtualModelInstanceRepository.java:79)
	at org.openflexo.foundation.fml.rt.FMLRTTechnologyAdapter.getVirtualModelInstanceRepository(FMLRTTechnologyAdapter.java:143)
	at org.openflexo.foundation.fml.rt.FMLRTTechnologyAdapter.getVirtualModelInstanceRepositories(FMLRTTechnologyAdapter.java:190)
	at org.openflexo.foundation.fml.rt.FMLRTTechnologyAdapter.notifyRepositoryStructureChanged(FMLRTTechnologyAdapter.java:198)
	at org.openflexo.foundation.technologyadapter.TechnologyAdapter.lambda$updateRepository$0(TechnologyAdapter.java:335)
```

**Cause and fix (2026-11-19, seen in the running Maintainer with a read-only probe, then reproduced headless).** Not a consequence of
`CORE-D-92`: it happens after a clean close too. A closed project keeps its delegate resource center and its project resource (cached by
the project loader). Opening it again scans the directory again, and the resource factories build NEW resources: the resource center
registers them in its URI map by artefact (which silently replaces the old entry, so `getAllResources()` stays the same size), but the
root folder still holds the old ones, so the browser listed every resource of the project twice (12 distinct objects for 6 files, one more
copy at each reopening). `ResourceRepositoryImpl.registerResource` now replaces, in the folder, an older resource of the same URI.
Releasing every resource at close was tried and rejected: the reopened project does not find its instances again.
Guarded by `TestReopenProjectNoDuplicates` (flexo-foundation-test): project opened from its directory in a new service manager, closed
by the project loader, reopened twice; the resources and the content of the root folder must not change.

**How to reproduce.** Reproduce `CORE-D-92`, then open the same project again.

**Workaround.** Quit and restart the application before opening the project again.

## Resources: loading

### CORE-D-26 — Loading a compilation unit leaves it modified  ·  `DONE`

**GitHub:** [openflexo-team/openflexo-core#87](https://github.com/openflexo-team/openflexo-core/issues/87)

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

**GitHub:** [openflexo-team/openflexo-core#88](https://github.com/openflexo-team/openflexo-core/issues/88)

**Symptom.** In the FML text rebuilt from the model (what the FML editor of the Modeller shows and saves), the actions of a technology that the unit `use`s but has no model slot role of are written without their name: `XLS::AddExcelSheet(sheetName="Goal Model")` becomes `XLS::null(sheetName="Goal Model")`, `B::CreateBPredicateFromString(…)` becomes `B::null()`, and `with BPredicateRole()` disappears. The log carries one `Cannot find FMLEntity for interface …` per action. The Modeller then fails to parse its own text (`ParserException token:null`). Measured in formod: `BMethodology` and `GoalModelingDiagram`, the only two units declaring three `use`.

**Reproduction (verified 2026-10-09 by execution).** `formod-module`, `FormodUnitsRoundTripTest`: after loading, `BMethodology` declares `BModelSlot` but the used model slots of its resource are `[AtelierBProjectModelSlot, FMLRTModelSlot]`, and `getFMLPrettyPrint()` contains `B::null()`.

**Mechanism — verified by tracing the two places that write the list.** The list of the resource is right after `setUsedModelSlots(String)` (3) and after the parser (3). During `finalizeDeserialization`, each model slot role calls `VirtualModel.declareUse(…)` (`ModelSlot.finalizeDeserialization`), the deprecated one: it builds the new list from `VirtualModel.getUseDeclarations()` — the declarations of the XML serialization, empty for a textual FML, whose `use` live in the compilation unit — plus the class of that role, and `CompilationUnitResource.updateFMLModelFactory` replaces both the list and the model factory with it. Only the model slots that have a role survive (`[FMLRT]`, then `[FMLRT, TypedDiagram]`); a technology used by its actions alone is never put back. The factory then does not know its entities, hence the `null`. It also added duplicate declarations to the VirtualModel.

**Fixed (2026-10-09).** `ModelSlot.finalizeDeserialization` asks the compilation unit (`uses` / `declareUse`, which build the list from the unit's own declarations) and only falls back on the VirtualModel's when there is none. Platform tests unchanged: `flexo-foundation-test` 521 (0 failure, 5 skipped), `fml-cli-test` 97, `integration-tests` 37; formod 32 + 15 + 56, and `Cannot find FMLEntity` is gone from their logs (it was in the thousands).

**Not fixed, noted.** `VirtualModel.uses/declareUse/getUseDeclarations` are still deprecated and still used by other callers (`CreateModelSlot`, the XML serialization): the same trap exists wherever they feed `updateFMLModelFactory`.

### CORE-D-32 — "DeclarationAction with EditionAction not implemented yet" is logged for declarations that are built and executed  ·  `DONE`

**GitHub:** [openflexo-team/openflexo-core#89](https://github.com/openflexo-team/openflexo-core/issues/89)

**Symptom.** One warning per declaration of the form `Type v = <edition action>` (`ExcelRow row = XLS::AddExcelRow(...)`, `Goal g = select unique Goal from ... where ...`, `MatchingSet m = begin match ...`): 81 at the start of formod-app, about 700 per test run. A `System.out.println("On obtient ...")` of debugging was left next to it.

**Verified by tests (2026-10-09) that the case is implemented, not only that it logs.** The declaration is built from the grammar production `initializer_fml_action` (not from the expression one) and `DeclarationActionNode` sets its assignable action and its child node *before* logging:
- `TestDeclarationWithEditionAction` (`flexo-foundation-test`, fixture `TestResourceCenter/FML/TestDeclarationWithAction.fml`): `select unique` and `select ... where` declarations hold their action (type, condition), the node of the declaration holds the node of the action, and the text printed from the model gives the declaration back. `TestMatchingVM` already asserted the `begin match` form (`InitiateMatching`).
- `TestDeclarationWithAction.fmlscript` (`AutomatedTests`, 98 tests): the declared variable is used by the next statement and gives the right values (`nameOfTheOneWithValue`, `countWithValue`).
- `FormodDeclarationsTest` (`formod-module`): every declaration of the 16 units of the Formose resource center holds its action, and the forms met include `AddExcelRow`, `CreateBExpressionFromString`, `select`, `select unique`, `begin match`.

**Fixed.** Warning and `println` removed from `DeclarationActionNode`. Platform tests: `flexo-foundation-test` 525 (0 failure, 5 skipped), `fml-cli-test` 98; formod 32 + 17 + 56; the message is gone from every log.

### CORE-D-39 — A compilation unit found half-copied by the directory watcher is never completed  ·  `DONE`

**GitHub:** [openflexo-team/openflexo-core#90](https://github.com/openflexo-team/openflexo-core/issues/90)

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

**GitHub:** [openflexo-team/openflexo-core#91](https://github.com/openflexo-team/openflexo-core/issues/91)

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

### CORE-D-94 — A VirtualModel edited in the FML editor is not saved, and comes back empty after reload  ·  `DONE` (closed 2026-11-19, not reproduced)

**GitHub:** [openflexo-team/openflexo-core#136](https://github.com/openflexo-team/openflexo-core/issues/136)

**Symptom (seen once, 2026-11-19, in the Maintainer, with the uncommitted phase 2 of `CORE-F-19` in the working tree).** A
VirtualModel edited in the FML editor, then saved, comes back empty when the project is reloaded.
- The log of the save shows only the project resource (`Saving resource FlexoProject … ProjectData`), not the edited
  `CompilationUnitResource` (`TestContainment`).
- On disk, `TestContainment.fml` is still the file written at its creation (an empty `model TestContainment { }`).

So the edited unit was not marked modified, or not offered by the save.

**Context.** This happened after `CORE-D-92` and `CORE-D-93`, while the resources of the project were registered twice: the
editor may have edited one copy and the save may have looked at the other. Before that, in the same session, the unit had been edited
in the text (concepts added, moved into one another, a deletion undone, see `PAMELA-D-8`).

**Not known.**
- Whether it happens in a clean session, without the duplicate resources.
- Whether `CORE-F-19` plays a part. It changed the stored property that holds the concepts (`contents` instead of `flexoConcepts` and
  `embeddedFlexoConcepts`), which the editor's `updateWith` and the modified flag go through.

**Not reproduced (2026-11-19).** A headless test follows the path of the FML editor on the same working tree (with `CORE-F-19`
phase 2): create a VirtualModel in a project, edit its text as `FMLEditorParser` does (parse, `updateWith`, `setResource`,
`manageImports`) adding two root concepts, one of them containing a third, save the project, reload it (`TestCoreD94` in
`flexo-foundation-test`). It passes: the unit, its VirtualModel and its resource are marked modified, `TestContainment.fml` is written with
the concepts, and they are back after reload. Not covered: several successive edits (move into another concept, a deletion undone), the
resources registered twice of `CORE-D-92` / `CORE-D-93`, and the real editor of the Maintainer.

**Closed (2026-11-19, `not planned`).** Not reproduced, and the initial diagnosis (the edited unit not marked modified, or not
offered by the save) is probably wrong: the likely cause is a crash during the save of the unit, in a session that also had `CORE-F-19`
phase 2 uncommitted and the resources registered twice. A hypothesis, not established. `TestCoreD94` stays as a guard.

**How to reproduce.** Not reproduced. If it happens again, open a new issue (linked to this one): keep the log of the save and the file on disk, and check whether the unit
was registered twice (`CORE-D-92` / `CORE-D-93`) and whether the unit was marked modified before the save.

**Workaround.** None known; check the file on disk after saving.

---

## Noise and fidelity found by mapping the warnings of loading a Formose project (2026-10-09)

### CORE-D-33 — The text rebuilt from the model is not the text that was parsed  ·  `DONE`

**GitHub:** [openflexo-team/openflexo-core#92](https://github.com/openflexo-team/openflexo-core/issues/92)

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

**GitHub:** [openflexo-team/openflexo-core#93](https://github.com/openflexo-team/openflexo-core/issues/93)

**Symptom.** A FML role pointing at a technology object is empty after a reload, its reference serialized as `<resource>#FLX_null`, although the object does give a serialization identifier at the time the project is saved.

**Cause (measured 2026-10-13 in formod, FORMOD-D-15).** `FlexoObjectReference.setObject` captures `resourceIdentifier`, `userIdentifier` and `objectIdentifier` when the reference is made, and `constructSerializationRepresentation` - what `FlexoObjectReferenceConverter` writes - rewrote those fields as they were. An identifier is not frozen, though: `notifySerializationIdHasChanged` exists for that, and a technology adapter may compute one from the place the object holds in its resource. A B predicate is identified by its containment path in its component, which it only takes once the model inserts it in a clause - after the role was assigned - so the reference kept the null it was given.

**Fixed (2026-10-13).** `constructSerializationRepresentation` now takes the identifiers from the live object again (`resource.getObjectIdentifier(modelObject)`) before writing, and only when the object still gives one: this refreshes an identifier, it never drops one.

**Blast radius, read in the code.** The only implementations of `getObjectIdentifier` in the workspace are `PamelaResourceImpl` (the `FlexoID`), `EMFModelResourceImpl` / `EMFMetaModelResourceImpl` (`EcoreUtil.getID`) and `BResourceImpl` (the path of the B object). The first three are stable, so the refresh is a no-op for them; the B one is what needed it.

**Tests.** No test in `openflexo-core`: the defect needs a resource whose identifiers depend on the place of the object, which only the B technology adapter has. It is guarded by `TestBMethology.testReloadProject` in `formod`, red before this change. Non-regression here: `flexo-foundation-test` 525 (520 successes, 5 skipped), `fml-cli-test` 99/99.

### CORE-D-52 — The text printed for a concept-typed property with an initializer does not parse again  ·  `DONE`

**GitHub:** [openflexo-team/openflexo-core#94](https://github.com/openflexo-team/openflexo-core/issues/94)

**Fixed (2026-11-14).** Fixed by the work on `CORE-D-33` / `CORE-D-60` (a Java-style declaration is printed without `with ConceptInstance()`), and the initializer is now printed from the model (`CORE-D-51`). The case of a property that also has a `with` clause is no longer unparsable but loses its initializer: `CORE-D-74`. Test: `TestPropertyInitializerRoundTrip` (fml-parser), which replaces the known-defect test.

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

### CORE-D-57 — The normalized pretty-print of some units loses expressions and does not parse again  ·  `DONE`

**GitHub:** [openflexo-team/openflexo-core#95](https://github.com/openflexo-team/openflexo-core/issues/95)

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

**GitHub:** [openflexo-team/openflexo-core#96](https://github.com/openflexo-team/openflexo-core/issues/96)

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

**GitHub:** [openflexo-team/openflexo-core#97](https://github.com/openflexo-team/openflexo-core/issues/97)

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

**GitHub:** [openflexo-team/openflexo-core#98](https://github.com/openflexo-team/openflexo-core/issues/98)

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

**GitHub:** [openflexo-team/openflexo-core#99](https://github.com/openflexo-team/openflexo-core/issues/99)

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

## FML properties

### CORE-D-50 — The default values of inherited properties are not set on a new instance  ·  `DONE`

**GitHub:** [openflexo-team/openflexo-core#100](https://github.com/openflexo-team/openflexo-core/issues/100)

**Fixed (2026-11-14).** `FlexoConceptInstance.initializeDefaultValues` iterates the accessible roles of the concept declared by it or one of its parents (a redefinition giving its own value), and evaluates default values and initializers in the context of the instance itself, the creation action not being attached to it yet. Test: `AutomatedTests/TestInheritedCreation.fmlscript`.

**Symptom.** A property declared with a default value in a parent concept (`int legs = 4;`) is null on a new instance of a child
concept that has its own creation scheme. The child's own default values are set.

**Reproduction (verified 2026-11-11 by execution, test `KnownDefects/CORE-D-50.fmlscript`, fixture `KDInheritance`).** With
`Animal { int legs = 4; create() { } }` and `OwnDog extends Animal { int tail = 1; create() { } }`, `vmi.new OwnDog()` gives
`tail == 1` and `legs == null`.

**Mechanism — verified in the code.** `FlexoConceptInstance` initializes default values by iterating
`getFlexoConcept().getDeclaredProperties(FlexoRole.class)`: declared roles only, not inherited ones (and roles only, not
`FMLVariable`s). Found by the external audit of the FML language of 2026-10-23 (its §3.6).

**Workaround.** Assign the inherited property in the child's creation scheme.

### CORE-D-51 — Initializers and cardinalities written in a property declaration are lost  ·  `DONE`

**GitHub:** [openflexo-team/openflexo-core#101](https://github.com/openflexo-team/openflexo-core/issues/101)

**Fixed (2026-11-14).** `FlexoRolePropertyNode` keeps the initializer of a declaration in the Java style as the default value, and prints it back; `FMLVariableNode` keeps and prints the cardinality. An edition action as initializer is supported (decided): `FlexoRole` is now an `FMLControlGraphOwner` with an embedded `initializer` control graph, built by the property nodes, printed back, and run when the instance is created, as default values are; its failure fails the creation. Tests: `AutomatedTests/TestPropertyInitializers.fmlscript` (fixture `PropertyInitializers`, which replaces `KDPropertyInit`), `TestPropertyInitializersValidation` (fml-cli-test), `TestPropertyInitializerRoundTrip` (fml-parser).

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

## Found by the p2pp harness on the FML parsing examples (2026-11-12)

### CORE-D-63 — A `use` of a class that does not resolve is rewritten as the generic `ModelSlot`  ·  `DONE`

**GitHub:** [openflexo-team/openflexo-core#102](https://github.com/openflexo-team/openflexo-core/issues/102)

**Symptom.** `use some.package.name.AModelSlot as MS;` is printed `use org.openflexo.foundation.technologyadapter.ModelSlot as MS;`, in the
preserving and in the normalized pretty-print. What the source said is lost. Files: `BasicDeclaration`, `TestNamespaces`, `UIAnnotations`,
`TestMatch` (`FMLParsingExamples`). Same family as CORE-D-58 (what refers to a type that does not resolve is dropped), for the `use` declaration.

**Fixed (2026-11-13), decided with the user: the name is kept by the model.** `UseModelSlotDeclaration` has a new property,
`unresolvedModelSlotClassName`, the class written, set by the parser when it does not resolve (the class stays null); both pretty-prints
write it. The parser reports a warning (`Cannot find model slot class ...`) instead of printing a stack trace. The four files are reproduced.

### CORE-D-64 — The `with (...)` of a `new X()` without a constructor name is lost  ·  `DONE`

**GitHub:** [openflexo-team/openflexo-core#103](https://github.com/openflexo-team/openflexo-core/issues/103)

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

**GitHub:** [openflexo-team/openflexo-core#104](https://github.com/openflexo-team/openflexo-core/issues/104)

**Symptom.** `log("Hello world!");` is printed `log"Hello world!";` by the preserving pretty-print and `log "Hello world!";` by the normalized one;
the source has parentheses and no such spacing. Not checked: whether `log"x"` parses again. Files: `MatchingVM`, `TestBehaviours2`,
`TestMiscellaneousActions` (`FMLParsingExamples`).

**Fixed (2026-11-13)** with CORE-D-33: the parentheses belong to the parsed text of the binding, which is kept while the binding does not change.
The three files are reproduced. The normalized pretty-print still writes `log "x"`, which parses back to the same model (invariant
`normalized-parse-back`).

### CORE-D-66 — The normalized pretty-print of a constructor call with arguments is not stable  ·  `DONE`

**GitHub:** [openflexo-team/openflexo-core#105](https://github.com/openflexo-team/openflexo-core/issues/105)

**Symptom.** `this.new MyVM(null,null,null,null)` (as the normalized pretty-print writes it) is parsed and normalized again as
`this.new MyVM(null,null,null,null,null,null,null,null)`: the arguments double at each round trip. File: `TestAddVirtualModelInstance`
(`FMLParsingExamples`), invariant `normalized-stable`.

**Fixed (2026-11-13) with CORE-D-64.** The arguments came from the Java instantiation that `this.new MyVM()` was built as (its type does not
resolve): taken for a concept, it has no argument, and the normalized pretty-print is stable. Not checked: whether `this.new X()` of a real
Java class doubles its arguments too.

### CORE-D-67 — A reference in an expression follows a first rename of its target, not a second one  ·  `DONE`

**GitHub:** [openflexo-team/openflexo-core#106](https://github.com/openflexo-team/openflexo-core/issues/106)

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

**GitHub:** [openflexo-team/openflexo-core#107](https://github.com/openflexo-team/openflexo-core/issues/107)

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

**GitHub:** [openflexo-team/openflexo-core#108](https://github.com/openflexo-team/openflexo-core/issues/108)

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

**GitHub:** [openflexo-team/openflexo-core#109](https://github.com/openflexo-team/openflexo-core/issues/109)

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

---

## FML runtime errors (found by the audit of `CORE-F-11`, 2026-11-13)

### CORE-D-75 — The initial value of a `for (init; cond; update)` loop is a Boolean when its initializer fails  ·  `DONE`

**GitHub:** [openflexo-team/openflexo-core#110](https://github.com/openflexo-team/openflexo-core/issues/110)

**Symptom (read).** `ExpressionIterationAction.evaluateInitExpression` prints the stack and returns `Boolean.TRUE` on a
`TypeMismatchException`, `InvocationTargetException` or `ReflectiveOperationException`, returns `false` on a `NullReferenceException`, and returns `true` when the
initializer is unset or invalid. The loop then runs with a Boolean as the value of its variable, and the failure is not reported.

**Fixed (2026-11-15, `CORE-F-11` phase 3).** `evaluateInitExpression` goes through `FMLEvaluation.evaluate`: a failing initializer fails the
loop with the error of its kind, no initializer gives `null`, and the loop never runs with a Boolean. Test: `TestFMLErrorKinds.failingLoopInitializer`
(fml-cli-test, script `ErrorKinds/FailingLoopInitializer.fmlscript`).

### CORE-D-76 — The iterator variable of `for (x : …)` stays declared when the loop body fails  ·  `DONE`

**GitHub:** [openflexo-team/openflexo-core#111](https://github.com/openflexo-team/openflexo-core/issues/111)

**Symptom (read).** `IterationAction.execute` dereferences the iterator only on a `ReturnException`: an `FMLExecutionException`
thrown by the body leaves `x` declared in the evaluation context. The same holds for `ExpressionIterationAction`. Also: a `return` inside the
iteration expression is cast to `List` and used as the collection (`evaluateIteration`).

**Fixed (2026-11-14, `CORE-F-11` phase 1).** `IterationAction` and `ExpressionIterationAction` remove the iterator in a `finally`, whatever
ends the loop (a failure, a `return`, the end). Not changed: a `return` inside the iteration expression is still taken as the collection
(an iteration expression is an expression, it does not hold a `return` in practice). Tests: none fails without the fix (no script makes the body of a loop fail); correct by
reading.

### CORE-D-77 — Creating a virtual model instance without a name throws a `java.security.InvalidParameterException`  ·  `DONE`

**GitHub:** [openflexo-team/openflexo-core#112](https://github.com/openflexo-team/openflexo-core/issues/112)

**Symptom (read).** `CreateFMLRTVirtualModelInstance` imports `java.security.InvalidParameterException` (a `RuntimeException` of the JDK security
API) and throws it when the name is undefined. The project's `InvalidParametersException` was meant. Since `CORE-F-11` phase 1 the status of
the action is set (`FAILED_EXECUTION`), but the failure is taken for a defect of the platform (an unchecked exception), not for an invalid
parameter.

**Fixed (2026-11-15, `CORE-F-11` phase 3).** The project's `InvalidParametersException` is thrown: the action ends `FAILED_EXECUTION` on a
foreseen, checked failure. Test: `TestFlexoRoleCardinality` (flexo-foundation-test), which fails without the fix (the test editor rethrows an
unchecked failure).

### CORE-D-78 — `CreateFlexoConceptInstance` throws a `ClassCastException` while logging, and ignores a failing creation scheme  ·  `DONE`

**GitHub:** [openflexo-team/openflexo-core#113](https://github.com/openflexo-team/openflexo-core/issues/113)

**Symptom (read).** In `doAction`, when the virtual model instance is not an `FMLRTVirtualModelInstance`, the warning "Not implemented feature
for …" casts that instance to `FMLRTVirtualModelInstance` to build its message: the log itself throws a `ClassCastException`. In the other
branch, `creationSchemeAction.doAction()` is called without checking its outcome, so a failing creation scheme still reads as a successful
creation.

**Fixed (2026-11-15, `CORE-F-11` phase 3).** The failure of the creation scheme is rethrown unchanged (`FMLExecution.rethrowFailure`), and an
instance that is not an `FMLRTVirtualModelInstance` fails the action with an `InvalidParametersException` whose message no longer casts it.
Not run: no test drives `CreateFlexoConceptInstance` with a failing creation scheme; correct by reading.

### CORE-D-79 — A behaviour call gives a `NullPointerException` instead of an FML error  ·  `DONE`

**GitHub:** [openflexo-team/openflexo-core#114](https://github.com/openflexo-team/openflexo-core/issues/114)

**Symptom (read).**
- `ExecuteFlexoBehaviour.execute`: when the behaviour reference fails to evaluate, `getFlexoBehaviour` prints the stack and returns null, then
  `actionScheme.getActionFactory(fci)` throws an NPE.
- `FlexoBehaviourPathElement.getBindingValue`, called outside a behaviour: when the instance has no resource center, the action is never built
  and `actionSchemeAction.setDeclaredConceptualLevel(…)` throws an NPE.
- `ExecuteFlexoBehaviour.execute` has the same structure: no resource center outside a behaviour, then an NPE on `actionSchemeAction.doAction()`.

**Fixed (2026-11-15, `CORE-F-11` phase 3).** `ExecuteFlexoBehaviour.execute` raises an `FMLNullReferenceError` for a null receiver or behaviour,
and both sites build the action outside a behaviour with the editor of the instance (`FlexoConceptInstance.getEditor()`), which needs no
resource center; the failure of the behaviour is rethrown unchanged. The path element raises an `FMLResolutionError` / `FMLTypeError` in a
strict evaluation instead of returning null after a warning. Not run: the call outside a behaviour without a resource center has no test;
correct by reading.

### CORE-D-81 — A failed action that edited nothing undoes the previous action, deleting the instance it ran on  ·  `DONE`

**GitHub:** [openflexo-team/openflexo-core#115](https://github.com/openflexo-team/openflexo-core/issues/115)

**Symptom (seen 2026-11-15 in the application, Maintainer launched from Eclipse).** A behaviour of a virtual model instance, run from its
contextual menu, fails (`log "longueur=" + tutu.length` with `tutu` null, an `FMLNullReferenceError` since `CORE-F-11` phase 3). Right after,
the three views of the virtual model instance close (`deleteModuleView()` three times in `Openflexo.out.log`): the instance has been deleted.

**Cause (read, from the logs and the code).** `InteractiveFlexoEditor.runExceptionHandler` calls `FlexoUndoManager.actionHasBeenPerformed(action,
false)`, whose `compensateFailedAction` did `if (canUndo()) undo();`. The behaviour edited nothing, so its compound edit is not significant;
Swing's `UndoManager.undo()` skips it and undoes the last significant edit instead: the creation of the instance, recorded just before
("PAMELA edit received outside official recording"). Older than `CORE-F-11`, which made such failures frequent: a null used to be silent.

**Fixed (2026-11-15).** `compensateFailedAction` undoes the edit of the failed action only, when it is significant and the next to be undone;
otherwise it drops it. Test: `TestFlexoUndoManager` (flexo-foundation-test), whose `aFailedActionThatEditedNothingUndoesNothing` fails
without the fix. Not checked in the application yet (needs a restart).

## FML types

### CORE-D-87 — A property initialised with `new HashMap<>()` fails with an `ArrayIndexOutOfBoundsException`  ·  `DONE`

**GitHub:** [openflexo-team/openflexo-core#28](https://github.com/openflexo-team/openflexo-core/issues/28)

**Symptom.** A property such as `Map<Object,Object> map = new HashMap<>();` makes the FML editor log `Unexpected exception : 1`
(`FMLEditorParser.updateEditorGutter`), and the instantiation of the VirtualModel fails.

**Verified (2026-11-17, branch 3.0, fml-cli script run with `OPENFLEXO_KNOWN_DEFECTS`).** With this VirtualModel:

```
import java.util.HashMap;
import java.util.Map;

@URI("http://openflexo.org/test/TestResourceCenter/KDGh28.fml")
public model KDGh28 {
	Map<Object,Object> map = new HashMap<>();
}
```

`vmi = new KDGh28() with (name="gh28");` fails with `FMLInternalError: Unexpected ArrayIndexOutOfBoundsException: Index 1 out of bounds for length 1`.
Top of the stack: `org.apache.commons.lang3.reflect.TypeUtils.getTypeArguments` (836) <- `org.openflexo.connie.type.TypeUtils.getTypeArguments` (1423)
<- `getTypeArgument` (1434) <- `isTypeAssignableFrom` (597) <- `DataBinding.performComputeValididy` (740) <- `DataBinding.isValid`
(also reached from `DataBindingNode.finalizeDeserialization`, which is the editor path). The message `1` of the editor is the index of the exception.

**Cause.** The diamond `new HashMap<>()` gives a parameterized type whose arguments are not resolved, and the
assignability check indexes the type arguments of `Map` (two) in an array that has one. `new HashMap<Object,Object>()` is the first thing to try.

**Fix (2026-11-18, connie 2.1.0).** The diamond is typed as a `ParameterizedType` carrying a single `DiamondType` argument, whatever
the number of type parameters of the raw type. `TypeUtils.isTypeAssignableFrom` then asked commons-lang3 for the arguments of
`HashMap<Diamond>` seen as a `Map` and indexed past them. A type with a `DiamondType` argument is now compared on its raw type only
(its arguments are inferred from the target). Regression test: `TestTypeUtils.testDiamondAssignability` (default-expression-language),
red without the fix. Verified end to end (2026-11-18, openflexo-dev composite): `KnownDefects/CORE-D-87.fmlscript` (fixture `KDGh28`) now passes: the
VirtualModel is instantiated and `vmi.map != null`.

**Reproduction.** Temporary fixture `KDGh28` (`openflexo-core/flexo-test-resources`), to become `KnownDefects/CORE-D-87.fmlscript`.

**Original report** by `enmanokatana`, 2025-11-25 (issue openflexo-core#28):

> **Summary**
> When importing and using **HashMap** in FML within the OpenFlexo editor, an error is thrown.
> 
> **Steps to Reproduce**
> 
> Open an FML file in the OpenFlexo editor
> Add an import statement for HashMap (`import java.util.HashMap;`)
> Attempt to use HashMap in the code
> Error is thrown
> 
> **Environment**
> 
> Version: 2.99
> Operating System:Windows 10
> Java version: 8
> 
> **Error in logs**
> `Thread[AWT-EventQueue-0,6,main]     WARNING 25/11/25 14:01:29,867  Unexpected notice at line:-1 Line -1: Unexpected exception : 1                                      [org.openflexo.fml.controller.widget.fmleditor.FMLEditorParser.updateEditorGutter]`
> 
> **Code Sample**
> ```
> import java.util.HashMap;
> 
> @URI("http://www.openflexo.org/projects/2025/11/Modelcheckingcomp_1763632671263.prj/TestGraphModel.fml")
> @Author("hayaoumo")
> public model TestGraphModel {
>    Map<Object,Object> map = new HashMap<>();
> }
> 
> ```
>

---

## Inheritance

### CORE-D-89 — A concept of a child VirtualModel cannot instantiate a concept inherited from the parent VirtualModel  ·  `DONE`

**GitHub:** [openflexo-team/openflexo-core#27](https://github.com/openflexo-team/openflexo-core/issues/27)

**Symptom (2025, version 2.99).** `FMLExecutionException: Cannot instanciate a FlexoConceptInstance : null FlexoConcept container`
(`FMLRTVirtualModelInstanceModelFactory.makeNewFlexoConceptInstance`) when the creation scheme of `TestConceptB`, a concept of
`TestModelB extends TestModelA`, runs `TestConceptA a = new TestConceptA();` where `TestConceptA` belongs to `TestModelA`.

**Verified (2026-11-17, branch 3.0, fml-cli script).** The original error is no longer reachable: the VirtualModel is now rejected at validation with
`cannot instantiate TestConceptA in TestConceptB: TestConceptA is not nested in TestConceptB, use container.new TestConceptA(...)`.
But the suggested form does not work either: with `container.new TestConceptA()` the creation fails with
`cannot instantiate TestConceptA in KDGh27C`. So a concept inherited from the parent VirtualModel cannot be instantiated from a concept of the child.

**Cause.** A gap: the validation looked for the instantiated concept among the root concepts declared by the VirtualModel of the behaviour only
(`getAllRootFlexoConcepts()` does not include those of the parent VirtualModel), and a bare `new` was accepted only when the concept was nested in the
concept of the behaviour.

**Fix (CORE-F-18).** A `new` resolves its container by climbing the containment tree (`FMLContainerResolver`): a root concept is hosted by the
nearest virtual model instance whose VirtualModel declares it **or inherits it**. Both `new TestConceptA()` and `container.new TestConceptA()` are valid
in `TestConceptB` of the child VirtualModel. Script `AutomatedTests/CORE-D-89.fmlscript`.

**Reproduction.** Fixtures `KDGh27A`, `KDGh27B`, `KDGh27C`; the script is now `AutomatedTests/CORE-D-89.fmlscript`.

**Original report** by `hibahnaini`, 2025-09-24 (issue openflexo-core#27):

> Erorr: org.openflexo.foundation.fml.rt.FMLExecutionException: Cannot instanciate a FlexoConceptInstance : null FlexoConcept container
> 	at org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstanceModelFactory.makeNewFlexoConceptInstance(FMLRTVirtualModelInstanceModelFactory.java:132)
> 	at org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstance$FMLRTVirtualModelInstanceImpl.makeNewFlexoConceptInstance(FMLRTVirtualModelInstance.java:141)
> 	at org.openflexo.foundation.fml.rt.action.AbstractCreationSchemeAction.doAction(AbstractCreationSchemeAction.java:175)
> 	at org.openflexo.foundation.fml.rt.action.CreationSchemeAction.doAction(CreationSchemeAction.java:118)
> 	at org.openflexo.foundation.action.FlexoAction.doActionInContext(FlexoAction.java:271)
> 	at org.openflexo.view.controller.InteractiveFlexoEditor.runAction(InteractiveFlexoEditor.java:224)
> 	at org.openflexo.view.controller.InteractiveFlexoEditor.doExecuteAction(InteractiveFlexoEditor.java:191)
> 	at org.openflexo.view.controller.InteractiveFlexoEditor.executeAction(InteractiveFlexoEditor.java:181)
> 	at org.openflexo.view.controller.InteractiveFlexoEditor.performAction(InteractiveFlexoEditor.java:141)
> 	at org.openflexo.foundation.action.FlexoAction.doAction(FlexoAction.java:231)
> 
> [BugTest.zip](https://github.com/user-attachments/files/22511020/BugTest.zip)

### CORE-D-91 — A root concept extending a nested concept is printed inside the container of its parent  ·  `DONE`

**GitHub:** [openflexo-team/openflexo-core#133](https://github.com/openflexo-team/openflexo-core/issues/133)

**Symptom (reproduced 2026-11-18, branch 3.0).** A concept declared at the root of a VirtualModel that extends a nested concept is
printed inside the container of its parent. Saving the unit moves it there, and its URI changes from `…#Gamma` to `…#Root1#Gamma`.

```
public model Containment {
	public concept Root1 {
		public concept Alpha { }
	}
	public concept Gamma extends Alpha { }
}
```

After parsing, `Gamma.getContainerFlexoConcept()` is null, its URI is `…#Gamma`, and its applicable container (inherited from
`Alpha`) is `Root1`. That part is right. `getNormalizedFML()` prints `Gamma` inside `Root1`, and re-parsing gives a concept nested in `Root1`.

**Cause.**
- `VirtualModelNode` prints `getAllRootFlexoConcepts()`, which leaves out every concept that has an applicable container.
- `FlexoConceptNode` prints `getAllEmbeddedFlexoConceptsDeclaringThisConceptAsContainer()`, which selects on the applicable container,
  not on the declared one.

Printing therefore follows the inherited container instead of the place of declaration.

**Impact (at the time).** `BPMNModel.fml` used this pattern (`Event` outside `Process`, extending the nested `FlowNode`). It no longer does: its
concepts have since been flattened, all at the root.

**How to reproduce.** `KnownDefectsConceptContainmentTest.rootConceptExtendingANestedOneStaysAtTheRoot` (fml-parser), with
`OPENFLEXO_KNOWN_DEFECTS=1`.

**Fix (2026-11-19).** With the lexical container of `CORE-F-19`, the pretty-print follows `getContents()`: `VirtualModelNode`, `FlexoConceptNode`,
`FlexoEventNode` and `FlexoExceptionNode` print the concepts declared in them, not those whose inherited container they are.
`getAllRootFlexoConcepts()` keeps its meaning (no applicable container), which the instance creation and the matching rely on.
The test, moved to the VirtualModel contents for its first assertion, now passes.

**Note (2026-11-19).** The model of the symptom is not valid: a concept nested in `Root1` has its instances in an instance of `Root1`, so a
concept extending it must be declared in `Root1` or in a subtype of `Root1`. That constraint was not checked: see `CORE-D-95`. The printing
fix stays right, a unit is written where each concept is declared.

### CORE-D-95 — A concept extending a nested concept is accepted when declared outside the container of its parent  ·  `DONE`

**GitHub:** [openflexo-team/openflexo-core#137](https://github.com/openflexo-team/openflexo-core/issues/137)

**Symptom (found 2026-11-19, while fixing `CORE-D-91`).** With `Alpha` nested in `Root1`, a root `concept Gamma extends Alpha { }` was
loaded and validated without any issue, although the instances of `Gamma` would have no `Root1` instance to live in.

**Semantics.** A concept B nested in A has its instances in an instance of A. A concept C extending B is therefore declared in A, or in
a subtype of A. With several parents, the container of C is a subtype of the container of each of them.

**Cause.** `ContainerFlexoConceptMustBeConsistentWithParents` only ran when C had a declared container: a root concept (container null)
was never checked, and its inherited container (the applicable one) hid the inconsistency instead of reporting it. The effect also showed
in the browsers, which list roots by the inherited container and children by the declared one, so such a concept was shown nowhere.

**Fix (2026-11-19).** The rule checks every parent and no longer skips a root concept: a validation error
`concept_($validable.name)_must_be_contained_in_a_subtype_of_the_container_of_its_parents`. Tests: `TestConceptContainment` (fml-parser):
root, in the container, in a subtype of it, in an unrelated one, two parents.

**Semantics decided.** The direct container of C is a subtype of the direct container of each parent, as written. A container inherited
from the parents is not a substitute for a declaration: a root concept extending a nested one is an error, also across VirtualModels
(`inside` is refused between compilation units, so the container is specialised in the extending model instead). The fixture
`TestInheritedConcepts` was migrated accordingly: `SpecializedItem` is declared in a `SpecializedBox extends Box`.

**Not done.** The loading is not refused (it is a validation error, as for the other consistency rules). The containment of a concept
whose parent is a root concept is not checked: see `CORE-D-96`.
