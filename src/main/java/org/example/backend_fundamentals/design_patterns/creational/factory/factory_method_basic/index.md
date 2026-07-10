---
order: 20
---

# Factory Method — Learning Variant

The **minimal** implementation of the GoF Factory Method pattern: enough structure to demonstrate the pattern's essence, stripped of everything you'd normally layer on top in production code (interface above the abstract class, dependency injection, instance-based services).

Use this variant to study Factory Method's core mechanics without DI, composition, or mockability getting in the way of the lesson.

## The shape

```
Employee (interface)
   ├── AndroidDeveloper      (package-private)
   ├── BackendDeveloper      (package-private)
   └── IosDeveloper          (package-private)

DeveloperHiringProcess (abstract class)            ← the only creator abstraction
   │   ├── onboard()                    — template method, final
   │   └── createDeveloper()            — abstract factory-method hook
   ├── AndroidHiringProcess
   ├── BackendHiringProcess
   └── IosHiringProcess

EmailService, OfferLetterService                   — static utility classes

HR ──── depends on ────▶ DeveloperHiringProcess   ← bound directly to the abstract class
```

## The code

### The abstract creator — template method

```java
public abstract class DeveloperHiringProcess {

  public final Employee onboard() {
    checkBudget();
    Employee developer = createDeveloper();
    provisionLaptop(developer);
    String email = EmailService.createEmailAccount(developer);
    OfferLetterService.sendOfferLetter(developer, email);
    return developer;
  }

  protected abstract Employee createDeveloper();

  private void checkBudget() { /* ... */ }
  private void provisionLaptop(Employee developer) { /* ... */ }
}
```

### A concrete creator

```java
public class AndroidHiringProcess extends DeveloperHiringProcess {

  @Override
  protected Employee createDeveloper() {
    return new AndroidDeveloper();
  }
}
```

Notice: **no constructor**. The abstract parent has no fields, so there's nothing to inject.

### The static service

```java
public class EmailService {

  private EmailService() {}

  public static String createEmailAccount(Employee developer) {
    // ...
  }
}
```

### The client

```java
public class HR {

  private final DeveloperHiringProcess hiringProcess;   // bound to the abstract class directly

  HR(DeveloperHiringProcess hiringProcess) {
    this.hiringProcess = hiringProcess;
  }

  public Employee hireForTeam() {
    return hiringProcess.onboard();
  }
}
```

### The runner

```java
public static void main(String[] args) {
  HR hrForAndroid = new HR(new AndroidHiringProcess());
  HR hrForBackend = new HR(new BackendHiringProcess());
  HR hrForIos     = new HR(new IosHiringProcess());

  hrForAndroid.hireForTeam();
  hrForBackend.hireForTeam();
  hrForIos.hireForTeam();
}
```

## What this variant demonstrates well

- **Polymorphic creation.** No `if`/`else` — the JVM dispatches to the right `createDeveloper()` based on the concrete creator's runtime type. This is the core of Factory Method.
- **Template method.** The 5-step `onboard()` flow lives in the parent. Every concrete creator inherits it for free.
- **Open/Closed Principle.** Adding a new developer type (e.g., `Windows`) means adding `WindowsDeveloper` + `WindowsHiringProcess`. No existing file changes.
- **Why abstract class, not interface.** `DeveloperHiringProcess` is an abstract class because it owns a shared `final` template method — something an interface cannot express cleanly.

## What this variant intentionally omits (and why)

| Omitted | Why |
| --- | --- |
| `HiringProcess` interface above the abstract class | Only one kind of flow (developer). Adding an interface would be speculative generality. See *"When to add another layer of abstraction"* in `DesignThinkingProcess.md`. |
| Instance-based services with dependency injection | Keeps the lesson focused on Factory Method, not on DI / composition. |
| Mocking support for services | No tests yet; static calls are fine in this scope. |

Each omission is a **deliberate simplification**, not an oversight. When any of them becomes a real need, the pattern is graceful to evolve into &mdash; which is what the production variant does.

## Pros

- Shortest possible code that shows the pattern.
- No DI wiring to distract from the concept.
- Easy to explain line-by-line to someone learning the pattern for the first time.

## Cons (why this isn't production-grade)

- **Static service calls are hard to mock.** If you ever want to unit-test `DeveloperHiringProcess.onboard()` without actually printing emails to the console, you'd need `PowerMock` or similar. Instance-based services with DI make this trivial.
- **HR is tied to developer-flavoured abstraction.** If you later add `SalesHiringProcess`, `HR`'s field type (`DeveloperHiringProcess`) won't accept it. OCP is violated at the HR layer.
- **Services are hidden collaborators.** `DeveloperHiringProcess.onboard()` reaches out to `EmailService.createEmailAccount(...)` as a static call; no one reading the class signatures can tell it depends on those services. Explicit constructor injection makes dependencies visible.

## Files in this package

| File | Role |
| --- | --- |
| `Employee.java` | Product interface |
| `AndroidDeveloper.java`, `BackendDeveloper.java`, `IosDeveloper.java` | Concrete products |
| `DeveloperHiringProcess.java` | Abstract creator with template method |
| `AndroidHiringProcess.java`, etc. | Concrete creators |
| `EmailService.java`, `OfferLetterService.java` | Static utilities |
| `HR.java` | Client |
| `FactoryMethodRun.java` | Runnable demo |

Run:

```bash
mvn -q exec:java -Dexec.mainClass="org.example.design_patterns.creational.factory_method_basic.FactoryMethodRun"
```

## Upgrading to the production variant

See `../factory_method/FactoryMethodProd.md` for the step-by-step differences and `../Factory.md` for a side-by-side comparison. The migration is small but deliberate &mdash; six concrete changes that you can make one at a time.

## Related files

- `../simple_factory/SimpleFactory.md` — the preceding step.
- `../factory_method/FactoryMethodProd.md` — the next step up.
- `../Factory.md` — overall comparison.
- `../../../todo/study_plan/deep_dives/DesignThinkingProcess.md` — design principles behind these choices.
- `/java/oop/access_modifiers/deep_dive/` — why `protected`, `final`, `abstract`, etc.


