---
order: 30
---
# Inheritance

> One of the four OOP pillars and the one most often misused. The right framing isn't "inheritance vs composition" — it's "when does each one fit?"

---

## What inheritance actually gives you

Three things, in order of how commonly they're reached for:

1. **Code reuse** — shared fields and methods live in the parent once, not duplicated across children.
2. **Polymorphism** — subclass instances can be used wherever the parent type is expected (virtual dispatch picks the right override at runtime).
3. **Taxonomy** — a formal is-a claim that shapes how the type system sees the hierarchy.

Reaching for inheritance for code reuse alone — without a genuine is-a relationship — is the most common inheritance mistake.

---

## How Java resolves method calls up the chain

When you call `obj.method()`:

1. The JVM looks at the **actual runtime class** of `obj`.
2. Does that class define `method()`? If yes — call it and stop.
3. If not, go to the parent class. Check there.
4. Repeat up the hierarchy until `Object`.
5. If not found anywhere — compile error (caught before runtime).

This is virtual dispatch. Inheritance creates the chain; polymorphism makes the lookup dynamic.

**Fields are different.** Field access is resolved at compile time by the *declared type*, not the runtime type. This is why you never "override" a field — you shadow it, which is almost always a bug.

---

## `super` — two distinct uses

**Use 1 — Constructor chaining (most common):**

```java
public class Circle extends Shape {
    public Circle(int x, int y, String color, int radius) {
        super(x, y, color);    // must be the FIRST statement
        this.radius = radius;
    }
}
```

Rules:

- `super(...)` must be the **first statement** in the constructor — no exceptions.
- If you don't write it, Java silently inserts `super()` (no-arg parent constructor).
- If the parent has no no-arg constructor and you don't call `super(...)` explicitly — **compile error**.

**Use 2 — Method delegation (less common):**

```java
@Override
public String toString() {
    return "Circle[" + super.toString() + ", radius=" + radius + "]";
}
```

Calls the parent's version of the method. Use it to *extend* parent behaviour, not replace it. Avoid when you just want to replace — override without calling `super` in that case.

Two facts that catch people:

- `super.method()` is **not polymorphic** — the bytecode (`invokespecial`) hardcodes the parent class. But calls *inside* the parent method are still polymorphic and may dispatch back into the child. So `super.describe()` runs `Parent.describe()`, yet a `name()` call inside `Parent.describe()` will still find the child's override.
- There is no `super.super.method()` in Java. You can only reach one level up — the language deliberately preserves the immediate parent's encapsulation.

See `SuperMethodDelegation.java` in this folder for the small demo.

---

## Constructor chaining — the order

Every object is constructed top-down, from the root of the hierarchy to the actual class:

```
new Circle(10, 10, "red", 5)
  → Circle() calls super(x, y, color)
      → Shape() runs first — Shape's fields are set
  → then Circle's own body runs — Circle's fields are set
```

`super(...)` must be first because the parent must be fully constructed before the child can safely access `this`. Attempting to use `this` before `super(...)` is a compile error.

---

## The is-a test — necessary but not sufficient

The classic English test: *"Is a B truly an A, such that anywhere I use an A, a B would work without the caller noticing?"*

If yes → inheritance may be right.
If no → use composition.

But passing the English test is not enough. The **LSP (Liskov Substitution Principle)** is the precise version:

- A subtype may not **strengthen preconditions** — demand more from callers than the parent did.
- A subtype may not **weaken postconditions** — promise less than the parent did.
- A subtype must **preserve invariants** of the parent.

From this repo: `OfferLetter extends JobOffer` sounds natural ("an offer letter is a job offer in document form"), but fails LSP. Code that receives a `JobOffer` and calls `.getBonuses()` to compute totals would be broken by an `OfferLetter`. Substitution fails. The right design is composition: `String letter = render(offer)` — a renderer takes a `JobOffer` and produces a string.

**The practical shortcut:** if you need to add an `if (this instanceof SubType)` guard anywhere, or if a subclass needs to throw `UnsupportedOperationException` on inherited methods, the inheritance is wrong.

The canonical violation is `Square extends Rectangle`: mathematically a square is a rectangle, but a `Square.setWidth(w)` must also change height to keep the square invariant — surprising any caller written against `Rectangle`. See `LiskovSquareRectangle.java` for the broken version and the `Shape`-interface fix.

