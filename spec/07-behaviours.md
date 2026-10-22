# 7. Behaviours

A *behaviour* is what an instance of a concept can do: a name, arguments, an optional return type and a
body. Behaviours are declared in models, concepts and events.

## 7.1 Syntax

```ebnf
behaviour_decl =
    annotation* visibility? "create" "(" formal_arguments? ")" body                     (* anonymous creation scheme *)
  | annotation* visibility? "create" "::" lidentifier "(" formal_arguments? ")" body    (* named creation scheme *)
  | annotation* visibility? "delete" "(" formal_arguments? ")" body                     (* anonymous deletion scheme *)
  | annotation* visibility? "delete" "::" lidentifier "(" formal_arguments? ")" body    (* named deletion scheme *)
  | annotation* visibility? "abstract"? type? lidentifier "(" formal_arguments? ")" body  (* method *)
  | annotation* "listen" reference_type "from" expression body                          (* event listener *)
  | annotation* visibility? type? lidentifier "(" formal_arguments? ")"
        "with" behaviour_ref fml_parameters? body                                       (* technology-specific *)

behaviour_ref = uidentifier | cidentifier "::" uidentifier

formal_arguments = formal_argument ( "," formal_argument )*
formal_argument  = "required"? ( primitive_type | reference_type ) lidentifier ( "=" expression )?

body  = block | ";"
block = "{" block_statement* "}"
```

The *kind* of a behaviour is determined by its introducer:

| Introducer | Kind | Runs |
|---|---|---|
| `create(…)`, `create::n(…)` | creation scheme | when an instance is created with `new` |
| `delete(…)`, `delete::n(…)` | deletion scheme | when an instance is deleted with `delete` |
| `type? name(…)` | action scheme (a *method*) | when called |
| `listen E from src` | event listener | when an event of type `E` is fired by `src` |
| `… with Kind(…)` | technology-specific behaviour | as defined by the technology |

### 7.1.1 Methods

```fml-member
public String greet() {
	return "hello";
}

public String greet(String who) {
	return "hello " + parameters.who;
}

public remember(String text) {
	last = parameters.text;
}
```

A method has an optional visibility, an optional return type, a name and arguments. When the return type is
omitted, as in `remember`, the behaviour acts on the instance and returns nothing. A method may be overloaded:
two methods have the same name when their arguments differ (§7.4).

A method is called on an instance: `talker.greet()` from a script or from another unit, `this.greet()` from
a behaviour of the same instance (§9.6.3).

### 7.1.2 Abstract methods

`abstract` followed by a signature and `;` declares a method without a body. Each concept that extends the
declaring concept gives the body; a call goes to the body of the concept of the instance (§5.3.1).

```fml-member
abstract String sound();
```

A body of `;` is the only form that declares a bodyless behaviour. For a method that is not marked
`abstract`, `;` yields a behaviour with an empty control graph.

## 7.2 Arguments

An argument has a type, a name, optionally `required` and optionally a default value.

* The type is a primitive type or a reference type (§4). A technology-specific type is not accepted as the type of
  a formal argument.
* `required` marks the argument as mandatory: the application that asks for the arguments (a dialog, a
  palette) insists on a value for it.
* A default value, `= expression`, is the value the argument starts from in such a dialog.

```fml-member
create(required String label, int capacity = 10) {
	label = parameters.label;
	capacity = parameters.capacity;
}
```

> **Implementation note.** A default value does **not** allow a call to omit the argument: `new Shelf("x")`
> against `create(required String label, int capacity = 10)` does not resolve, is reported *cannot find
> constructor*, and yields `null` at run time (`CORE-D-4`). Every argument is passed at every call. To offer a
> shorter call, declare a second, named creation scheme.

### 7.2.1 Reading the arguments

The arguments of a behaviour are read in its body through the variable `parameters`:

```fml-member
public int add(int a, int b) {
	return parameters.a + parameters.b;
}
```

> A bare parameter name, `a` instead of `parameters.a`, **is not an error**: it resolves to nothing and gives
> `null`. This is the most frequent cause of a `null` that should not be (§9.6.3, §13.6).

## 7.3 Creation and deletion schemes

### 7.3.1 Creation

A creation scheme initialises a new instance. It runs when the instance is created with `new` (§9.7). A concept may declare several; **at most one is anonymous**, the
others are named with `create::name`, and a `new` chooses among them by name (`new Shelf::withDefaultCapacity(l)`).

```fml-member
create(required String label, int capacity = 10) {
	label = parameters.label;
	capacity = parameters.capacity;
}

create::withDefaultCapacity(String label) {
	label = parameters.label;
	capacity = 10;
}
```

