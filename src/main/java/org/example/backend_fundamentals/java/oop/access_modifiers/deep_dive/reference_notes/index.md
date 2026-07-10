---
order: 10
---

# Reference Notes: Access Modifiers and Method Attributes

These are preserved detailed notes from the original long deep-dive page. Use the parent page for normal revision.

A walkthrough of every `public` / `protected` / `private` / `final` / `abstract` choice in the `factory_method/` package, and the general principles behind them. Use this to build an intuition for picking modifiers deliberately instead of reflexively.

---

## The guiding principle

> **Choose the most restrictive access that still lets the intended callers work. Use modifier choice to *communicate intent*, not just enforce it.**

Expanded:

1. **Start with the most restrictive (`private`), loosen only as needed.** Don't default to `public` and tighten reactively.
2. **Access modifiers are documentation.** `protected` signals "subclass territory" the way `final` signals "don't override." Picking the weakest option that compiles wastes a communication opportunity.
3. **Access and abstractness should agree.** A `public abstract class` with a `public` constructor is misleading — the constructor should match the "this is for subclasses" intent, not just the class's visibility.
4. **Think about who you force to know what.** A `public` member invites the world to depend on it. Once it has callers, you can't tighten it without breaking them.

Visibility hierarchy (Java):

```
private  <  package-private (no keyword)  <  protected  <  public
  ↓                   ↓                          ↓              ↓
same class     same package                subclasses      anyone
                                           + package
```

### Valid access modifiers for top-level vs nested classes

Before applying any modifier, it helps to know which ones are even legal depending on where the class sits.

**Top-level classes** (declared at the outermost level, not inside another class) accept only two modifiers:

| Modifier | Effect |
| --- | --- |
| `public` | Visible to everyone |
| *(none — package-private)* | Visible only within the same package |

`private` and `protected` are **compile errors** on a top-level class:
- `private` means "visible only within the enclosing class" — for a top-level class there is no enclosing class, so the concept does not exist.
- `protected` means "visible to subclasses and same package" — for a top-level class subclasses can live anywhere, making the semantics ambiguous. Java simply disallows it.

**Nested classes** (static nested, inner, local, anonymous) have broader options:

| Kind | Valid modifiers |
| --- | --- |
| Static nested class | `public`, `protected`, package-private, `private` — all four |
| Non-static inner class | `public`, `protected`, package-private, `private` — all four |
| Local class (inside a method) | None — visibility is implicitly limited to the enclosing block |
| Anonymous class | None — it's an expression, not a declaration |

The practical consequence: if you want to hide a class completely from other packages, make it package-private (no modifier) at the top level, or `private` as a nested class. You cannot use `private` on a top-level class no matter how much you want to.

---

## Walkthrough: every modifier choice in `factory_method/`

### `HiringProcess` (interface)

```java
public interface HiringProcess {
  Employee onboard();
}
```

| Element | Modifier | Why |
| --- | --- | --- |
| `interface HiringProcess` | `public` | The contract is meant to be used from anywhere (including `HR`, which may live elsewhere). Package-private would work today but arbitrarily limits future use. |
| `Employee onboard()` | *(implicit `public abstract`)* | Interface methods are `public abstract` by default. **You cannot write `private`, `protected`, or package-private on an interface method** — interfaces are pure contracts, and contracts don't have "partial" visibility. |

### `DeveloperHiringProcess` (abstract class)

```java
public abstract class DeveloperHiringProcess implements HiringProcess {

  private final EmailService emailService;
  private final OfferLetterService offerLetterService;

  protected DeveloperHiringProcess(EmailService e, OfferLetterService o) { ... }

  @Override
  public final Employee onboard() { ... }

  protected abstract Employee createDeveloper();

  private void checkBudget() { ... }
  private void provisionLaptop(Employee developer) { ... }
}
```

#### `public abstract class`

- **`public`** — subclasses may live in other packages (e.g., a future `sales/` subpackage). Public keeps options open.
- **`abstract`** — cannot be instantiated directly. Forces the `createDeveloper()` contract onto subclasses at compile time.

