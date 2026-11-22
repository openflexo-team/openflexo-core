# 14. Instantiation and containment

This chapter defines where `new` puts the instance it creates. The expression itself (its syntax, the three kinds of
type it can instantiate, the `with` clause) is in §9.7; the containment of concepts is declared in §5.2.1; the
instances and their `container` are in §13.2.

## 14.1 Containment, in two words

* A concept is either **nested** in a container concept (declared inside it, §5.2.1) or **at the root** of its model. A
  concept inherits the container of its parents when it declares none: its *applicable container* is the declared one,
  else the most specialized of those of its parents.
* An **event** and an **exception** are concepts like the others: they may be nested in a concept, and everything in
  this chapter holds for them. Their instances too have their host as `container`, already in their creation scheme;
  being signals, they are not stored in it, nor saved with the model instance.
* A virtual model is either a **root** model, or declares a **container model** (it is declared in the directory of
  another model, §5.1.4).
* At run time, an instance of a nested concept is contained in an instance of its applicable container concept (or of a
  specialization of it); an instance of a root concept, event or exception belongs to a model instance; a model
  instance may be contained in another model instance. `container` designates that container (§13.2).

## 14.2 The container of a `new`

`new X(args)` has no container of its own. The instance is created in a **host**, found from a **starting
object**:

| Form | Starting object |
|---|---|
| `new X(args)` | `this`, the instance on which the behaviour runs |
| `c.new X(args)` | the object designated by the path `c` |

From the starting object, the containment tree is **climbed**: the starting object itself, then its container, then the
container of that one, up to the model instance, then to the model instance that contains it, if any. The **host** is
the **nearest** object that can hold an `X`:

| `X` is | A host is |
|---|---|
| a concept, event or exception nested in a container concept `K` | an instance of `K` or of a specialization of `K` |
| a root concept, event or exception | a model instance of the model that declares `X`, or of a model that extends it |
| a virtual model that declares a container model `M` | a model instance of `M` or of a specialization of `M` |
| a virtual model with no container model | none: it is not hosted by an instance, and does not climb (§9.7, §12.5) |

The rules that follow hold for every form.

* **The nearest wins.** There is no ambiguity error: the first host met on the way up is chosen. A nearer host of the
  same type cannot occur, since a concept cannot be nested in a concept it extends (§5.1.5).
* **The starting object is a candidate.** A `new X()` that was valid because `this` could hold an `X` keeps its
  meaning: the instance is created in `this`.
* **A sibling is a climb.** From an instance of `B`, nested in `A`, `new B(…)` finds no `B` able to hold a `B`, climbs
  to the instance of `A`, and creates a `B` beside the current one: its `container` is that `A`.
* **The explicit form climbs from its path.** `c.new X(…)` climbs from the object designated by `c`, which need not
  be the container of `X`.
* **A behaviour of an ancestor is tolerated.** A behaviour declared on a model, or on a concept, may run on an instance of a
  specialization: when the model climbed is a parent of the model a root concept or a contained model is declared in, or the
  concept of the starting object is a parent of the container concept of `X`, the `new` is accepted when validated, and it
  succeeds when the climb meets a host at run time.
* **A model nested in another one has no other fallback.** A virtual model that declares a container model needs a
  hosting model instance on the way up: when there is none, the `new` fails with an `FMLResolutionError`. The `rc`, `path`
  and `folder` parameters apply to a model that declares no container model only.
* **A nested concept is not created in the model.** From a behaviour declared on a model, a `new` of a concept nested in
  another one is an invalid binding: there is no instance of the container to climb to.
* **No syntax is added.** A bare `new` is still printed bare: `this` is never written, and `this.new X()` is not
  rewritten.

```fml-member
public concept Alpha {

	String name;

	create(String name) {
		this.name = name;
	}

	public concept Beta {

		String name;

		create(String name) {
			this.name = name;
		}

		// The Alpha that holds this Beta
		public Beta sibling(String name) {
			return new Beta(name);
		}

		// The model instance, two steps up
		public Alpha another(String name) {
			return new Alpha(name);
		}

		// The same, from another Beta
		public Alpha anotherFrom(Beta other, String name) {
			return other.new Alpha(name);
		}
	}
}
```

## 14.3 Where it is checked

The same rule is applied twice.

* **When the unit is validated**, on the concepts: starting from the concept of the behaviour (or, for `c.new X`, the
  concept of the type of `c`), the applicable containers are climbed, then the models. When no host can be reached,
  the `new` is an invalid binding, reported with the concepts that were looked at:

  ```
  cannot instantiate Beta in Containment: no container (an instance of Alpha) can be reached from Containment (looked at: Containment)
  ```

  The `unmatched: new X(…)` of a `match` is not checked: it is evaluated like any `new`, from `this`, and not from the `from`
  container of the `match` (§10.9).
* **When it runs**, on the instances: starting from the object, the containers are climbed with the concepts of the
  instances, which may be specializations of those that were validated. A `new` whose host cannot be found raises an
  `FMLResolutionError` (§13.6); an invalid binding is an execution error as any other (§9.8).

> **Implementation note.** `FMLContainerResolver` implements both sides; `CreationSchemePathElement` calls it.
> The checks of the instance factory, which require a container compatible with the concept, are kept: for a concept, they
> see a host chosen by the climb; a virtual model with no container model has no host. The instance of an event or of an exception
> keeps its host (`FlexoEventInstance.setHost`, `FlexoExceptionInstance.setHost`), returned by `getContainer()` and, when it is
> a concept instance, by `getContainerFlexoConceptInstance()`, so that every climb goes through it; it is not added to the
> instances of its host, and its source model instance is the one of its host. One with no creation scheme is created the same way.
> `TestContainment.fmlscript` and `TestContainmentValidation` (fml-cli-test) run the examples of this chapter on
> `FML/Containment.fml`.