A creation scheme reaches that of its parent concept with `super(args)` (anonymous) or `super.name(args)` (named)
(§5.3.2). A model that declares only a named creation scheme is instantiated by name
(`new Journal::start("Diary") with (name = "diary")`); `new Journal()` does not resolve against it.

A concept that declares no creation scheme can be created without arguments.

### 7.3.2 Deletion

```fml-member
delete() {
	container.last = "note deleted";
}
```

A deletion scheme runs when an instance is deleted with `delete` (§10.4). A concept may declare several, told
apart by name. Which of them `delete x` runs when there are several is not specified by this document; the
closing clause of a matching set runs the **first** one that the concept declares (§10.9), which is why a
concept that is closed by a matching set should declare exactly one.

> **Implementation note.** Deleting an instance does not remove it from the multi-valued properties that
> reference it: take it out first (`books.remove(parameters.book); delete parameters.book;`) (`CORE-D-10`).

## 7.4 Calling a behaviour

A call is an expression `target.name(arguments)` (§9.6.4). The behaviour called is selected by

1. the **name**, and
2. the **number** of arguments, which must be exactly the number of declared arguments (default values do not
   change this), and
3. the **types** of the arguments, to choose among behaviours of the same name and number.

The lookup starts at the concept of the instance the call is made on and goes up the inheritance graph; the most
specific matching behaviour is called.

**A behaviour of the same instance is called on `this`.** A bare call, `twin()`, does not resolve and evaluates
to `null` without an error:

```fml-member
public String twin() {
	return "twin";
}

public String bareCall() {
	return twin();
}

public String thisCall() {
	return this.twin();
}
```

`bareCall()` returns `null`, `thisCall()` returns `"twin"`. The rule holds for behaviours of a model called
from the model.

A behaviour that is recursive calls itself the same way (`this.fact(n - 1)`).

## 7.5 Return

A `return` statement ends the behaviour and gives its value (§8.8). A `return` inside a loop ends the
behaviour, which is how a search stops early.

## 7.6 Instantiation from a behaviour

Inside a behaviour of a **model**, `new Shelf(…)` creates an instance of the concept `Shelf` in the model
instance. Inside a behaviour of a **concept**, `new` creates a concept **nested in that concept**, inside the
current instance. A concept declared elsewhere (at the model level for instance) is created through its
container, `container.new Tool(…)`; a bare `new` of such a concept gives `null` (§9.7).

## 7.7 Event listeners

```ebnf
listener = annotation* "listen" reference_type "from" expression body
```

A listener has no name and no visibility. It belongs to the instance that holds it: the instance listens to the
source designated by `from` for as long as the instance exists. It runs when an event of the given type is fired by the object the
`from` expression designates, which is typed as a virtual model instance. Inside the body, the variable `evt` is
the event that was fired:

```fml-member
listen ShelfCreated from this {
	lastCreatedShelf = evt.label;
}
```

The event type must resolve to an event or concept type; otherwise the semantic
issue *Unexpected event type* is reported.

A listener with a body of `;` is accepted and does nothing.

## 7.8 Technology-specific behaviours

A behaviour declared with `with Kind(parameters)` is a behaviour of a **kind that a technology adapter
defines**. It is how a model hooks into what happens in a federated resource: a drop on a diagram, a navigation,
a synchronization. The `Kind` and its parameters are resolved among the behaviour classes of the adapters
declared with `use` (§3.4), or by `TA::Kind`.

```fml-member
dropShape(required String label) with DropScheme(targetType = Diagram) {
	log "dropped";
}
```

The fully qualified form `with DIAGRAM::DropScheme(…)` selects the adapter explicitly. The core platform
defines no non-deprecated kind of this family; in particular the former synchronization scheme is deprecated
and a behaviour named `synchronization() { … }` is a plain method.

Whether a kind resolves depends on the adapters installed (§3.4); when it does not, the unit is built with
the behaviour unresolved and the problem is reported.

## 7.9 Annotations on behaviours

Annotations precede a behaviour. `@UI(…)` describes how the application asks for the arguments (§11.4);
any other annotation is kept (§11.2). A `listen` behaviour has annotations but no visibility.

## 7.10 Bodies

A body is either a block or `;`. A block is a sequence of block statements (chapter 8). Within a body

* the variables in scope are, from innermost to outermost, the local variables, the properties of the concept,
  those of the enclosing concepts and model, and the built-in names `this`, `container`, `parameters`,
  `super` and, in a listener, `evt` (§9.6.3);
* the instructions that act on models are the edition actions of chapter 10.

A body is not required to be non-empty. A creation scheme with an empty body creates an instance with its initial
values and nothing else.
