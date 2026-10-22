# Appendix A. Grammar summary

This appendix gives the complete syntax of FML in one place. It is a transcription of the productions of
[`fml.sablecc`](../fml-parser/src/main/sablecc/fml.sablecc) into the notation of §1.5, with the alternative labels
of the source grammar dropped except where the label names a distinction the text relies on. Where this appendix and
the grammar disagree, the grammar is right.

Terminals in double quotes are keywords or punctuation; `lidentifier`, `uidentifier`, `cidentifier`, `lit_*` are the
token classes of chapter 2. The four entry points are listed first.

## A.1 Entry points

```ebnf
entry_point = start_compilation_unit fml_compilation_unit
            | start_binding          expression
            | start_command          command
            | start_script           script
```

The `start_*` tokens are not read from the input: the lexer produces them to select the entry point and the initial
lexical state (§1.2, §2.3).

## A.2 Compilation unit

```ebnf
fml_compilation_unit = namespace_decl* use_decl* import_decl* type_decl* model_decl

namespace_decl = "namespace" lit_string "as" identifier ";"
use_decl       = "use" composite_tident "as" cidentifier ";"
import_decl    = "import" composite_tident ";"
               | "import" composite_tident "as" identifier ";"
               | "import" reference_by_uri ";"
               | "import" type_decl_type identifier "from" reference_by_uri ";"
type_decl      = "typedef" type_decl_type "as" uidentifier ";"
type_decl_type = reference_type | technology_specific_type      (* may be empty in the source grammar *)

identifier     = lidentifier | uidentifier | cidentifier

reference_by_uri = "[" uri_expression "]"
                 | "[" uri_expression ":" uri_expression "]"
uri_expression   = uri_primary | uri_expression "+" uri_primary
uri_primary      = lidentifier | uidentifier | cidentifier | lit_string
```

## A.3 Declarations

```ebnf
model_decl   = annotation* visibility? "abstract"? "model" uidentifier super_clause? inside_clause?
               "{" member* "}"
concept_decl = annotation* visibility? "abstract"? "concept" uidentifier super_clause? inside_clause?
               "{" member* "}"
event_decl   = annotation* visibility? "abstract"? "event" uidentifier super_clause? inside_clause?
               "{" member* "}"
enum_decl    = annotation* visibility? "enum" uidentifier "{" enum_value ( "," enum_value )* "}"
enum_value   = cidentifier

super_clause  = "extends" super_type_list
super_type_list = composite_tident ( "," composite_tident )*
inside_clause = "inside" composite_tident
visibility    = "private" | "protected" | "public"

member =
    annotation* visibility? type cardinality? variable_declarator ";"
  | annotation* visibility? type cardinality? lidentifier "with" uidentifier fml_parameters? ";"
  | annotation* visibility? type cardinality? lidentifier "with" cidentifier "::" uidentifier fml_parameters? ";"
  | annotation* visibility? "abstract" type variable_declarator ";"
  | annotation* visibility? type lidentifier "values" expression ";"
  | annotation* visibility? type lidentifier "{" get_decl set_decl? "}" ";"
  | behaviour_decl
  | concept_decl | event_decl | enum_decl
  | assert_decl

get_decl = type "get" "(" ")" flexo_behaviour_body
set_decl = "set" "(" type lidentifier ")" flexo_behaviour_body

variable_declarator = lidentifier
                    | lidentifier "=" expression
                    | lidentifier "=" fml_action_exp
```

## A.4 Invariants

```ebnf
assert_decl = simple_assert_declaration | iteration_assert_declaration
simple_assert_declaration = "assert" expression ";"
                          | "assert" expression failure_clause
iteration_assert_declaration = "assert" "(" type lidentifier ":" expression ")"
                               "{" simple_assert_declaration* "}"
failure_clause = "onfailure" ":" statement
```

## A.5 Behaviours

```ebnf
behaviour_decl =
    annotation* visibility? "create" "(" formal_arguments_list? ")" flexo_behaviour_body
  | annotation* visibility? "create" "::" lidentifier "(" formal_arguments_list? ")" flexo_behaviour_body
  | annotation* visibility? "delete" "(" formal_arguments_list? ")" flexo_behaviour_body
  | annotation* visibility? "delete" "::" lidentifier "(" formal_arguments_list? ")" flexo_behaviour_body
  | annotation* visibility? "abstract"? type? lidentifier "(" formal_arguments_list? ")" flexo_behaviour_body
  | annotation* "listen" reference_type "from" expression flexo_behaviour_body
  | annotation* visibility? type? lidentifier "(" formal_arguments_list? ")"
        "with" uidentifier fml_parameters? flexo_behaviour_body
  | annotation* visibility? type? lidentifier "(" formal_arguments_list? ")"
        "with" cidentifier "::" uidentifier fml_parameters? flexo_behaviour_body

formal_arguments_list = formal_argument ( "," formal_argument )*
formal_argument = "required"? primitive_type lidentifier default_argument_value?
                | "required"? reference_type lidentifier default_argument_value?
default_argument_value = "=" expression

flexo_behaviour_body = block | ";"
```

