# 13. Execution model

The previous chapters describe what a program says. This chapter describes what it **does**: what exists when a
model runs, how a behaviour is executed, when values are computed and changes are noticed, what happens when
something goes wrong, and how a program is checked.

## 13.1 Two levels: describing and running

Everything declared in FML exists at two levels.

* The **model level** is what is written: virtual models, concepts, events, properties, behaviours. It is the level
  of *types*. Its objects form the *FML meta-model* (`FML`).
* The **run-time level**, called *FML@RT*, is what exists when the model runs: instances, which hold the values.

Each notion has its counterpart:

| Model level (FML) | Run-time level (FML@RT) |
|---|---|
| `VirtualModel` (`model`) | `VirtualModelInstance` |
| `FlexoConcept` (`concept`) | `FlexoConceptInstance` |
| `FlexoEvent` (`event`) | `FlexoEventInstance` |
| `FlexoProperty` / `FlexoRole` | a value held by the instance, for a role an `ActorReference` |
| `FlexoBehaviour` | `FlexoBehaviourAction`, created each time the behaviour runs |
| `FlexoEnum` | `FlexoEnumInstance` (a value of the enum) |

A virtual model is a concept, so a virtual model instance is a concept instance: it holds values, runs behaviours,
and contains other instances.

The running of a program is therefore: *load* the units (§3.8) to get the model level; *instantiate* a virtual
model; *call* behaviours on the instances; *observe* the values.

## 13.2 Instances and containment

An instance of a concept always lives inside an instance of its container: the instance of the virtual model or of
the concept in which the concept is declared (§5.2.1).

* `new X(…)` in a behaviour creates the instance in the **current** instance, when `X` is declared in the concept
  of that instance (or, from a model, in the model). Otherwise it is created through its container,
  `c.new X(…)`, where `c` designates the instance in which to create it (§9.7).
* `container` designates the instance that contains `this`. For an instance of a contained model (§5.1.4), it is
  the instance of its container model.
* A query searches what an instance **contains**, nested instances included (§10.2.2).

A **virtual model instance** is itself stored in a resource of a resource center. Instantiating a model from a script
needs a writable resource center (§12.5); the instance has a name, given by `with (name = …)`.

## 13.3 Properties of an instance

An instance holds the values of the properties of its concept and of its ancestors.

* A typed property holds `null` until it is assigned or initialised (§6.2); a multi-valued property holds an
  empty list (§6.3).
* An expression property computes its value from other properties (§6.4); a get/set property runs its `get` and
  `set` (§6.7).
* A role-bound property reaches the object of a resource (§6.5).

Properties inherited from several ancestors are one set of properties of the instance; there is no per-ancestor copy.

## 13.4 Executing a behaviour

When a behaviour is called, the platform creates a `FlexoBehaviourAction`, binds the arguments of the call, and executes
the body.

1. **Binding model.** The body is evaluated in a chain of scopes, innermost first: the control graph's own variables
   (locals, loop variables), the behaviour's (`parameters`), the concept's (its properties, `this`, `container`),
   the virtual model's (§9.6.3).
2. **Order.** Statements run in order (§8). A block is a sequence; `if`, `while` and `for` choose and repeat;
   `return` ends the behaviour with a value.
3. **Edition actions** are executed as statements: each does its work and may produce a value that is assigned or
   returned (chapter 10).
4. **Result.** The value of `return`, or `null` if none.

Execution is **synchronous**: the behaviour runs in the calling thread, and the call returns when the body has
completed. This holds for events too (§10.6): the listeners of a fired event complete before `fire` returns.

A behaviour that is called while it is running (recursion) gets its own action and its own variables.

## 13.5 Creation and deletion

* `new X(args)` creates the instance, whose properties with an initial value start from it (§6.2), and runs the creation
  scheme matching the arguments (§7.3.1), whose parent scheme is run by `super(…)`.
* `delete e` runs the deletion scheme of the instance's concept (§7.3.2) and removes the instance. The instance is
  **not** removed from the multi-valued properties that hold it (§10.4).

