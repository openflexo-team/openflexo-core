# 5. Models, concepts, events and enums

The body of a compilation unit is one `model`. A model contains *members*: properties (chapter 6),
behaviours (chapter 7), and nested declarations of concepts, events and enums, with invariants. This
chapter describes the declarations that give a program its structure.

## 5.1 Models

```ebnf
model_decl    = annotation* visibility? "abstract"? "model" uidentifier
                super_clause? inside_clause?
                "{" member* "}"

super_clause  = "extends" composite_tident ( "," composite_tident )*
inside_clause = "inside" composite_tident
visibility    = "public" | "protected" | "private"
member        = property_decl | behaviour_decl | concept_decl | event_decl | enum_decl | invariant_decl
```

A *virtual model* (the `model`) is the unit of a compilation unit. It is itself a concept: whatever §5.2
says of concepts holds for models, and a model can have properties, behaviours, nested concepts, events,
enums and invariants. What distinguishes it is that it is the root of its unit, has an address (§3.2), and
is what a program **instantiates** to start working (§13.1).

```fml
@URI("http://openflexo.org/test/TestResourceCenter/Kennel.fml")
@Version("1.0")
@Description("A kennel")
public model Kennel {
}
```

### 5.1.1 Annotations

The annotations that precede `model` carry the unit's metadata: `@URI`, `@Version`, `@Description`
(§11.3). Any other annotation is kept as it is written (§11.2).

### 5.1.2 Visibility and abstractness

A visibility keyword may precede `abstract` and `model` (§5.8). `abstract`
declares that the model is not meant to be instantiated directly but completed by models that extend it.

### 5.1.3 Extending a model

`model Child extends Parent, Other { … }` declares that `Child` has the concepts, properties and behaviours of
each parent, in the way concept inheritance works (§5.3). The parent must be resolvable by the typing
space, which means that the unit must import the unit that declares it (§3.5.2): extending a model
without importing it is the common cause of an unresolved parent.

A virtual model instance of the child can instantiate the concepts it inherits from the parent models.

> **Implementation note.** A child model that does not declare a creation scheme of its own and only
> inherits one is instantiated as an instance of the parent, not of the child (`CORE-D-15`).

### 5.1.4 Contained models

A model may be *contained* in another. A contained model is not declared inside the text of its container; it
is a separate unit stored in a sub-directory of the container's resource directory (§3.2), and the container
declares a property typed with it and creates its instances from a behaviour, like instances of a concept:

```fml-member
Catalog catalog;

public Catalog createCatalog() {
	catalog = new Catalog() with (name = "catalog");
	return catalog;
}
```

At run time the instance of the contained model reports the instance of its container (§13.2).

### 5.1.5 The inside clause

`inside C` declares that the model (or concept, or event) lives **inside concept `C` declared in another
compilation unit**, which lets a unit contribute to the containment structure of a model it does not own. The
container must be resolvable and be a concept; otherwise the semantic issue "Container concept … not found" is
reported on the type.

## 5.2 Concepts

```ebnf
concept_decl = annotation* visibility? "abstract"? "concept" uidentifier
               super_clause? inside_clause?
               "{" member* "}"
```

A *concept* describes a kind of object: the properties its instances hold, the behaviours they can
perform, the concepts nested in it. A concept is a **type** (§4.3), and the declaration of an *instance* of it
is made at run time by `new` (§9.7).

### 5.2.1 Nesting is containment

A concept declared inside a model, or inside another concept, is **contained** by its enclosing declaration:

```fml
public model Library {

	public concept Shelf {

		String label;

		public concept Book {
			String title;
		}

		public concept Novel extends Book {
			String author;
		}
	}
}
```

Containment is a structural fact with consequences.

* **At run time**, an instance of a nested concept lives inside an instance of its container: a book created by
  a shelf is contained in that shelf and its `container` is that shelf (§13.2).
