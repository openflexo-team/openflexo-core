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
                 | "throw" expression ";"
                 | "try" block catch_clause+
                 | "try" block catch_clause* "finally" block

catch_clause        = "catch" "(" reference_type lidentifier ")" block

for_init            = statement_expression | type variable_declarator
statement_expression = assignment | pre_increment | pre_decrement | post_increment | post_decrement
                     | method_invocation | new_instance | fml_action_exp
variable_declarator  = lidentifier | lidentifier "=" expression | lidentifier "=" fml_action_exp
```

`statement_no_short_if` is `statement` without an `if` lacking an `else`; it resolves the dangling `else` as in
Java: an `else` belongs to the nearest preceding `if`.

A statement may be written without braces wherever the grammar accepts a statement, including inside the *then*
arm of an `if/else`. The empty statement `;` does nothing. A statement that cannot be built into the control graph
is reported as a semantic issue when the unit is parsed.

> **Implementation note.** A body written without braces is printed back with braces, and a broken indentation,
> when the unit is saved from the FML editor (`CORE-D-56`); the meaning is kept.

There is no `break`, no `continue`, no `switch`, and no labelled statement. A loop ends when its condition does,
with `return`, or with an error (§8.10).

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


A local cannot have the name of a local visible where it is declared — a local of an enclosing block, an earlier
local of the same block, the variable of an enclosing loop — as in Java; validation reports such a redeclaration.
Two sibling blocks, or two loops one after the other, may each declare a local of the same name. After the closing
brace of its block, the name of a local is again the property of the same name, if any; the same holds for the
variable of a loop after the loop.

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
A compound assignment `x op= v` behaves as in Java: `x` is read, then `v` is evaluated, and `x op v`, computed with
the operators of §9.5, is converted to the type of `x` before it is assigned. `n += 2;` on `n = 5` gives 7;
`n /= 2;` on an `int` `n = 5` gives 2 (where `n = n / 2` gives 2.5, §9.5.1); `s += 1;` on `s = "a"` gives `"a1"`,
and on a `null` string `s += "b";` gives `"nullb"`. Prefer the statement form; as an expression nested in another
one, only `=` and `+=` are supported by the expression grammar.

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
| evaluates to `null`, or goes through a `null` value (null reference) | an **execution error**: the behaviour stops |
| is not set, is **invalid** (an unresolved name), or raises any other error | an **execution error**: the behaviour stops |

A null check guards the rest of a condition, since `&&` and `||` short-circuit (§9.3): `if (p != null && p.age > 18)`.
Before 3.0 (`CORE-F-11`), a `null` condition, and a null reference in a condition, were taken as false.

The same rule holds for the condition of `while`, of `do … while` and of a classic `for` (a `for` without condition
apart, §8.5).

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

The body runs once, then again as long as the condition, evaluated after each run, is true:

```fml-member
public int doWhileCount() {
	int n = 0;
	do {
		n = n + 1;
	} while (n < 3);
	return n;
}
```

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

A classic `for` has an initialisation, a condition and an update. The initialisation must declare and initialize
the loop variable (`int i = 0`); a statement expression there (`i = 0`) is accepted by the grammar but reported as a
semantic issue. The condition is optional: without it the loop runs until a `return`, as in Java
(`for (int i = 0; ; i++)`). The update is optional too, and is a single statement expression (`i++`, `i += 2`).

> After the first `;` of a `for` header the lexer is back in the `normal` state (§2.3.1).

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

`++` and `--` are accepted as statements and as expressions: `n++;` adds 1 to `n`, `n--;` removes 1.

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

## 8.10 Exceptions: throw, try, catch, finally

A runtime error of FML (§13.6) and an exception defined in FML (§5.6) can be raised and handled by the behaviour that
expects them.

### throw

`throw expression;` raises the value of the expression, which is either a new instance of an exception
(`throw new OutOfStock(parameters.n, quantity);`) or an error caught by an enclosing `catch` clause (`throw e;`). Thrown
again from the block of the clause that caught it, an error is the same error: it keeps its FML stack, which still names
the statement where it was first raised. An exception kept and thrown again elsewhere is a new error, with its own stack. Throwing `null` is a null reference error; the validation reports a `throw` whose value is neither an exception
nor an error type.

A body that ends with `throw` gives no value, whatever the type declared by its behaviour.

### try, catch, finally

```fml-member
public int tryToTake(int n) {
	try {
		this.take(parameters.n);
		return parameters.n;
	} catch (OutOfStock e) {
		return 0 - e.requested;
	} catch (FMLError e) {
		log e.kind + ": " + e.message;
		return -1;
	} finally {
		finallyCount = finallyCount + 1;
	}
}
```

The block of the `try` runs. An error raised by it, by the behaviours it calls included, is given to the **first**
`catch` clause that catches it; the following clauses are not tried. A clause names:

* an exception defined in FML: it catches that exception and its sub exceptions; its variable is the exception instance,
  with its properties;
* `FMLError` or one of its subtypes (table below), named without import: it catches the errors of that type, an
  exception defined in FML included for `FMLError`; its variable is the error.

| Type | Caught errors |
|---|---|
| `FMLError` | every foreseen error, and every exception defined in FML |
| `FMLNullReferenceError` | a `null` in a path, a `null` condition, a `for` over `null` |
| `FMLTypeError` | a type mismatch, an invalid cast, an argument of the wrong type |
| `FMLResolutionError` | a name that does not resolve at run time |
| `FMLAssignmentError` | a target that cannot be assigned |
| `FMLArgumentError` | a missing or invalid argument of a behaviour |
| `FMLCardinalityError` | a `select unique` with several results, a required role without value |
| `FMLTechnologyError` | a foreseen failure of a technology (a missing sheet, a text that is not B, …) |
| `FMLHostError` | a failure of Java code called from FML |
| `FMLUserError` | every exception defined in FML |

Every caught error gives `message`, `kind` (in English: `Null reference`, `Type error`, … or the name of the exception)
and `fmlStackTrace`. The variable of a clause is visible in its block only, and must not have the name of a visible
local variable.

An error that no clause catches goes on to the caller. A defect of the platform (`FMLInternalError`) and a cancellation
by the user (`FMLCancellation`) are never caught, not even by `catch (FMLError e)`: the validation reports a clause that
names them. A `return` crosses a `try`: it is not an error. A clause placed after one that catches all it would catch
is never run: the validation warns about it.

The `finally` block runs on every exit of the `try`: at its normal end, after a clause, when an error goes on, and on a
`return`. An error or a `return` of the `finally` block itself replaces the one going on.

A caught error is *handled*: it is not reported (no record in the FML console, no dialog), even when it is the failure of
a behaviour called by the block. What ran before the error is kept: there is no transaction (§13.6).

> **Implementation note.** In a getter, a setter or an expression property read outside a behaviour (by the user interface,
> by an fml-cli expression), the failure of a behaviour called by a `try` block is reported before the `catch` clause
> handles it (`CORE-D-85`).