When a resource is unloaded or a project closed, its instances are *released*, not deleted: the deletion schemes of
its instances are not run (`CORE-D-21`, `CORE-D-22`).

## 13.6 Null and failure

FML does not have exceptions in its syntax: there is no `try`, `catch` or `throw`. A problem during execution has one of
these outcomes, which this section collects (they are justified where the construct is described).

| Situation | Outcome | Reference |
|---|---|---|
| a path whose element does not resolve (misspelling, missing import, bare parameter name, bare call) | the expression is `null`; execution goes on | §9.8 |
| a path that goes through a `null` value | in a value position, an **execution error** that stops the behaviour; in an `if` condition, the condition is false | §9.8, §8.4 |
| a type error (`1 + "a"`, string used as a number) | an execution error that stops the behaviour | §9.5 |
| an `if` or `while` whose condition is invalid | the condition is true | §8.4 (`CORE-D-8`) |
| a `new` that does not resolve to a creation scheme | `null` | §7.2, §9.7 |
| a technology-specific action that cannot be built | reported at load; the unit keeps its text | §10.10.2 |
| `assert` false in a script | the run fails with a message naming the line | §12.6 |
| a failed `load` or `new` in a script | the script stops | §12.7 |

An execution error is signalled to the caller of the behaviour as an `FMLExecutionException`; a script that called
it stops, and an application reports it.

The consequence of the first row is the discipline this specification repeats: **a `null` where a value is expected
is first a name that does not resolve.** Validate (§13.8) before debugging.

## 13.7 Change notification

Instances and metamodel objects notify their changes through `PropertyChangeSupport`: assigning a property raises a
notification for it, carrying the old and the new value, and an assignment of a value equal to the current one raises
none. These notifications keep the following current:

* expression properties and bindings, which listen to the properties they read — although an expression property that
  has been read can keep its earlier value (`CORE-D-11`, §6.4);
* the string representation of an instance, given by `@Renderer`, which follows the properties it reads (§11.4);
* user interfaces and inspectors, which are bound to the instance;
* `notify "name" [in o]` raises one explicitly (§10.7).

## 13.8 Validation

A compilation unit and an instance can be **validated**. Validation checks the rules of the meta-model, among which

* every binding in the unit is valid: its path resolves against the typing space (§4.8, §9.6);
* the types of declarations resolve; the parents of a concept resolve; the container named by `inside` exists;
* the invariants of an instance hold (§5.6.1).

Validation is the way to find the errors that execution hides: an unresolved path is `null` at run time (§9.8), an
invalid condition is true (§8.4), a missing `import` gives `null` (§3.5.2). It can be run on a unit and on an instance, and the editors
report its results.

A caution on the empty unit. A parse error leaves an **empty** compilation unit behind (§1.5), and an empty unit
reports **zero** validation errors. "Zero validation errors" proves nothing about a `.fml` file unless the unit is
also shown to hold its concepts and the log shows no *ParserException*.

## 13.9 Matching sets

A matching set (§10.9) is a run-time structure holding the instances of a concept that exist when it is opened, and
removing from the pool of the unmatched each one that a `match` finds. `end match` handles what is left. A set is
held by a local variable of the behaviour; a `match` without a set uses an implicit one that belongs to the
execution of the behaviour and is finalised when the behaviour ends (§10.9.4).

## 13.10 Concurrency and re-entrance

FML defines no concurrency. A behaviour runs to completion in the calling thread (§13.4). Listeners are called inside
`fire`. A program must not assume anything about the interleaving of the changes made to resources by other
threads; a directory-watching resource center, for instance, observes the file system in a thread of its own.

## 13.11 Re-runnable behaviours

The platform's use of FML is largely *synchronisation*: a behaviour that is run again and again over sources that
change. Such a behaviour must be written to be **re-runnable**:

* use `match` with explicit sets (§10.9) rather than `select` and `new`, so that a second run creates nothing;
* guard each `add` on a multi-valued property with `contains` (§6.3);
* remove an instance from the properties that hold it before deleting it (§10.4);
* update the matched instance from its source on every run, since a `match` returns it rather than recreating it.
