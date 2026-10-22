# 9. Expressions

An expression computes a value. Expressions appear in conditions, assignments, arguments, `return`
statements, initial values, `values` clauses, `where` clauses, annotations, and — through the `binding` entry
point — in the data bindings of user interfaces and inspectors.

FML expressions are evaluated by **Connie**, the expression language of the platform. They look like Java
expressions and mostly behave like them; the differences are specified in this chapter and are not details:
division is decimal, `&&` and `||` do not short-circuit, strings are never converted to numbers, and a path
that does not resolve evaluates to `null` instead of failing.

## 9.1 Syntax

```ebnf
expression      = left_hand_side assignment_operator expression
                | conditional_exp

conditional_exp = conditional_or_exp
                | conditional_or_exp "?" expression ":" conditional_exp

conditional_or_exp  = conditional_and_exp { "||" conditional_and_exp }
conditional_and_exp = inclusive_or_exp   { "&&" inclusive_or_exp }
inclusive_or_exp    = exclusive_or_exp   { "|" exclusive_or_exp }
exclusive_or_exp    = and_exp            { "^" and_exp }
and_exp             = equality_exp       { "&" equality_exp }
equality_exp        = relational_exp     { ( "==" | "!=" ) relational_exp }
relational_exp      = shift_exp
                    | shift_exp ( "<" | ">" | "<=" | ">=" ) shift_exp
                    | shift_exp "instanceof" type dim*
shift_exp           = add_exp            { ( "<<" | ">>" | ">>>" ) add_exp }
add_exp             = mult_exp           { ( "+" | "-" ) mult_exp }
mult_exp            = unary_exp          { ( "*" | "/" | "%" ) unary_exp }

unary_exp = "++" unary_exp | "--" unary_exp | "+" unary_exp | "-" unary_exp
          | "~" unary_exp  | "!" unary_exp
          | "(" type dim* ")" unary_exp                          (* cast *)
          | postfix_exp
postfix_exp = primary | postfix_exp "++" | postfix_exp "--"

primary    = composite_ident | primary_no_id
primary_no_id = literal
              | "(" expression ")"
              | field_access
              | method_invocation
              | new_instance
```

## 9.2 Operators and precedence

From the tightest binding to the loosest:

| Level | Operators | Associativity |
|---|---|---|
| 1 | postfix `++` `--` | left |
| 2 | `!` `~` cast `(T)` | right |
| 3 | prefix `++` `--`, unary `+` `-` | right |
| 4 | `*` `/` `%` | left |
| 5 | `+` `-` | left |
| 6 | `<<` `>>` `>>>` | left |
| 7 | `<` `>` `<=` `>=` `instanceof` | none |
| 8 | `==` `!=` | left |
| 9 | `&` | left |
| 10 | `^` | left |
| 11 | `\|` | left |
| 12 | `&&` | left |
| 13 | `\|\|` | left |
| 14 | `? :` | right |
| 15 | `=` `+=` `-=` `*=` `/=` `%=` `<<=` `>>=` `>>>=` `&=` `^=` `\|=` | right |

The relational operators are **non-associative**: `a < b < c` does not parse. Parentheses group:

```fml-expr
2 + 3 * 4
```

```fml-expr
(2 + 3) * 4
```

The conditional operator may be nested in either branch:

```fml-expr
n > 0 ? "pos" : (n < 0 ? "neg" : "zero")
```

The operators supported by the evaluator are the binary `&&` `||` `==` `!=` `<` `<=` `>` `>=` `+` `-` `*` `/`
`%` `<<` `>>` `>>>` `&` `|` `^` and `=`, `+=`, and the unary `!` `+` `-` `++` `--` `~`.

## 9.3 Boolean operators do not short-circuit

`&&` and `||` evaluate **both** operands, even when the first one decides the result.

```fml-member
public int andOperands() {
	touched = 0;
	if (false && this.touch() == 1) {
		touched = touched + 100;
	}
	return touched;
}
```

Here `touch()` is called although the left operand is `false`. Consequences:

* a guard such as `x != null && x.foo == 1` evaluates `x.foo` when `x` is null, which raises a null
  reference (§9.8);
* an expensive right-hand side is always paid for.

A guard must be written with nested `if`s, which are the only short-circuiting construct:

```fml-member
public boolean safe(Person p) {
	if (parameters.p == null) {
		return false;
	}
	return parameters.p.name == "Ann";
}
```

## 9.4 Literals and values

An expression works with integers, decimals, strings, characters, booleans, `null`, and the instances of
concepts (§2.6). The null literal designates the absence of a value (§13.6).

## 9.5 Numbers and strings

### 9.5.1 Arithmetic

* `+`, `-` and `*` between two integers give an integer. Between an integer and a decimal they give a decimal.
* **`/` and `%` give a decimal, even between integers**: `7 / 2` is `3.5`, `6 / 3` is `2.0`, `7 % 2` is `1.0`.
  The declared type of the behaviour does not change this.
