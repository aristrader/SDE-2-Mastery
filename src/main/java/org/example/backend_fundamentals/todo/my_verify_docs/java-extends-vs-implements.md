# Java: `extends` vs `implements`

A reference for the difference between `extends` and `implements` in Java, plus related topics (inheritance, interfaces, polymorphism, generics, design tradeoffs) with questions to explore.

---

## TL;DR

- **`extends`** — inheritance. A class inherits from a class, or an interface inherits from another interface.
- **`implements`** — contract fulfillment. A class provides concrete behavior for an interface.

| | `extends` | `implements` |
|---|---|---|
| Class → Class | ✅ (single only) | ❌ |
| Class → Interface | ❌ | ✅ (multiple allowed) |
| Interface → Interface | ✅ (multiple allowed) | ❌ |
| Inherits state (fields) | ✅ | ❌ (only `public static final` constants) |
| Inherits method bodies | ✅ | Only `default` / `static` methods |
| Must override abstract methods | Only if parent is abstract | ✅ all non-default methods |

---

## Syntax Examples

```java
// class extends class — single inheritance, inherits fields + methods
class Dog extends Animal { }

// class implements interface(s) — multiple allowed, must provide bodies
class Dog implements Runnable, Serializable { }

// interface extends interface(s) — multiple allowed
interface SmartDog extends Runnable, Comparable<Dog> { }

// combined: one superclass, many interfaces
class Dog extends Animal implements Runnable, Serializable { }

// abstract class — can extend and implement
abstract class Shape implements Drawable { }
```

---

## Rules of Thumb

- Pick `extends` when reusing an implementation ("is-a specialized kind of").
- Pick `implements` when satisfying a contract ("can do X"). Prefer this — Java allows multiple interfaces but only one superclass.
- An interface can `extend` many interfaces because it's just widening the contract, not inheriting state.
- Favor composition over inheritance when the "is-a" relationship is weak.

---

## Example from this codebase

Stage classes like `OcrStage`, `LivenessStage`, `FaceMatchStage` **implement** `VerificationStage` (the contract). None of them `extends` a shared base class — this keeps the pipeline flexible because `StageExecutor` only cares about the interface, not any shared state.

```java
// verify/stage/core/VerificationStage.java
public interface VerificationStage {
    boolean canExecute(VerificationAggregateRequest request);
    CompletableFuture<VerificationResult> execute(...);
    void insert(VerificationAggregateResultInternal result, VerificationResult stageResult);
}

// verify/stage/impl/OcrStage.java
public class OcrStage implements VerificationStage { ... }
```

---

## Questions to Explore Later

### Basics
1. Why can Java classes only `extend` one class but `implement` many interfaces?
   (Answer hint: "diamond problem" — ambiguity when inheriting state from multiple classes.)
2. Can an interface have fields? What happens if it does?
   (They're implicitly `public static final`.)
3. What's the difference between an abstract class and an interface in modern Java (post-Java 8 `default` methods)?
4. What does `extends` mean for generics — e.g. `List<? extends Number>` — and why is it different from class-level `extends`?
5. Can an interface `extend` a class? Why or why not?
6. What happens if two interfaces you implement define the same `default` method? How is the conflict resolved?

### Inheritance Mechanics
7. What gets inherited when you `extend`? (Fields, methods, static members, constructors?)
8. Why aren't constructors inherited? How does `super(...)` work?
9. What's the order of constructor calls in a deep class hierarchy?
10. What's the difference between overriding, overloading, and hiding (for static methods)?
11. What does `final` mean on a class vs a method vs a field — and why would you use each?
12. What's covariant return types? (Override returning a more specific type.)

### Interfaces Deep Dive
13. What are `default` methods and why were they added in Java 8?
14. What are `static` methods on interfaces used for? (Hint: factory methods, utilities.)
15. What are `private` methods on interfaces (Java 9+) for?
16. What is a `sealed` interface (Java 17+) and when would you use one?
17. What's a marker interface? (E.g. `Serializable`, `Cloneable`.) Are they still idiomatic, or have annotations replaced them?
18. Functional interfaces (`@FunctionalInterface`) — what makes one "functional" and how does that connect to lambdas?

### Polymorphism & Design
19. What's the Liskov Substitution Principle and how does it relate to `extends`?
20. Why is "composition over inheritance" a common guideline? When does inheritance still win?
21. What's the Template Method pattern — and why does it require `extends`?
22. What's the Strategy pattern — and why does it pair naturally with `implements`?
23. How does `instanceof` work with interfaces vs classes?
24. Pattern matching for `instanceof` (Java 16+) and switch patterns (Java 21) — how do sealed types + pattern matching replace some uses of inheritance?

### Access Modifiers & Visibility
25. What can a subclass do with `protected` members of its parent?
26. When you override a method, can you make it less accessible? (No — covariance of visibility.)
27. Can you override a `private` method? (No — it's not polymorphic; you'd just be hiding it.)

### Generics & Bounds
28. Difference between `<T extends Number>` and `<T extends Comparable<T>>`?
29. Why does generics' bound always say `extends` even for interfaces? (Java reuses the keyword.)
30. What's the difference between `? extends T` (covariance) and `? super T` (contravariance)?
31. Why can't you do `new T()` in generic code? (Type erasure.)

### This Codebase (my-verify)
32. Why does `VerificationStage` not have any default implementations — what would be the tradeoff of adding them?
33. Where are `abstract class` patterns used in this repo, and why were they chosen over interfaces?
34. Could Spring's `@Service` beans be interfaces with multiple implementations selected by profile? How is that wired?
35. What does `@Override` actually enforce, and why should every override have it?

### Gotchas
36. What happens if the parent class adds a new method that conflicts with a method in the subclass?
37. Why is `Object` the implicit parent of every class?
38. What is the "fragile base class" problem?
39. Why can a class extend a `final` class never — and what's the alternative?
40. Difference between `implements Comparable<T>` and `implements Comparator<T>`? (Natural order vs. external order.)

---

## Related Topics

- **Abstract classes** — when the "some implementation + some contract" middle ground wins.
- **Mixins & traits** — what other languages offer that Java approximates with default methods.
- **Sealed classes/interfaces (JEP 409)** — restricting the hierarchy, enabling exhaustive pattern matches.
- **Records (JEP 395)** — implicitly `final`, can implement interfaces but cannot extend another class.
- **Anonymous classes & lambdas** — runtime-implementing an interface without a named class.
- **Dynamic proxies / `Proxy.newProxyInstance`** — implementing an interface at runtime (used by Spring AOP, Feign clients in this repo).
- **SOLID principles** — especially Liskov (L) and Interface Segregation (I), which directly motivate when to `extend` vs `implement`.

---

## Further Reading

- *Effective Java* (Joshua Bloch) — Items 18 (favor composition), 19 (design for inheritance or prohibit it), 20 (prefer interfaces to abstract classes), 21 (design interfaces for posterity), 22 (interfaces for types only).
- Java Language Spec §8.1.4 (class extension) and §9.1.3 (interface extension).
- JEP 181 (nest-based access), JEP 409 (sealed classes), JEP 395 (records).
