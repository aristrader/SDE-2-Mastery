---
order: 20
---
# Abstraction — the details

> One of the four OOP pillars. Often confused with encapsulation — they are different things.

---

## Two senses of the word (often conflated)

1. **Conceptual abstraction** — hiding *how* behind *what*. A `List` abstracts over array vs linked list internals. `HiringProcess.onboard()` abstracts over whether you're hiring Android or Backend. The caller doesn't need to know the mechanism.
2. **Java abstraction mechanisms** — the `abstract` keyword, `interface`. These are the *tools* for expressing conceptual abstraction. The concept exists independently of the keyword.

Confusing the two leads to mistakes like "we have an interface therefore we have good abstraction" — an interface whose single implementation is obvious and never swapped is just boilerplate, not meaningful abstraction.

---

## Abstraction vs Encapsulation — they are different

| | Abstraction | Encapsulation |
| --- | --- | --- |
| Hides | Complexity — you don't need to know *how* | Internal state — you can't *touch* this directly |
| Achieved by | Interfaces, abstract classes, naming | Access modifiers, private fields, constructors |
| Analogy | Car steering wheel — you don't need to know the steering column mechanics | Locked dashboard — you cannot access the internals directly |
| Goal | Simplify the caller's mental model | Protect internal consistency |

Both work together. A `JobOffer` class *encapsulates* its fields (private, validated) and *abstracts* the concept of a job offer (callers work with salary and bonuses, not raw database rows).

---

## Leaky abstraction

An abstraction leaks when the caller's code depends on something the abstraction was supposed to hide. The test:
**if you swap the implementation behind the abstraction, do callers break?** If yes, leaky.

### Joel's Law

Joel Spolsky's *Law of Leaky Abstractions* (2002): **all non-trivial abstractions, to some degree, are leaky.** That's not pessimism — it's a calibration. The goal isn't perfection; it's deciding which leaks you can live with and which you make explicit in the contract.

### Four flavors of leak

**1. Type leaks — caller has to cast.**

```java
Object getItem(int i);          // leaks: caller must cast
List<String> getItem(int i);    // doesn't leak
```

Casts are the most visible leak. The abstraction is screaming "you actually need to know what's inside me." Same for raw types and downcasts on supposedly polymorphic returns.

**2. Performance leaks — Big-O bleeds through.**

`ArrayList` and `LinkedList` both implement `List`; both satisfy `get(i)`. But `ArrayList.get` is O(1) and `LinkedList.get` is O(n). Code in tight loops encodes the assumption silently — swap the implementation and the code is correct but unusably slow. The compiler can't catch this; only documentation can.

**3. Behavioral leaks — exceptions, threading, mutation, ordering.**

```java
List<Integer> immutable = List.of(1, 2, 3);
immutable.add(4);   // compiles; UnsupportedOperationException at runtime
```

The type is `List`, but the underlying mutability/thread-safety/iteration-order/exception-types vary by implementation: `HashMap` vs `ConcurrentHashMap`, `HashSet` vs `LinkedHashSet` vs `TreeSet`, a `Repository` that throws `SQLException` from one impl and `MongoTimeoutException` from another. The compiler can't catch any of this; the contract has to spell it out.

**4. Network/IO leaks — distance and failure bleed through.**

A "remote object" call or ORM lazy-load looks like a method call but actually involves network latency, retries, and timeouts. JPA's `user.getOrders()` looks free; in production it's an N+1 query problem. The relational/network reality leaks through as performance.

### Quick detection test

Walk through these on any API you're using or designing:

- Can callers swap one implementation for another without code changes? *No → leaky.*
- Are they relying on specific exceptions, ordering, performance, threading, or null behaviour the type doesn't promise? *Yes → leaky.*
- Do they ever cast or `instanceof`-check a return value? *Yes → very likely leaky.*
- Does the abstraction have "footguns" — runtime exceptions for operations that compile fine? *Yes → leaky (the type system is over-promising).*

### What to do anyway

The lesson isn't *don't abstract.* It's:

- **Make likely leaks explicit in the contract.** Document Big-O, threading, ordering, exception types — they're part of the API even when the type system can't enforce them. `ArrayList`'s docs publish "constant-time random access" for exactly this reason.
- **Pick the right abstraction level for callers.** `List` for iteration, `Stream` for lazy composition, `Page<T>` for paging. Wrong level → callers fight the abstraction → leaks.
- **Return the narrowest interface that satisfies the use case** (Effective Java Item 64). Don't return `ArrayList` if `List` would do; don't return `Object`.
- **Beware "magic" abstractions** — Spring's `@Transactional` silently fails on self-invocation through `this.method()` because the proxy isn't involved. Magic that hides too much usually leaks somewhere unexpected.

See `LeakyVsCleanAbstraction.java` in this folder for the cast-test in code.

---

## Levels of abstraction in this repo

| Level | Example | What it hides |
| --- | --- | --- |
| Interface | `HiringProcess`, `FurnitureSetFactory`, `Employee` | Entire implementation — callers know nothing concrete |
| Abstract class | `DeveloperHiringProcess` | The template (onboard flow) — exposes only the `createDeveloper()` hook |
| Concrete class | `AndroidHiringProcess`, `CheapChair` | Nothing further — this is the implementation |

GoF's first design principle: *program to an interface, not an implementation.* In practice: hold the most abstract type that still gives you what you need.

- `HR` holds `HiringProcess`, not `AndroidHiringProcess` — it only needs `onboard()`.
- `furnishRoom` takes `FurnitureSetFactory`, not `CheapFurnitureFactory` — it only needs `createChair()` and `createSofa()`.
- The loop in `PolymorphicPrototypeRun` holds `List<Shape>`, not `List<Circle>` — it only needs `clone()`.

---

## Quick recall

**Q. Abstraction vs encapsulation in two sentences (no shared "hide")?**
A. Abstraction simplifies what callers must understand by exposing only the *what*, not the *how*. Encapsulation protects internal state by controlling who can read or modify it.

**Q. The one-line test for a leaky abstraction?**
A. If swapping the implementation forces callers to change, the abstraction leaks.

**Q. Joel's Law?**
A. All non-trivial abstractions, to some degree, leak. Pick which leaks you tolerate and document the rest.

**Q. Why is `Object getItem(int)` leakier than `String getItem(int)`?**
A. `Object` forces callers to cast, which means they must know the actual stored type — the storage decision is no longer hidden.

**Q. "Program to an interface, not an implementation" — what does that look like in this repo?**
A. Hold the most abstract type that still does the job. `HR` holds `HiringProcess` (not `AndroidHiringProcess`); `furnishRoom` takes `FurnitureSetFactory` (not `CheapFurnitureFactory`); the loop in `PolymorphicPrototypeRun` holds `List<Shape>` (not `List<Circle>`).

---

## Related topics

- **Encapsulation** — its frequently-confused sibling.
- **Abstract Class vs Interface** — the Java mechanics for expressing abstraction.
- **SOLID — DIP** — "depend on abstractions" makes "what *is* an abstraction in this code?" a real question.


