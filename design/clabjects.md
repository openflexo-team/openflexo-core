# Clabjects — a multi-level extension of FML (exploratory study)

Status: **exploratory**, nothing implemented. Backlog entry: `CORE-F-7`.

This note records the first analysis of a new language built as an extension of FML, where the separation between
`FlexoConcept` and `FlexoConceptInstance` gives way to **clabjects**: elements that are at once a concept (they declare
features and behaviours) and an instance (they value features). FML itself stays as it is.

Each point is marked **[decided]** (a choice already made) or **[proposed]** (a recommendation still to confirm).

---

## 1. Motivation

- **Business cases** where type and instance stack up: product lines, digital twins (product model → series →
  physical item), catalogues.
- **Free modelling.** The Free Modelling Editor (FME) works bottom-up: the user draws objects first and abstracts
  concepts later. The levels are not known in advance, and features are valued at levels and in orders that are not
  known in advance either. Today the FME *emulates* this on two levels (`DropShape`, `InstantiateNewFMEProperty`,
  `GeneralizeConcepts`).
- **Removing the design-time / run-time boundary**: creating or changing concepts while instances exist.
- **Unified federation.** Technological spaces are already two-level (EMF `EClass`/`EObject`, OWL class/individual,
  `ClassRole` holding a class as a value; `IFlexoOntologyClabject` exists in `flexo-ontology`, with no implementation).
  Multi-level modelling applied to federation is not offered by existing multi-level tools.
- **Research**: a contribution to the multi-level modelling community (MULTI workshop). The MULTI process challenge,
  already solved in two-level FML (`openflexo-integration-tests/multi-process-challenge`, HAL hal-03692372), gives a
  baseline for comparison.

## 2. Theoretical position

**[decided] No mandatory potency, no declared levels.** Potency-based deep instantiation (Atkinson & Kühne; Melanee,
MetaDepth) fixes in advance at which level each feature is valued. That rigidity does not fit free modelling. The
chosen direction is **progressive refinement**, in the spirit of prototype-based languages: the levels *emerge* from
the refinement chain. Related work without potency: DeepTelos (most general instances), a-posteriori typing (de Lara,
Guerra, Cuadrado).

What potency gives is recovered with two markers (§4.3) and an optional classification (§4.4). Where a level guarantee
is wanted, **[decided]** potency can be added optionally, counted on refinement steps (§4.5).

FML itself is the special case where everything is valued exactly one level below its declaration: a `FlexoConcept`
declares, its `FlexoConceptInstance` values, and a default value (`=`) is a value given one level up and copied down.

## 3. Syntax principles

- **[decided] One grammar.** The language reuses `fml-parser/src/main/sablecc/fml.sablecc` and adds only what is needed:
  a `clabject` keyword for declarations and a way to value a feature declared higher up. There is a precedent for this:
  fml-cli already shares the grammar through its own entry points.
- **[proposed] `:=` values a feature.** Three operators, three meanings:

  | Syntax | Meaning | Exists |
  |---|---|---|
  | `String marque = "x";` | default value, copied into each new instance | yes (`FlexoRole.setDefaultValue`) |
  | `String marque values expr;` | derived property, recomputed, never stored | yes (`ExpressionProperty`) |
  | `marque := "Renault";` | stored valuation of an inherited feature | new |

  The rule is that no type precedes the name. `marque := …` and `Type id …` differ from their second token on.
- **[proposed] `concept`, `clabject` and `instance` are three ways to write one entity.** A `concept` declares and values
  nothing. An `instance` is a leaf. A `clabject` is the general case. Since there is a single relation, there should be
  a single symbol: `instance X : T` and `instance X extends T` must be harmonized.

## 4. Model

### 4.1 Refinement
`B extends A` makes B a refinement of A. B inherits A's declarations, valuations and behaviours, and may add
declarations, value features and redefine behaviours. Multiple refinement is allowed, as `getParentFlexoConcepts()`
already allows multiple inheritance.

### 4.2 Valuations
- **[proposed] Delegation, not copy.** A clabject reads an unvalued feature through its parents. Changing a valuation
  in the middle of the chain therefore reaches every descendant at once, and declaring a new feature at the top needs no
  migration. The value-generalization gesture of the FME (§6) only makes sense with delegation.
- **Lookup**: the first valuation met along the linearization of the parents wins.
- **[proposed] Re-valuing in a chain is an error**, unless an explicit `override` is introduced for a real use case.
- **A declared, unvalued feature reads null.** This is normal in free modelling. Typing is static for the structure
  (which features exist) and dynamic for their presence (whether they are valued).
