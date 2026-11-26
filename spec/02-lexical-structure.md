# 2. Lexical structure

The input of the parser is a sequence of Unicode characters (code points up to `U+FFFF`). The lexer groups
them into *tokens*, discarding white space and comments. Unlike the lexer of most languages, the FML lexer
has **states**: the same characters can be a keyword in one state and an identifier in another. Section 2.3
is the most important one of this chapter.

## 2.1 White space and comments

White space is any sequence of space, horizontal tab, form feed, and line terminators. A *line terminator*
is LF, CR, or CR LF. White space separates tokens and is otherwise ignored.

Three kinds of comment are ignored wherever white space is:

```ebnf
traditional_comment    = "/*" … "*/"      (at least one character between the delimiters)
documentation_comment  = "/**" … "*/"
end_of_line_comment    = "//" { any character but a line terminator } [ line terminator ]
```

Comments do not nest. A documentation comment (`/** … */`) is lexically a comment like the others: the
grammar gives it no place of its own, and it is not part of any declaration.

> **Implementation note.** The lexer recognises `/**/` as an (empty) documentation comment.

```fml
/**
 * A documentation comment.
 */
model Commented {
	// a line comment
	int count; /* a block comment */
}
```

## 2.2 Input elements

After removal of white space and comments the input is a sequence of tokens of the following kinds:
identifiers, keywords, literals, operators, separators, and — in the `command` and `script` states only —
directive tokens (§2.5).

Tokens are recognised by the **longest match**. When two token definitions match the same longest prefix,
the one that is declared first in `fml.sablecc` wins. Two consequences are used throughout this
specification:

* a keyword beats an identifier of the same spelling (keywords are declared before identifiers);
* an integer literal beats an identifier made of digits (§2.6).

## 2.3 Lexical states

The lexer is in one of four states: `normal`, `binding`, `command`, `script`. The entry point of the parse
chooses the initial state (§1.2). A token definition lists the states in which it exists; a token that
lists none exists in all four. A word that is a keyword only in other states is lexed, in the current
state, as an ordinary identifier.

### 2.3.1 State transitions

Only a few tokens change the state:

| Token | In state | Moves to |
|---|---|---|
| `import`, `use`, `values`, `log` | `normal` | `binding` |
| `(` | `normal` | `binding` |
| `;` | `binding` | `normal` |
| `)` | `binding` | `normal` |

The `script` and `command` states are never left. In `binding`, `(` and `)` do not nest: **the first `)`
returns the lexer to `normal`**.

The practical effect is the following. In `normal` the whole keyword set of FML is reserved. The text of
an *expression context* — what follows `import`, `use`, `values` or `log` up to the `;`, and what is
written between parentheses — is lexed in `binding`, where only a small set of keywords is reserved
(§2.5), so that the names of properties and behaviours that coincide with the other keywords (`end`,
`delete`, `action`, `default`…) can be written there. A *declaration* is in `normal`, so it cannot use
such a name:

```fml-invalid
model Names {
	int end;
}
```

A reference to a property from inside an expression context is lexed in `binding`; a property whose name
is a keyword of `normal` can therefore only be reached there, which is why the practice is to avoid such
names altogether. The following is accepted:

```fml
model Names {
	int total;
	int doubled values total * 2;
	boolean done values total > 0;
}
```

> **Implementation note.** Because `)` leaves `binding` unconditionally, a keyword that is not reserved
> in `binding` is nevertheless reserved again after the first `)` of an expression context:
> in `if ((a) == end)` the word `end` is the keyword (the text does not parse), while in
> `if (a == end)` it is an identifier. Avoid names that are keywords of `normal` in expression contexts
> that contain nested parentheses.

### 2.3.2 Keywords usable as property names

The grammar admits five keywords as the last element of a dotted name in `normal`
(`authorized_kw_in_composite_ident`): `concept`, `create`, `get`, `model`, `set`. They can follow a dot
(`x.model`) and name a left-hand side or a primary; no other keyword can.

## 2.4 Identifiers

One token covers names.

```ebnf
ident = ( 'a'..'z' | '_' | 'A'..'Z' ) letter_or_digit*
```

where `letter_or_digit` is a Unicode letter, a Unicode decimal digit, `$` or `_`, and a Unicode letter is
one of the ranges enumerated in the `unicode_letter` helper of the grammar (Latin, Greek, Cyrillic,
Hebrew, Arabic, the Indic scripts, Thai, Hangul, CJK and others). Only the first character is restricted
to ASCII.

The case of a name carries no meaning for the grammar: `Book`, `book`, `BOOK`, `A` and `_tmp` are all
identifiers, and any of them may name a model, a concept, a property, a variable, an enum value or a
technology-adapter alias. Upper-initial names for models, concepts, events, enums and types,
lower-initial names for properties, behaviours and variables, and capitals for enum values and
technology-adapter aliases are conventions, followed by the examples of this specification.

Where a type and a value can both be written, the grammar parses a name and the semantic analysis
decides what it is (§4.8):

* `Foo.bar(x)` is a static call when `Foo` names a type and is not a variable in scope, and a call on a
  value otherwise (§9.6.4);
* the type of a cast `(Foo) x` is a parenthesed expression that must be a (qualified) name of a type (§9.9);
* the value of a qualified argument `x = Foo` is a type when the parameter takes a type (§10.10.1).

The dollar sign may appear inside an identifier after its first character, and as a separator in
the *nested type name* `Model$Concept` (§4.3).

### 2.4.1 Escaped identifiers

A name that cannot be written as an identifier is written `{"text"}`: a left brace, a string literal and a
right brace, read as one token (blanks may surround the literal; a comment or a line break may not). The grammar accepts it as a *prefix* of a dotted name (`{"a b"}.x`) and as the last element
of a dotted name used as a left-hand side or a primary. It is meant for names that are not valid
identifiers, for instance a name that comes from an external resource and contains a space.