* A **cast** `(int) d` truncates a decimal to an integer: `(int) 3.7` is `3`. The cast is the only way to
  obtain an integer from a division.

### 9.5.2 Strings

* `+` concatenates when its **left operand is a string**; a number, a boolean, an instance or `null` on the
  right is written as text (`"a" + 1` is `"a1"`, `"x" + null` is `"xnull"`). An instance is written by its string
  representation (the `@Renderer` of its concept, §11.4).
* `1 + "a"` is a type error and stops the behaviour.
* **A string is never a number.** `"4.5" * 1.0` is an error. To convert, call a Java method: import the class
  and use the simple name — `import java.lang.Double;` then `Double.parseDouble("4.5")`.
* `==` between two strings compares their **contents**, not their identity.
* The Java methods of a string are available on a variable, a parameter or a property
  (`s.toUpperCase()`, `s.replace("a", "b").length()`).

> **Implementation note.** A method called directly on a **string literal**, `"hello".length()`, evaluates to
> `null` without a message (`CORE-D-19`). Put the literal in a variable first.

### 9.5.3 Comparison

`==` and `!=` compare numbers by value, strings by contents, and instances of concepts by identity. `<`, `<=`,
`>`, `>=` compare numbers. Comparing a value with `null` is written `== null`.

An enum value is compared with `==` to another value of the same enum. In a script, an enum read as a
property must be compared through `toString()` (§12.7).

### 9.5.4 Lists

A Java list is created with `new ArrayList<Type>()` and used with its Java methods (`add`, `get`, `size`,
`contains`). Multi-valued properties (§6.3) are lists.

## 9.6 Paths

A *path* designates a value by following properties and calls: `guest.name`, `fiction.books.size`,
`this.container.label`, `shelf.newBook("x").title`.

### 9.6.1 Syntax

```ebnf
composite_ident = identifier_prefix* ( lidentifier | cidentifier | escaped_identifier | keyword5 )
identifier_prefix = lidentifier "." | escaped_identifier "."
keyword5 = "concept" | "create" | "get" | "model" | "set"

field_access = "super" "." lidentifier
             | composite_ident "." "super" "." lidentifier
             | primary_no_id "." ( lidentifier | keyword5 )

method_invocation = primary "(" argument_list? ")"
                  | "super" "(" argument_list? ")"
                  | type "." lidentifier "(" argument_list? ")"      (* static call *)
```

Three consequences of this syntax:

* every intermediate element of a dotted name is a `lidentifier` (property names start with a lowercase
  letter or underscore); the **last** element may also be a constant identifier or one of the five keywords of
  §2.3.2;
* a call is applied to a *primary*, so `a.b(1).c` and `a.f().g()` are paths; a call may follow a call;
* **a static call** is `Type.method(args)`, with the type in the typing space (§4.8) or fully qualified
  (`java.lang.Double.parseDouble(x)`). There is no syntax to read a static **field**.

`select …` is not an expression: `(select T from x).size` does not parse. Assign the result, or put it in a
behaviour (§10.2).

### 9.6.2 Resolution

A path is resolved step by step against types. The first element is looked up in the **binding model** in scope
(§9.6.3); each following element is looked up in the type of the previous one. The elements that can be
found, in the order in which they are tried for a concept instance, are

1. a property of the concept or of its ancestors;
2. a behaviour of the concept or of its ancestors (for a call);
3. a native property of the runtime object, such as `container` (the instance that contains it), and, for lists,
   `size`, `get(i)`, …

For an object of a Java type, the elements are the Java getters, fields and methods that the type exposes. For an
object of a technology-specific type, they are those the adapter exposes (§4.7).

A **concept instance exposes to a path only its properties and its behaviours** (plus the native properties):
Java methods of the underlying runtime object — `toString()`, `hashCode()` — are invalid bindings on it. To display
an instance, use string concatenation; to reach a platform method, bind a cast to a local variable first (see
below).

> **Implementation note.** `((FMLRTVirtualModelInstance) this).refreshModelSlot("x")` evaluates to `null`;
> the cast must be bound to a local: `FMLRTVirtualModelInstance vmi = (FMLRTVirtualModelInstance) this;
> vmi.refreshModelSlot("x");`. Validation reports the inline form as an unresolved path element.

### 9.6.3 The scope of a bare name

A name that begins a path is looked up outward, from the innermost scope:

1. the **local variables** of the enclosing blocks;
2. the **properties of the concept**, including inherited ones;
3. the **properties of the enclosing concept or model**, outward to the model.

The built-in names are always available:

| Name | Denotes | Available in |
|---|---|---|
| `this` | the current instance | every behaviour, expression property, invariant |
| `container` | the instance that contains `this` | every behaviour |
| `parameters` | the arguments of the running behaviour: `parameters.x` | every behaviour |
| `super` | the parent concept (§5.3.2) | behaviours of a concept with a parent |
| `evt` | the event being handled | a `listen` body |
| `selected` | the candidate under examination | a `where` clause of a `select` (§10.2) |
| `value` | the value being assigned | a `set` body (§6.7) |
| `instance` | the instance being rendered or inspected | a `@Renderer` expression (§11.4) |

