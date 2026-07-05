---
order: 10
---

# Builder — Director (simple variant)

A Director is just an object that holds reusable, *named recipes* &mdash; multi-step sequences of builder calls. With one, "what makes an offer 'senior'?" has a single home; without one, the recipe is duplicated at every call site.

This package shows the **simple** flavour where each recipe takes the concrete builder type. The richer GoF flavour, with recipes typed against a shared step interface, lives in `director_builder_gof/`.

## The shape

```
JobOffer + JobOffer.Builder    — same EJ-style builder as simple_builder/

OfferDirector
  ├── constructStandardOffer(JobOffer.Builder)   ┐
  ├── constructSeniorOffer(JobOffer.Builder)     │ — named recipes
  └── constructRelocateOffer(JobOffer.Builder)   ┘
```

## The code

```java
public class OfferDirector {

  public void constructStandardOffer(JobOffer.Builder b) {
    b.joiningBonus(5_000).relocationBonus(0).performanceBonus(0);
  }

  public void constructSeniorOffer(JobOffer.Builder b) {
    b.joiningBonus(10_000).relocationBonus(0).performanceBonus(15_000);
  }

  public void constructRelocateOffer(JobOffer.Builder b) {
    b.joiningBonus(5_000).relocationBonus(20_000).performanceBonus(10_000);
  }
}
```

Call site:

```java
OfferDirector director = new OfferDirector();

JobOffer.Builder builder = new JobOffer.Builder(200_000, "BANGALORE"); // candidate-driven
director.constructSeniorOffer(builder);                                 // recipe applied
JobOffer offer = builder.build();                                       // caller materialises
```

## Three load-bearing decisions

### Recipes return `void`

Each recipe configures the builder and returns nothing. This signals the GoF original shape: "I configured the builder; it's still yours." The caller stays in control.

A reasonable variant in modern Java is to return the builder for fluency: `director.constructSeniorOffer(builder).performanceBonus(20_000).build()`. Both work. `void` is closer to GoF; returning the builder is more idiomatic Java today.

### The Director never calls `build()`

The caller does. This separation is on purpose:

- The recipe is reusable across call sites; *when* to materialise is per-call.
- The caller may layer additional setters on the builder *after* the recipe runs &mdash; e.g., overriding the performance bonus the recipe set.
- Validation still happens in `build()`. A recipe that produces an invalid configuration for a given salary is correctly rejected at `build()` time. The Director cannot smuggle an invalid product past the contract.

### Required fields are not part of recipes

`salary` and `city` vary per candidate &mdash; they are not template-driven. The caller passes them to the `JobOffer.Builder` constructor. The Director only configures the template-driven optional fields (the bonuses). A Director recipe encodes "what makes an offer 'senior'", not "who is receiving this offer".

## When the Director earns its keep

| Reach for it when… | Skip it when… |
| --- | --- |
| The same multi-step configuration is needed at many call sites and "what is a senior offer?" should have one definition | There's only one product and the call site naturally knows what it wants |
| A non-trivial number of "named templates" exist (standard / senior / relocate / intern) and they may evolve | Each product is constructed differently and there's no reusable recipe |
| Construction step *order* matters and is non-obvious | Order doesn't matter or fluent setters already make it clear |

## A lighter alternative

If you only have one or two recipes and they're stable, named static factory methods on the builder often replace the Director:

```java
// On JobOffer.Builder:
public static Builder seniorOfferBuilder(int salary, String city) {
  return new Builder(salary, city)
    .joiningBonus(10_000)
    .performanceBonus(15_000);
}
```

That's lighter than a separate Director class and works well for the single-product case. The Director shape really earns its keep when you need recipes pluggable at runtime, or when you cross over into the GoF multi-product flavour.

## Demo runner

The runner walks four scenarios:

1. Standard recipe.
2. Senior recipe.
3. Relocate recipe.
4. Senior recipe + post-recipe override (`b4.performanceBonus(20_000)` after `constructSeniorOffer`).

Scenario 4 is the proof that the caller, not the Director, owns `build()`.

## Related

- `simple_builder/BuilderBasic.md` &mdash; the underlying builder this Director drives.
- `director_builder_gof/BuilderDirectorGof.md` &mdash; the Director with multi-product polymorphism via a shared step interface.


<ExerciseNav />