- **[proposed] Conflict between parents.** When two parents, neither more specific than the other, value the same
  feature differently, the child is invalid until it values that feature itself. **This must be the same rule as for
  behaviours.** Today FML resolves a behaviour in multiple inheritance depth first, in the declaration order of the
  parents, first found wins, with no ambiguity error (`FlexoConcept.getFlexoBehaviour`). Either the new language keeps
  that rule for both, or it is stricter for both. The second option is recommended, without changing FML.

### 4.3 Own features
**[proposed]** `own` marks a feature (or a behaviour) of the clabject itself, not inherited by its descendants:

```fml
clabject Clio extends RenaultCar {
    modele := "Clio";                  // valuation inherited by every Clio car
    Integer cylindree;                 // declaration: every Clio car has one
    own Date lancement := 1990-05-01;  // Clio's own feature, not a feature of cars
}
```

An inherited declaration plays the role of "valued further down" (potency 2 or more); an own feature plays the role of
"valued here" (potency 1 seen from above). A descendant may not redeclare an own feature under the same name.

### 4.4 Optional classification
`own` is local: nothing forces *every* car model to have a launch date, and an `own` behaviour reaches neither
sub-models nor cars. A rule shared by all models needs a second, optional relation, which is the powertype pattern
without level numbers:

```fml
concept CarModel {
    Date lancement;
    void recall(String reason) { ... }   // applies to RenaultCar, Clio... not to cars
}
clabject Clio extends RenaultCar : CarModel { lancement := 1990-05-01; }
```

