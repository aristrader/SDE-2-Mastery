# Dependency Inversion (DIP) vs. Dependency Injection (DI) — the details

> Two ideas that constantly get conflated. They are **related but distinct**, and you can have one without the other. Both are central to almost every pattern that enables testing and substitution.

---

## Dependency Inversion Principle (DIP) — the **principle**

The "D" of SOLID. Two clauses, in Robert C. Martin's original phrasing:

1. **High-level modules should not depend on low-level modules.** Both should depend on **abstractions**.
2. **Abstractions should not depend on details.** Details should depend on abstractions.

Plainer phrasing: *don't let your important code (orchestrators, business logic) reach down to depend on specific implementations. Have both depend on an interface in the middle.*

### What "inversion" means — the arrow flip

In naive OOP, dependency arrows run "downhill" from high-level → low-level:

```
HR ──▶ AndroidHiringProcess
HR ──▶ BackendHiringProcess          (HR is locked to specific concrete classes)
HR ──▶ IosHiringProcess
```

DIP says: introduce an abstraction in the middle and **flip the bottom arrow** so concrete classes point *up* at the abstraction:

```
                   HiringProcess (interface)
                   ▲       ▲
                   │       └── implements ── AndroidHiringProcess, BackendHiringProcess, ...
HR ──── depends on ┘
```

Both ends point at the same abstraction. High-level code no longer reaches *down* at low-level details; details point *up* at a stable abstraction. That arrow flip is the "inversion."

### Where DIP lives in the production factory_method code

Exactly here:

```java
// HR.java (production variant)
private final HiringProcess hiringProcess;   // ← interface, not the concrete abstract class
```

If this were `private final DeveloperHiringProcess hiringProcess;` (as in the *learning* variant), DIP would be partially violated — HR would be locked to the developer branch even though `DeveloperHiringProcess` is itself an abstract class. The interface above is the *most abstract* suitable type for HR's role; depending on it, not the abstract class, is what makes the dependency DIP-clean.

---

## Dependency Injection (DI) — the **technique**

A technique for *how* a class receives its dependencies — push them in from outside, instead of constructing them internally. Three common flavours in Java:

| Flavour | What it looks like | When to use |
| --- | --- | --- |
| **Constructor injection** | `HR(HiringProcess hp) { this.hp = hp; }` | Default. Fields can be `final`; dependencies guaranteed at construction. |
| **Setter injection** | `void setProcess(HiringProcess hp)` | Optional dependencies, or when breaking circular dependencies. |
| **Field injection** | `@Autowired HiringProcess hp;` (Spring) | Convenient but harder to test, fields cannot be `final`, dependencies hidden — usually avoided in modern style. |

DI is **just the mechanic** of pushing dependencies in. It says nothing about whether what gets pushed in is an abstraction or a concretion.

---

## DIP vs DI — they are not the same thing

| Question | DIP | DI |
| --- | --- | --- |
| What is it? | A **principle** | A **technique** |
| What does it govern? | *What* you depend on (abstraction vs. concretion) | *How* the dependency arrives |
| Where does it live? | In **type signatures** of fields / parameters | In **construction** code |
| Can you have one without the other? | **Yes** — see matrix below | **Yes** |

### The four combinations

| | DI ✓ | DI ✗ |
| --- | --- | --- |
| **DIP ✓** | **Best practice.** HR receives a `HiringProcess` interface in its constructor. | Rare. HR holds an interface field but constructs the implementation internally (reflection, service locator). DIP honoured but no testability win from DI. |
| **DIP ✗** | **The subtle trap.** HR gets injected, but the parameter type is concrete. *"I'm using DI"* — yet the code still couples to a specific implementation. | The classic mess. HR does `new AndroidHiringProcess()` inside its own constructor. |

The bottom-right (neither) is where most legacy code lives. The top-left (both) is where you want to be.

---

## How to spot violations

**DIP violations:**

- A high-level class names a concrete class in a field, parameter, or return type.
- The class can't be unit-tested without spinning up real low-level dependencies.
- Adding a new variant of a low-level component requires editing high-level code.

**DI without DIP (the trap):**

- A class accepts a dependency via its constructor (DI ✓) but the parameter type is concrete.
- *"I'm using DI"* feels true, yet the type signature still couples high-level to low-level.
- Easy fix: change the parameter type from `ConcreteClass` to `AnInterface`.

**DIP without DI (rare):**

- Code holds an interface field but constructs the implementation internally via `new` or a static lookup.
- Principle honoured (field type), testability lost (no seam to inject a mock).

