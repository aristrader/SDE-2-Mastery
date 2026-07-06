---
order: 20
---

# SOLID Principles

> Five principles, each with a one-line test. Most GoF patterns are illustrations of one or more of them.

---

## S — Single Responsibility Principle (SRP)

A class should have only one reason to change. Keep each class focused on one concern.

**Test:** *"Can I describe what this class does without using 'and'?"* If you have to say "it does X **and** Y," that's two responsibilities.

---

## O — Open/Closed Principle (OCP)

Classes should be open for extension but closed for modification. Add new behavior by adding new code, not by editing existing code.

**One-line test:** *"To add a new case, do I have to edit any file that already existed?"* If yes, OCP is violated. If you only add new files, OCP is respected.

**Canonical example:** a Simple Factory with `if`/`else` on a type violates OCP (adding a type edits the factory); a Factory Method with subclasses respects it (adding a type adds a new subclass).

### Open/Closed in practice — how to actually apply it

Understanding OCP is half the job. The concrete techniques for *applying* it:

- **Polymorphism** — Factory Method, Strategy. New cases are new subclasses or new strategy implementations.
- **Registries / strategy maps** — `Map<Key, Supplier<Product>>`. New cases are new map entries (often loaded from configuration).
- **Configuration** — externalising decisions to config files.
- **Feature flags** — gating new behaviour without removing the old.

Knowing *how* to extend without modifying is what makes OCP actionable.

### Programming to an interface, not an implementation

GoF's first design principle and the practical expression of DIP. You'll see this phrase everywhere in pattern literature — it means *"hold the most abstract type that still gives you what you need."*

In every pattern in this repo:
- `HR` holds `HiringProcess`, not `AndroidHiringProcess`.
- `furnishRoom` takes `FurnitureSetFactory`, not `CheapFurnitureFactory`.
- `List<Shape>` not `List<Circle>`.

---

## L — Liskov Substitution Principle (LSP)

Subtypes must be usable wherever their base type is expected, without breaking callers' expectations. This is formalised by three rules from **Design by Contract**:

**1. A subtype may not strengthen preconditions** — demand more from callers than the parent did.

Precondition = what the caller must satisfy for the method to work (e.g. "amount > 0", "input non-null"). If the subclass requires more, callers written for the parent suddenly fail.

```java
// Parent accepts any positive amount
void deposit(double amount) { if (amount > 0) balance += amount; }

// BAD subclass — demands amount > 100, which is stricter than the parent's > 0
@Override void deposit(double amount) { if (amount > 100) balance += amount; } // ← precondition strengthened
```

**2. A subtype may not weaken postconditions** — promise less than the parent did.

Postcondition = what the method guarantees it will return/produce. If the subclass delivers less, callers depending on the parent's guarantee break.

```java
// Parent guarantees a non-null list
List<String> getItems() { return items; }  // never null

// BAD subclass — returns null when empty
@Override List<String> getItems() { return items.isEmpty() ? null : items; } // ← postcondition weakened
```

**3. A subtype must preserve the invariants of the parent.**

Invariant = a truth that must always hold on the object. If the subclass breaks it, the parent's own code stops working correctly.

The canonical violation is `Square extends Rectangle`. `Rectangle` has the invariant that width and height are independent. `Square` must keep them equal, so `setWidth(5)` secretly changes height too — any caller that set width and height separately is now broken.

```java
// Caller written against Rectangle — reasonable expectation
Rectangle r = new Square();
r.setWidth(5);
r.setHeight(3);
assert r.area() == 15; // FAILS — Square kept width == height, so area is 9
```

**One-line test:** *"If I substitute the subtype, would any caller relying on the supertype's contract break or be surprised?"* If yes, the inheritance is wrong — use composition.

**Canonical example (from this repo's discussion):** `OfferLetter extends JobOffer` would be an LSP violation. An `OfferLetter` is a *representation* of an offer (text), not a JobOffer with extra features. Code that expects a `JobOffer` (e.g., uses its bonuses to compute totals) would be surprised by an OfferLetter substitution. The right relationship is *composition / one-way derivation*: `String letter = render(offer)`, where the renderer takes a `JobOffer` and returns a string.

**Heuristic:** if subtype B *redefines* what supertype A means (a different responsibility, not a refinement), inheritance is wrong; reach for composition or a renderer/adapter.

---

## I — Interface Segregation Principle (ISP)

Many small, focused interfaces are better than one fat one. Clients should not be forced to depend on methods they don't use.

**Test:** *"Does any implementer of this interface have to provide an empty or `UnsupportedOperationException` body for some method?"* If yes, the interface should be split.

---

## D — Dependency Inversion Principle (DIP)

Depend on abstractions, not concretions. High-level modules should not depend on low-level modules — both should depend on interfaces.

The principle is covered in detail (along with its frequent companion, Dependency Injection) in the **DIP vs DI** doc. For SOLID purposes, the test is:

**One-line test:** *"Does the high-level class name a concrete low-level class anywhere in its field types or parameters?"* If yes, DIP is violated.

---

## Quick recall

**Q. SRP — the one-line test?**
A. Can you describe what the class does without using "and"? If you say "it does X **and** Y," that's two responsibilities — split.

**Q. OCP — the one-line test?**
A. To add a new case, do you have to edit a file that already existed? If yes, OCP is violated. The principle says: new cases should add files, not modify them.

**Q. Three practical techniques for applying OCP?**
A. Polymorphism (Factory Method, Strategy), registries / strategy maps (`Map<Key, Supplier<Product>>`), and configuration / feature flags.

**Q. LSP — the one-line test?**
A. If you substitute the subtype, would any caller relying on the supertype's contract break or be surprised? If yes, the inheritance is wrong — reach for composition.

**Q. ISP — the one-line test?**
A. Does any implementer of this interface have to write an empty body or throw `UnsupportedOperationException` for some method? If yes, split the interface.

**Q. DIP — the one-line test?**
A. Does the high-level class name a concrete low-level class anywhere in its field types or parameters? If yes, DIP is violated. Depend on abstractions; let DI deliver the concrete.

**Q. "Program to an interface, not an implementation" — what does it mean in practice?**
A. Hold the most abstract type that still does the job. `HR` holds `HiringProcess` (not `AndroidHiringProcess`); `furnishRoom` takes `FurnitureSetFactory` (not `CheapFurnitureFactory`); the loop in `PolymorphicPrototypeRun` holds `List<Shape>` (not `List<Circle>`).

---

## Related topics

- **DIP vs DI** — DIP overlaps with the DI deep-dive; here just understand the principle, the other doc covers how DI delivers on DIP.
- **Inheritance / LSP** — LSP is the formal is-a test from the Inheritance doc.
- **Coupling and Cohesion** — SRP is fundamentally about high cohesion.

