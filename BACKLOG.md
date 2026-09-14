# openflexo-core — Backlog

Features and evolutions to develop in openflexo-core, near to long term. Known bugs go to `KNOWN_DEFECTS.md`
(identifiers `CORE-D-<n>`), not here.

Identifiers are stable and never reused: `CORE-F-<n>`. Reference them from commits and from other entries.

Status legend: `TODO` · `IN PROGRESS` · `BLOCKED` · `DONE` · `DEFERRED`.

---

## FML language

### CORE-F-1 — Arbitrary min/max bounds on FlexoRole cardinality  ·  `TODO`

**Problem.** The FML grammar (`fml-parser/src/main/sablecc/fml.sablecc`, production `cardinality`) accepts any
bounds after a property type: `[2,8]`, `[2,*]`, `[*,5]`, `[1,5]`. `PropertyCardinality` (flexo-foundation) only
has four values — `ZeroOne`, `One`, `ZeroMany`, `OneMany` — so other bounds are read as `ZeroMany` (`OneMany`
for `[1,n]`) and lost in the model.

**Current state.** The parser raises a `SemanticAnalysisWarning` ("Cardinality [2,8] is not supported, read as
[0,*]"), and the syntax-preserving pretty-print keeps the original text; the normalized pretty-print writes the
degraded form.

**Existing FML using such bounds.**
- `flexo-test-resources/.../FMLParsingExamples/TestProperties.fml` — `String[2,8] auMoins2AuPlus8;`
- `flexo-test-resources/.../FMLParsingExamples/TestBasicTypes.fml` and
  `TestFMLPrettyPrint2/TestBasicTypesWithComments.fml` — `ConceptA[2,*] otherConceptsA;`

**To decide.**
- Metamodel shape: explicit lower/upper bound properties on `FlexoRole` (PAMELA `@Getter`/`@Setter`), or a
  richer cardinality value type replacing the `PropertyCardinality` enum.
- Compatibility: keep `PropertyCardinality` and `isMultipleCardinality()` working for existing callers, derived
  from the bounds.
- Parsing and pretty-printing: `ObjectNode.getCardinality` / `serializeCardinality` / `cardinalityContents`;
  drop the warning once bounds are representable.
- Runtime semantics: whether and where bounds are enforced (validation rules on instances, edition actions
  adding or removing values). How `One` / `OneMany` are enforced today must be checked first.
- Roles and model slots of technology adapters that carry a cardinality.

**Acceptance criteria.**
- `[2,8]`, `[2,*]`, `[*,5]` parse without warning and keep their bounds in the model.
- The normalized pretty-print writes the bounds and a round trip preserves them (extend `TestFMLPrettyPrint11`).
- `ZeroOne` / `One` / `ZeroMany` / `OneMany` behave as before; pretty-print tests and AutomatedTests stay green.

### CORE-F-2 — Remove or redefine the "default" deletion scheme of a concept  ·  `TODO`

**Problem.** `FlexoConcept.getDefaultDeletionScheme()` returns the first accessible deletion scheme, so which scheme is "the default"
depends on declaration order (and inheritance), and its intended semantics are unclear. The notion is not meant to be kept as is.

**Where it is used.**
- `unmatched: delete()` in an `end match` (`EndMatchActionNode.finalizeDeserialization()`).
- `FlexoConceptInstance.delete()`, hence the pruning of a behaviour's default matching set (`CORE-D-1`).
- `DeleteFlexoConceptInstance` when no deletion scheme is given.

**Why it matters.** A concept closed by an `end match` that declares several deletion schemes silently gets the first one — possibly
one that deletes federated artifacts instead of only releasing references. `CORE-D-2` makes it worse: `unmatched: method()` falls back
to the first action scheme, which may be a deletion scheme.

**To decide.**
- Drop the notion: require an explicit deletion scheme wherever more than one is accessible (validation error), or
- Redefine it explicitly (e.g. an annotation marking the default deletion scheme), with a validation rule when it is ambiguous.
- What `FlexoConceptInstance.delete()` does when no scheme can be chosen.

**Related.** Fix together with `CORE-D-1` and `CORE-D-2` (see [`KNOWN_DEFECTS.md`](./KNOWN_DEFECTS.md)).
