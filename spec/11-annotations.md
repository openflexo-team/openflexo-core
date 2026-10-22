# 11. Annotations

An *annotation* attaches information to a declaration without changing its structure: the address of a model,
the string representation of a concept, the table a concept is stored in. Annotations are how FML carries
everything that is not structure, and the way a technology adapter or a tool adds its own information.

## 11.1 Syntax

```ebnf
annotation = "@" annotation_tag                                       (* flag *)
           | "@" annotation_tag "(" conditional_exp ")"               (* single value *)
           | "@" annotation_tag "(" key_value ( "," key_value )* ")"  (* named values *)
           | "@" annotation_tag "(" annotation* ")"                   (* nested annotations *)

annotation_tag = composite_tident | composite_cident
key_value      = composite_ident "=" conditional_exp
```

The tag is a `uidentifier` (`@Renderer`) or a `cidentifier` (`@URI`, `@UI`); it may be qualified by a package
path. An annotation has one of four shapes:

```fml
@Marker
@Note("hello")
@Owner(name = "Ann", team = "core")
@Groups(
	@Member("a")
	@Member("b")
)
public model Tagged {
}
```

* `@Marker` is a **flag**: it says something by being there.
* `@Note("hello")` has a **value**, an expression (a string, a number, a path).
* `@Owner(name = "Ann", team = "core")` has **named values**; each key is a name, each value an expression.
* `@Groups(…)` contains **annotations**: the way a group of related annotations is written. A pair of
  parentheses with nothing inside is also accepted.

A value is a conditional expression (§9.1): it can use all operators except assignment.

## 11.2 Placement and retention

Annotations go before the element they describe: a model, a concept, an event, an enum, a property, a behaviour
(any of its forms except that the keyword `listen` follows its annotations). Several may precede a declaration, in
any order. An annotation cannot be attached to a statement, an argument or a local variable.

```fml
public model Cards {

	@Renderer(name + "!")
	public concept Card {

		@Hint("on a property")
		String name;

		create(String name) {
			name = parameters.name;
		}

		@Hint("on a behaviour")
		public String greet() {
			return "hi";
		}
	}
}
```

**The platform keeps every annotation it reads, on the element it precedes, and writes it back as it was
written, whether it knows the name or not.** Annotations are *metadata* of the declaration, available to any tool
that reads the model.

A consequence is that **a misspelled annotation is not an error**: `@Discription` is kept like any other and
does nothing. Nothing checks the name of an annotation against a list.

## 11.3 Annotations on a model

| Annotation | Value | Meaning |
|---|---|---|
| `@URI("…")` | string | the address of the model (§3.2) |
| `@Version("…")` | string | its version |
| `@Description("…")` | string | a short text |

They are read back when the model is loaded. A version is normalised: `@Version("2")` is the version `2.0`.

The value of `@URI` is a string; a namespace alias (§3.3) is not resolved in it (`CORE-D-16`).

`@Description` is read on any FML object (the description of a concept, a property or a behaviour is the
`@Description` that precedes it); `@Author` is read on a concept.

## 11.4 Annotations on a concept

| Annotation | Meaning |
|---|---|
| `@Renderer(expression)` | the *string representation* of the instances of the concept |
| `@Inspector("File.inspector")`, `@Inspector(derived = …)` | the inspector of the concept, or a derived inspector (the instance is `instance`) |
| `@UI("File.fib")`, `@UI(default = "A.fib", compact = "B.fib")` | the user-interface component(s) of the concept, overriding the convention |

**`@Renderer`.** The expression is evaluated on each instance to give its string representation: the text that
is obtained when the instance is concatenated to a string, or shown in a browser. It follows the properties it
reads, so the representation stays current when they change, and it applies to the concepts that extend the
concept. The instance is available as `instance`, and its properties as bare names.

```fml
public model RenderedCards {

	@Renderer(name + "!")
	public concept Card {
		String name;
	}

	public concept Gold extends Card {
	}
}
```

With `name = "x"`, `"" + card` is `"x!"`; after `name = "y"` it is `"y!"`; a `Gold` card inherits the renderer.

**`@UI` and `@Inspector`.** By convention the user interface and the inspector of a concept are files of the
container directory (§3.2) named after the concept. The annotations override that convention, naming another
file; a multi-valued annotation names variants, the key `default` being the one used by default.

## 11.5 Annotations on a behaviour

| Annotation | Meaning |
|---|---|
| `@UI(@Widget(value = "arg", label = "key"), …)` | how the application asks for the arguments |
| `@Visible(expression)` | whether the behaviour is offered on an instance; offered when absent |
| `@Enable(expression)` | whether the behaviour is enabled on an instance |

`@UI` holds one annotation per argument, to choose the widget of the dialog that asks for the argument: `value`
names the argument, `label` gives the key of its label.

```fml-member
@UI(
	@TextField(value = "title", label = "book_title")
)
@Visible(capacity > 0)
public Book newBook(required String title) {
	return new Book(parameters.title);
}
```

`@Visible` and `@Enable` are expressions evaluated on the instance (the properties of the concept are in
scope); a behaviour is offered, or enabled, when the expression is true.

The dialog exists only in the application. Tests of the platform check that the annotation is loaded, not the dialog.

## 11.6 Annotations of a technology adapter

A technology adapter defines annotations for its own needs; the platform keeps them like any other. The
relational adapter maps the concepts of a model onto a relational schema:

```fml-member
@Table("CLIENT")
public concept Client {
	@Property(column = "ID", id = "true")
	int id;
	@Property(column = "NAME")
	String name;
	@Property(column = "SALESMAN", fk = "ID")
	Salesman salesman with ConceptInstance();
}
```

`@Table` names the table; `@Property` gives the column (`column`), marks the identifier (`id`) and describes a
reference to another table (`fk`, or `mappedBy` for the other side). The adapters document their own annotations.

## 11.7 Reading annotations

Annotations are metadata of the FML object. From a script or a tool, an object reports whether it holds one
(`hasMetaData("Marker")`) and gives its values:

```fml-script
assert virtualModel.hasMetaData("Marker");
assert virtualModel.hasMetaData("Nope") == false;
card = virtualModel.getFlexoConcept("Card");
assert card.hasMetaData("Renderer");
```