---

## TL;DR

> **DIP says:** *depend on abstractions, not concretions.*
> **DI says:** *don't construct your dependencies — receive them.*
>
> They reinforce each other in modern code, but answer different questions: DIP is *what type*, DI is *how it arrives*. In casual talk it doesn't matter; in design discussions, the distinction avoids bad refactors.

---

## DI in practice — read this before the Pattern Selection Exercise

### The pain DI relieves

```java
// Hardcoded — class welds itself to a concrete implementation:
public class SignupService {
  private final EmailSender sender = new EmailSender();
}

// Injected — collaborator passed in from outside:
public class SignupService {
  private final EmailSender sender;
  public SignupService(EmailSender sender) {
    this.sender = sender;
  }
}
```

When a class news-up its own collaborators, you can't:

- Replace the real implementation in tests (no place to inject a mock).
- Swap implementations per environment (dev vs prod, local SMTP vs SES).
- Configure the dependency from outside (timeouts, URLs, retries).
- Reuse the class with a different collaborator.

DI moves construction out of the using class into a separate **wiring layer** — either explicit setup in `main()` (manual DI) or a container like Spring.

### Why constructor injection wins over the other flavours

Constructor injection is the modern default — and Spring's official recommendation since v4 — for these reasons:

1. **Final fields.** Dependency cannot be reassigned — JMM publication guarantees + thread safety + clear intent.
2. **No half-initialised objects.** The object cannot even *exist* without its full set of dependencies.
3. **Visible dependency list.** The constructor signature is the one place to read what a class needs.
4. **Testable without a framework.** `new SignupService(mockSender)` works in any test; no Spring required.
5. **Spring-agnostic.** The class doesn't depend on `@Autowired` or any framework annotation. Spring detects the single constructor automatically.

### `@Autowired` vs DI — they are at different levels

A common confusion worth nailing down:

| | DI | `@Autowired` |
| --- | --- | --- |
| What is it? | A **technique** — push dependencies in from outside | A **Spring annotation** that tells the Spring container how to inject a value |
| Where does it live? | Anywhere — plain Java, manual `main()` wiring, or any framework | Only in Spring (and Spring-compatible) projects |
| Is one necessary for the other? | No. You can do DI with no annotations at all (manual DI). | `@Autowired` is one *implementation* of DI — Spring's. |

Three subtleties about `@Autowired` worth knowing:

1. **Modern Spring (4.3+) auto-injects a single constructor.** No annotation needed:
   ```java
   @Service
   public class SignupService {
     private final EmailSender sender;
     public SignupService(EmailSender sender) { this.sender = sender; }
   }
   // No @Autowired anywhere. Spring detects the single constructor and uses it.
   ```
   Adding `@Autowired` here is redundant. Older Spring code carries it as habit; new code should drop it.

2. **`@Autowired` on a field = field injection (avoid).**
   ```java
   @Service
   public class SignupService {
     @Autowired private EmailSender sender;   // field injection
   }
   ```
   The downsides: cannot make the field `final`; dependencies hidden from constructor signature; only constructible via Spring, i.e. impossible to unit-test without the framework. Tolerated historically; avoid in new code.

3. **`@Autowired` on a setter = setter injection (rare).** Used for genuinely optional dependencies or to break circular dependencies. Modern code expresses optional dependencies as `Optional<Foo>` constructor parameters or with `@Nullable`, not as setters.

**Bottom line:** in modern Spring, prefer constructor injection with no `@Autowired` annotation at all. The constructor itself is the DI mechanism; the annotation is a Spring-internal hint that's no longer needed.

### Inversion of Control — the broader principle DI is a special case of

DI is a specific case of **Inversion of Control (IoC)**: instead of a class controlling how its dependencies arrive, something else (the caller, a container) controls it. Hollywood Principle: *"Don't call us, we'll call you."*

IoC shows up beyond construction too — the JVM calling `main()`, Spring calling `@PostConstruct`, JUnit calling `@Test` methods. DI is the *construction-time* face of IoC.

### How DI relates to the other creational patterns

Once DI is in your codebase, several patterns from this repo shift in role:

