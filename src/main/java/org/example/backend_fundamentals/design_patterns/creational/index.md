---
order: 20
---

# Creational Design Patterns — Learning Guide

A reading sequence that builds naturally, front-loads the practically useful patterns, and slots in two non-GoF side quests at the right moment. Follow this order if you're learning from scratch; jump to any section if you already know some of them.

---

## Recommended sequence

```
Singleton  →  Factory Method  →  Builder  →  Side quest A  →  Abstract Factory  →  [Side quest B]  →  Prototype
```

Each step below tells you what to read, what to try, and why it comes where it does.

---

## 1. Singleton

**Read:** `singleton/Singleton.md`

**Run:** `ThreadSafeSingleton.main` — races 50 threads; should always print PASS.

**Try yourself:** Implement all five variants from scratch (no singleton, eager, lazy, thread-safe, Bill Pugh). Then answer: why is `BillPughSingleton` preferred over `ThreadSafeSingleton`?

**Why first:** Simplest creational pattern. Introduces the idea that construction can be controlled — which all the others build on.

---

## 2. Factory Method

**Read:** `factory/Factory.md` — overview of Simple Factory vs GoF Factory Method and when to promote one to the other.

Then the pattern-specific docs in order: 20. `factory/simple_factory/SimpleFactory.md` — the non-GoF starting point
2. `factory/factory_method_basic/FactoryMethodBasic.md` — learning variant (static services, no interface above the abstract class)
3. `factory/factory_method/FactoryMethodProd.md` — production variant (injected services, `HiringProcess` interface, DIP-clean `HR`)

**Run:** `simple_factory/SimpleFactoryRun`, `factory_method_basic/FactoryMethodRun`, `factory_method/FactoryMethodRun` — compare the wiring cost across the three.

**Try yourself:** Build a `NotificationFactory` with `EmailNotification` and `SmsNotification`. Make the caller (`NotificationSender`) depend only on the `Notification` interface, not on either concrete class.

**Why here:** Introduces polymorphic creation — the concept that powers Abstract Factory and most DI containers.

---

## Side quest A — Static factory methods (Effective Java Item 1)

**Read:** `static_factory_methods/StaticFactoryMethods.md`

**Run:** `static_factory_methods/TemperatureRun`

**Why here:** Takes ~20 minutes, pays off forever. Explains why `Integer.valueOf(5)` beats `new Integer(5)`, why `List.of(...)` looks the way it does, and how `JobOffer.builder()` differs from a true static factory method. Sets up the mental model for Spring `@Bean` methods.

---

## 3. Builder

**Read:** `builder/Builder.md` — overview of the four variants.

Then the variant-specific docs in order: 20. `builder/simple_builder/BuilderBasic.md` — hand-written EJ-style (required fields in constructor, validation in `build()`)
2. `builder/lombok_builder/BuilderLombok.md` — what `@Builder` generates and what it costs you
3. `builder/director_builder/BuilderDirector.md` — Director as a recipe holder; never calls `build()`
4. `builder/director_builder_gof/BuilderDirectorGof.md` — GoF Director: same recipe drives multiple builders, different artefacts

**Run:** All four `JobOfferRun` mains. The GoF one is the most instructive — notice how one recipe produces a `JobOffer` from one builder and a `String` letter from another.

**Try yourself:** Model a `Pizza` with a required size and optional toppings. First in plain Java (required fields in builder constructor, validation in `build()`), then with `@Builder`. Notice what `@Builder` cannot enforce.

**Why here:** The most practically useful creational pattern. You will use it constantly in real Java code.

---

## Side quest B — Dependency Injection

**Read:** `todo/FoundationsToRead.md` → "DI in practice"

**Why here:** Once Factory Method clicks, DI is the natural next question — *what if something else held my object graph and handed me my collaborators?* Doing this detour makes Abstract Factory feel motivated rather than theoretical.

**Three passes:** manual DI (constructor injection, wired in `main`) → Spring basics (`@Component`, `@Autowired`, `@Bean`) → bean scopes (singleton vs prototype vs request).

