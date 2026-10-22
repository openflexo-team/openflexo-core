# 12. FML-script

FML-script is the other textual surface of the language. Where FML *defines* a model, FML-script *drives* it: it
loads resources, creates instances, calls behaviours and checks values. A script file has the extension
`.fmlscript`; the same text, typed one command at a time, is the language of the interactive shell.

FML-script shares expressions, types, `new … with (…)`, `assert`, and path syntax with FML proper; everything in
chapters 4 and 9 holds inside a script. Only the top level and the set of reserved words differ.

## 12.1 Structure

```ebnf
script        = ( command ";" )*
command       = directive | fml_command

fml_command   = "context"
              | "assert" expression
              | ( left_hand_side "=" )? fml_action_exp
              | expression
```

A script is a sequence of `;`-terminated commands, with `//` and `/* … */` comments. A command is a **directive**
(§12.2) or an **FML command**:

* `context` prints the bindings currently in scope;
* `assert e` checks that `e` is true;
* `name = expression;` evaluates the expression and binds the result to `name`; the name then stands for that
  value in the commands that follow;
* an edition action (§10), optionally assigned;
* an expression alone, evaluated for its effect (a call, a `log`).

`log expression` (§10.5) is available in scripts.

```fml-script
virtualModel = load -r ["http://openflexo.org/test/TestResourceCenter/TaskList.fml"];
assert virtualModel != null;

service ResourceCenterService add_temp_rc;
list = new TaskList() with (name = "groceries");
assert list.taskCount == 0;

milk = list.addTask("Buy milk");
assert milk.label == "Buy milk";
log "Test PASSED";
```

## 12.2 Directives

```ebnf
directive =
    "help" | "history" | "pwd" | "ls" | "services" | "exit" | "quit"
  | "cd" path
  | "execute" path
  | "service" identifier identifier directive_argument?
  | "activate" identifier
  | resources_directive
  | ( left_hand_side "=" )? ( open_directive | load_directive | enter_directive )
  | more_directive

directive_argument = expression | "-d" path | "-f" path | "-r" reference_by_uri | "-rc" reference_by_uri
```

| Directive | Effect |
|---|---|
| `help`, `history` | the shell's help and the history of commands |
| `pwd`, `ls`, `cd path` | print, list and change the current location in the tree of resources |
| `execute path` | execute the script at the path |
| `services` | list the services of the platform |
| `service Name action [argument]` | drive a service, e.g. `service ResourceCenterService status;`, `service ResourceCenterService add_temp_rc;` |
| `activate Adapter` | activate a technology adapter |
| `resources …` | list resources (§12.3) |
| `open …`, `load …` | open or load a resource (§12.3) |
| `enter …` | enter a resource, a file or an object |
| `more …` | display the content of a resource, file or object |
| `exit`, `quit` | leave the shell |

The names `Name` and `action` of `service` are identifiers; the argument is an expression or a flagged argument
(`-d` directory, `-f` file, `-r` resource, `-rc` resource center). The services and actions that exist are those of
the platform (`ResourceCenterService` is the one used to obtain a writable resource center).

## 12.3 Resources and loading

```ebnf
resources_directive = "resources"
                    | "resources" identifier                         (* of an adapter *)
                    | "resources" "*" reference_by_uri               (* of a resource center *)
                    | "resources" identifier reference_by_uri        (* of an adapter, in a resource center *)

open_directive  = "open"  "-r" reference_by_uri | "open"  path
load_directive  = "load"  "-r" reference_by_uri | "load"  path
enter_directive = "enter" "-r" reference_by_uri | "enter" "-f" path | "enter" expression
more_directive  = "more" | "more" "-r" reference_by_uri | "more" "-f" path | "more" expression
```

A resource is designated by its address in brackets (§3.5.3): `[uri]`, or `[uri : object]` for an object in a
resource. `-r` introduces a resource reference, `-rc` a resource-center reference, `-f` a file path, `-d` a
directory path.

`load -r […]` loads the resource and **gives the loaded object**. For an `.fml` resource, this is the virtual model
itself; for a data resource (a spreadsheet, an XML document), the resource, whose data is reached through its
properties. `open` takes the same references and also gives a value, so it can be assigned in the same way.

```fml-script
libraryXML = load -r ["http://openflexo.org/xml-test/TestResourceCenter/XML/Library1.xml"];
resources;
resources XML;
```

