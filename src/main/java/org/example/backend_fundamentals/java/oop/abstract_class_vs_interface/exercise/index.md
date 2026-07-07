---
order: 10
search: false
---

# Abstract Class vs Interface Practice

## Exercises

Each takes 15–30 minutes:

- [ ] **Abstract Factory variation: factory base class.** In `creational/abstract_factory/`, introduce an `AbstractFurnitureFactory` abstract class that `CheapFurnitureFactory` and `LuxuryFurnitureFactory` extend. Give it a `final` template method `deliverSet()` that calls `createChair()` + `createSofa()` and prints a delivery summary. Compare against the current "static helper on the runner" approach. Which feels more natural for *this* domain? When would you flip the choice?
- [ ] **Strategy pattern in two flavours.** Write a tiny Strategy example (e.g., `PaymentStrategy` with `CardPayment` / `CashPayment` / `UpiPayment`) once with `interface PaymentStrategy` and once with `abstract class PaymentStrategy`. Note where each adds value (interface — clean substitution; abstract class — shared logging or audit hooks). Does it match your answer to question #1?
- [ ] **`default` methods test.** Take any interface in this repo and add a `default` method that depends on the abstract methods. Implement it across two classes — does the default work? When would you instead promote to abstract class?
- [ ] **Builder hierarchy (EJ Item 2 advanced).** Outside this repo: read EJ Item 2's `Pizza` / `NyPizza` / `Calzone` example. The `Pizza` parent is **abstract class**, not interface — why? Tie back to question #1 (state) and question #4 (template method). This also explains why some Builder products in production code are `abstract`, not `final`.

---