#### Fields — `private final`

```java
private final EmailService emailService;
private final OfferLetterService offerLetterService;
```

- **`private`** — strongest possible access. Subclasses **cannot** touch these fields directly. If a subclass needs email functionality, it does so indirectly via the template method that uses them.
  - *Why not `protected`?* Because no subclass needs direct access today. Giving it out would make every concrete creator dependent on the field names — making rename-refactors risky.
  - *Why not package-private?* Same reason: limit the blast radius of change.
- **`final`** — assigned exactly once, in the constructor. After that, never reassigned.
  - Prevents accidental reassignment later in method bodies.
  - Communicates "this is a permanent collaborator, not a mutable setting."
  - Helps thread safety: `final` fields have publishing guarantees under the JMM.

> **Principle:** fields default to `private final` unless you have a reason to widen.

#### Constructor — `protected`

```java
protected DeveloperHiringProcess(EmailService e, OfferLetterService o) { ... }
```

- **`protected`** — callable only by subclass constructors via `super(...)` and by code in the same package.
  - *Why not `public`?* An abstract class cannot be instantiated via `new` anyway. `public` would *lie* about the constructor's usage, and some linters (SpotBugs, Sonar) flag it.
  - *Why not package-private?* Would restrict subclassing to the same package. That's not what you want — future `sales/` subpackage subclasses would break.
  - *Why not `private`?* Would prevent subclassing entirely (subclass constructors couldn't call `super`). Only useful for sealed designs, not here.

> **Rule of thumb:** abstract-class constructors are almost always `protected`.

#### `onboard()` — `public final`

```java
@Override
public final Employee onboard() { ... }
```

- **`public`** — required. Implementing an interface method **cannot reduce visibility** (see "Java rules" section below). Since `HiringProcess.onboard()` is `public`, the implementation must also be `public`.
- **`final`** — this is the *template method*. Subclasses must not replace the algorithm. They plug in only at `createDeveloper()`.
  - Without `final`, a subclass could override `onboard()` and undo the entire pattern silently. `final` makes that impossible at compile time.
  - This is the **#1 place `final` carries design meaning** in this codebase.

> **Principle:** mark template methods `final`. The whole point of a template method is that the parent owns the algorithm.

#### `createDeveloper()` — `protected abstract`

```java
protected abstract Employee createDeveloper();
```

- **`protected`** — callable by:
  1. Subclasses (who need to *override* it — they provide the implementation).
  2. Code inside the same package (rarely used, but permitted).

  *Why not `public`?* This is a **hook**, not an API. External callers should go through `onboard()`, not reach past it to ask "please just create one."
  *Why not `private`?* Private methods **cannot be overridden**. The whole purpose of this method is to be overridden — `private` would defeat the Factory Method pattern.
  *Why not package-private?* Works today, but a future subclass in another package couldn't override it. Same reasoning as the constructor.

- **`abstract`** — no implementation here. Every concrete subclass is forced by the compiler to provide one, or it won't compile.

> **Rule of thumb:** subclass hook methods in a template-method pattern are `protected abstract`.

#### `checkBudget()` / `provisionLaptop()` — `private`

```java
private void checkBudget() { ... }
private void provisionLaptop(Employee developer) { ... }
```

- **`private`** — only `onboard()` (in the same class) calls these. They're implementation details of the algorithm, not hooks.
  - *Why not `protected`?* A subclass doesn't need to customize "how we check budget" or "how we provision a laptop." If one ever did — say, `AndroidHiringProcess` wanted to check a different budget — we'd promote those methods to `protected` *at that point*. Until then, `private` keeps the interface minimal.
  - *Why not package-private?* Same reason: they're not meant for anyone else, even same-package classes.

> **Principle:** a private method is a future hook waiting for justification. Don't promote it to `protected` until you have a real subclass that needs to override it.

### Services (`EmailService`, `OfferLetterService`)

```java
public class EmailService {
  public String createEmailAccount(Employee developer) { ... }
}
```

- **`public class`** — the hiring processes need to reference them, and `HR` / test code may need to construct them too.
- **`public` method** — the service's primary API.
- **Instance method (no `static`)** — lets the caller hold an instance reference, enabling:
  - Mocking in tests.
  - Swapping implementations later.
  - Future state (company domain, SMTP client, etc.) without changing method signatures.
- **No `final` class** — conceivable future subclassing (e.g., a `GmailEmailService extends EmailService`). If you don't see that coming, adding `final class EmailService` would be reasonable.

---

## Class-level modifier decisions: `final` and `static`

The walkthrough above covers method-level and field-level modifiers in depth. Two more decisions live at the *class* level and surface most clearly in the Builder pattern (`creational/builder/`): **`final` on the class itself** and **`static` on a nested class**. Plus a closely related cross-cutting principle: **`private` constructors as a construction chokepoint.**

### `final` on a class

```java
public final class JobOffer {
  private final int salary;
  // ... all fields private final, ctor private, only path is Builder.build()
}
```

**What it does:** prevents subclassing. For an immutable value class, this isn't a gratuitous restriction — it closes the only remaining hole in the immutability contract.

Without `final` on the class, even with `private final` fields and a `private` constructor, a subclass could:

- Add mutable fields (`private int extra; public void mutate() {...}`).
- Override `toString` / `equals` / `hashCode` to misbehave.
- Be substituted in by a reflective or generative path, and callers typed against `JobOffer` would never know they actually held an `EvilJobOffer`.

`final` on the class makes these impossible at compile time. This is **Effective Java Item 17 ("Minimize mutability"), rule #1**: *make the class final*. It's the same reason `String`, `Integer`, `LocalDateTime`, and most JDK value-like classes are `final`.

#### When *not* to mark a class `final`

| Scenario | Why `final` is wrong here |
| --- | --- |
| Builder hierarchy (EJ Item 2's advanced example: `Pizza` ↔ `NyPizza` / `Calzone`) | The product *must* be inheritable; mark it `abstract`, not `final`. |
| Domain inheritance (`Document` ↔ `PdfDocument` / `WordDocument`) | Same — the type is the parent of a hierarchy. |
| Framework requirements (Hibernate / Spring CGLIB proxies) | Some frameworks generate runtime subclasses. |
| Genuine extension points published as part of a public API | Extension is the contract. |

For everything else — flat data classes, builder products, value objects, single-purpose immutables — `final class` is the right default.

> **Principle:** if your design pursues immutability, `final class` closes the loop. Drop `final` only when inheritance is genuinely required by the design.

### `static` on a nested class

Java has two flavours of nested class:

| Kind | Declaration | Holds reference to enclosing instance? | Constructed as |
| --- | --- | --- | --- |
| **Static nested** | `static class Foo` | No | `new Outer.Foo(...)` |
| **Inner** (non-static) | `class Foo` | Yes (implicit) | `someOuter.new Foo(...)` |

For Builder pattern, `static` is **required** — for two compounding reasons:

1. **Chicken-and-egg with the enclosing instance.** A non-static inner class requires an existing enclosing instance to be constructed. The whole point of the Builder is to construct the *first* `JobOffer` — there is no enclosing instance yet. The compiler refuses `new JobOffer.Builder(...)` for a non-static nested class; you would need `someExistingOffer.new Builder(...)`, which is absurd ("give me a `JobOffer` so I can construct a `JobOffer`").
2. **Hidden retention of the enclosing instance.** Non-static inner classes silently hold a reference to the enclosing instance. If the Builder ever escapes the construction expression and gets retained, it pins the original `JobOffer` and its entire referent graph in memory — a classic source of leaks.

#### What "hidden retention" means in practice

The compiler adds an invisible field to every non-static inner class instance pointing back to the outer instance. You never write it; it is always there:

```java
public class JobOffer {
    private final String title;
    private final int salary;

    // Hypothetical — NON-static Builder (wrong)
    public class Builder {
        // Compiler silently inserts:
        // private final JobOffer JobOffer.this;   ← invisible, always present

        private String title;
        private int salary;
    }
}
```

Normally the Builder is created and discarded in a single expression, so this hidden pointer is never a problem:

```java
JobOffer offer = new JobOffer.Builder("Engineer", 100_000)
                     .bonus(20_000)
                     .build();
// Builder unreachable after this line — GC can collect it
```

The leak happens when the Builder *escapes* — gets stored somewhere and lives beyond that expression:

```java
JobOffer.Builder builder = new JobOffer.Builder("Engineer", 100_000);
JobOffer offer = builder.build();
cache.put("lastUsedBuilder", builder);   // Builder is now retained by the cache
```

Because `builder` holds the hidden pointer to the enclosing `JobOffer` instance, keeping `builder` alive also keeps that entire object alive — and everything it references:

```
cache
  └── builder
        └── [hidden field] → jobOfferTemplate (JobOffer)
                                ├── title
                                ├── salary
                                ├── bonuses  (List of Bonus objects)
                                └── department (Department)
                                        └── employees (List of 200 Employees)
                                                └── ...
```

The garbage collector follows every reference. As long as `cache` holds `builder`, and `builder` holds the hidden pointer, nothing in that entire tree can be collected — even if nothing else in your code references it. That tree is the **referent graph**. This is a memory leak: objects that should be dead are kept alive by a reference you didn't know existed.

A `static` nested class has no hidden pointer. If the same `builder` is cached, only its own fields are retained — not the outer `JobOffer` and everything reachable from it.

There is no upside to a non-static Builder. It needs no instance state from `JobOffer`; it only needs access to the private constructor and private static members, both of which `static` nested classes already get.

#### Generalising to all nested classes

**Default nested classes to `static`. Add the inner-class flavour only when you specifically need access to enclosing-instance state.**

The historical Java default of "non-static unless you say `static`" is a frequent source of memory leaks. Common offenders:

- Non-static `Listener` / `Runnable` / `Comparator` inner classes that outlive their enclosing instance.
- Anonymous inner classes declared in non-static contexts (they capture `this` of the enclosing instance silently).
- Helper classes nested for namespacing rather than for genuine instance binding.

When in doubt, write `static`. The compiler will tell you if you actually needed the enclosing instance — at which point you can drop `static` deliberately, with a reason.

### `private` constructors as a chokepoint

Both Builder and Factory Method (and Singleton, and static factory methods) use restricted constructors to channel construction through a single point. Worth naming as a cross-cutting principle:

| Pattern | Chokepoint | Why the constructor is private / restricted |
| --- | --- | --- |
| Singleton | `getInstance()` | Single shared instance |
| Builder | `Builder.build()` | Validation + immutability guarantee |
| Static factory methods (EJ Item 1) | `Foo.of(...)` / `Foo.valueOf(...)` | Named entry points; can cache; can return subtype |
| Factory Method (GoF) | `creator.createDeveloper()` | Caller depends on abstract product type, not the concrete one |

The shape is always the same: *no caller can write `new ConcreteType(...)` directly; they must go through the named chokepoint.* That makes whatever the chokepoint enforces — validation, caching, polymorphism — a **compiler-enforced guarantee**, not a convention.

> **Principle:** a private constructor signals "construction is funneled — go through the chokepoint." Always pair it with a documented entry point (factory method, builder, `getInstance`).

---

## Why the interface can't express some of these choices

One question that comes up repeatedly: *"Could I have used an interface instead of an abstract class?"* The table below shows what gets lost.

| Want to… | Abstract class | Interface |
| --- | --- | --- |
| Hold `private final` fields (services, config) | ✅ | ❌ Only `public static final` constants |
| Receive collaborators via `protected` constructor | ✅ | ❌ No constructors at all |
| Expose `protected` helpers to subclasses | ✅ | ❌ Methods are effectively `public` |
| Keep helpers `private` (not part of the API) | ✅ | ✅ Since Java 9 — but only for helpers used by `default` methods inside the interface |
| Mark the template method `final` | ✅ | ❌ `default` methods cannot be `final`; the subclass can always override |
| Force subclasses to implement a hook | ✅ (`abstract`) | ✅ (implicit abstract) |

Most of the modifier choices in `DeveloperHiringProcess` are things an interface can't express. That's the concrete reason the GoF Factory Method pattern uses an abstract class. Modern Java with `default` methods narrows the gap, but state + `final` template methods still require a class.

---

## Java rules that constrain modifier choice

### You cannot *reduce* visibility when overriding or implementing

If a parent method is `public`, the child implementation must also be `public`. You can **widen** visibility (`protected` → `public`) but not narrow it. This is why `onboard()` in `DeveloperHiringProcess` is `public` — it's implementing `HiringProcess.onboard()`, which is `public`.

### `private` methods cannot be overridden

A `private` method is invisible to subclasses, so declaring one with the same signature in a subclass creates a *new* method, not an override. This is why the factory-method hook is `protected`, never `private`.

### `static` methods cannot be overridden, only *hidden*

A `static` method with the same signature in a subclass doesn't override — it shadows. Virtual dispatch doesn't apply. This is why Factory Method's hook must be an **instance** method.

### Interface methods cannot be `protected` or package-private

Interfaces are pure contracts. Every non-`private` method is implicitly `public`. If you need `protected` visibility, you need an abstract class.

### Interface methods cannot be `final`

Interface `default` methods may always be overridden by implementers. This is a genuine limitation: if you need "template method that cannot be replaced," you need a `final` method on an abstract class.

### Constructors cannot be `abstract`

They also cannot be `final`. Constructors only allow access modifiers.

### `protected` vs package-private — the exact difference

Both restrict access to a subset of the codebase, but the subsets are different:

| | Same class | Same package | Subclass in **different** package | Unrelated class in different package |
| --- | --- | --- | --- | --- |
| `private` | ✅ | ❌ | ❌ | ❌ |
| package-private | ✅ | ✅ | ❌ | ❌ |
| `protected` | ✅ | ✅ | ✅ | ❌ |
| `public` | ✅ | ✅ | ✅ | ✅ |

The one difference between `protected` and package-private: **`protected` extends access to subclasses across package boundaries**.

```java
package com.company.animals;

public class Animal {
    int packagePrivateField;      // visible inside com.company.animals only
    protected int protectedField; // visible inside com.company.animals + any subclass anywhere
}
```

```java
package com.company.animals;      // same package

public class Dog extends Animal {
    void test() {
        packagePrivateField = 1;  // OK — same package
        protectedField = 1;       // OK — same package
    }
}
```

```java
package com.company.police;       // different package

public class PoliceDog extends Animal {
    void test() {
        packagePrivateField = 1;  // COMPILE ERROR — different package, not a subclass exemption
        protectedField = 1;       // OK — subclass, even across packages
    }
}
```

```java
package com.company.police;       // different package, NOT a subclass

public class SomeOtherClass {
    void test(Animal a) {
        a.packagePrivateField = 1;  // COMPILE ERROR
        a.protectedField = 1;       // COMPILE ERROR — not a subclass
    }
}
```

**The mental model:**
- Package-private = "visible to my colleagues in the same package."
- Protected = "visible to my colleagues in the same package, plus any subclass that extends me — wherever they live."

### `protected` across packages — the two-condition rule

The simple summary "protected = same package OR any subclass" is correct for **declaring** access. But for **invoking** a protected member from a different package, Java enforces a second, stricter condition that surprises most people. Both must hold:

1. The accessing class must be a **subclass** of the class declaring the protected member.
2. The access must go through a reference whose **declared type** is the accessing class itself (or a subtype of it) — not the parent type, not a sibling subclass type.

The first is the well-known rule. The second is the trap.

**Critical:** access checks happen at **compile time**, on the **declared type** of the reference. The runtime type of the object the variable points to is irrelevant for access checking.

#### Three scenarios — see the demo code

Live demos are in `java/foundations/access_modifiers/other/`. Same parent class (`ProcessTemplate`), same protected method (`doWork()`), just three different callers.

**Scenario A — unrelated class in a different package** (`OtherPackageRunner`):

```java
// in package .access_modifiers.other
public class OtherPackageRunner {              // does NOT extend ProcessTemplate
    public void demo() {
        ProcessTemplate p = new ReportProcess(...);
        p.execute();    // ✅ public
        // p.doWork();  // ❌ compile error — not a subclass, fails condition 1
    }
}
```

**Scenario B — subclass, but reference is parent or sibling type** (`OtherSubclass.scenarioB_...`):

```java
// in package .access_modifiers.other
public class OtherSubclass extends ProcessTemplate {  // IS a subclass — condition 1 holds
    public void demo() {
        ProcessTemplate parent = new ReportProcess(...);
        // parent.doWork();    // ❌ compile error — reference type is ProcessTemplate, not OtherSubclass

        ReportProcess sibling = new ReportProcess(...);
        // sibling.doWork();   // ❌ compile error — ReportProcess is a sibling, not OtherSubclass or its subtype
    }
}
```

This is the surprise. The accessing class IS a subclass. The runtime object IS a `ReportProcess`. Both feel like they should grant access. But condition 2 fails: the reference's **declared type** must be `OtherSubclass` or a subtype of it.

**Scenario C — subclass, reference is own type** (`OtherSubclass.scenarioC_...`):

```java
// in package .access_modifiers.other
public class OtherSubclass extends ProcessTemplate {
    public void demo() {
        this.doWork();      // ✅ 'this' is OtherSubclass
        super.doWork();     // ✅ direct inherited access
        OtherSubclass own = new OtherSubclass();
        own.doWork();       // ✅ reference type is OtherSubclass (the accessing class itself)
    }
}
```

Both conditions hold. This is the *only* shape of cross-package protected access that compiles.

#### Why does condition 2 exist?

To stop sibling subclasses from breaking each other's encapsulation.

Imagine the rule were just "you're a subclass, you have access":

```java
public class Animal {
    protected void eat() { ... }
}

public class Cat extends Animal {
    @Override protected void eat() { /* Cat-specific delicate logic */ }
}

public class Dog extends Animal {
    void mess(Animal a) {
        a.eat();   // if this compiled, a Dog could call Cat.eat() on a Cat through an Animal reference
    }
}
```

`Dog` and `Cat` are both subclasses of `Animal`, but they're unrelated to each other. Without condition 2, `Dog` could reach into a `Cat` instance via an `Animal` reference and invoke `Cat`'s protected internals. That's exactly the leak Java prevents.

The mental model: each subclass gets its own inherited copy of the protected member. The cross-package rule lets you use **your own copy** (via `this`, `super`, or your-own-type references) — not reach into someone else's.

#### Quick lookup

| Caller's relationship to declaring class | Reference type | Allowed (cross-package)? |
| --- | --- | --- |
| Unrelated | anything | ❌ |
| Subclass | parent type | ❌ (Scenario B) |
| Subclass | sibling-subclass type | ❌ (Scenario B) |
| Subclass | own type, `this`, or `super` | ✅ (Scenario C) |
| (Same package, any caller, any reference) | — | ✅ (the simple rule) |

**Why this matters for patterns:**
- `DeveloperHiringProcess`'s constructor and `createDeveloper()` are `protected` specifically so subclasses in *other* packages (a hypothetical `sales/` package) can call `super(...)` and override the hook. Package-private would silently break any such subclass.
- `EmailService` and `OfferLetterService` fields are `private` (not `protected`) because no subclass should ever reach the parent's services directly — they go through the template method.

---

## Common traps

1. **Everything `public` by default.** The lazy choice. Leaks implementation details; invites coupling.
2. **`public` constructors on abstract classes.** Technically legal, semantically wrong. Use `protected`.
3. **Making overridable methods `private`.** Doesn't compile as an "override" — just creates a shadowed method. Use `protected` or `package-private`.
4. **Not marking template methods `final`.** A subclass silently replaces the algorithm and nobody notices until it breaks production.
5. **`protected` fields** without good reason. Subclasses coupling to parent fields is a common refactoring pain. Prefer `private` with `protected` getters / setters, or better, don't expose at all.
6. **Forgetting `@Override`.** Annotation is optional but catches typos and signature mismatches immediately. Always add it when overriding or implementing.
7. **Using `final` on an interface method.** Compile error — flags that you wanted an abstract class.
8. **Using `static` for shared logic when instance methods would work.** Static calls can't be overridden or mocked. Prefer instance helpers held as fields.
9. **Non-static nested classes when no enclosing-instance access is needed.** Silent memory hazard — the inner class pins the enclosing instance. Default nested classes to `static` and drop the keyword only when you genuinely need enclosing-instance state.
10. **Forgetting `final` on an immutable value class.** Even with `private final` fields and a `private` constructor, a subclass can re-introduce mutability or break the `equals` / `hashCode` / `toString` contracts. `final class` is the closing brace on the immutability guarantee — Effective Java Item 17 rule #1.
11. **Mixing `final` fields with fluent setters in a Builder.** Required fields should be `final` *and* set in the Builder constructor. Optional fields should be non-final *and* exposed as fluent setters. Trying to do both at once produces "cannot assign to final variable" at every setter call.
12. **Assuming "subclass = full protected access" across packages.** A subclass in a different package can call `protected` only through a reference whose declared type is the subclass itself (or a subtype). Calls through a parent-typed or sibling-typed reference fail to compile, even when the runtime object is a real subclass instance. See the "two-condition rule" section for the full breakdown.

---

## Quick decision heuristic

When introducing a new member:

```
Start with: private
↓
Does a subclass genuinely need to override or call it?
  Yes → protected
  No  → stay at private

Does code outside this package need to use it?
  Yes → public
  No  → stay at protected / package-private

Is this a method that should not be overridden?
  Yes → add final

Is this a method that must be overridden (no sensible default)?
  Yes → add abstract (requires an abstract class or interface)

Does the method access instance state?
  Yes → instance method
  No  → consider static (utility) — but beware of mocking / testability
```

For fields, default to `private final`. Widen only when pressure appears.

For classes, default to package-private unless outside packages genuinely need to see them. (Lots of public classes in a codebase is a coupling smell.)

For class-level decisions:

```
Is this class an immutable value / data carrier?
  Yes → mark it final (close the immutability loop)
  No, but no inheritance is planned → still consider final
  No, inheritance is part of the design → abstract, not final

Is this class nested inside another?
  Does it need access to enclosing-instance state?
    No  → static (the modern default)
    Yes → non-static (inner) — and accept the implicit reference

Is this class meant to be constructed only via a chokepoint
(builder, factory method, getInstance)?
  Yes → make the constructor private (or as restricted as possible)
        and document the chokepoint as the only entry
```

---

## Applying it: map back to `factory_method/`

Every modifier in `DeveloperHiringProcess` is the output of running the heuristic above:

| Member | Modifier | Why the heuristic landed here |
| --- | --- | --- |
| `emailService`, `offerLetterService` | `private final` | Default; no subclass needs direct access. |
| Constructor | `protected` | Subclasses need to `super(...)` it; public would be misleading on an abstract class. |
| `onboard()` | `public final` | `public` forced by interface contract; `final` because it's a template method. |
| `createDeveloper()` | `protected abstract` | Must be overridable (not `private`); not an external API (not `public`). |
| `checkBudget()`, `provisionLaptop()` | `private` | Default; no current need for subclass customization. |

If any of those needs change, the modifier changes with it — **the modifier is a decision, not a default**.

---

## Related topics

- **SOLID principles** — SRP and DIP directly drive many of the modifier decisions here (single responsibility → private helpers; dependency inversion → depend on the interface, not the concrete).
- **Abstract class vs Interface** — the "Why the interface can't express some of these choices" section above is a preview; the full comparison is its own topic.
- **Design Thinking Process** — "abstraction shape follows from state" is a companion principle: the right modifier choice often follows from asking what state the class holds and who needs it.