The two forms to remember are

* **a parameter is read through `parameters`.** A bare parameter name resolves to nothing and yields `null`,
  without an error; this is the most frequent source of a missing value;
* **a behaviour of the same instance is called on `this`**: a bare call yields `null` too.

A local variable hides a property of the same name; `this.name` still reaches the property. The property of a
nested concept hides that of its container; `container.name` reaches the container's.

For the model `NameLookup`, whose `Item` has `name` and `label` and whose model has `owner` and `label`, in a
behaviour of `Item` named `Pen`:

| Expression | Value |
|---|---|
| `name` | `"Pen"`, the property of the concept |
| `this.name` | `"Pen"` |
| `owner` | `"Ann"`, the property of the model |
| `container.label` | the `label` of the model |
| `label` | the `label` of the item: the concept's property wins over the model's |

### 9.6.4 Calls

A call `target.name(args)` is resolved by name, number of arguments and types (§7.4). The call of a behaviour
of the same instance is `this.name(args)`. A call may be used as a value or as a statement.

An argument list is a comma-separated list of expressions.

## 9.7 Creating instances: `new`

```ebnf
new_instance = ( composite_ident "." )? "new" reference_type "(" argument_list? ")" with_clause?
             | ( composite_ident "." )? "new" uidentifier "::" lidentifier "(" argument_list? ")" with_clause?

with_clause  = "with" fml_parameters
```

`new` creates an instance of a concept, of a virtual model, or of a Java class, according to what the
type resolves to:

```fml-member
public Task addTask(String label) {
	Task task = new Task(parameters.label);
	tasks.add(task);
	return task;
}

public List<Task> fresh() {
	List<Task> l = new ArrayList<Task>();
	return l;
}
```

* **A concept** — the arguments are those of a creation scheme (§7.3.1). The second form, `new Shelf::named(args)`,
  names the creation scheme. Where the concept is nested in the one whose behaviour runs, the new instance is
  contained in the current instance. Otherwise the instance must be created through its container, written
  `container.new X(args)`, where the prefix `composite_ident.` designates the instance in which to create it.
* **A virtual model** — a new virtual model instance, which needs a `with (name = "…")`.
* **A Java class** — the Java constructor is called; the `with` clause is not used.

> **A bare `new` of a concept that is not nested in the current one gives `null`**, without an error. From a
> behaviour declared on the **model**, the enclosing context is the instance of the model, so a bare `new` of
> any concept of the model is correct.

The `with` clause supplies the parameters of the instantiation of a virtual model: `name` (mandatory; the name of
the instance), and, where relevant, `virtualModelInstanceTitle`, `uri`, `rc` (the resource center),
`path` and `folder`.

```fml-script
list = new TaskList() with (name = "groceries");
```

The arguments must match a creation scheme in number (§7.2). A concept that declares no creation scheme is created
with no argument.

## 9.8 Null

`null` belongs to every non-primitive type and is also the value of an unassigned primitive-typed property
(§4.2). Two different things are called "null" when an expression is evaluated, and they must not be confused:

* a path whose elements **do not resolve** (a misspelled name, a missing import, a bare parameter name) is an
  *invalid* binding, and evaluates to `null` without any message;
* a path that resolves but **goes through a `null` value** (`p.name` where `p` is `null`) raises a *null
  reference*. In an expression statement, an assignment or a `return`, it is an **execution error** that stops the
  behaviour. In the condition of an `if`, a null reference makes the condition **false**, and so does a `null`
  value (§8.4).

A comparison to `null` is written `== null` or `!= null`. Because `&&` does not short-circuit (§9.3), a null
guard must be a separate `if`.

## 9.9 Type tests and casts

`e instanceof T` tests whether the value of `e` is an instance of `T`; `T` may be a concept (instances of
sub-concepts satisfy it) or a Java type. `(T) e` is a cast: for a primitive target it converts the number
(`(int) 3.7`); for a reference target it asserts the type for the binding analysis, which is how the members of a
Java type are made available on an expression of a more general type. A cast may be followed by `[]` in the
grammar; arrays are otherwise not a type of FML (§4.4).

## 9.10 The `binding` entry point

An expression alone is an input of the grammar (§1.2). It is how the data bindings embedded in user-interface
components and inspectors are parsed. The same expression grammar applies, evaluated in the binding model of the
embedding component; the first element of a path is then a variable of that model.

## 9.11 Evaluation errors

| Situation | Result |
|---|---|
| a path element that does not resolve (invalid binding) | `null`, and the behaviour goes on |
| a path through a `null` value, in a value position | execution error: the behaviour stops |
| a path through a `null` value, in an `if` condition | the condition is false |
| a type error (`1 + "a"`, `"4.5" * 1.0`) | execution error: the behaviour stops |
| an invalid condition in `if` / `while` | taken as true (`CORE-D-8`) |

Validation (§13.8) reports unresolved paths; it is the only place where they surface before execution.