---

## The fragile base class problem

A hidden hazard: changes to a base class silently break subclasses.

```java
class Shape {
    public void normalize() {
        scale(1.0 / getMaxDimension());  // calls scale() internally
    }
}

class Circle extends Shape {
    @Override
    public void scale(double factor) {
        this.radius *= factor;
        log("scaled");   // Circle's author assumed scale() is called directly
    }
}
```

Now `Circle.normalize()` calls `Circle.scale()` — which is correct — but `Circle`'s author never anticipated `normalize()` calling `scale()` twice per normalization cycle in some future version of `Shape`. Both classes are internally correct; the interaction is broken.

This is why Effective Java Item 19 says: **design and document for inheritance, or else prohibit it** (`final class`).

When you make a class non-final, you're implicitly promising subclasses: "you can extend me and it'll work." But if you haven't thought through which methods are safe to override and what invariants must hold, a subclass can break you silently — exactly like `CountingList` below:

```java
public class CountingList extends ArrayList<String> {
    private int count = 0;

    @Override public boolean add(String s) {
        count++;
        return super.add(s);
    }

    @Override public boolean addAll(Collection<? extends String> c) {
        count += c.size();
        return super.addAll(c); // ArrayList.addAll() internally calls add() — count incremented twice
    }
}
```

`ArrayList` never documented that `addAll` calls `add` internally. The subclass that overrides both gets double-counting. Nobody did anything wrong in isolation — the interaction is broken.

Bloch's rule: if you haven't documented exactly which methods call which and what subclasses may override, make the class `final`. The cost of `final` is just "use composition instead." The cost of a wrong inheritance hierarchy is silent breakage.

**Template Method — why the workflow method is `final`**

Template Method is the pattern that enforces this discipline structurally:

```java
abstract class HiringProcess {
    final void run() {          // ← final: the workflow is locked
        screen();
        interview();
        sendOffer();
    }

    abstract void interview();  // ← subclasses only fill in this step
    void screen()    { /* default */ }
    void sendOffer() { /* default */ }
}
```

`run()` is `final` because it *is* the contract — the sequence `screen → interview → sendOffer` is the invariant the parent owns. Without `final`, a subclass could override `run()` and skip screening entirely:

```java
@Override void run() {
    sendOffer(); // caller using HiringProcess never knows screening was skipped
}
```

The parent is saying: **you get to fill in the blanks (`interview()`), but you don't get to rewrite the sentence.**

Item 19 and Template Method are the same idea from different angles. Item 19 says "be explicit about what subclasses can touch." Template Method *enforces* that by making the workflow `final` and only exposing the intended hooks as `abstract`.

---

## Composition over inheritance

When you want code reuse without a genuine is-a, composition is the right tool.

**Inheritance for reuse — the trap:**

```java
// LoggingList "is-a" ArrayList? No. It wraps one.
class LoggingList<E> extends ArrayList<E> {
    @Override public boolean add(E e) { log(e); return super.add(e); }

    @Override public boolean addAll(Collection<? extends E> c) {
        c.forEach(this::log);
        return super.addAll(c);  // Bug: ArrayList.addAll() calls add() internally,
    }                             // so log() fires twice per element
}
```

**Composition — the fix:**

```java
class LoggingList<E> {
    private final List<E> delegate = new ArrayList<>();

    public boolean add(E e) { log(e); return delegate.add(e); }

    public boolean addAll(Collection<? extends E> c) {
        c.forEach(this::log);
        return delegate.addAll(c);  // delegate never calls back to our add()
    }
}
```

The wrapper controls every call path. No fragile-base-class surprise.

The canonical version of this trap is Bloch's `InstrumentedHashSet extends HashSet` (Effective Java Item 18): the subclass overrides `add` and `addAll` to count additions, but `HashSet.addAll` internally calls `add`, so via virtual dispatch the child's already-incremented `add` fires again — every batch insert is counted twice. See `FragileBaseClassAndComposition.java` for the double-count in code and the composition fix that eliminates it.

| Situation | Use |
| --- | --- |
| Genuine is-a with polymorphic substitution | Inheritance |
| Parent designed for extension (abstract, documented hooks) | Inheritance |
| You want to mix behaviour from two sources | Composition (Java only allows one `extends`) |
| You want to reuse code without is-a | Composition |
| You need to swap the implementation at runtime | Composition (hold a field, not a fixed parent) |
| Framework requires it (JUnit, Android Activity) | Inheritance |

