---
order: 10
search: false
---

# Practice

## Exercise: abstract-factory - Abstract Factory variation

### Goal
Understand when an abstract class is better than an interface as a factory base.

### Task
In `creational/abstract_factory/`, introduce an `AbstractFurnitureFactory` abstract class that `CheapFurnitureFactory` and `LuxuryFurnitureFactory` extend. Give it a `final` template method `deliverSet()` that calls `createChair()` + `createSofa()` and prints a delivery summary.

### Checks
- Compare against the current "static helper on the runner" approach. Which feels more natural for *this* domain? When would you flip the choice?

## Exercise: strategy-flavours - Strategy pattern in two flavours

### Goal
See the trade-offs between interface and abstract class in Strategy.

### Task
Write a tiny Strategy example (e.g., `PaymentStrategy` with `CardPayment` / `CashPayment` / `UpiPayment`) once with `interface PaymentStrategy` and once with `abstract class PaymentStrategy`.

### Checks
- Note where each adds value (interface — clean substitution; abstract class — shared logging or audit hooks). Does it match your answer to question #1?

## Exercise: default-methods - Default methods test

### Goal
Explore Java 8 `default` methods in interfaces.

### Task
Take any interface in this repo and add a `default` method that depends on the abstract methods. Implement it across two classes.

### Checks
- Does the default work? When would you instead promote to an abstract class?

## Exercise: builder-hierarchy - Builder hierarchy (EJ Item 2)

### Goal
Understand state in Builder hierarchies.

### Task
Outside this repo: read EJ Item 2's `Pizza` / `NyPizza` / `Calzone` example.

### Checks
- The `Pizza` parent is **abstract class**, not interface — why? Tie back to question #1 (state). This explains why some Builder products in production code are `abstract`, not `final`.