**[proposed] Two relations, no level numbers**: refinement carries inheritance and delegation; classification gives a
clabject its own features and behaviours. "Clio is a car model" is carried by classification, or derived ("refines
`Car` and values `modele`").

### 4.5 Optional potency
**[decided] Potency is optional, and every refinement step decrements it.** A model without any potency behaves exactly
as described above. Potency is added where a guarantee is wanted ("this feature is valued by the cars, not by the
models").

- **The default is `*` (unbounded).** Without potency, everything is `*`. Optional potency only allows a finite
  value where wanted.
- **It is a constraint, not a structure.** The model stays the refinement model. Potency adds rules checked by FML
  validation.
- **Distance.** `d(C, X)` is the number of `extends` steps from X up to C.
- **Feature potency (mutability).** A feature declared on C with `@Potency(p)` may be valued only on clabjects X such
  that `d(C, X) = p`. A clabject at distance p that leaves it unvalued gets a validation *warning*, not an error: free
  modelling must tolerate incomplete objects. A model validated without such warnings guarantees that the feature is
  valued at depth p and below, which gives static typing a non-null guarantee there.
- **Durability stays `*`.** A value given at distance p remains readable below it by delegation. In Melanee's terms,
  potency here constrains *mutability* (where a value may be set), not *durability* (how far the feature exists).
- **`@Potency(0)` is not `own`.** A feature with potency 0 is valued on C itself and stays readable by C's descendants;
  an `own` feature is not visible to them at all.
- **Clabject potency.** `@Potency(q)` on a clabject bounds the depth of its refinement descendants. 0 makes a leaf that
  cannot be refined. Each refinement step decrements it.

```fml
concept Car {
    @Potency(1) String marque;     // valued by a brand: RenaultCar
    @Potency(2) String modele;     // valued by a model: Clio
    @Potency(3) Color  couleur;    // valued by a car: myCar
}
clabject RenaultCar extends Car { marque := "Renault"; }
clabject Clio extends RenaultCar { modele := "Clio"; }
instance myCar extends Clio { couleur := Color.bleue; }
// error: modele valued at distance 1, its potency is 2
clabject Zoe extends Car { modele := "Zoé"; }
```

**Multiple refinement.** With `myCar extends Clio, BlueCar`, `myCar` can be at different distances from `Car`
depending on the path (3 through `Clio`, 1 through `BlueCar`). **[proposed]** A feature with a finite potency requires
every path from X to the declaring clabject to have the same length. Otherwise it is a validation error.

**Refactoring gestures keep the potencies consistent.** Counting every refinement step makes potency sensitive to the
insertion and removal of levels, the classic "level stability" criticism. In the FME, generalization inserts levels, so:
- **[proposed]** `Generalize` (inserting a clabject on a path) adds 1 to the finite potencies of the features declared
  above the insertion point and valued below it, and to the clabject potencies above it. Dissolution subtracts 1.
- **[proposed]** A value generalization that moves a valuation to a distance its potency forbids is refused, unless the
  user accepts the proposed potency change.

**Behaviours.** **To decide:** restricting a behaviour to receivers of a given remaining potency (models but not cars:
`recall`) overlaps the optional classification of §4.4. Both mechanisms must not be kept for the same need.

## 5. Behaviours

```fml
concept Car {
    String immat; String marque; String modele; Color couleur; Integer km;
    create(String immat) { this.immat = parameters.immat; this.km = 0; }
    String describe() { return marque + " " + modele + " " + couleur; }
    void drive(Integer d) { this.km = km + d; }
}
clabject RenaultCar extends Car { marque := "Renault"; }
clabject Clio extends RenaultCar {
    modele := "Clio";
    String describe() { return "Clio: " + super.describe(); }
}

Car myCar = container.new Clio("AB-123-CD");   // a new leaf refining Clio
myCar.drive(120);                              // values km on myCar only
Clio.describe();                               // "Clio: Renault Clio null": Clio is a receiver too
```

- **`this` is the receiver, at any level.** A behaviour declared on C applies to C and all its descendants, with
  prototype semantics. An `own` behaviour applies to C only.
- **Creating is refining.** `new Clio(...)` creates a clabject whose single parent is `Clio`, and the gesture is the
  same at any level. Creation schemes are inherited. `abstract` means "cannot be a leaf".
- **A write always values the receiver.** **Trap:** `Clio.drive(5)` values `km` on `Clio`, and every Clio car whose `km`
  is not valued reads 5 through delegation. Validation should at least warn when an "item" feature is written on a
  clabject that has descendants.
- **Dispatch and `super`** keep the current mechanism: most specialized along the chain, `declaredConceptualLevel` for
  `super`.
- **A leaf may have its own behaviours** (a prototype, useful in the FME). The cost of a full concept (binding model,
  control graph) is paid only then.

## 6. Free Modelling Editor scenario

| Step | User gesture | With clabjects | Today |
|---|---|---|---|
| 1 | Drops 3 shapes: clioPaul, clioMarie, zoeJean | 3 leaves, no parent | `DropShape` + one concept per instance |
| 2 | Adds "couleur" on clioPaul | `Color couleur := rouge;` declared **and** valued on the leaf | `InstantiateNewFMEProperty` changes the concept |
| 3 | The two Clio share modele="Clio": generalizes | `Clio` created, **the valuation moves up**, removed from the leaves | `GeneralizeConcepts` moves the declaration, not the value |
| 4 | Clio and Zoé share marque="Renault": generalizes | `RenaultCar` inserted above: three levels nobody declared | same, values not factored |
| 5 | Decides that Clio has a launch date | `own Date lancement` on `Clio` | impossible without a second concept |

Another session could factor `marque` before `modele`: the order is free.

Consequences:
- a **value generalization** gesture, extending `GeneralizeConcepts`;
- its inverse, **dissolution** (§8);
- a **multi-level resource**: `Clio` is both type and instance, so an FME model cannot be split into `.fml` and
  `.fml.rt`.

## 7. Textual FML@RT (first milestone)

**[decided]** The `.fml.rt.xml` serialization (PAMELA XML, unreadable) is replaced by FML syntax with instances only.
This milestone has value by itself (readability, git diffs, hand edits), it is a prerequisite of clabjects, and it can
be done without settling the theory: an `instance` is a clabject at the leaf.

Excerpt of `openflexo-integration-tests/digital-twin-demo/.../INSTANCES/lineL2.fml.rt` (instance names illustrative):

```fml
instance lineL2 : ProductionLine {
    ligneXml    := XML::Model(uri="http://openflexo.org/digital-twin-demo/production-line.xml");
    maintenance := FMLRT::Reflected(uri="http://openflexo.org/digital-twin-demo/maintenance.xlsx");

    instance DEP01 : Equipment #FLX_6 {
        xmlElement    := XML::Ref(path="/0/1");
        assetRecord   := maintenance.AssetRecord[5];
        readings      := maintenance.Reading[2, 9, 15, 17, 24, 30, 32, 39, 45, 48];
        interventions := maintenance.Intervention[4];
    }
    instance link1 : MaterialLink #FLX_80 {
        xmlLink := XML::Ref(path="...");
        source  := EQ2;
        target  := EQ3;
    }
}
```

Requirements found on this real file:
- **Model slots are valued like properties** (`ligneXml := …` replaces `TypeAwareModelSlotInstance`), with the same
  operator as roles.
- **Identity.** Instances only have a `flexoID` today. Text needs a readable name for references, and other resources
  (diagrams, FME) still need a stable identifier (`#FLX_6`). A name may be generated from a key property, but a derived
  key (`Equipment.code` derives from `xmlElement`) is only known once the model slot is bound, so the name must be a
  label.
- **Three kinds of references** already appear in this one file: an instance of the same file by name, a technology
  object by path (`XML::Ref`), a reflected instance by concept and key (`maintenance.AssetRecord[5]`). Each technology
  adapter has its own `ActorReference` today. **[proposed]** A generic `Slot::Ref(args)` form, parsed as is and handed
  over to the technology adapter, so that the grammar does not change for each adapter (as TA-qualified edition actions
  already do).
- **Forward references** need a two-pass resolution, as the parser already does for types (`unresolvedTypes`).
- **[proposed] Literals only, no expressions**, in an instance file: nothing is evaluated at load time.
- **Files owned by the machine**: the run time rewrites them, and formatting and comments are not preserved.
- **To measure**: parse plus semantic analysis versus direct PAMELA XML, on a VMI with thousands of instances.
- **Migration**: a converter from `.fml.rt.xml` to text, on the model of the `.fml.xml` → FML converter. That converter
  only runs in file-based resource centers, not in jar ones.
- **Open**: serializing `FMLVariableActorReference` values.

## 8. Hard points

1. **Metamodel.** `FlexoConcept` (an `FMLObject`, design time, `.fml`) and `FlexoConceptInstance` (run time,
   `ActorReference`s, `.fml.rt.xml`) are disjoint hierarchies. With refinement, the lock becomes "add valuations to a
   concept" instead of "merge concept and instance". A concept stays heavy (binding model, behaviours, inspector,
   typing space entry), so **lightweight leaves** are needed: a leaf pays the cost of a concept only when it declares
   features or behaviours.
2. **`select` and `match`.** `select Car from vmi` would return the whole hierarchy (`RenaultCar`, `Clio`, `myCar`…).
   Explicit filters are needed (`select leaf Car`, `where (c refines Clio)`). The matching engine (`unmatched: new …`,
   deletion at end-match) must never delete an intermediate clabject; it already has open defects (CORE-D-1, CORE-D-2).
3. **Deleting an intermediate clabject.** Forbid, cascade, or **dissolve**: attach the children to the parent and copy
   the valuations down, the inverse of value generalization. **[proposed]** Dissolution by default.
4. **Changes at run time.** Adding a feature or a behaviour to `Clio` reaches all cars, but the binding models and
   control graphs of the descendants must be notified and revalidated, at a cost proportional to their number. Changing
   a behaviour while it runs needs a rule (finish on the old version, or refuse).
5. **Resources.** FME models need multi-level resources. The `CompilationUnitResource` / VMI resource split holds for
   classic FML only.
6. **Tooling.** Inspectors per concept, FME, FIB browsers and `.fml.rt` all assume two levels.
7. **Interoperability with FML** is still open: one-way (the new language imports FML) or both ways.

## 9. Proposed path

1. **Milestone A — textual FML@RT**, framed on `digital-twin-demo`: instance entry point in the grammar, references
   contributed by technology adapters, converter, performance measure.
2. **Milestone B — clabjects in the FME**, small models and immediate benefit (the two-level emulation disappears):
   valuations on concepts, `own`, delegation, value generalization and dissolution, multi-level resource.
3. **Then**: classification (§4.4), optional potency and the gestures that maintain it (§4.5), `select`/`match` semantics, run-time change propagation, federation (technology
   objects as clabjects), and a comparison with the existing two-level solution of the MULTI challenge.

Each step is proven by running FML, not by reading: the MULTI challenge scripts and the digital-twin-demo tests give
existing executable references.

## References

- Atkinson, Kühne — potency-based deep instantiation; "Misconceptions about Potency-Based Deep Instantiation",
  MODELS 2024 companion.
- Carvalho, Almeida — MLT, multi-level theory (SoSyM).
- Neumayr et al. — Dual Deep Modeling.
- Jeusfeld, Neumayr — DeepTelos.
- de Lara, Guerra, Cuadrado — a-posteriori typing.
- Frank, Clark — FMMLx / XModeler (LE4MM, University of Duisburg-Essen).
- MULTI workshop (multi-level modeling, with MODELS), 2024 and 2025 editions.
