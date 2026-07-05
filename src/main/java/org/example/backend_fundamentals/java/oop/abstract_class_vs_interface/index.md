---
order: 100
---
# Abstract Class vs. Interface — the decision

> The most common "which should I use?" question in Java. Both express abstraction, but with different powers and limits.

---

## Feature comparison

| Feature | Abstract class | Interface |
| --- | --- | --- |
| Instance fields (state) | ✅ Yes | ❌ No (only `public static final` constants) |
| Constructors | ✅ Yes | ❌ No |
| `protected` / package-private members | ✅ Yes | ❌ No — everything is effectively `public` |
| Method bodies (implementation) | ✅ Yes (regular + abstract) | ✅ Yes — via `default` and `static` methods (Java 8+) |
| Can a class have more than one? | ❌ Only one parent class (single inheritance) | ✅ A class can implement many interfaces |
| Static-initializer blocks | ✅ Yes | ❌ No |
| Ideal conceptual role | Says *what a thing IS* (Dog **is an** Animal) | Says *what a thing CAN DO* (Dog **can** Bark) |

---

## When to reach for each

**Use an abstract class when:**

- You need shared **instance state** (fields) across all subclasses.
- You need a **constructor** to enforce invariants ("you can't construct me without X").
- You need **`protected`** members that subclasses can touch but outsiders cannot.
- There is genuine "IS-A" taxonomy (e.g., `Shape` → `Circle` / `Square`).
- You want a **template method** — a concrete method in the parent that calls abstract hook methods in the subclass (this is the GoF Factory Method's whole point).

**Use an interface when:**

- You only want to describe a **capability** or **contract**, not a taxonomy.
- The same capability cuts across unrelated class hierarchies (e.g., `Comparable`, `Serializable`).
- You want a type that a class can implement **alongside** extending some other class.
- You have no state or constructor requirements.

---

## Default methods — a nuance

Since Java 8, interfaces can have `default` and `static` methods. This blurs the line but does **not** eliminate it:

- `default` methods still have no access to instance fields (there are none) or `protected` members.
- You cannot force construction through a constructor.
- You can still implement the interface in multiple classes simultaneously.
- `default` methods cannot be `final` — implementers can always override them.

Use default methods for convenience implementations on top of a small set of truly abstract methods — not to smuggle state-like behavior into an interface.

---

## Template Method — the named GoF pattern

A **template method** is a `final` concrete method on an abstract class that calls `abstract` hook methods that subclasses fill in. The parent owns the algorithm; the children only customise the steps that vary.

```java
public abstract class DeveloperHiringProcess {
    public final Employee onboard() {        // ← template (final, can't be replaced)
        checkBudget();
        Employee e = createDeveloper();      // ← hook (abstract, child fills in)
        provisionLaptop(e);
        return e;
    }
    protected abstract Employee createDeveloper();   // ← hook
    private void checkBudget() { ... }               // ← internal step, hidden
    private void provisionLaptop(Employee e) { ... } // ← internal step, hidden
}
```

This is why Factory Method uses an abstract class, not an interface. An interface cannot:
- Mark a `default` method `final` (so the algorithm could be replaced)
- Hold private fields for shared state
- Use `protected` to expose hooks to subclasses without making them public

Template Method is named in the GoF Behavioural section, but you've already been writing it whenever you used Factory Method.

---

## Rule of thumb

- **Start with an interface.** If you later need state, a constructor, a `protected` member, or a `final` method, promote to an abstract class.
- When you see an abstract class in a GoF pattern (like Factory Method's `DeveloperHiringProcess`), it's usually because the parent wants a **template method** plus shared state — things an interface cannot do cleanly.
- Don't avoid one in favor of the other dogmatically; each exists for different jobs.

---

## Four decision questions

Run through these in order:

1. **Does this thing need state across implementations?** Yes → abstract class. No → prefer interface.
2. **Is it a "what kind of object am I" claim** (taxonomy / IS-A) **or a "what can this object do" claim** (capability)? Taxonomy → abstract class. Capability → interface.
3. **Could the same capability appear on completely unrelated hierarchies?** Yes (`Comparable`, `Serializable`) → interface. No (a `Shape` parent for `Circle` / `Square`) → abstract class.
4. **Do I need a template method that calls subclass-specific hooks?** Yes → abstract class — interfaces have `default` methods but no `final`, so you can't lock the algorithm.

---

## Practice exercises

Each takes 15–30 minutes:

- [ ] **Abstract Factory variation: factory base class.** In `creational/abstract_factory/`, introduce an `AbstractFurnitureFactory` abstract class that `CheapFurnitureFactory` and `LuxuryFurnitureFactory` extend. Give it a `final` template method `deliverSet()` that calls `createChair()` + `createSofa()` and prints a delivery summary. Compare against the current "static helper on the runner" approach. Which feels more natural for *this* domain? When would you flip the choice?
- [ ] **Strategy pattern in two flavours.** Write a tiny Strategy example (e.g., `PaymentStrategy` with `CardPayment` / `CashPayment` / `UpiPayment`) once with `interface PaymentStrategy` and once with `abstract class PaymentStrategy`. Note where each adds value (interface — clean substitution; abstract class — shared logging or audit hooks). Does it match your answer to question #1?
- [ ] **`default` methods test.** Take any interface in this repo and add a `default` method that depends on the abstract methods. Implement it across two classes — does the default work? When would you instead promote to abstract class?
- [ ] **Builder hierarchy (EJ Item 2 advanced).** Outside this repo: read EJ Item 2's `Pizza` / `NyPizza` / `Calzone` example. The `Pizza` parent is **abstract class**, not interface — why? Tie back to question #1 (state) and question #4 (template method). This also explains why some Builder products in production code are `abstract`, not `final`.

---

## Where the decision came up in this repo

- **Abstract Factory:** "should `furnishRoom` go on `FurnitureSetFactory` (interface, default method) or stay on the runner side?" — see `creational/abstract_factory/`. Answer: keep it on the caller because consumption isn't the factory's responsibility — but a `default` method *would* have worked technically.
- **Factory Method:** `DeveloperHiringProcess` is an abstract class because it holds `private final` services and a `final onboard()` template method. An interface couldn't express either.
- **Builder (GoF Director):** `OfferConstructionSteps` is a pure interface with no state; the concrete builders hold their own state. The steps are a *capability* every builder shares; per-builder state is per-builder, not per-step-interface.

---

## Quick recall

**Q. Three things an abstract class can do that an interface cannot (cleanly)?**
A. Hold instance state, enforce construction through a constructor, and mark methods `final` to lock the algorithm. Interfaces have `default` methods but no instance fields, no constructors, and no way to forbid override.

**Q. Rule-of-thumb starting point?**
A. Start with an interface. Promote to an abstract class only when you actually need state, a constructor, `protected` visibility, or a `final` method.

**Q. Why is `DeveloperHiringProcess` an abstract class rather than an interface?**
A. It holds `private final` services as shared state and a `final onboard()` template method that calls the abstract `createDeveloper()` hook. An interface can't hold instance state and can't mark `default` methods `final` — both load-bearing for this design.

**Q. Multiple inheritance — what does Java allow?**
A. One `extends` (single class inheritance), many `implements` (multiple interface implementation). Abstract classes inherit the single-class limit; interfaces don't.

**Q. What does a `default` method on an interface specifically *not* give you?**
A. No access to instance state (there is none), no `final`, no `protected` visibility — and implementers can always override. It's a convenience layer, not a replacement for abstract-class capability.

**Q. The four decision questions in order?**
A. (1) Need state across implementations? (2) Taxonomy or capability? (3) Could the capability cut across unrelated hierarchies? (4) Need a template method with locked algorithm? Two or more "yes / capability" answers → interface; otherwise abstract class.

---

## Related topics

- **Access Modifiers Deep Dive** — explains *why* the modifiers chosen for `DeveloperHiringProcess` (protected ctor, public final, protected abstract, private helpers) only work in an abstract class.
- **Inheritance** — covers `extends` mechanics that abstract classes use; interfaces use `implements`.
