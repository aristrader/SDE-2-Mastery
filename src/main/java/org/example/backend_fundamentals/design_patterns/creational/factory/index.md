---
order: 30
---

# Factory Patterns in Java — Three Variants Side by Side

"Factory" is an umbrella term. This repo has **three distinct implementations** of the same business scenario (hiring developers), each demonstrating a different point on the factory-pattern spectrum.

This document is the **overview and comparison** across all three. Use the generated topic cards below to open each variant in navigation order.

---

## At a glance

| Aspect | Simple Factory | Factory Method (Learning) | Factory Method (Production) |
| --- | --- | --- | --- |
| GoF pattern? | No | Yes | Yes |
| How the concrete type is chosen | `if`/`else` on an enum | Polymorphic dispatch | Polymorphic dispatch |
| Creator is… | One class with a static method | An abstract class | An abstract class implementing an interface |
| Services are… | N/A (no services modelled) | Static utility classes | Instance classes held as fields |
| Client depends on… | The factory + discriminator enum | The abstract creator class | A `HiringProcess` interface |
| OCP satisfied? | No — adding a type edits the factory | Yes — for developer types | Yes — for both types AND whole new flows |
| Testable via mocks? | Limited | No (static services can't be mocked cleanly) | Yes — inject mock services |
| DI-framework ready? | No | No | Yes (Spring-style) |
| # of files for same demo | 6 | 11 | 13 |
| Good for… | Small, stable product sets | Learning Factory Method mechanics | Production code |

---

## Quick mental model — the one-line distinction

If you remember nothing else from this document, remember this:

> **Simple Factory: dispatch via `if`/`else`.**
> **Factory Method: dispatch via polymorphism.**

That single difference &mdash; *how does the code pick which concrete product to instantiate?* &mdash; is the substantive distinction between the two patterns. Everything else (number of files, OCP behaviour, where the choice lives, whether you need a discriminator enum) follows from this one mechanical choice.

### Dispatch in Simple Factory

```java
public static Employee getDeveloper(Developer developer) {
  if (developer.equals(Developer.ANDROID_DEVELOPER)) {
    return new AndroidDeveloper();
  } else if (developer.equals(Developer.BACKEND_DEVELOPER)) {
    return new BackendDeveloper();
  }
}
```

The factory inspects a value the caller passed in (`developer`) and decides what to build with an `if`/`else` (or `switch`). Selection is a **runtime data lookup** &mdash; the factory itself is doing the picking.

### Dispatch in Factory Method

```java
// In the abstract creator's template method:
Employee developer = createDeveloper();      // ← virtual dispatch happens here

// In the concrete creators (no if/else anywhere):
@Override public Employee createDeveloper() { return new AndroidDeveloper(); }
@Override public Employee createDeveloper() { return new BackendDeveloper(); }
```

The factory has **no `if`/`else`**. The JVM looks at the *runtime type* of the creator object and dispatches to the correct override. Selection is **baked into the type system** &mdash; the type itself is the picker.

### Three other phrasings, in case one clicks better

- *Simple Factory* asks the caller **"which one?"** &mdash; *Factory Method* asks the caller **"which factory?"**
- *Simple Factory* selects with **data**; *Factory Method* selects with **types**.
- *Simple Factory* tells the factory what to make; in *Factory Method* you pick a maker that always makes its own kind of thing.

### Why this distinction drives everything else

- **OCP.** A factory that dispatches via `if`/`else` must be edited when a new case is added. A factory that dispatches via polymorphism extends naturally &mdash; you add a subclass.
- **Discriminator enum.** Simple Factory needs one (the value the caller passes in). Factory Method doesn't &mdash; the type is the discriminator.
- **Shared creation logic.** Simple Factory is one method, with no good place to host shared steps. Factory Method's abstract creator can hold a template method that wraps the factory method with shared logic on either side.
- **Number of classes.** A `switch` lives in one class; a polymorphic dispatch needs a hierarchy.

Once you see the dispatch mechanism, the rest of the differences are not arbitrary &mdash; they're consequences.

---

## The three at a structural glance

### Simple Factory

```
DeveloperSimpleFactory.getDeveloper(Developer.ANDROID_DEVELOPER)
        │
        └── if/else → new AndroidDeveloper()
```

One class does the picking. The caller hands it a discriminator.

### Factory Method — Learning variant

```
HR ──▶ DeveloperHiringProcess (abstract, holds template method)
              ▲
              ├── AndroidHiringProcess
              ├── BackendHiringProcess
              └── IosHiringProcess
```

Polymorphism picks. The concrete class the HR was constructed with determines which `createDeveloper()` runs. Services are static utilities called directly from the template method.

### Factory Method — Production variant

```
HR ──▶ HiringProcess (interface)
              ▲ implements
              └── DeveloperHiringProcess (abstract, holds EmailService + OfferLetterService as fields)
                         ▲
                         ├── AndroidHiringProcess
                         ├── BackendHiringProcess
                         └── IosHiringProcess

(SalesHiringProcess and DesignerHiringProcess would slot in as siblings of DeveloperHiringProcess)
```

Interface above the abstract class unlocks multi-flow orchestration; services are instance classes passed through constructors.

---

## The transition — why we moved from Learning to Production

Six concrete changes take you from `factory_method_basic/` to `factory_method/`. Each is small, each has a concrete trigger, and you could (should!) apply them one at a time if you were evolving real code.

### 1. Added a `HiringProcess` interface above `DeveloperHiringProcess`

- **Why:** anticipation of a second kind of hiring flow (`SalesHiringProcess`, `DesignerHiringProcess`). The interface gives HR something abstract to depend on.
- **Trigger:** "I can name a concrete second use case" — the YAGNI test is now passing.
- **Cost:** one new file.
- **Benefit:** HR can hire any flow without edits (OCP at the orchestrator level).

### 2. Services became instance classes

Before:
```java
public class EmailService {
  private EmailService() {}
  public static String createEmailAccount(Employee developer) { /* ... */ }
}
```

After:
```java
public class EmailService {
  public String createEmailAccount(Employee developer) { /* ... */ }
}
```

- **Why:** **testability** (static calls can't be mocked without PowerMock-style hacks), **composability** (multiple hiring processes can share a configured service instance), and **future state** (e.g., SMTP client, company domain config).
- **Trigger:** needing to write a test, or wanting to configure the service.
- **Cost:** minor API change (callers use `email.createEmailAccount(...)` instead of `EmailService.createEmailAccount(...)`).
- **Benefit:** the services are now first-class collaborators, not hidden dependencies.

### 3. `DeveloperHiringProcess` gained a constructor and fields

- **Why:** to hold the services as instance fields. This is the crucial consequence of #2.
- **Trigger:** services became instance-based, so somebody has to hold the instance.
- **Cost:** concrete creators now need constructors chaining `super(...)`.
- **Benefit:** dependencies are explicit in the type signature — no hidden static calls.

> **Key insight:** the jump from static services to instance services *forced* `DeveloperHiringProcess` to be a class holding fields. An interface cannot hold instance fields — so this is the concrete reason the creator is an abstract class and not an interface. See *"abstraction shape follows from state"* in `DesignThinkingProcess.md`.

### 4. Concrete creators (`AndroidHiringProcess`, etc.) gained constructors

- **Why:** direct side-effect of #3. Subclasses must call `super(emailService, offerLetterService)` to satisfy the parent's constructor.
- **Cost:** a 3-line constructor per concrete creator.
- **Benefit:** none new — this is just the plumbing for #3.

### 5. `HR` changed field type from `DeveloperHiringProcess` to `HiringProcess`

- **Why:** **Dependency Inversion Principle.** HR is the high-level orchestrator; it should depend on an abstraction, not a specific branch of the hierarchy.
- **Trigger:** to make HR accept future flows (sales, designer) without edits.
- **Cost:** three characters (`Developer` removed from the type name).
- **Benefit:** HR is now reusable across any `HiringProcess`.

### 6. `FactoryMethodRun` now constructs services explicitly and passes them in

Before:
```java
HR hr = new HR(new AndroidHiringProcess());   // no services — static calls do the work
```

After:
```java
EmailService emailService = new EmailService();
OfferLetterService offerLetterService = new OfferLetterService();
HR hr = new HR(new AndroidHiringProcess(emailService, offerLetterService));
```

- **Why:** services are now instance-based and passed as dependencies.
- **Trigger:** direct consequence of #2–#4.
- **Cost:** more lines in `main`. In a Spring app, this is what `@Service` / `@Autowired` replaces.
- **Benefit:** wiring is explicit. Nothing is hidden in static state.

### What did NOT change

- The `Employee` interface (product contract).
- The concrete `*Developer` classes (they still implement `Employee` the same way).
- The template method pattern in `DeveloperHiringProcess.onboard()` (same 5 steps, same order).
- The OCP story for adding a new developer type (still two new files, no existing edits).

**Everything that changed was about *how collaborators are delivered*, not about the Factory Method pattern itself.**

---

## When to use which

### Use Simple Factory when…

- Product set is 1–3 types and stable.
- No shared creation logic needs a home.
- You want the shortest possible code that hides `new`.

### Use Factory Method Learning variant when…

- You are *explaining* the pattern to someone.
- You are in an early prototype where DI / testing aren't needs yet.
- Adding DI would genuinely distract from the pattern being taught.

### Use Factory Method Production variant when…

- You are in a production codebase.
- You expect new product types or new flows over time.
- Testability and explicit dependencies matter.
- You are (or will be) in a DI framework.

### Skip factories entirely when…

- You have only one concrete product and it's unlikely to grow. Use plain `new`.
- You are in a Spring app with a stable set of beans — the container already *is* the factory. Injecting the bean directly is the right call.

---

## Decision heuristic

```
Do I have more than one concrete product?                 ── No ─▶ Skip factories.
Do I need to hide construction from callers?              ── No ─▶ Skip factories.
Is the product set small (≤ 3) and stable?                ── Yes ─▶ Simple Factory.
Will the product set grow, or does creation have shared
   steps that should be reused?                           ── Yes ─▶ Factory Method.
Are services (collaborators) involved? Will I test with
   mocks? Are multiple flows likely?                      ── Yes ─▶ Production variant.
Otherwise                                                 ────▶ Learning variant.
```
