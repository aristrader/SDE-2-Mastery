---
order: 40
---

# Builder — Director (GoF flavour)

The original GoF formulation. The Director's recipes are typed against an abstract **step interface**, not a concrete builder. Because of that, the same recipe can drive multiple builders &mdash; each producing a *different* artefact.

This is the headline lesson: **same recipe, different builder, different artefact.**

## The shape

```
OfferConstructionSteps  (interface — only the template steps)
   ↑ implements                    ↑ implements
JobOfferBuilder              OfferLetterBuilder
   ↓ build()                       ↓ build()
JobOffer                     String  (printable letter)

OfferDirector
  ├── constructSeniorOffer(OfferConstructionSteps)
  └── constructJuniorOffer(OfferConstructionSteps)
```

Note what the file tree shows: two top-level builders side by side, both implementing the same step interface. That's the visible structural difference vs the simple variant.

## The code

```java
public interface OfferConstructionSteps {
  void joiningBonus(int amount);
  void relocationBonus(int amount);
  void performanceBonus(int amount);
}

public class JobOfferBuilder implements OfferConstructionSteps {
  private final int salary;        // candidate-driven, ctor-supplied
  private final String city;
  private int joiningBonus, relocationBonus, performanceBonus;

  public JobOfferBuilder(int salary, String city) { ... }

  @Override public void joiningBonus(int amount)      { this.joiningBonus = amount; }
  @Override public void relocationBonus(int amount)   { this.relocationBonus = amount; }
  @Override public void performanceBonus(int amount)  { this.performanceBonus = amount; }

  public JobOffer build() { /* validate + new JobOffer(...) */ }
}

public class OfferLetterBuilder implements OfferConstructionSteps {
  private final StringBuilder letter = new StringBuilder();

  public OfferLetterBuilder(String candidateName, int salary, String city) {
    letter.append("Dear ").append(candidateName).append(",\n\n");
    letter.append("We are pleased to offer you a role in ").append(city)
        .append(" at INR ").append(salary).append(".\n");
  }

  @Override public void joiningBonus(int amount)     { letter.append("  - Joining bonus: ").append(amount).append("\n"); }
  @Override public void relocationBonus(int amount)  { letter.append("  - Relocation bonus: ").append(amount).append("\n"); }
  @Override public void performanceBonus(int amount) { letter.append("  - Performance bonus: ").append(amount).append("\n"); }

  public String build() { return letter.append("\nWelcome aboard!\n").toString(); }
}

public class OfferDirector {
  public void constructSeniorOffer(OfferConstructionSteps steps) {
    steps.joiningBonus(10_000);
    steps.relocationBonus(10_000);
    steps.performanceBonus(10_000);
  }
}
```

Call site:

```java
OfferDirector director = new OfferDirector();

// Same recipe, two builders, two artefacts:
JobOfferBuilder jobOfferBuilder = new JobOfferBuilder(200_000, "BANGALORE");
director.constructSeniorOffer(jobOfferBuilder);
JobOffer offer = jobOfferBuilder.build();   // structured immutable JobOffer

OfferLetterBuilder letterBuilder = new OfferLetterBuilder("Asha", 200_000, "BANGALORE");
director.constructSeniorOffer(letterBuilder);
String letter = letterBuilder.build();      // multi-line letter
```

## What the step interface deliberately does *not* contain

### No `build()` / `getResult()` method

The two builders return different types: `JobOfferBuilder.build()` returns `JobOffer`, `OfferLetterBuilder.build()` returns `String`. A single signature on the interface cannot cover both without generic gymnastics, and the Director never calls `build()` anyway. So `build()` lives on the concrete builder, not the interface.

### No salary / city / candidateName steps

These are **candidate-driven**, not template-driven. They vary per call, not per template. They enter via each builder's *own constructor*. The interface only declares the steps the Director's recipes invoke (the bonuses).

This is a useful distinction worth keeping in mind for any GoF Builder: **only the steps the recipe orchestrates belong on the interface.**

## Why the product's constructor is package-private here, not private