---

## Java rules worth knowing

- **Single class inheritance** — a class can `extend` only one class. Reason: multiple class inheritance creates the diamond problem (whose method wins?). Java resolves it by permitting multiple `implements` but only one `extends`.
- **`final class`** — cannot be subclassed. Use when a class is not designed for extension: immutable value types, security-sensitive types. `String`, `Integer`, `LocalDate` are all `final`.

  **Why is `String` final?** Four reasons that reinforce each other:

  | Reason | What breaks without `final` |
  |--------|----------------------------|
  | **Immutability** | `String`'s contract is "once created, never changes." A subclass could override methods to mutate content, breaking that guarantee. |
  | **Security** | `String` carries class names, file paths, URLs, credentials. A malicious subclass could change its value mid-operation after a security check passed. |
  | **String pool / interning** | The JVM interns string literals into a shared pool. If subclasses existed, two "equal" strings might be different subclass instances — interning would be unsafe. |
  | **Performance** | `String` caches its `hashCode` after first computation. A mutable subclass would silently return stale hashes. Also, `final` lets the JIT de-virtualize `String` method calls (no dispatch needed). |

  The immutability + security reasons are the primary ones. The pool and perf reasons are consequences of the same decision.
- **`abstract class`** — cannot be instantiated directly; at least one method must be `abstract`. The right shape when you want a partial implementation plus enforced hooks.
- **Interface `default` methods (Java 8+)** — interfaces can provide implementation, but they have no constructors and no instance fields. Not the same as class inheritance. Use `default` for convenience wrappers over abstract methods; use an abstract class when you need state or a constructor.

---

## Where this appears in this repo

| Pattern | Inheritance used | Why it is correct |
| --- | --- | --- |
| `prototype/polymorphic/` | `Circle extends Shape`, `Rectangle extends Shape` | Genuine is-a taxonomy + polymorphic dispatch via `clone()` |
| `factory_method/` | `AndroidHiringProcess extends DeveloperHiringProcess` | Template method — parent owns the algorithm, child fills one hook |
| `builder/director_builder_gof/` | `JobOfferBuilder implements OfferConstructionSteps` | Interface inheritance — shared step contract, no state |
| `abstract_factory/` | `CheapChair implements Chair` | Interface inheritance — capability contract only |

Notice: every `extends` in this repo either (a) hooks into a template method or (b) participates in polymorphic dispatch. None of them say "I want to reuse some code." That is the bar.

---

## Quick recall

**Q. Three things inheritance gives you?**
A. Code reuse, polymorphism (substitutability with virtual dispatch), and taxonomy (formal is-a). Reaching for it for code reuse alone — without is-a — is the most common mistake.

**Q. Inheritance and polymorphism — which enables which?**
A. Inheritance enables polymorphism, not the other way around. The `extends` relationship sets up substitutability; polymorphism is what happens at the call site as a result.

**Q. Why must `super(...)` be the first line in a constructor?**
A. The parent must be fully constructed before the child can safely access `this`. Using `this` before `super(...)` is a compile error.

**Q. Is `super.method()` polymorphic? Is `super.super.method()` legal?**
A. No to both. `super.method()` hardcodes the parent class via `invokespecial`; calls *inside* that parent method remain polymorphic. `super.super.` does not exist in Java by design.

**Q. The LSP one-line test?**
A. If swapping a subtype for the parent forces any caller to change behaviour, the inheritance violates LSP. Casts, `instanceof` guards, and `UnsupportedOperationException` overrides are the visible smells.

**Q. Why does `InstrumentedHashSet extends HashSet` double-count, and what fixes it?**
A. `HashSet.addAll` internally calls `this.add`, which dispatches virtually to the child's already-incremented `add`. Composition fixes it: wrap a `Set` field — `delegate.addAll` calls `delegate.add`, never back into our class.

**Q. One place in this repo where `extends` is correct and why?**
A. `AndroidHiringProcess extends DeveloperHiringProcess`. The parent's `onboard()` is `final` (template method); the child supplies the `createDeveloper()` hook only. Substitutability holds — `HR` works against `HiringProcess` and neither child surprises it.
