---
order: 50
---

# Pattern Selection Exercise — One Client, Many Factories

> **Cross-referenced from:** Part 04 topics Factory Method, Abstract Factory, Strategy, and Dependency Injection; Part 03 (Spring) for DI wiring.

A self-contained mini-project for learning **when to reach for which pattern** by solving the same problem in three different ways. Pick this up when you want to actually *choose* between Strategy, Registry, and Dependency Injection rather than just reading about them.

## The problem statement

In `design_patterns/creational/factory_method/`, `HR` is bound to a single `DeveloperHiringProcess` at construction time:

```java
public class HR {
  private final DeveloperHiringProcess hiringProcess;

  public HR(DeveloperHiringProcess hiringProcess) {
    this.hiringProcess = hiringProcess;
  }

  public Employee hireForTeam() {
    return hiringProcess.onboard();
  }
}
```

So `FactoryMethodRun.main` ends up creating **three** HR objects:

```java
HR hrForAndroid = new HR(new AndroidHiringProcess());
HR hrForBackend = new HR(new BackendHiringProcess());
HR hrForIos     = new HR(new IosHiringProcess());
```

That's fine for isolating the Factory Method lesson, but in a real app an HR department hires *all* roles. We want **one `HR` object** that can hire Android, Backend, iOS, or anything added later.

That question &mdash; "how does one orchestrator manage many polymorphic factories?" &mdash; is a **different concern from Factory Method**. Factory Method solved *how to create a product polymorphically*. Managing and selecting between many factories at runtime is an **object management / dispatch** concern, and it has its own set of patterns.

---

## The core insight

> **Factory Method and Strategy are structurally identical.** The only difference is intent: Factory Method *creates* something; Strategy *does* something.

Once you accept that, the "one HR" problem has three well-known solutions, each with a name.

---

## Option 1 — Strategy Pattern: pass the process per call

```java
public class HR {
  public Employee hireForTeam(DeveloperHiringProcess hiringProcess) {
    return hiringProcess.onboard();
  }
}

// usage
HR hr = new HR();
hr.hireForTeam(new AndroidHiringProcess());
hr.hireForTeam(new BackendHiringProcess());
hr.hireForTeam(new IosHiringProcess());
```

### What it is

Pass the "algorithm" (hiring process) into the method on every call. `HR` holds no hiring state.

### Pros

- One `HR` instance for the whole app.
- Highly flexible &mdash; the same HR can run any strategy, including ones that didn't exist when HR was written.
- HR has no knowledge of the type set &mdash; still pure abstraction.

### Cons

- The "which process?" decision has moved to the *caller* of `HR`. If that caller is itself reused across the app, you've just pushed the problem one layer out.
- Doesn't let HR pick the process based on runtime data (e.g. "which process for role = 'android'?") without more plumbing.

### When to reach for it

- The caller already *knows* which process it wants (e.g., a controller mapping an HTTP endpoint directly to a strategy).
- You want maximum flexibility and minimum state.

---

## Option 2 — Registry / Service Locator: HR holds a map

```java
public class HR {
  private final Map<Role, DeveloperHiringProcess> processes = Map.of(
      Role.ANDROID, new AndroidHiringProcess(),
      Role.BACKEND, new BackendHiringProcess(),
      Role.IOS,     new IosHiringProcess());

  public Employee hireForTeam(Role role) {
    DeveloperHiringProcess process = processes.get(role);
    if (process == null) {
      throw new IllegalArgumentException("Unknown role: " + role);
    }
    return process.onboard();
  }
}

// usage
HR hr = new HR();
hr.hireForTeam(Role.ANDROID);
hr.hireForTeam(Role.BACKEND);
hr.hireForTeam(Role.IOS);
```

### What it is

A keyed lookup of pre-built factories. HR is constructed once with the full map.

### Pros

- One `HR` instance, one parameter per call.
- Still polymorphic &mdash; HR calls `onboard()` on whatever it looks up.
- Caller passes a *key* (e.g. a role name from a request) instead of a concrete object.

### Cons

- **Reintroduces the enum/string discriminator** &mdash; the very thing Factory Method eliminated from client code.
- **Partial OCP regression**: adding `Windows` now means editing the `processes` map. Still better than Simple Factory's `if`/`else`, but it's no longer strictly OCP-clean.

### When to reach for it

- You have no DI framework (plain Java, CLI tool, library code).
- You want HR to be the single gateway for all hiring.
- The type set is stable enough that adding an entry to a map is acceptable.

---

## Option 3 — Dependency Injection: framework-wired registry

Option 3 is still a registry at runtime. The difference from Option 2 is that
Spring creates and maintains the registry map for you.

```java
public interface DeveloperHiringProcess {
  Employee onboard();
}

@Service
public class HR {
  private final Map<String, DeveloperHiringProcess> processes;

  // Spring injects EVERY bean implementing DeveloperHiringProcess,
  // keyed by bean name.
  public HR(Map<String, DeveloperHiringProcess> processes) {
    this.processes = processes;
  }

  public Employee hireForTeam(String role) {
    DeveloperHiringProcess process = processes.get(role);
    if (process == null) {
      throw new IllegalArgumentException("Unknown role: " + role);
    }
    return process.onboard();
  }
}

@Component("android")
public class AndroidHiringProcess implements DeveloperHiringProcess {
  @Override
  public Employee onboard() {
    return new Employee("Android Developer");
  }
}

@Component("backend")
public class BackendHiringProcess implements DeveloperHiringProcess {
  @Override
  public Employee onboard() {
    return new Employee("Backend Developer");
  }
}

@Component("ios")
public class IosHiringProcess implements DeveloperHiringProcess {
  @Override
  public Employee onboard() {
    return new Employee("iOS Developer");
  }
}
```