* **In the address** of the concept: `…/Library.fml#Shelf#Book` (§4.3).
* **For creation**: a bare `new Book(…)` in a behaviour of `Shelf` creates a `Book` in the current shelf
  (§9.7); a concept that is not nested in the current one is created through its container, `container.new X(…)`.
* **For queries**: `select X from this` searches what `this` contains, nested instances included (§10.2).

A nested concept is named by its simple name from anywhere in the unit (§4.8).

### 5.2.2 Abstract concepts

`abstract` makes a concept a description to complete: it declares properties and may declare abstract
behaviours (§7.1.2) to which the concepts that extend it give a body. An abstract concept may have creation
schemes, which its descendants reach with `super(…)` (§5.3.2).

## 5.3 Inheritance

`extends` names one or several concepts to build on:

```fml
public model Kennel {

	public abstract concept Creature {

		String name;

		create(String name) {
			name = parameters.name;
		}

		abstract String sound();

		public String describe() {
			return name + " says " + this.sound();
		}
	}

	public concept Dog extends Creature {

		create(String name) {
			super(parameters.name);
		}

		public String sound() {
			return "Woof";
		}
	}

	public concept Trained {
		String trick;
	}

	public concept ServiceDog extends Dog, Trained {
		create(String name, String trick) {
			super(parameters.name);
			trick = parameters.trick;
		}
	}
}
```

### 5.3.1 What is inherited

A concept has the properties and the behaviours of all its ancestors, and may add to them. A concept may
**override** a behaviour by declaring one with the same signature; a call runs the body of the concept of the
instance on which it is made (late binding), whichever concept the call is typed by. A property inherited from
an ancestor is the same property in the descendant: it is declared once.

A query on a concept returns the instances of that concept and of every concept that extends it (§10.2).

### 5.3.2 Reaching the parent

* In a creation scheme, `super(args)` runs the anonymous creation scheme of the parent; `super.name(args)`
  runs a *named* one (`create::name`). The two spellings are not interchangeable.
* In any other behaviour, `super.name(args)` calls the parent's version of `name`.

> **Implementation note.** With several parents, `super.name()` gives `null` and nothing reports it
> (`CORE-D-17`). Put the logic to reuse in a separate behaviour of the parent and call it on `this`.

### 5.3.3 Constraints

The inheritance graph must be acyclic; a hierarchy that is not is reported as inconsistent. Every parent
must resolve in the typing space (§4.8). A model extends models and a concept extends concepts: the grammar
accepts any `composite_tident` in the clause, and the kind of the parent is a matter for the analysis. This
specification does not define which of two equally specific behaviours inherited from different parents is
called.

## 5.4 Enums

```ebnf
enum_decl   = annotation* visibility? "enum" uidentifier "{" enum_value ( "," enum_value )* "}"
enum_value  = cidentifier
```

An enum is a closed set of named values. It can be declared in a model or in a concept.

```fml
public model Colors {

	enum Hue {
		RED,
		GREEN,
		BLUE
	}

	public concept Paint {
		Hue hue;

		enum Finish {
			MATT,
			GLOSS
		}

		Finish finish;
	}
}
```

The values are `cidentifier`s (§2.4), so each is written in capitals. An enum is a type (§4.3): a property,
a parameter or a local can be typed with it, and the value `null` belongs to it.

The set of all values of an enum is available, from a property or a value typed with the enum, through the
native path element `enumValues`, which yields a list; this is how a choice of values is offered to a user.
The textual name of a value is obtained by concatenating it to a string (`"" + v`). In the arguments of a
technology-specific action or role, a value is written `$NAME` (§10.10.1).

> **Implementation note.** The name of an enum value cannot be read with `toString()` in a
> binding: the enum type does not expose Java methods. Use concatenation.

## 5.5 Events

```ebnf
event_decl = annotation* visibility? "abstract"? "event" uidentifier
             super_clause? inside_clause?
             "{" member* "}"
```

