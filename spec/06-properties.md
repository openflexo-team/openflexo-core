# 6. Properties

A *property* is something an instance of a concept knows. A property has a name, a type and optionally a
cardinality; it comes in several kinds that differ in where the value comes from.

## 6.1 Syntax and classification

```ebnf
property_decl =
    annotation* visibility? type cardinality? variable_declarator ";"               (* typed *)
  | annotation* visibility? type cardinality? lidentifier "with" role_ref fml_parameters? ";"   (* bound to a role *)
  | annotation* visibility? "abstract" type variable_declarator ";"                 (* abstract *)
  | annotation* visibility? type lidentifier "values" expression ";"                (* expression *)
  | annotation* visibility? type lidentifier "{" get_decl set_decl? "}" ";"         (* get/set *)

variable_declarator = lidentifier
                    | lidentifier "=" expression
                    | lidentifier "=" fml_action_exp

role_ref = uidentifier | cidentifier "::" uidentifier
get_decl = type "get" "(" ")" body
set_decl = "set" "(" type lidentifier ")" body
```

Every property has a name (a `lidentifier`), a visibility (§5.8) and may carry annotations (chapter 11).

A **typed property** is classified by its type, which selects the kind of property that is built:

| Type of the property | Kind built | Holds |
|---|---|---|
| a primitive type, `String`, `java.util.Date` | *primitive role* | a simple value |
| another Java type (`List<String>`, `Map`, …) | *Java role* | a Java object |
| a concept or event type (§4.3) | *concept-instance role* | an instance of that concept |
| a virtual model type | *model-slot property* | an instance of that virtual model |

The other forms are described below. The distinction matters for **cardinality** (§4.6): a property with a
multiple cardinality holds a list of values of its type.

## 6.2 Typed properties and initial values

```fml-member
int count;
boolean flag;
String text;
double ratio;
Person other;
```

**A property with no initial value holds `null`.** This applies to the primitive types as well: a new instance
has `null` for `count`, `flag` and `text`, not `0`, `false` or the empty string. A value must be given
explicitly if it must exist from the start.

An *initial value* is an expression after `=`; it gives the value an instance starts with:

```fml-member
int preset = 7;
String greeting = "hi";
List<String> tags = new ArrayList<String>();
int titi = 2 + 3;
```

The initializer may also be an edition action (§10): `Person first = select Person from this;`.

A typed property is settable: `p = v;` in a behaviour, or `instance.p = v;` in a script, replaces its value
(§6.8).

## 6.3 Multi-valued properties

A cardinality after the type makes the property multi-valued:

```fml-member
Book[0,*] books;
String... labels;
String[1,*] atLeastOne;
```

A multi-valued property is **a list from the start**, empty until values are added. Its value is a Java list,
manipulated with its methods:

```fml-stmt
books.add(book);
books.remove(book);
int n = books.size;
Book first = books.get(0);
boolean has = books.contains(book);
```

A value is added with `add`, one at a time. Two idioms of a single-valued property are **wrong** on a
multi-valued one:

* assigning a collection (`books = someList;`) is a type error on an empty role and a no-op on a role that
  already equals it;
* `books.clear()` has no effect.

The re-runnable way to fill a multi-valued property is to guard each addition:
`if (!books.contains(b)) { books.add(b); }`.

## 6.4 Expression properties

```ebnf
expression_property = type lidentifier "values" expression ";"
```

An *expression property* has no storage: its value is the value of the expression, and follows the properties
that the expression reads. It is read-only.

```fml-member
Book[0,*] books;
int bookCount values books.size;
String status values done ? "done" : "open";
```

The expression is evaluated in the context of the instance (`this`, the properties of the concept, the
container). It is lexed in the `binding` state (§2.3.1), so that the names of properties that coincide with
keywords of `normal` can be written in it.