## A.6 Statements

```ebnf
block           = "{" block_statement* "}"
block_statement = type variable_declarator ";" | statement

statement =
    statement_without_trailing_substatement
  | "if" "(" expression ")" statement
  | "if" "(" expression ")" statement_no_short_if "else" statement
  | "while" "(" expression ")" statement
  | "for" "(" for_init? ";" ";" statement_expression? ")" statement
  | "for" "(" for_init? ";" expression ";" statement_expression? ")" statement
  | "for" "(" type lidentifier ":" expression ")" statement
  | "for" "(" type lidentifier ":" fml_action_exp ")" statement

for_init = statement_expression | type variable_declarator

statement_without_trailing_substatement =
    block
  | ";"
  | statement_expression ";"
  | "do" statement "while" "(" expression ")" ";"
  | "return" ";"
  | "return" expression ";"
  | "return" fml_action_exp ";"

statement_no_short_if =
    statement_without_trailing_substatement
  | "if" "(" expression ")" statement_no_short_if "else" statement_no_short_if
  | "while" "(" expression ")" statement_no_short_if
  | "for" "(" for_init? ";" ";" statement_expression? ")" statement_no_short_if
  | "for" "(" for_init? ";" expression ";" statement_expression? ")" statement_no_short_if

statement_expression =
    left_hand_side assignment_operator expression
  | left_hand_side assignment_operator fml_action_exp
  | "++" unary_exp | "--" unary_exp | postfix_exp "++" | postfix_exp "--"
  | method_invocation
  | new_instance
  | fml_action_exp
```

## A.7 Expressions

```ebnf
expression = left_hand_side assignment_operator expression | conditional_exp
left_hand_side = composite_ident | field_access
assignment_operator = "=" | "*=" | "/=" | "%=" | "+=" | "-=" | "<<=" | ">>=" | ">>>=" | "&=" | "^=" | "|="

conditional_exp     = conditional_or_exp | conditional_or_exp "?" expression ":" conditional_exp
conditional_or_exp  = conditional_and_exp | conditional_or_exp "||" conditional_and_exp
conditional_and_exp = inclusive_or_exp    | conditional_and_exp "&&" inclusive_or_exp
inclusive_or_exp    = exclusive_or_exp    | inclusive_or_exp "|" exclusive_or_exp
exclusive_or_exp    = and_exp             | exclusive_or_exp "^" and_exp
and_exp             = equality_exp        | and_exp "&" equality_exp
equality_exp        = relational_exp      | equality_exp ( "==" | "!=" ) relational_exp
relational_exp      = shift_exp
                    | shift_exp ( "<" | ">" | "<=" | ">=" ) shift_exp
                    | shift_exp "instanceof" type dim*
shift_exp           = add_exp  | shift_exp ( "<<" | ">>" | ">>>" ) add_exp
add_exp             = mult_exp | add_exp ( "+" | "-" ) mult_exp
mult_exp            = unary_exp | mult_exp ( "*" | "/" | "%" ) unary_exp

unary_exp = "++" unary_exp | "--" unary_exp | "+" unary_exp | "-" unary_exp | unary_exp_not_plus_minus
unary_exp_not_plus_minus = postfix_exp | "~" unary_exp | "!" unary_exp | "(" type dim* ")" unary_exp
postfix_exp = primary | postfix_exp "++" | postfix_exp "--"

primary       = primary_no_id | composite_ident
primary_no_id = literal | "(" expression ")" | field_access | method_invocation | new_instance

field_access = "super" "." lidentifier
             | composite_ident "." "super" "." lidentifier
             | primary_no_id "." lidentifier
             | primary_no_id "." authorized_kw
authorized_kw = "concept" | "create" | "get" | "model" | "set"

method_invocation = primary "(" argument_list? ")"
                  | "super" "(" argument_list? ")"
                  | type "." lidentifier "(" argument_list? ")"

new_instance = new_containment_clause? "new" reference_type "(" argument_list? ")" precise_clause?
             | new_containment_clause? "new" uidentifier "::" lidentifier "(" argument_list? ")" precise_clause?
new_containment_clause = composite_ident "."
precise_clause = "with" fml_parameters

argument_list = expression ( "," expression )*

literal = lit_integer | lit_float | "true" | "false" | lit_character | lit_string | "null"
dim = "[" "]"
```

## A.8 Names

```ebnf
composite_ident =
    identifier_prefix* authorized_kw
  | identifier_prefix* lidentifier
  | identifier_prefix* escaped_identifier
  | identifier_prefix* cidentifier
composite_tident  = identifier_prefix* uidentifier composite_tident_suffix*
composite_cident  = identifier_prefix* cidentifier
composite_tident_suffix = "$" uidentifier
identifier_prefix = lidentifier "." | escaped_identifier "."
escaped_identifier = "{" lit_string "}"
```

