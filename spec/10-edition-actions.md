# 10. Edition actions

Besides control structures and assignments, a behaviour body holds *edition actions*: instructions that
create, find, change, delete or signal objects, and that act on resources. The platform's name for them is
*edition actions*; most have a keyword of their own, and they are **grammar constructs**, never method
calls.

## 10.1 Syntax

```ebnf
fml_action_exp =
    cidentifier "::" uidentifier fml_parameters in_clause? from_clause?      (* technology action *)
  | "delete" expression from_clause?
  | "select" "unique"? select_type with_clause? from_clause where_clause?
  | "connect" left_hand_side "using" expression
  | "log" expression
  | "fire" expression
  | "notify" expression in_clause?
  | "begin" "match" uidentifier from_clause where_clause?
  | "match" uidentifier in_clause? from_clause qualified_where_clause? "unmatched" ":" new_instance
  | "end" "match" uidentifier in_clause "unmatched" ":" end_match_clause
  | "(" fml_action_exp ")"

in_clause    = "in" expression
from_clause  = "from" expression
where_clause = "where" "(" argument_list? ")"
qualified_where_clause = "where" "(" qualified_argument_list? ")"
select_type  = reference_type | technology_specific_type
with_clause  = "with" uidentifier fml_parameters
end_match_clause = "delete" "(" argument_list? ")" | method_invocation
```

An edition action is not an expression: it is accepted where the grammar says `fml_action_exp`, which is

* as a statement (§8.7);
* on the right-hand side of an assignment, of a declaration's initial value, of a property's initial value;
* after `return`;
* as the source of an enhanced `for`.

It is **not** accepted as an operand: `(select T from x).size` does not parse. Assign the result to a variable, or
return it from a behaviour.

`new` (§9.7) is an expression but acts like an action; it is covered in chapter 9.

## 10.2 select

```fml-member
public List<Article> allArticles() {
	return select Article from this;
}

public List<Article> ofKind(String kind) {
	return select Article from (this) where (selected.kind == parameters.kind);
}

public Article named(String name) {
	return select unique Article from (this) where (selected.name == parameters.name);
}
```

`select` finds instances. Its parts:

* the **type** to look for: a concept, a virtual model, an `Model$Concept` nested name (§4.3), or a
  technology-specific type (§4.7);
* `from` and the **source**, an expression that designates where to start: an instance of a concept or of a
  model, or a resource or an element of one for a technology type. The parentheses are optional (`from this`,
  `from (this)`) and only needed for readability when a `where` follows;
* an optional `where (…)` with the conditions;
* an optional `unique`;
* an optional `with Kind(parameters)`, the parameters of the fetch request for technology types.

### 10.2.1 The result

* Without `unique`, the result is a **list**, possibly empty, of the instances of the type and of the types that
  extend it, **in the order of their creation**. A deleted instance is not found.
* With `unique`, the result is **one instance or `null`**. When several instances match, one of them is returned
  without an error; the language does not say which, and a program that needs exactly one must select the list
  and check its size.

### 10.2.2 The source

The search covers what the source **contains**, nested instances included. From a model, `select Part from this`
finds the parts of every crate; from one crate, the parts of that crate only. To fetch an instance of a model,
`select MyModel from container`; to fetch a concept of another model, `select AnOtherModel$AConcept from
instance`.

### 10.2.3 Conditions

In the `where` clause, the candidate under examination is `selected`. A condition is a boolean expression; the
clause is an *argument list*, so several conditions may be listed separated by commas, which means **and**, or
combined with `&&` and `||`:

```fml-member
public List<Article> a(String kind, int quantity) {
	return select Article from (this) where (selected.kind == parameters.kind, selected.quantity >= parameters.quantity);
}

public List<Article> b() {
	return select Article from (this) where (selected.kind == "fruit" || selected.kind == "vegetable");
}
```

A `where` may read a derived (expression) property: `where (selected.isEntry)`.

### 10.2.4 Where `select` may be written

`select` is an action of a **behaviour body**, and a keyword of `normal` and `binding`. It is not a script
statement: in a script it is an ordinary identifier (§12.8), and `select T from vmi;` on its own does not
parse. To check what a query finds from a script, call a behaviour that returns it.

## 10.3 new

