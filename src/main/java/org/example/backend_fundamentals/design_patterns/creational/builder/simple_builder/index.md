---
order: 40
---

# Builder — Basic (EJ Item 2 hand-written)

The Effective Java-style Builder, hand-written. This is the canonical Java form of the pattern and the default you should reach for when the product is immutable, has several optional fields, and needs validation at construction.

## The shape

```
JobOffer (final class, immutable)
   ├── all fields private final
   ├── private constructor — only Builder.build() can call it
   └── static nested Builder
       ├── required fields: final, set in Builder ctor
       ├── optional fields: non-final, set via fluent setters
       └── build() — runs all validation, returns a new JobOffer
```

## The code

```java
public final class JobOffer {

  private final int salary;            // required
  private final String city;           // required
  private final int joiningBonus;      // optional, defaults to 0
  private final int relocationBonus;
  private final int performanceBonus;

  private JobOffer(Builder b) {        // private — only Builder may call
    this.salary = b.salary;
    this.city = b.city;
    // ... etc
  }

  public static class Builder {

    private final int salary;          // required → final
    private final String city;
    private int joiningBonus;          // optional → defaulted, mutable
    private int relocationBonus;
    private int performanceBonus;

    public Builder(int salary, String city) {  // required up front
      this.salary = salary;
      this.city = city;
    }

    public Builder joiningBonus(int v)     { this.joiningBonus = v; return this; }
    public Builder relocationBonus(int v)  { this.relocationBonus = v; return this; }
    public Builder performanceBonus(int v) { this.performanceBonus = v; return this; }

    public JobOffer build() {
      if (salary <= 0) throw new IllegalStateException("salary must be > 0");
      if (city == null || city.isBlank()) throw new IllegalStateException("city required");
      if (joiningBonus < 0 || relocationBonus < 0 || performanceBonus < 0)
        throw new IllegalStateException("bonuses cannot be negative");
      int total = joiningBonus + relocationBonus + performanceBonus;
      if (total > salary) throw new IllegalStateException("total bonuses cannot exceed salary");
      return new JobOffer(this);
    }
  }
}
```

Call site:

```java
JobOffer offer = new JobOffer.Builder(150_000, "HYDERABAD")
    .joiningBonus(20_000)
    .performanceBonus(15_000)
    .build();
```

## Three load-bearing decisions

### Required vs optional split

Required fields are constructor arguments of `Builder`. Optional fields are fluent setters with sensible defaults. The compiler refuses to start a build chain without the required fields &mdash; this is the structural guarantee that telescoping constructors and JavaBeans setters can't deliver together.

### Why the product's constructor is `private`

The only path to a `JobOffer` runs through `Builder.build()`. With a `public` constructor, callers could bypass `build()` and skip validation. Making the constructor private turns "validation runs in `build()`" from a convention into a compiler-enforced guarantee.

### Why all validation lives in `build()`

- **Single-field rules** (e.g., `salary > 0`) *could* fail fast in setters as a UX nicety.
- **Cross-field rules** (e.g., `total bonuses ≤ salary`) *can only* be checked once every field has its final value &mdash; which is precisely what `build()` guarantees.
- Centralising in `build()` keeps the contract in one findable place and ensures it runs regardless of which optional setters were called.

## Why the `Builder` is `static` (and nested)

Two reasons:

1. **Chicken-and-egg with the enclosing instance.** A non-static inner class requires an existing `JobOffer` to construct. The Builder's job is to construct the *first* one, so a non-static inner class wouldn't even compile (`new JobOffer.Builder(...)` would be illegal).
2. **Hidden retention of enclosing instance.** Non-static inner classes silently hold a reference to the outer instance. If a Builder ever escaped the construction expression, it would pin a `JobOffer` in memory.

See `todo/AccessModifiersDeepDive.md` &raquo; "Class-level modifier decisions" for the broader rule (default nested classes to `static`).

## Why the product is `final`

To close the immutability contract. Without `final`, a subclass could:

- Add mutable fields.
- Override `equals` / `hashCode` / `toString` to misbehave.
- Be substituted in for `JobOffer`, and callers would never know.

`final class` makes those impossible at compile time. This is Effective Java Item 17 rule #1.

## When to reach for this variant

- 4+ fields, several optional.
- Immutability matters.
- Validation is non-trivial (especially cross-field).
- You want compile-time enforcement of required fields.

## When *not* to use this

- 2–3 fields all required &mdash; a plain constructor is enough.
- The product is a mutable domain entity.
- You're picking *which subclass* to construct &mdash; that's Factory Method, not Builder.

## Related variants in this family

- `lombok_builder/BuilderLombok.md` &mdash; Lombok's `@Builder` auto-generates this shape, but loses required-field enforcement and built-in validation.
- `director_builder/BuilderDirector.md` &mdash; adds a Director holding reusable construction recipes.
- `director_builder_gof/BuilderDirectorGof.md` &mdash; the GoF flavour with a shared step interface enabling multi-product polymorphism.