## A.9 Types

```ebnf
type = primitive_type | "void" | reference_type at_version? | technology_specific_type at_version?
primitive_type = "int" | "short" | "long" | "float" | "double" | "char" | "byte" | "boolean"
reference_type = composite_tident type_arguments?
technology_specific_type = uidentifier fml_parameters

type_arguments = "<" ">" | "<" type_argument_list ">"
                 (* plus the two alternatives that close with ">>" and ">>>" *)
type_argument_list = ( type_argument "," )* type_argument
type_argument = reference_type | "?" wildcard_bounds?
wildcard_bounds = "extends" reference_type | "super" reference_type

at_version = "@" "(" version ")"
version = lit_integer | lit_float | lit_float lit_float lidentifier?

cardinality = "[" lit_integer "," lit_integer "]" | "[" lit_integer "," "*" "]"
            | "[" "*" "," lit_integer "]"         | "[" "*" "," "*" "]"
            | "..."
```

## A.10 Edition actions

```ebnf
fml_action_exp =
    cidentifier "::" uidentifier fml_parameters in_clause? from_clause?
  | "delete" expression from_clause?
  | "select" "unique"? select_type with_clause? from_clause where_clause?
  | "connect" left_hand_side "using" expression
  | "log" expression
  | "fire" expression
  | "notify" expression in_clause?
  | "begin" "match" uidentifier from_clause where_clause?
  | "match" uidentifier in_clause? from_clause qualified_where_clause? "unmatched" ":" new_instance
  | "end" "match" uidentifier in_clause "unmatched" ":" end_match_action_clause
  | "(" fml_action_exp ")"

select_type = reference_type | technology_specific_type      (* may be empty in the source grammar *)
end_match_action_clause = (* empty *) | "delete" "(" argument_list? ")" | method_invocation
with_clause = "with" uidentifier fml_parameters
in_clause = "in" expression
from_clause = "from" expression
where_clause = "where" "(" argument_list? ")"
qualified_where_clause = "where" "(" qualified_argument_list? ")"

fml_parameters = "(" qualified_argument_list? ")"
qualified_argument_list = qualified_argument ( "," qualified_argument )*
qualified_argument = lidentifier "=" expression
                   | lidentifier "=" reference_type
                   | lidentifier "=" ( "$" lidentifier | "$" uidentifier )
                   | lidentifier "=" qualified_instance
                   | lidentifier "=" "{" ( qualified_instance ( "," qualified_instance )* )? "}"
qualified_instance = uidentifier ":" fml_parameters
                   | cidentifier "::" uidentifier ":" fml_parameters
```

## A.11 Annotations

```ebnf
annotation = "@" annotation_tag
           | "@" annotation_tag "(" conditional_exp ")"
           | "@" annotation_tag "(" annotation_key_value_pairs ")"
           | "@" annotation_tag "(" annotation* ")"
annotation_tag = composite_tident | composite_cident
annotation_key_value_pairs = annotation_key_value_pair ( "," annotation_key_value_pair )*
annotation_key_value_pair = composite_ident "=" conditional_exp
```

## A.12 Commands and scripts

```ebnf
script  = ( command ";" )*
command = directive | fml_command

fml_command = "context"
            | "assert" expression
            | ( left_hand_side "=" )? fml_action_exp
            | expression

directive =
    "help" | "history" | "pwd" | "ls" | "services" | "exit" | "quit"
  | "cd" path
  | "execute" path
  | "service" identifier identifier directive_argument?
  | "activate" identifier
  | resources_directive
  | ( left_hand_side "=" )? open_directive
  | ( left_hand_side "=" )? load_directive
  | ( left_hand_side "=" )? enter_directive
  | more_directive

directive_argument = expression | "-d" path | "-f" path | "-r" reference_by_uri | "-rc" reference_by_uri

resources_directive = "resources" | "resources" identifier
                    | "resources" "*" reference_by_uri | "resources" identifier reference_by_uri
open_directive  = "open"  "-r" reference_by_uri | "open"  path
load_directive  = "load"  "-r" reference_by_uri | "load"  path
enter_directive = "enter" "-r" reference_by_uri | "enter" "-f" path | "enter" expression
more_directive  = "more" | "more" "-r" reference_by_uri | "more" "-f" path | "more" expression

path = simple_path | "." | ".." | "/" path | simple_path "/" path | "." "/" path | ".." "/" path
simple_path = path_terminal | path_terminal simple_path | path_prefix simple_path
path_prefix = path_terminal ( "." | "-" | "-r" | "-d" | "-f" )
path_terminal = identifier | literal | "resources"
```

## A.13 Lexical grammar

The token definitions are in chapter 2: identifiers (§2.4), keywords by state (§2.5), literals (§2.6), operators and
separators (§2.7), white space and comments (§2.1), and the state transitions (§2.3.1).