## 2.5 Keywords

### 2.5.1 Language keywords

Reserved in `normal` unless stated otherwise.

| Group | Keywords |
|---|---|
| Declarations | `model` `concept` `event` `exception` `enum` `typedef` `namespace` `use` `import` |
| Modifiers | `abstract` `public` `protected` `private` `required` |
| Structure | `extends` `as` `with` `from` `values` |
| Properties and behaviours | `create` `delete` `get` `set` `listen` `super` `throws` |
| Statements | `if` `else` `for` `while` `do` `return` `throw` `try` `catch` `finally` |
| Actions | `select` `unique` `where` `in` `match` `begin` `end` `unmatched` `new` `fire` `notify` `connect` `using` `log` |
| Invariants | `assert` `onfailure` |
| Reserved, unused | `action` `default` `then` |

The primitive-type keywords `boolean`, `char`, `short`, `int`, `long`, `byte`, `float`, `double` and `void`
exist in all states. So do the literals `true`, `false` and `null`. `String`, `Date` and the other Java
types are not keywords; they are identifiers resolved through the typing space (§4.8).

### 2.5.2 Where each keyword is reserved

Keywords are not reserved in every state. The set reserved in `binding`, as well as `new` and `with`, is
the following; **every other language keyword is an ordinary identifier in `binding`.**

| Reserved in `normal` and `binding` | Reserved in all four states |
|---|---|
| `as` `begin` `extends` `from` `instanceof` `match` `required` `select` `unique` `where` | `new` `with` |

Additionally `assert` and `return` are reserved in `normal`, `command` and `script` (not in `binding`); `log` in
`normal`, `command` and `script` (and it moves `normal` to `binding`, §2.3.1); `import`, `use` and
`values` are reserved in `normal` only.

This asymmetry has one visible consequence for authors: in a `script`, `select` is not a keyword
(§12.8), while in an expression context of a model it is.

### 2.5.3 Directive tokens

The `command` and `script` states reserve the names of the shell directives (§12.2):

`help` `history` `execute` `pwd` `ls` `cd` `services` `service` `activate` `resources` `open` `load` `more`
`enter` `exit` `quit` `context`

and the options `-rc` `-r` `-d` `-f`. These words are ordinary identifiers in `normal` and `binding`; in a
script they cannot be used as variable names, nor as the name of a behaviour called from the script.

## 2.6 Literals

```ebnf
literal = integer | floating_point | "true" | "false" | character | string | "null"
```

### Integers

```ebnf
lit_integer = ( decimal_numeral | hex_numeral | octal_numeral ) ( "l" | "L" )?
decimal_numeral = "0" | '1'..'9' digit*
hex_numeral     = "0" ( "x" | "X" ) hex_digit+
octal_numeral   = "0" octal_digit+
```

There is no binary literal and no digit separator: `0b101` is the integer `0` followed by the identifier
`b101`, and `1_000` is a single constant identifier. A numeral such as `08` is also an identifier, since it
is neither a valid octal nor a decimal numeral.

### Floating-point numbers

```ebnf
lit_float = digit+ "." digit* exponent? suffix?
          | "." digit+ exponent? suffix?
          | digit+ exponent suffix?
          | digit+ exponent? suffix
exponent  = ( "e" | "E" ) ( "+" | "-" )? digit+
suffix    = "f" | "F" | "d" | "D"
```

`1.` , `.5`, `1e3`, `1f` and `1.5d` are floating-point literals. A range written `1..5` lexes as `1.`
followed by `.5`; FML has no range operator.

### Characters and strings

```ebnf
lit_character = "'" ( any input character but ' and \  |  escape ) "'"
lit_string    = '"' { any input character but " and \  |  escape } '"'
escape        = \b | \t | \n | \f | \r | \" | \' | \\ | octal_escape
octal_escape  = \ ( o o? | z o o )         where o is 0..7 and z is 0..3
```

A string literal cannot contain a line terminator. There is no `\u` escape: a character literal such as
`'A'` is a lexical error. Adjacent literals are not concatenated by the lexer; concatenation is the
`+` operator.

### The null literal

`null` denotes the absence of value (§9.8, §13.6).

## 2.7 Operators and separators

| Kind | Tokens |
|---|---|
| Arithmetic | `+` `-` `*` `/` `%` `++` `--` |
| Bitwise and shift | `&` `\|` `^` `~` `<<` `>>` `>>>` |
| Logical | `&&` `\|\|` `!` |
| Comparison | `==` `!=` `<` `<=` `>` `>=` |
| Conditional | `?` `:` |
| Assignment | `=` `+=` `-=` `*=` `/=` `%=` `<<=` `>>=` `>>>=` `&=` `\|=` `^=` |
| Separators | `(` `)` `[` `]` `{` `}` `;` `,` `.` `..` `...` `:` `::` `@` `$` |

`..` is used by the path syntax of the shell (§12.4); `...` is a cardinality (§4.6); `::` qualifies a
technology-adapter role, action or behaviour, and a named creation scheme; `$` introduces a nested type
name or an enum-value argument; `@` introduces an annotation and a type version.

The lexer produces `>>` and `>>>` as single tokens. The grammar compensates in generic types so that
`List<List<String>>` parses (§4.4).

## 2.8 Summary of lexical gotchas

* A property cannot be called `end`, `match`, `get`, `set`, `delete`, … in a declaration: these are
  keywords in `normal`. They are usable inside expression contexts (§2.3.1) and, for five of them, after a dot.
* `)` ends the `binding` state even when it closes an inner parenthesis.
* There are no binary literals, digit separators, or `\u` escapes.
* In a script, the directive words are reserved and `select` is not.
