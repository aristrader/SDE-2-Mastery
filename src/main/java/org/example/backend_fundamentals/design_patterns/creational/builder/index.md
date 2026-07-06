---
order: 40
---

# Builder — Overview

The Builder pattern constructs a complex object step-by-step, separating the *configuration* of a product from its final *materialisation*. The umbrella thesis: callers should not write `new ConcreteProduct(...)` directly when the product has many fields, several optional, and is meant to be immutable and validated.

This package walks four variants. The four are progressively richer, but they share the same product family (`JobOffer`) so the comparison is concrete. Use the generated topic cards below to open the variant pages in navigation order.

## The four variants

| Variant | What's added on top |
| --- | --- |
| Hand-written EJ-style Builder | Required-field enforcement, fluent setters, validation in `build()`, immutability |
| Lombok `@Builder` | Annotation generates the boilerplate &mdash; at the cost of required-field enforcement and validation |
| Director (simple) | Director holds reusable recipes typed against a concrete builder |
| Director (GoF) | Director recipes typed against a shared step interface, so the same recipe drives multiple builders producing different artefacts |

## When to reach for which

- **EJ-style Builder** is the default for "an immutable object with several optional fields, validated at construction." 90% of real Builder usage.
- **Lombok `@Builder`** is the default when the product is simple, the contract is permissive, and the boilerplate cost matters more than required-field enforcement.
- **Simple Director** is worth adding when you have N call sites configuring the same multi-step "named template" (standard / senior / relocate). One place owns the recipe.
- **GoF Director** is worth adding when *more than one product* shares a construction recipe and is best built side-by-side. In production code, often replaced by `model.build()` followed by a separate renderer/serialiser.

## What every variant has in common

Three guarantees, regardless of variant:

1. **Readable construction.** Named, fluent setters at the call site instead of positional `new` arguments.
2. **Immutability of the product.** Fields are `final`; no setters on the product itself.
3. **Construction is funneled** through a chokepoint (`build()`). Validation, when it exists, runs there &mdash; never observed in a half-built state.

The variants differ in *how strictly* each of these is enforced and *what additional structure* the pattern offers (recipe reuse, multi-product polymorphism). They do not differ on the underlying philosophy.

## What you should not use Builder for

- An object with 2–3 always-required fields and no validation. A plain constructor is enough.
- A mutable domain entity that changes over time. JavaBeans-style setters are correct there.
- Selecting *which subclass to construct*. That's Factory Method, not Builder. Builder configures one product; Factory chooses among products.