### What it is

A DI container (Spring, Guice, Micronaut, etc.) auto-assembles the registry from every bean that implements the interface.

In Spring specifically, this constructor parameter is the trigger:

```java
public HR(Map<String, DeveloperHiringProcess> processes)
```

Spring reads that as:

> Give `HR` a map of every bean whose type is `DeveloperHiringProcess`.

The map key is the bean name:

```java
@Component("android") // map key = "android"
```

So Spring injects something equivalent to:

```java
Map.of(
    "android", androidHiringProcessBean,
    "backend", backendHiringProcessBean,
    "ios", iosHiringProcessBean)
```

If you omit the explicit component name:

```java
@Component
public class AndroidHiringProcess implements DeveloperHiringProcess { ... }
```

then Spring's default bean name is usually `androidHiringProcess`, so the map key changes too.

That is why Option 2 and Option 3 look similar:

- **Option 2:** you manually write the map.
- **Option 3:** Spring writes the map from registered beans.

### Pros

- One `HR` instance. One parameter per call.
- **OCP fully restored** &mdash; adding a type really does mean *only adding a new class*. The map updates itself.
- Easily testable &mdash; pass a mock map in tests.
- This is how ~90% of production Java code handles "many factories of the same interface."

### Cons

- Requires a DI framework.
- Wiring happens at runtime &mdash; a typo in the `@Component("...")` key is caught at call time, not compile time.

### When to reach for it

- You're already in a Spring / Micronaut / Guice codebase.
- You expect new types to be added frequently, possibly by other teams or plugins.
- You want the most "hands-off" scaling &mdash; drop in a new class, it just works.

---

## Comparison at a glance

| Aspect | Current (Factory Method only) | Option 1: Strategy | Option 2: Registry | Option 3: DI |
| --- | --- | --- | --- | --- |
| Number of HR objects | One per type | **One total** | **One total** | **One total** |
| Where the "which type?" choice lives | Caller that constructs HR | Caller that calls `hireForTeam` | HR (via map) | Framework + key at call time |
| Who builds the lookup map? | Nobody | Nobody | Your code | Spring / DI container |
| Has a type discriminator (enum/string)? | No | No | **Yes** | Yes (bean key) |
| Open/Closed for adding a new type | Respected | Respected | **Partial** (map edit) | Respected |
| Requires a framework | No | No | No | **Yes** |
| Reusability for "do anything" clients | Low | High | Medium | Medium |
| Typical real-world use | Teaching example | Per-request strategies | Framework-less apps, small tools | Production Spring / Guice apps |

---

## Which pattern is each one, named properly?

| Design choice | Pattern name |
| --- | --- |
| HR binds to one process at construction | Plain **Factory Method** client *(current)* |
| Caller passes the process per call | **Strategy Pattern** |
| HR holds a `Map<Key, Factory>` | **Registry** / **Service Locator** |
| Spring injects a `Map<String, Factory>` | **Dependency Injection** creating a framework-managed registry |

Factory Method pushed the "which type?" question out of the *factory* and into the *subclass choice*. But someone, somewhere, still has to make that choice. These three options just move it around:

- **Strategy**: caller chooses per call.
- **Registry**: HR chooses based on a key.
- **DI**: framework resolves at wire-time from config/annotations.

None of the three is strictly better &mdash; they sit on a spectrum from "compile-time wiring" to "runtime config-driven wiring."

---

## Decision matrix — which to use when

| If… | Reach for |
| --- | --- |
| …you're *learning* the Factory Method pattern | The current, plain version (one HR per process) |
| …the caller always knows which process it wants | **Strategy** (Option 1) |
| …you need one gateway to rule them all, no framework available | **Registry** (Option 2) |
| …you're in a Spring app | **DI** (Option 3) |
| …you're building an extensible plugin architecture | **DI** (Option 3) or **Registry** with service loading (`java.util.ServiceLoader`) |
| …the type set will never grow | Keep it simple &mdash; **Simple Factory** is probably fine |

---

## TL;DR

- The "three HR objects" smell in the current Factory Method example is **not** a flaw in Factory Method &mdash; it's a different concern (orchestration) pretending to be part of the same lesson.
- **One HR** is achievable in three ways: **Strategy** (pass per call), **Registry** (HR holds a map), **DI** (framework wires the map).
- Option 2 and Option 3 both use a map. The difference is ownership: your code builds the Option 2 map; Spring builds the Option 3 map from beans.
- Strategy ≈ Factory Method structurally; the difference is intent (*do* vs *create*).
- Registry compromises OCP slightly; DI restores it fully at the cost of a framework.
- In production Spring code, Option 3 (DI) wins. Without a framework, Option 2 (Registry) is the pragmatic next step.

## Quick recall

**Q. If the caller knows the behavior per call, which pattern fits?**
A. Strategy.

**Q. If one gateway picks an implementation by key without Spring, what fits?**
A. Registry.

**Q. If Spring injects `Map<String, Implementation>`, what is the design shape?**
A. Dependency Injection plus polymorphism by interface.

**Q. What makes Spring put values into that map?**
A. The constructor asks for `Map<String, DeveloperHiringProcess>`, so Spring injects all `DeveloperHiringProcess` beans keyed by bean name.

**Q. Is "one HR object" a Factory Method problem?**
A. No. It is an orchestration/wiring concern around the factory method.

**Q. Which option is usually best in production Spring code?**
A. DI with constructor injection and framework-managed implementation lookup.