An *event* is a concept whose instances are signals. It is declared like a concept, with properties and a
creation scheme; it is raised with `fire` (§10.6) and received by a `listen` behaviour (§7.7).

```fml-member
event ShelfCreated {
	String label;

	create(String label) {
		label = parameters.label;
	}
}

public Shelf newShelf(String label) {
	Shelf shelf = new Shelf(parameters.label, 20);
	fire new ShelfCreated(parameters.label);
	return shelf;
}

listen ShelfCreated from this {
	lastCreatedShelf = evt.label;
}
```

An event may extend other events, be abstract, and contain concepts and events. An event type can be used as the
type of a `listen`.

## 5.6 Invariants

```ebnf
invariant_decl = simple_invariant | iteration_invariant
simple_invariant     = "assert" expression ";"
                     | "assert" expression "onfailure" ":" statement
iteration_invariant  = "assert" "(" type lidentifier ":" expression ")"
                       "{" simple_invariant* "}"
```

An *invariant* is a condition that every instance of the concept is expected to satisfy. It is written in the
body of the concept (or of a model) like a member.

```fml
public model TestInvariants {

	public concept AConcept {

		int a;
		boolean h;
		float i;
		ASubConcept[0,*] subConcepts with ConceptInstance(container = this);

		assert (i != -1);
		assert h onfailure: {
			h = true;
		}
		assert (a > 1) onfailure: {
			while (a <= 1) {
				this.incrementA();
			}
		}
		assert (ASubConcept c : subConcepts) {
			assert c.aBoolean onfailure: c.aBoolean = true;
			assert c.anOtherBoolean onfailure: {
				log "This is a failure case";
			}
		}

		void incrementA() {
			a = a + 1;
		}

		public concept ASubConcept {
			boolean aBoolean;
			boolean anOtherBoolean;
		}
	}
}
```

A **simple invariant** is a boolean expression evaluated on the instance (`this` and the properties of the concept
are in scope). An optional `onfailure:` clause gives a *repair*: a statement, usually a block, that restores
the condition.

An **iteration invariant** `assert (T x : collection) { … }` evaluates its simple invariants once for each element
of the collection, with `x` bound to the element.

### 5.6.1 When invariants are checked

Invariants are evaluated by the **validation** of an instance, not on each change. Validation of an instance
evaluates the invariants of its concept and, recursively, those of all its ancestors. Each false invariant is
reported as a violation (*concept constraints should not be violated*). An invariant whose evaluation raises
an error is skipped: it is neither a violation nor satisfied.

The `onfailure` clause is part of the model and is written back, as the repair that the author attaches to the
invariant. The validation rule of the platform reports the violation; it does not execute the clause.

## 5.7 Members

The member alternatives, in the order of the grammar, are:

| Form | Chapter |
|---|---|
| typed property: `type [cardinality] name [= init];` | §6.1, §6.2 |
| property bound to a role: `type [cardinality] name with Role(args);` | §6.5 |
| abstract property: `abstract type name;` | §6.4 |
| expression property: `type name values expression;` | §6.3 |
| get/set property: `type name { type get() {…} set(type v) {…} };` | §6.6 |
| behaviour: method, `create`, `delete`, `listen`, technology-specific | chapter 7 |
| nested `concept`, `event`, `enum` | §5.2, §5.4, §5.5 |
| invariant: `assert …` | §5.6 |

A member may be preceded by annotations (chapter 11) and, for properties and behaviours, by a visibility.

## 5.8 Visibility

`public`, `protected` and `private` precede models, concepts, events, enums, properties and behaviours. A declaration
without keyword has the fourth visibility, *default*, which is distinct from `public` and is written back as no
keyword.

| Visibility | Written | Meaning |
|---|---|---|
| default | nothing | the element has no declared visibility |
| public | `public` | the element is exposed by the tooling: a `public` behaviour is offered on the instances in the application |
| protected | `protected` | declared protected |
| private | `private` | declared private |

Visibility is not an access restriction on a script, which can read and write a `private` property (§12.7).