| Pattern | What DI does to it |
| --- | --- |
| **Singleton** | Spring beans default to **singleton scope** — the container is your singleton manager. `getInstance()`, double-checked locking, Bill Pugh holders all become unnecessary in a Spring app. Your Singleton implementations remain *correct*; they just become *unneeded* once Spring is wiring things. |
| **Factory Method** | Subsumed when the factory exists *only* to hand callers an instance. The container constructs and injects. Factories survive when they make non-trivial **runtime** choices (pick a strategy by input not known at wiring time). |
| **Builder** | Orthogonal — both coexist. DI gives you long-lived **collaborators**; Builder constructs short-lived **values**. A `JobOffer` is built per request; the `EmailSender` that delivers it is wired once at startup. |
| **Static factory methods (EJ Item 1)** | Spring's `@Bean` methods inside `@Configuration` classes *are* static factory methods. Side Quest A pays off here. |

### Spring's role beyond manual DI

Spring is a **DI container**. You annotate classes (`@Service`, `@Component`, `@Repository`, `@Controller`, or `@Configuration` + `@Bean`); Spring scans them at startup, builds the dependency graph, and instantiates everything in the right order.

Three things Spring adds beyond manual DI:

1. **Automatic graph construction.** No hand-written `main()` wiring code; component scanning does it.
2. **Bean scopes.** `singleton` (default — one per container), `prototype` (new instance per injection), `request` / `session` for web apps.
3. **Lifecycle hooks.** `@PostConstruct`, `@PreDestroy`, `ApplicationContextAware`, etc.

### Why "manual DI first, then Spring" is the recommended order

Two passes that teach different things:

- **Manual DI in plain `main()`** teaches that DI is *just constructor parameters*. No magic. Feel the pain when the graph gets bigger than five classes — you're typing `new` for every node.
- **Spring** teaches that the container *is* automation of the manual setup, plus scopes and lifecycle. Without doing manual DI first, Spring feels mysterious; afterwards, it feels like exactly what you'd build if you did the manual version a hundred times.

Skipping manual DI and going straight to Spring is the most common reason developers cargo-cult `@Autowired` everywhere without understanding what the annotation actually does.

---

## Concept-check questions to answer before coding

1. In `creational/factory/factory_method/`, `HR` already takes a `HiringProcess` in its constructor. So `HR` is *already* using DI. What about `EmailService` and `OfferLetterService` — are they DI-friendly too? What signal in `DeveloperHiringProcess`'s constructor tells you the answer?
2. Spring beans default to singleton scope. If `EmailService` is a Spring bean and `AndroidHiringProcess` and `BackendHiringProcess` are too, how many `EmailService` instances does Spring create? What does that imply for `EmailService`'s thread-safety obligations?
3. Suppose you write `class HR { private final HiringProcess hp = new AndroidHiringProcess(...); }`. List two reasons this is hard to test, and the *one* small change that fixes both.

---

## Suggested practice path (three stages)

- [ ] **Stage 1 — Audit pass on `factory_method/`.** Confirm everything is DI-clean already (most of it is). Identify the wiring code in `FactoryMethodRun.main()` — that's literally a manual DI bootstrap.
- [ ] **Stage 2 — Manual DI from scratch in a new package.** Small `signup-service` style example contrasting "bad" (internal `new`) with "good" (constructor-injected), with a hand-written `main()` doing the wiring. Feel the typing as the graph grows.
- [ ] **Stage 3 — Spring layer on top.** Add `@Service` / `@Component` annotations, an `@SpringBootApplication` entry point, watch the wiring code disappear. Demonstrate singleton vs prototype scope side-by-side.

---

## Pointers in this codebase

- `creational/factory_method/HR.java` — DIP applied (field is the `HiringProcess` interface) + DI applied (constructor injection).
- `creational/factory_method_basic/HR.java` — DIP only partially applied (field is `DeveloperHiringProcess`, the abstract class — an abstraction, but not the most abstract suitable one) + DI applied. Useful contrast — same DI mechanic, weaker DIP.
- `creational/Factory.md` — discusses the migration that introduced DIP at the HR layer (change #5 in the transition).

---

## Done when

You can answer these three concept-check questions from above without looking:

1. In `factory_method/`, is `EmailService` DI-friendly? What signal tells you?
2. If `EmailService` is a Spring singleton bean and both hiring-process subclasses hold it, how many `EmailService` instances exist? What thread-safety does that require?
3. Write `class HR { private final HiringProcess hp = new AndroidHiringProcess(...); }`. Name two testability problems and the one-line fix.

---

## Related topics

- **SOLID — DIP** — this doc is the deep dive; the SOLID doc has the one-line summary.
- **Pattern Selection Exercise** (`todo/study_plan/deep_dives/PatternSelectionExercise.md`) — Stage 3 of the practice path above is implemented there with Strategy/Registry/DI variants.