**Try yourself:** Refactor the `NotificationFactory` from the Factory Method exercise so Spring injects the right implementation based on configuration. You should end up deleting the factory class entirely.

---

## 4. Abstract Factory

**Read:** `abstract_factory/AbstractFactory.md`

**Run:** `abstract_factory/RunAbstractFactory`

**Try yourself:** Build a `UIFactory` with `LightThemeFactory` and `DarkThemeFactory`. Each produces matching `Button`, `TextField`, and `Checkbox`. Write a `renderUI(UIFactory)` helper on the client side — not on the factory.

**Why here:** Abstract Factory only makes sense once plain Factory Method is solid. The pattern is Factory Method scaled to a coordinated family of products.

**Key decision to think through:** When does a set of objects qualify as a "family"? (Answer: when mixing products from different families would produce incorrect behaviour — e.g., a luxury chair with a cheap sofa breaks the design guarantee.)

---

## 5. Prototype

**Read:** `prototype/Prototype.md` (once written)

**Try yourself:** Implement a `Shape` with a `List<Point>` both ways:
1. `Cloneable` + `clone()` — observe the shallow-copy trap (both copies share the same list).
2. Copy constructor — proper deep copy with no shared state.

Form your own opinion on which is safer and clearer to read.

**Why last:** Least used in modern Java. The GoF mechanics (`Cloneable`/`clone()`) are widely considered broken; copy constructors or serialization libraries win in practice. Worth knowing for interviews and for recognising the pattern when you see it.

---

## Side quest B revisit — DI orchestration mini-project

**Read:** `todo/study_plan/deep_dives/PatternSelectionExercise.md`

Practice choosing between Strategy, Registry, and Spring DI to orchestrate multiple factories from a single client. This is the synthesis exercise — it forces you to pick between patterns rather than just implement them.

---

## Completion criteria

You can move on from any pattern when you can answer these without looking them up:

1. What problem does this pattern solve? (one sentence)
2. What does the minimum implementation look like? (can you write it from scratch?)
3. Where does it appear in the JDK or a popular library?
4. When would you *not* use it?
5. How does it interact with dependency injection?

---

## If you only have time for the essentials

**Singleton + Factory Method + Builder** cover roughly 90 % of creational code in real Java projects. Add Side quest A (static factory methods) for another 20 minutes that pays off in every codebase you read.

Abstract Factory earns its keep when you interview or when you encounter JDBC / UI toolkits. Prototype is mostly for interview prep and recognising the shape in libraries.

---

## All docs at a glance

| Pattern | Overview doc | Variant / detail docs |
| --- | --- | --- |
| Singleton | `singleton/Singleton.md` | — |
| Factory Method | `factory/Factory.md` | `simple_factory/SimpleFactory.md`, `factory_method_basic/FactoryMethodBasic.md`, `factory_method/FactoryMethodProd.md` |
| Builder | `builder/Builder.md` | `simple_builder/BuilderBasic.md`, `lombok_builder/BuilderLombok.md`, `director_builder/BuilderDirector.md`, `director_builder_gof/BuilderDirectorGof.md` |
| Static factory methods | `static_factory_methods/StaticFactoryMethods.md` | — |
| Abstract Factory | `abstract_factory/AbstractFactory.md` | — |
| Prototype | `prototype/Prototype.md` | — |

**Foundations and cross-cutting:**
- `design_patterns/foundations/` — SOLID, supporting principles, OOP pillars, coupling/cohesion/smells, DIP vs DI
- `todo/study_plan/deep_dives/DesignThinkingProcess.md` — pattern-agnostic design heuristics (6-step process)
- `todo/study_plan/deep_dives/PatternSelectionExercise.md` — when to use which pattern, "one HR, many factories" orchestration exercise
- `todo/study_plan/deep_dives/PatternSelectionScenarios.md` — 25 production scenario exercises across the creational patterns


<ExerciseNav />