> **Implementation note.** An expression property that has been read once may keep its earlier value after the
> property it reads changes (`CORE-D-11`): reading `box.itemCount` (2), removing an item, and reading again can
> still give 2. In a behaviour, read the underlying value directly (`items.size`); when asserting a derived
> value after a change, also assert the underlying value.

An expression property is the concise form of a get-only property (§6.6) and is preferred to it unless the body
needs statements.

## 6.5 Properties bound to a role

```fml-member
Shape shape with DIAGRAM::ShapeRole(container = diagram);
ASubConcept[0,*] subConcepts with ConceptInstance(container = this);
Row[0,*] rows with XLS::RowRole(sheet = sheet);
```

A property declared with `with` is **bound to an element of a resource** or to instances of a concept, instead
of holding a value of its own. The role is named by a `uidentifier` resolved among the roles offered by the
technology adapters declared with `use` (§3.4), or by the fully qualified form `TA::Role`, where `TA` is the
alias of a `use`. Its parameters are the *qualified arguments* of §10.10.1.

If the role cannot be found, the semantic issue `role_not_found: Name` is reported on the name.

Two families of role exist.

* A **role** binds the property to an object of a resource (a cell, an XML element, a diagram shape, an EMF
  class) or to an instance of a concept (`ConceptInstance`). Its parameters tell how the object is
  found or created: the container, the type, the name…
* A **model slot** binds the property to a *whole resource* of a technology (a spreadsheet, an XML document,
  a diagram): the property gives access to the resource and is the root of the paths that go through it.

Both are declared the same way. The platform distinguishes them by the class of the role. The role and
adapter names in the example above (`DIAGRAM::ShapeRole`, `XLS::RowRole`) are illustrative: the roles that exist
are those of the adapters installed, and `ConceptInstance` is the one that the platform itself provides.

What each adapter offers is described by that adapter; this specification fixes the syntax and the
resolution rules only.

## 6.6 Abstract properties

```fml
public model Publications {

	public abstract concept Publication {
		abstract String code;
	}

	public concept Book extends Publication {
		String title;
		String code values "B-" + title;
	}
}
```

An abstract property is declared by a concept and **given its value by the concepts that extend it**, usually
by redeclaring it as an expression property (§6.4), a get/set property (§6.7) or a stored property. The
declaration carries no initializer: `abstract` type name `;`.

## 6.7 Get/set properties

```fml-member
String displayName {
	String get() {
		return "Shelf " + label;
	}
	set(String value) {
		label = value;
	}
};
```

A property defined by a `get` behaviour and optionally a `set` behaviour. Reading it calls `get()`, assigning
it calls `set` with the assigned value. The declaration is **terminated by `;` after the closing brace**.

Without a `set`, the property is read-only: an assignment to it is ignored and no error is reported.

```fml-member
String fixed {
	String get() {
		return "constant";
	}
};
```

> **Implementation note.** In the `set` block the value is always named `value`. The name declared in the
> parameter list is ignored: `set(String aName) { a = aName; }` leaves `a` null and logs *BindingVariable aName does
> not exist* (`CORE-D-3`). Declare the parameter as `value`.

`get` and `set` are keywords of `normal`, so a property cannot be named `get` or `set`.

## 6.8 Reading and writing

A property is read by its name inside the concept (§9.6.3), `this.name`, or from outside by a path
`instance.name`. It is written by assignment (§8.3, §9.2) when it is settable, which holds for typed
properties, role-bound properties, and get/set properties with a `set`; it does not hold for expression
properties and for get-only properties.

Assigning a value equal to the current one does nothing (no notification is raised). Assigning to a property
notifies the listeners of the instance, which is how user interfaces and expression properties are kept
current (§13.7).

Visibility does not restrict a script: it can read and write a `private` property.

## 6.9 Names and scope

A property declared in a model is visible in the behaviours of the model and, by the name resolution of §9.6.3,
in the behaviours of the concepts nested in it, unless a property of the same name is declared closer. A
property of a nested concept hides the property of its container of the same name; the container's remains
reachable as `container.name`.