See §9.7. A creation uses `new Concept(args)` for a concept, `new Concept::name(args)` for a named creation scheme,
`container.new Concept(args)` to create through a container, and `new Model() with (name = …)` for a virtual model.

## 10.4 delete

```fml-stmt
delete parameters.note;
delete shape from diagram;
```

`delete e` deletes the object the expression designates. For an instance of a concept, the deletion scheme of the
concept runs (§7.3.2). `delete e from c` deletes the object from the container `c`; it is the form used for
technology objects that are removed from a resource.

Deleting an instance does not remove it from the multi-valued properties that hold it (`CORE-D-10`):

```fml-member
public removeBook(Book book) {
	books.remove(parameters.book);
	delete parameters.book;
}
```

## 10.5 log

`log expression` writes the value of the expression, as a string, to the log of the virtual machine:

```fml-stmt
log "text is " + parameters.text;
log("Hello" + "world!");
```

`log` is a keyword that moves the lexer to the `binding` state until the `;` (§2.3.1), so the expression
is lexed there. The parenthesised form is the same action with a parenthesised expression.

## 10.6 fire

```fml-stmt
fire new ShelfCreated(parameters.label);
```

`fire e` raises the event that the expression `e` designates; `e` must be an instance of an event (§5.5). The event
is raised on the **virtual model instance in which the behaviour runs** (its *source*). It is delivered to every
`listen` behaviour (§7.7) of a live instance that listens to that source and whose event type is the type of the
event or one of its ancestors.

Delivery is **synchronous**: the listeners run in the thread that fires, one after the other, and `fire`
returns when they have all completed.

## 10.7 notify

`notify "name"` raises a property-change notification for the property called `name`, on the current instance
or, with `in`, on the object the expression designates:

```fml-stmt
notify "total";
notify "total" in other;
```

`notify` is how a program tells the listeners of an object (user interfaces, expression properties) that a value
changed without being assigned through the platform. The expression for the name is a string; the `in` object
must support property-change listening.

## 10.8 connect

```fml-stmt
connect spreadsheet using resource;
```

`connect m using r` binds the model-slot property `m` to the resource `r`: the data of the resource becomes the
value of the property, so that paths that go through `m` reach the content of the resource. The left-hand side is a
property; the expression after `using` designates a resource, usually obtained through a `load` or a technology
action. It is the action of the platform's resource model, used by technology adapters.

## 10.9 Matching: begin match, match, end match

A federation often has to keep a set of instances in step with something else: one badge for each member of a
team, one concept instance for each row of a spreadsheet, one graphical element for each class of a diagram.
Written with `select` and `new`, such a behaviour would create the same instances at every run. The *match* family
makes it repeatable: it reuses the instance that already corresponds to a source, creates the ones that are
missing, and removes the ones whose source is gone.

Three instructions cooperate around an explicit **matching set**:

1. `begin match C from e` opens a matching set, filled with the instances of `C` that exist already.
2. `match C in set from e where (p = v, …) unmatched: new …` looks in the set for an instance of `C` whose
   properties `p` equal `v`. If there is one, it is **used** and taken out of the pool of the unmatched; if not, the
   `unmatched:` clause **creates** it. The instruction gives the matched instance, found or created.
3. `end match C in set unmatched: delete()` handles what is left in the pool, the instances that no source claimed,
   and deletes them.

```fml-member
public int syncBadges() {
	MatchingSet<Badge> badges = begin match Badge from this;
	for (Member m : select Member from this) {
		Badge b = match Badge in badges from this where (member = m) unmatched: new Badge(m);
		b.text = m.name;
	}
	end match Badge in badges unmatched: delete();
	List<Badge> all = select Badge from this;
	return all.size;
}
```

The first run creates one badge for each member; a second run creates nothing and finds the very same badges, which
the behaviour can bring up to date; a source that disappears takes its badge with it; a new source gets one.

### 10.9.1 The `where` of a match

The `where` of a `match` is **not** that of a `select`. It states the properties of the instance to find as
*qualified arguments*, `property = value`: the left side is a property of the concept matched, the right side a
value. There is no `selected`. A criterion whose value is `null` is ignored.

An instance is matched **at most once** per matching set: matching the same source twice in one set creates a
second instance.

### 10.9.2 `unmatched`

* In a `match`, `unmatched:` is followed by a `new` expression: `new Badge(m)` or the named form
  `new Badge::init(m)`. It is executed when no instance in the set satisfies the criteria.
