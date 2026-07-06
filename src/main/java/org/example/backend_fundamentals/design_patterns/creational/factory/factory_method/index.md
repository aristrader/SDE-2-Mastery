---
order: 10
---

# Factory Method — Production Variant

The same Factory Method pattern as `factory_method_basic/`, but refactored to the shape you'd actually want in a production codebase: an interface above the abstract class, instance-based services held as fields, explicit constructor injection, and DIP-clean client dependencies.

## The shape

```
HiringProcess (interface)                          ← outward contract
   ↑ implements
DeveloperHiringProcess (abstract class)            ← holds services, owns template method
   │   ├── onboard()                    — template method, final
   │   └── createDeveloper()            — abstract factory-method hook
   ├── AndroidHiringProcess
   ├── BackendHiringProcess
   └── IosHiringProcess

EmailService, OfferLetterService                   — instance classes (no static)

HR ──── depends on ────▶ HiringProcess             ← depends on the interface, not the class
```

## The code

### The interface — outward contract

```java
public interface HiringProcess {
  Employee onboard();
}
```

Purely the contract. No fields, no defaults, no implementation.

### The abstract creator — composition + template method

```java
public abstract class DeveloperHiringProcess implements HiringProcess {

  private final EmailService emailService;                  // composition: held as fields
  private final OfferLetterService offerLetterService;

  protected DeveloperHiringProcess(
      EmailService emailService, OfferLetterService offerLetterService) {
    this.emailService = emailService;
    this.offerLetterService = offerLetterService;
  }

  @Override
  public final Employee onboard() {
    checkBudget();
    Employee developer = createDeveloper();
    provisionLaptop(developer);
    String email = emailService.createEmailAccount(developer);      // instance call
    offerLetterService.sendOfferLetter(developer, email);
    return developer;
  }

  protected abstract Employee createDeveloper();
  private void checkBudget() { /* ... */ }
  private void provisionLaptop(Employee developer) { /* ... */ }
}
```

### A concrete creator — passes services up via `super(...)`

```java
public class AndroidHiringProcess extends DeveloperHiringProcess {

  protected AndroidHiringProcess(EmailService emailService, OfferLetterService offerLetterService) {
    super(emailService, offerLetterService);
  }

  @Override
  public Employee createDeveloper() {
    return new AndroidDeveloper();
  }
}
```

### Instance-based services

```java
public class EmailService {

  public String createEmailAccount(Employee developer) {
    // instance method — can hold state, be mocked, be swapped
  }
}
```

### The client — depends only on the interface

```java
public class HR {

  private final HiringProcess hiringProcess;                // interface, not class

  HR(HiringProcess hiringProcess) {
    this.hiringProcess = hiringProcess;
  }

  public Employee hireForTeam() {
    return hiringProcess.onboard();
  }
}
```

### The runner — wires the services explicitly

```java
public static void main(String[] args) {
  EmailService emailService = new EmailService();
  OfferLetterService offerLetterService = new OfferLetterService();

  AndroidHiringProcess androidHiringProcess =
      new AndroidHiringProcess(emailService, offerLetterService);
  BackendHiringProcess backendHiringProcess =
      new BackendHiringProcess(emailService, offerLetterService);
  IosHiringProcess iosHiringProcess =
      new IosHiringProcess(emailService, offerLetterService);

  HR hrForAndroid = new HR(androidHiringProcess);
  HR hrForBackend = new HR(backendHiringProcess);
  HR hrForIos     = new HR(iosHiringProcess);

  hrForAndroid.hireForTeam();
  hrForBackend.hireForTeam();
  hrForIos.hireForTeam();
}
```

## What this variant adds over the learning version

| Addition | What it buys you |
| --- | --- |
| `HiringProcess` interface above the abstract class | HR can hire any future flow (sales, designer) without edits. |
| Instance-based services (`EmailService`, `OfferLetterService`) | Mockable in tests; swappable per environment; can hold state. |
| `DeveloperHiringProcess` receives services via `protected` constructor | Explicit dependencies — visible in the type signature, not hidden behind static calls. Real DI-friendly. |
| Concrete creators chain services via `super(...)` | Side-effect of the above. |
| `HR` depends on `HiringProcess` (not `DeveloperHiringProcess`) | Dependency Inversion Principle — HR depends on the abstraction, not a specific branch. |
| Explicit service wiring in `main` | In a Spring app, this wiring is done by the container. In plain Java, it's explicit but honest. |

Each addition has a concrete pay-off. None of them are architectural ceremony for its own sake.

## What this variant still is *not*

- **Not Spring-wired.** The `main` method still constructs collaborators by hand. In a Spring app you'd replace those `new`s with `@Service` / `@Component` annotations and `@Autowired` constructor injection. The mechanics would stay; the wiring would move to the container.
- **Not multi-flow yet.** Only the developer branch exists. When `SalesHiringProcess` is added (as a sibling of `DeveloperHiringProcess`), `HR` accepts it without edits thanks to the `HiringProcess` interface.
- **No tests.** The package runs via `FactoryMethodRun` like every other demo in this codebase.

## Pros

- **Testable.** Mock the two services in a unit test; exercise `DeveloperHiringProcess.onboard()` without actually printing to the console.
- **OCP at every layer.** Adding a new developer type is additive; adding a whole new flow (sales, designer) is also additive, thanks to the interface.
- **Explicit collaborators.** Any reader can see at the constructor signature what a hiring process depends on.
- **Idiomatic for Spring and other DI frameworks.** Drop in `@Service` / `@Component` and you're done.

## Cons

- **More files, more ceremony.** The same demo runs through 13 files instead of 11.
- **Wiring in `main` is verbose** without a DI framework. Mitigated by Spring or equivalent in production code.

## Files in this package

| File | Role |
| --- | --- |
| `HiringProcess.java` | Outward contract |
| `Employee.java` | Product interface |
| `AndroidDeveloper.java`, `BackendDeveloper.java`, `IosDeveloper.java` | Concrete products |
| `DeveloperHiringProcess.java` | Abstract creator, holds services, owns template method |
| `AndroidHiringProcess.java`, etc. | Concrete creators (with constructors chaining services) |
| `EmailService.java`, `OfferLetterService.java` | Instance-based services |
| `HR.java` | Client — depends on `HiringProcess` |
| `FactoryMethodRun.java` | Runnable demo |

Run:

```bash
mvn -q exec:java -Dexec.mainClass="org.example.design_patterns.creational.factory_method.FactoryMethodRun"
```

## Related files

- `../factory_method_basic/FactoryMethodBasic.md` — the preceding step.
- `../Factory.md` — overall comparison + the exact diff between basic and prod.
- `../../../todo/study_plan/deep_dives/DesignThinkingProcess.md` — design principles behind these choices.
- `../../../java/foundations/access_modifiers/AccessModifiersDeepDive.md` — why `protected`, `final`, `private`, etc.