## 12.4 Paths

The commands `cd`, `execute`, `open`, `load`, `enter` and `more` take file-system-like paths:

```ebnf
path          = simple_path                          (* a name *)
              | "."                                  (* the current location *)
              | ".."                                 (* the parent location *)
              | "/" path                             (* absolute *)
              | simple_path "/" path
              | "." "/" path
              | ".." "/" path

simple_path   = path_terminal
              | path_terminal simple_path
              | path_prefix simple_path
path_prefix   = path_terminal ( "." | "-" | "-r" | "-d" | "-f" )
path_terminal = identifier | literal | "resources"
```

A path is a sequence of segments separated by `/`; `.` and `..` are the current and parent locations. A segment is
a name with dots and dashes, so `foo-bar/x.y` is a valid path. The words reserved for options (`-r`, `-d`, `-f`)
may occur inside a segment.

## 12.5 Instantiating and driving a model

To use a model in a script:

1. `load` it (§12.3);
2. obtain a **writable resource center** with `service ResourceCenterService add_temp_rc;`, needed before creating any
   instance;
3. instantiate it with `new Model(args) with (name = "…")` (§9.7). The `with (name = …)` is mandatory for a virtual
   model: omitting it stops the script with *cannot instantiate … because new VirtualModelInstance name was
   not set*;
4. call its behaviours and read its properties with ordinary paths.

`new <Model>(args)` reaches only an **unnamed** creation scheme (§7.3.1). A model that declares only a named one is
instantiated by name:

```fml-script
diary = new Journal::start("Diary") with (name = "diary");
assert diary.label() == "Diary";
```

An instance can also be created inside another instance: `vm.new Concept(args)`, or, for a model loaded as a
value, `markdownModel.new MarkdownModel(resource) with (name = "…")`.

## 12.6 Assertions

`assert e` states a value that must hold; the run fails, with a message that names the line, if it does not.

Assert **values**, not mere success: `assert list.taskCount == 2;`, `assert milk.status == "done";`. A script that
only runs proves nothing about behaviour.

## 12.7 Differences with a behaviour body

A script is checked and executed under rules of its own.

* **The whole script is analysed before any command runs.** An unresolved binding anywhere, even on the last
  line, aborts the run before the first line executes: none of the `log` lines are printed. When a script does not
  print what is expected, look at its last lines first.
* **A failed `load` or `new` stops the script**, with a message that names the command; nothing after it runs.
  Inside a behaviour the same mistake often gives a silent `null` (§9.8).
* **Visibility is ignored.** A script can read and write a `private` property.
* **`select` is not available** (it is an identifier in the `script` state, §2.5.2). To check what a query finds,
  call a behaviour that returns it: `assert list.openTasks().size == 2;`.
* **An enum read from a property is compared through `toString()`:** `assert p.cardinality.toString() == "ZeroMany";`.
  The direct comparison with a string literal fails with a type mismatch.
* **A script variable can be bound once.** After a second assignment to the same name, that name reads as `null`
  (`CORE-D-20`): give every value its own name.
* There is no `if`, loop, or declaration in a script: those statements belong to behaviours (chapter 8). A
  computation that needs them is written as a behaviour of the model and called from the script.

## 12.8 Reserved words

FML-script reserves its command names on top of the keywords of FML (§2.5): `help` `history` `execute` `pwd`
`ls` `cd` `services` `service` `activate` `resources` `open` `load` `more` `enter` `exit` `quit` `context`, and
the options `-rc` `-r` `-d` `-f`. They are identifiers in FML proper and in expression contexts.

A behaviour or a creation scheme whose name is one of these words parses inside FML proper but fails from a script
(*expecting: lidentifier*): `open` is a legitimate behaviour name in a model and not a callable one in a script. Rename
it if a script must call it.

In the `script` state the only language keywords are `new`, `with`, `assert` and `log` (§2.5.2); every other word
of the language (`select`, `where`, `from`, `match`, `instanceof`, …) is an identifier there, which is why those
constructs are not available in a script. The primitive-type keywords and the literals `true`, `false` and `null`
exist in all states.

## 12.9 The interactive shell

The `command` entry point (§1.2) parses one command at a time, with the lexical state `command`. It admits the same
directives and expressions as a script. The shell adds nothing to the language: its history, completion and
output are features of the application.
