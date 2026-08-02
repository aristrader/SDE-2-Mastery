---
order: 30
---

# Dependency Inversion (DIP) vs. Dependency Injection (DI)

> DIP and DI are related, but they are not the same thing. DIP is about the type you depend on. DI is about how that dependency is supplied.

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

### Where DIP lives in the production Factory Method code

Exactly here:

```java
// HR.java (production variant)
private final HiringProcess hiringProcess;   // ← interface, not the concrete abstract class
```

If this were `private final DeveloperHiringProcess hiringProcess;` (as in the learning variant), DIP would be weaker. `DeveloperHiringProcess` is abstract, but it still means "developer hiring only." `HR` does not need to know that. `HR` only needs "something that can onboard an employee."

The production code has three layers:

```java
public interface HiringProcess {
  Employee onboard();
}

public abstract class DeveloperHiringProcess implements HiringProcess {
  @Override
  public final Employee onboard() {
    Employee developer = createDeveloper();
    // shared developer onboarding steps...
    return developer;
  }

  protected abstract Employee createDeveloper();
}

public class AndroidHiringProcess extends DeveloperHiringProcess {
  @Override
  public Employee createDeveloper() {
    return new AndroidDeveloper();
  }
}
```

`DeveloperHiringProcess` is an abstraction, but it is still a developer-specific abstraction. It represents one branch of hiring flows: Android, backend, iOS, and any other developer role. `HiringProcess` is the broader contract HR actually needs.

That distinction matters when wiring:

```java
EmailService emailService = new EmailService();
OfferLetterService offerLetterService = new OfferLetterService();

HiringProcess androidProcess =
    new AndroidHiringProcess(emailService, offerLetterService);

HR hrForAndroid = new HR(androidProcess);
```

The object is still concrete at the edge (`new AndroidHiringProcess(...)`), but the high-level client receives it through the `HiringProcess` interface:

```java
class HR {
  private final HiringProcess hiringProcess;

  HR(HiringProcess hiringProcess) {
    this.hiringProcess = hiringProcess;
  }

  Employee hireForTeam() {
    return hiringProcess.onboard();
  }
}
```

So the dependency chain reads:

```text
HR ──depends on──▶ HiringProcess ◀──implemented by── DeveloperHiringProcess ◀──extended by── AndroidHiringProcess
```

If a future `SalesHiringProcess` implements `HiringProcess` as a sibling of `DeveloperHiringProcess`, `HR` does not change. If `HR` depended on `DeveloperHiringProcess`, the sales branch would not fit the constructor. That code would still use constructor injection, but the dependency type would be too narrow.

---

## Dependency Injection (DI) — the **technique**

A technique for how a class receives its dependencies: pass them in from outside instead of constructing them internally.

| Flavour | What it looks like | When to use |
| --- | --- | --- |
| **Constructor injection** | `HR(HiringProcess hp) { this.hp = hp; }` | Default. Fields can be `final`; dependencies guaranteed at construction. |
| **Setter injection** | `void setProcess(HiringProcess hp)` | Optional dependencies, or when breaking circular dependencies. |
| **Field injection** | `@Autowired HiringProcess hp;` (Spring) | Avoid in new code. Fields cannot be `final`; dependencies are hidden. |

DI is just the mechanic of passing dependencies in. It does not guarantee DIP. You can inject the wrong type.

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
| **DIP ✓** | **Best practice.** `HR` receives a `HiringProcess` interface in its constructor. | Rare. `HR` holds an interface field but still creates the implementation itself. |
| **DIP ✗** | **Common trap.** `HR` receives a dependency, but the type is concrete or too narrow. | Worst case. `HR` does `new AndroidHiringProcess()` internally. |

Aim for the top-left: constructor injection plus an interface type.

---

## How to spot violations

**DIP violations:**

- A high-level class names a concrete or too-specific class in a field, parameter, or return type.
- A class cannot be tested without real low-level dependencies.
- Adding a new variant means editing the high-level class.

Example:

```java
class HR {
  private final AndroidHiringProcess hiringProcess; // too specific
}
```

Better:

```java
class HR {
  private final HiringProcess hiringProcess; // broad enough for HR's role
}
```

**DI without DIP (the trap):**

- A class accepts a dependency via its constructor, but the parameter type is concrete or too narrow.
- "I'm using DI" is true, but the type signature is still coupled.
- Easy fix: change the parameter type from `ConcreteClass` to `AnInterface`.

**DIP without DI (rare):**

- Code holds an interface field but constructs the implementation internally with `new` or a static lookup.
- The field type is okay, but testing is still harder because callers cannot pass a fake.

---

## TL;DR

> **DIP says:** *depend on abstractions, not concretions.*
> **DI says:** *don't construct your dependencies — receive them.*
>
> They reinforce each other, but answer different questions: DIP is *what type*, DI is *how it arrives*.

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

When a class creates its own collaborators, you cannot:

- Replace the real implementation in tests.
- Swap implementations per environment (dev vs prod, local SMTP vs SES).
- Configure the dependency from outside (timeouts, URLs, retries).
- Reuse the class with a different collaborator.

DI moves construction to a wiring layer: either explicit setup in `main()` or a container like Spring.

