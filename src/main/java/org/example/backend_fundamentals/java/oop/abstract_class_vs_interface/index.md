---
order: 130
---
# Abstract class vs. interface: choose the constraint you need

Both create an abstraction, but they impose different inheritance and API constraints. Start from the pressure: do consumers need a role they can combine with other roles, or does a controlled family need shared state and a protected algorithm?

---

## Feature comparison

| Feature | Abstract class | Interface |
| --- | --- | --- |
| Instance fields (state) | ✅ Yes | ❌ No (fields are `public static final` constants) |
| Constructors | ✅ Yes | ❌ No |
| `protected` / package-private instance members | ✅ Yes | ❌ No — Java 8 interface instance methods are public |
| Method bodies (implementation) | ✅ Yes (regular + abstract) | ✅ Yes — via `default` and `static` methods (Java 8+) |
| Can a class have more than one? | ❌ Only one parent class (single inheritance) | ✅ A class can implement many interfaces |
| Static-initializer blocks | ✅ Yes | ❌ No |
| Best fit | Shared implementation within one controlled base family | A role/capability usable across unrelated types |

---

## When to reach for each

**Use an abstract class when:**

- You need shared **instance state** (fields) across all subclasses.
- You need a **constructor** to enforce invariants ("you can't construct me without X").
- You need **`protected`** members that subclasses can touch but outsiders cannot.
- The implementations are one controlled family and inherit a real common implementation, not merely a shared name.
- You need a **template method**: a concrete parent algorithm with hooks whose ordering the parent owns.

**Use an interface when:**

- You only want to describe a **capability** or **contract**, not a taxonomy.
- The same capability cuts across unrelated class hierarchies (e.g., `Comparable`, `Serializable`).
- You want a type that a class can implement **alongside** extending some other class.
- You have no state or constructor requirements.

---

## Default methods: evolution aid, not shared object state

Since Java 8, interfaces can have `default` and `static` methods. This blurs the line but does **not** eliminate it:

- `default` methods still have no access to instance fields (there are none) or `protected` members.
- You cannot force construction through a constructor.
- You can still implement the interface in multiple classes simultaneously.
- `default` methods cannot be `final` — implementers can always override them.

Use a default method for a convenience implementation based only on other interface methods. Its trade-off is API evolution: it can add behavior without breaking every existing implementer, but it becomes part of a public contract that implementations may override.

When defaults collide, a class method wins over an interface default; the more specific subinterface wins; unrelated defaults require the implementing class to override and choose, optionally with `Left.super.method()`. This is the recovery path for a default-method diamond conflict.

---

## Template Method — the named GoF pattern

A **template method** is a concrete method that owns an algorithm and calls overridable hooks. Mark it `final` when subclasses must not replace the algorithm. The parent owns the order; the children customise only the variable steps.

```java
public abstract class DeveloperHiringProcess {
    public final String onboard() {          // ← template (final, can't be replaced)
        checkBudget();
        String developer = createDeveloper(); // ← hook (abstract, child fills in)
        provisionLaptop(developer);
        return developer;
    }
    protected abstract String createDeveloper();     // ← hook
    private void checkBudget() { }                    // ← internal step, hidden
    private void provisionLaptop(String developer) { } // ← internal step, hidden
}
```

This creator uses an abstract class because this design needs protected hooks/state and a locked template. Factory Method itself does not require every creator to be an abstract class. An interface cannot:
- Mark a `default` method `final` (so the algorithm could be replaced)
- Hold private fields for shared state
- Use `protected` to expose hooks to subclasses without making them public

Template Method and Factory Method are distinct patterns. A Factory Method creator may use a template method to orchestrate its factory hook, but neither pattern implies the other.

---

## Decision path

Prefer an interface when callers only need a stable role and implementations may belong to unrelated type hierarchies. Prefer an abstract class when shared state, constructor-enforced invariants, protected hooks, or a final template algorithm are essential. Do not introduce an abstract base “just in case”: Java permits only one superclass, so a needless base consumes the most limited inheritance slot.

---

## Four decision questions

Run through these in order:

1. **Does the base need to own shared instance state or constructor invariants?** Yes → an abstract class may fit. State held separately by each implementation does not require one.
2. **Does the family share base behavior or invariants, not merely a taxonomy label?** Yes → an abstract class may fit; an IS-A statement alone is not enough.
3. **Could unrelated hierarchies need the role?** Yes (`Comparable`, `Serializable`) → interface.
4. **Do I need a template method that calls subclass-specific hooks?** Yes → abstract class — interfaces have `default` methods but no `final`, so you can't lock the algorithm.

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
A. Use an interface for a role across unrelated types; use an abstract base only when shared implementation constraints require it.

**Q. Why is `DeveloperHiringProcess` an abstract class rather than an interface?**
A. It holds `private final` services as shared state and a `final onboard()` template method that calls the abstract `createDeveloper()` hook. An interface can't hold instance state and can't mark `default` methods `final` — both load-bearing for this design.

**Q. Multiple inheritance — what does Java allow?**
A. One `extends` (single class inheritance), many `implements` (multiple interface implementation). Abstract classes inherit the single-class limit; interfaces don't.

**Q. What does a `default` method on an interface specifically *not* give you?**
A. No access to instance state (there is none), no `final`, no `protected` visibility — and implementers can always override. It's a convenience layer, not a replacement for abstract-class capability.

**Q. The four decision questions in order?**
A. (1) Need state? (2) Controlled base family or role? (3) Must unrelated types use it? (4) Need a locked template method? State or a locked template points to an abstract base; a cross-cutting role points to an interface.
