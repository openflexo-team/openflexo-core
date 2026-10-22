# 8. Statements

The body of a behaviour is a *block*: a sequence of statements that run in order. A statement may contain
others (a condition, a loop, a block). Statements that act on models and resources are *edition actions*
(chapter 10); they are statements by position and are described there.

## 8.1 Syntax

```ebnf
block            = "{" block_statement* "}"
block_statement  = type variable_declarator ";"                       (* local variable *)
                 | statement

statement        = block
                 | ";"
                 | statement_expression ";"
                 | "if" "(" expression ")" statement
                 | "if" "(" expression ")" statement_no_short_if "else" statement
                 | "while" "(" expression ")" statement
                 | "do" statement "while" "(" expression ")" ";"
                 | "for" "(" for_init? ";" expression? ";" statement_expression? ")" statement
                 | "for" "(" type lidentifier ":" ( expression | fml_action_exp ) ")" statement
                 | "return" ";"
                 | "return" ( expression | fml_action_exp ) ";"

for_init            = statement_expression | type variable_declarator
statement_expression = assignment | pre_increment | pre_decrement | post_increment | post_decrement
                     | method_invocation | new_instance | fml_action_exp
variable_declarator  = lidentifier | lidentifier "=" expression | lidentifier "=" fml_action_exp
```

`statement_no_short_if` is `statement` without an `if` lacking an `else`; it resolves the dangling `else` as in
Java: an `else` belongs to the nearest preceding `if`.

There is no `break`, no `continue`, no `switch`, no `try`/`catch`/`throw`, and no labelled statement. A
loop ends when its condition does, or with `return`.

## 8.2 Local variables

A local variable is declared with its type and optionally an initial value:

```fml-stmt
int total = 0;
String name;
List<Shelf> shelves = select Shelf from this;
```

The type is mandatory; there is no type inference in a declaration. A local variable lives in the block that
declares it, from its declaration to the closing brace, and hides a property of the same name: `this.name` still
reaches the property.

```fml-member
public int scoped() {
	int x = 0;
	if (true) {
		int y = 5;
		x = y;
	}
	return x;
}
```

A local variable that is not initialised holds `null`, as a property does (§4.2).

The initial value of a declaration may be an expression or an edition action (§10): a `select`, a `match`,
a technology action.

## 8.3 Assignment

```ebnf
assignment = left_hand_side assignment_operator ( expression | fml_action_exp )
left_hand_side = composite_ident | field_access
```

An assignment is a statement expression: `x = x + 1;`, `container.last = "done";`, `shape = DIAGRAM::AddShape(…);`.

The left-hand side is a dotted name or a field access. **An assignment to a name that is not a local variable
is an assignment to the property of that name** in the current scope (§9.6.3): `touched = touched + 1;`.
An assignment to a path (`other.value = 3;`) sets the property of the object the prefix designates. An
assignment to a property that is not settable (an expression property, a get-only property) is ignored without an
error (§6.8).

The assignment operators are `=`, `+=`, `-=`, `*=`, `/=`, `%=`, `<<=`, `>>=`, `>>>=`, `&=`, `|=`, `^=`.

> **Implementation note.** A compound assignment used as a **statement** ignores its operator: `n += 2;` on
> `n = 5` leaves `n` equal to `2`, because it is executed as `n = 2` (`CORE-D-7`). Write `n = n + 2;`. As an
> expression nested in another one, `+=` is supported by the expression grammar; avoid it.

## 8.4 Conditions

```fml-member
public String grade(int score) {
	if (parameters.score >= 90) {
		return "A";
	}
	else if (parameters.score >= 50) {
		return "B";
	}
	else {
		return "C";
	}
}
```

The condition is an expression of type boolean. It is evaluated once; the `then` branch runs when it is true and
the `else` branch, if any, when it is false.

The evaluation of a condition has a rule of its own:

| The condition… | …is taken as |
|---|---|
| evaluates to `true` / `false` | that value |
| evaluates to `null` | false |
| goes through a `null` value (null reference) | false |
| is not set, is **invalid** (an unresolved name), or raises any other error | **true** |

> **Implementation note.** The last row is a defect (`CORE-D-8`): an `if` whose condition does not resolve runs the
> `then` branch, and a `while` loops forever. Nothing is reported at run time; validate the unit (§13.8).

## 8.5 Loops

### while

```fml-member
public int sumWhile(int n) {
	int i = 0;
	int total = 0;
	while (i < parameters.n) {
		i = i + 1;
		total = total + i;
	}
	return total;
}
```

### do … while

Accepted by the grammar:

> **Implementation note.** A `do … while` executes its body **once** and drops the condition (`CORE-D-6`).
> Use `while`.

### for

```fml-member
public int sumFor(int n) {
	int total = 0;
	for (int i = 1; i <= parameters.n; i++) {
		total = total + i;
	}
	return total;
}
```

A classic `for` has an initialisation (a declaration or a statement expression), a condition and an update, each
optional in the grammar. The update is a single statement expression. `i++` is supported in the update.

> **Implementation note.** A `for` without a **condition** (`for (int i = 0; ; i++)`) is accepted by the
> grammar and silently not built: the behaviour that contains it disappears and a call to it is an unresolved
> binding (`CORE-D-9`). Always write the condition; to loop until a `return`, write `while (true)`.
> Also, after the first `;` of a `for` header the lexer is back in the `normal` state (§2.3.1).

### enhanced for

```fml-member
public String joinWords() {
	String all = "";
	for (String w : words) {
		all = all + w + ",";
	}
	return all;
}

public String chorus() {
	String all = "";
	for (Creature c : select Creature from this) {
		all = all + c.sound() + ";";
	}
	return all;
}
```

`for (T x : source)` iterates over the elements of a list; the source is an expression or an edition action
(typically a `select`). The loop variable is a new local of type `T`, bound to each element in turn. The elements
are visited in the order of the list; for a `select` this is the order of creation of the instances (§10.2).

## 8.6 Increment and decrement

`++` and `--` are accepted as statements and as expressions.

> **Implementation note.** `n++;`, `++n;`, `n--;` and `--n;` used as **statements** make the behaviour return
> `null` (`CORE-D-18`). They work in the update part of a `for`. Write `n = n + 1;`.

## 8.7 Expression statements

A method call, a `new`, or an edition action used for its effect is a statement:

```fml-stmt
words.add(parameters.word);
new Shelf("Fiction", 20);
log "text is " + parameters.text;
delete parameters.book;
fire new ShelfCreated(parameters.label);
```

The value of such a statement is discarded.

## 8.8 Return

`return;` ends the behaviour. `return expression;` ends it and gives the value. `return fml_action_exp;` returns the result of an edition action, as in
`return select unique Article from (this) where (selected.name == parameters.name);`.

A `return` inside a loop ends the behaviour:

```fml-member
public String firstLong(int size) {
	for (String w : words) {
		if (w.length() >= parameters.size) {
			return w;
		}
	}
	return null;
}
```

## 8.9 Blocks and sequencing

Statements of a block run in order; a block is a statement. The body of a behaviour is a block. Internally the
platform represents a body as a *control graph* of sequences, conditionals, iterations and actions, with
the assignment of every value to a variable or property being an edition action; the textual form above is the
only form this specification defines.