`JobOfferBuilder` is a *separate top-level class* in the same package as `JobOffer`. For it to call `new JobOffer(...)`, the constructor needs at least package-private access. The encapsulation cost is small &mdash; only same-package code can construct directly &mdash; and the simplification (no static-factory bridge) is worth it.

If you wanted the constructor to remain `private` for stricter encapsulation, the alternatives are:

1. Move the builder back to a nested class inside the product (sacrificing the file-tree clarity).
2. Add a package-private static factory method on the product that the builder calls.

The trade-off is mild; this package picks the simpler option.

## When the GoF Builder Director actually fits

The pattern earns its keep when **the two outputs are independently meaningful artefacts of the same conceptual recipe**, with neither derivable from the other.

The classic example &mdash; Refactoring Guru's cars + manuals &mdash; just barely fits because the manual isn't derived from the car at runtime; both are produced *during* the same construction process.

For our `JobOffer` + `OfferLetter`, the fit is **weaker**. The letter is unambiguously *derived* from the offer (same data, different format). In production code, this is the wrong pattern.

## The production answer: model + renderer, not two builders

```java
JobOffer offer = new JobOfferBuilder(200_000, "BANGALORE")
    .joiningBonus(10_000)
    .build();

String letter = OfferLetterRenderer.render(offer, "Asha");
```

The renderer is a one-way function: `JobOffer → String`. No second builder, no duplicated fields, no Director recipe needed (because the recipe was already applied when the JobOffer was built).

This is how 99% of real codebases handle "I have a domain object and I need a presentation of it":

- HTML view of an `Order`
- PDF of an `Invoice`
- JSON serialisation of a `User`
- Email template of a `JobOffer`

The model is the source of truth; the presentation depends on the model. Builders build models. Renderers / templates / serialisers project them.

## Why `OfferLetter extends JobOffer` is the *wrong* fix

A natural-sounding alternative: "since the letter contains all the offer data, why not make `OfferLetter` extend `JobOffer`?" This violates the Liskov Substitution Principle.

Ask: "is an `OfferLetter` a `JobOffer`?" Answer: no, it's a *representation* of one. Code expecting a `JobOffer` (e.g., to compute totals from its bonus fields) would be surprised by an `OfferLetter` substitution. The two have different responsibilities (data record vs text artefact) and different invariants.

When the relationship between B and A is "B is derived from A", the right tool is **composition or rendering**, not inheritance. See `todo/FoundationsToRead.md` &raquo; LSP for the heuristic.

## Why this package keeps the GoF setup anyway

Two distinct concerns:

1. **Engineering correctness** for the real "I have an offer, I need a letter" problem &rarr; renderer, not a second builder.
2. **Pattern pedagogy** for "what does the GoF Builder Director look like when applied?" &rarr; this package's setup.

The package is doing #2. The contrast against the renderer approach is itself a useful lesson: **recognising a pattern is not the same as recommending it**.

## When the GoF flavour does fit

Use this pattern when **two or more genuinely independent artefacts share a construction recipe**, with neither derivable from the other:

- A document parser producing both an AST and a syntax-highlighted view, both as primary outputs.
- A network protocol message produced as both a binary frame and a human-readable trace, both primary outputs.
- Tooling generators producing parallel artefacts (e.g., a code generator that emits both server stubs and client stubs from the same schema spec).

In each case neither output is derivable from the other; both are produced in the same act.

## Demo runner

Three scenarios:

1. Senior recipe &rarr; `JobOfferBuilder` &rarr; `JobOffer` (structured object).
2. **Same** senior recipe &rarr; `OfferLetterBuilder` &rarr; multi-line letter. *This is the pattern's wow moment.*
3. Junior recipe &rarr; `OfferLetterBuilder` &rarr; different letter, showing recipes vary independently.

## Related

- `director_builder/BuilderDirector.md` &mdash; the simple variant where each recipe takes a concrete builder.
- `simple_builder/BuilderBasic.md` &mdash; the underlying EJ Builder shape.
- `todo/FoundationsToRead.md` &raquo; LSP &mdash; the principle behind why `OfferLetter extends JobOffer` is wrong.
- `todo/AccessModifiersDeepDive.md` &raquo; "Class-level modifier decisions" &mdash; covers `final`, `static`, and chokepoint constructors.