### Why constructor injection wins over the other flavours

Constructor injection is the default in modern Java/Spring:

1. Fields can be `final`.
2. The object cannot exist without required dependencies.
3. The constructor shows what the class needs.
4. Tests can call `new SignupService(fakeSender)` without Spring.
5. With one constructor, Spring can inject it automatically.

### `@Autowired` vs DI — they are at different levels

A common confusion worth nailing down:

| | DI | `@Autowired` |
| --- | --- | --- |
| What is it? | A **technique** — push dependencies in from outside | A **Spring annotation** that tells the Spring container how to inject a value |
| Where does it live? | Anywhere — plain Java, manual `main()` wiring, or any framework | Only in Spring (and Spring-compatible) projects |
| Is one necessary for the other? | No. You can do DI with no annotations at all (manual DI). | `@Autowired` is one *implementation* of DI — Spring's. |

Three rules:

1. **Modern Spring (4.3+) auto-injects a single constructor.** No annotation needed:
   ```java
   @Service
   public class SignupService {
     private final EmailSender sender;
     public SignupService(EmailSender sender) { this.sender = sender; }
   }
   // No @Autowired anywhere. Spring detects the single constructor and uses it.
   ```
   Adding `@Autowired` here is redundant.

2. **`@Autowired` on a field = field injection (avoid).**
   ```java
   @Service
   public class SignupService {
     @Autowired private EmailSender sender;   // field injection
   }
   ```
   The downsides: the field cannot be `final`, dependencies are hidden, and the class is annoying to test without Spring.

3. **`@Autowired` on a setter = setter injection.** Use this rarely, usually for optional dependencies.

**Bottom line:** prefer constructor injection. In modern Spring, a single constructor usually needs no `@Autowired`.

### Inversion of Control — the broader principle DI is a special case of

DI is one kind of **Inversion of Control (IoC)**: the class does not control how dependencies arrive. The caller or container controls that.

IoC appears elsewhere too: Spring calls lifecycle methods, JUnit calls test methods, and frameworks call your handlers. DI is the construction-time version.

### How DI relates to the other creational patterns

Once DI is in your codebase, several patterns from this repo shift in role:

| Pattern | What DI does to it |
| --- | --- |
| **Singleton** | Spring beans are singleton-scoped by default. You usually do not need manual `getInstance()` code in a Spring app. |
| **Factory Method** | Often unnecessary when the factory only creates an object. Still useful when the choice happens at runtime. |
| **Builder** | Separate concern. DI wires long-lived services; Builder creates short-lived values. |
| **Static factory methods** | Spring `@Bean` methods are factory methods owned by configuration code. |

### Spring's role beyond manual DI

Spring is a DI container. It scans components, builds the object graph, and creates objects in the right order.

Three things Spring adds beyond manual DI:

1. Automatic graph construction.
2. Bean scopes: `singleton`, `prototype`, `request`, `session`.
3. Lifecycle hooks like `@PostConstruct` and `@PreDestroy`.

### Manual DI before Spring

Manual DI makes the idea obvious:

- The class receives dependencies through constructors.
- Some outside code creates the objects and passes them in.
- As the graph grows, that outside wiring becomes repetitive.

Spring automates that repetitive wiring and adds scopes/lifecycle. It is easier to understand after seeing the manual version once.

---

## Quick checks

1. `HR(HiringProcess hiringProcess)` is both DI and DIP. The dependency is passed in, and the type is an interface.
2. `DeveloperHiringProcess(EmailService emailService, OfferLetterService offerLetterService)` is DI-friendly. Its services are constructor parameters, not hidden `new` calls.
3. If `EmailService` is a default Spring bean, Spring creates one instance per application context. That means it must be thread-safe if multiple requests can use it.
4. `private final HiringProcess hp = new AndroidHiringProcess(...)` uses a good field type, but it is not DI. Move the `HiringProcess` parameter into the constructor.

---

## Pointers in this codebase

- `design_patterns/creational/factory/factory_method/playground/HR.java` — DIP applied: field type is `HiringProcess`; DI applied: constructor injection.
- `design_patterns/creational/factory/factory_method_basic/playground/HR.java` — DI applied, but DIP is weaker: field type is `DeveloperHiringProcess`.
- `design_patterns/creational/factory/index.md` — explains the migration that added `HiringProcess` above `DeveloperHiringProcess`.

---

## Quick recall

**Q. DIP vs DI?**
A. DIP is the principle: depend on abstractions. DI is the technique: receive dependencies from outside, usually through constructors.

**Q. Constructor injection vs internal `new`?**
A. Constructor injection makes dependencies explicit and replaceable. Internal `new` hard-codes the concrete class and hurts testing.

**Q. In Spring, why is manual Singleton usually unnecessary?**
A. Spring beans are singleton-scoped by default, so the container already manages one shared instance.

**Q. When does a factory still make sense in a DI app?**
A. When there is a real runtime choice, not just "give me this dependency."

**Q. What is the one-line fix for `private final HiringProcess hp = new AndroidHiringProcess(...)`?**
A. Accept `HiringProcess` in the constructor and assign it to the field.