* In an `end match`, `unmatched:` is followed by `delete()`.

`MatchingSet<T>` is a language type (§4.8); the set is typically held in a local variable. `set` is a keyword:
do not name the variable `set`.

### 10.9.3 Several concepts in one behaviour

Open **one matching set per concept**, at the start, and close each at the end:

```fml-member
public int syncBoth() {
	MatchingSet<Badge> badges = begin match Badge from this;
	MatchingSet<SquadTag> tags = begin match SquadTag from this;
	for (Member m : select Member from this) {
		match Badge in badges from this where (member = m) unmatched: new Badge(m);
	}
	for (Squad s : select Squad from this) {
		match SquadTag in tags from this where (squad = s) unmatched: new SquadTag(s);
	}
	end match Badge in badges unmatched: delete();
	end match SquadTag in tags unmatched: delete();
	return 0;
}
```

### 10.9.4 The standalone `match`

A `match` written without `in set` uses an **implicit matching set** that belongs to the behaviour execution. It
creates what is missing, and at the end of the behaviour it **deletes** what it did not match: its effect is
"synchronise", not "create if absent".

> **Implementation note.** The implicit set is typed by the first `match` executed in the behaviour. A second concept
> matched in the same behaviour never finds an instance and is duplicated at every run (`CORE-D-1`). Use one explicit
> set per concept.

### 10.9.5 Known limits

Each gives a wrong result without any message; the pattern of §10.9 avoids all of them.

* **The `where` of `begin match` is ignored** (`CORE-D-12`): the set holds every instance of the concept, and
  an `end match … unmatched: delete()` then deletes those the condition meant to leave out. Filter in the `match`
  instructions, by choosing what to iterate over.
* **`unmatched:` of an `end match` must be `delete()`.** The grammar also accepts a call
  `unmatched: someBehaviour()`, but that behaviour is not called: the first behaviour that the concept declares
  runs on each leftover instance, which may be a deletion scheme (`CORE-D-2`).
* **`delete()` runs the first deletion scheme the concept declares.** A concept closed by an `end match` must
  declare exactly one deletion scheme, anonymous, whose body only releases its roles; otherwise the closing may
  write back destructively into the federated data.
* An explicit matching set that is never closed by an `end match` is never finalised: omitting the `end match`
  is how to keep a matching set from pruning.

## 10.10 Technology actions

```fml-stmt
shape = DIAGRAM::AddShape(container = diagram) in diagram;
connector = DIAGRAM::AddConnector(fromShape = a, toShape = b) in diagram;
```

An action contributed by a technology adapter is written `ALIAS::Action(arguments)`, where `ALIAS` is the alias
declared by `use` (§3.4) and `Action` the name of the action in the adapter. It is an action of the resource
the adapter manages. The optional `in` clause names the object in which the action is performed, and `from` the
object it starts from.

### 10.10.1 Qualified arguments

```ebnf
fml_parameters         = "(" qualified_argument_list? ")"
qualified_argument     = lidentifier "=" expression                          (* a value *)
                       | lidentifier "=" reference_type                      (* a type *)
                       | lidentifier "=" "$" ( lidentifier | uidentifier )   (* an enum value *)
                       | lidentifier "=" qualified_instance                  (* a parameter object *)
                       | lidentifier "=" "{" qualified_instance ( "," qualified_instance )* "}"   (* a list *)
qualified_instance     = uidentifier ":" fml_parameters
                       | cidentifier "::" uidentifier ":" fml_parameters
```

Arguments are named: each is `name = value`, and the names are those of the action's parameters in the adapter.
A value can be an expression, a type, an enum value written `$NAME`, an object built from nested parameters
(`Type: (…)`), or a list of such objects.

The same form is used for role parameters (§6.5), for behaviour kinds (§7.8), for `select … with` and for the
precise parameters of `new … with (…)`.

### 10.10.2 Resolution

If the adapter cannot be found, the action is not built; the unit keeps its text and the problem is reported
(*unresolved technology-specific action*). A technology used only through actions must still be declared with
`use`.

## 10.11 Assignment and declaration with an action

An action is assignable: `x = select …;`, `Type v = ALIAS::Action(…);`, `Badge b = match …;`. The type of the
variable must accept the type the action produces: a `List` for a `select`, one instance for a `select unique`
or a `match`.
