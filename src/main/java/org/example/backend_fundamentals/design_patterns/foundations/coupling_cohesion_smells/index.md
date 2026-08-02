---
order: 10
---

# Coupling, Cohesion, and Code Smells

> The vocabulary that lets you say *why* a refactor or pattern is an improvement, not just *what* it does differently.

---

## Coupling and cohesion

- **High cohesion** — things that belong together live together.
- **Low coupling** — modules know as little about each other as possible.

**Sibling concepts.** Cohesion looks *inward* — how tightly do this class's responsibilities hang together as one thing? Coupling looks *outward* — how entangled is this class with its neighbours? The pair — high cohesion + low coupling — is what almost every GoF pattern is reaching for. Cohesion is also SRP turned inward; coupling is the outward counterpart.

**Why this matters for patterns:** most GoF patterns exist to lower coupling and/or raise cohesion. Naming these lets you articulate *why* a pattern is an improvement over the "before" code, not just *what* it does differently.

### Coupling in real code

You do **not** need to memorize a long list of coupling names. For interviews, be able to spot this:
- **Good:** a class depends on a small public contract.
- **Bad:** a class knows another class's internals, global state, external formats, or branching flags.

Pattern work usually moves code from the second bucket toward the first.

#### Good: depend on a small contract

```java
interface Notifier {
    void send(String to, String body);
}

class OrderService {
    private final Notifier notifier;

    OrderService(Notifier notifier) {
        this.notifier = notifier;
    }

    void placeOrder(String email) {
        notifier.send(email, "Order placed");
    }
}
```

This is normal healthy coupling. `OrderService` knows how to ask for a notification; it does not know whether it is email, SMS, or a fake test notifier.

#### Smell: passing a whole object for one field

```java
class ShippingCalculator {
    int estimate(User user) {
        return estimateByZip(user.getAddress().getZipCode());
    }

    int estimateByZip(String zipCode) {
        return zipCode.startsWith("9") ? 12 : 8;
    }
}
```

The method only needs a zip code, but now it knows `User -> Address -> zipCode`. Prefer the narrower method shape:

```java
int estimateByZip(String zipCode) {
    return zipCode.startsWith("9") ? 12 : 8;
}
```

If the fields belong together, pass a real value object like `ShippingAddress`, not a huge unrelated object.

#### Smell: boolean flag chooses behavior

```java
class ReportExporter {
    void export(Report report, boolean asPdf) {
        if (asPdf) {
            exportPdf(report);
        } else {
            exportCsv(report);
        }
    }
}
```

The call site `export(report, true)` is unclear. Prefer named methods:

```java
exporter.exportPdf(report);
exporter.exportCsv(report);
```

If the variation keeps growing, use Strategy:

```java
interface ExportFormat {
    void export(Report report);
}

class ReportExporter {
    void export(Report report, ExportFormat format) {
        format.export(report);
    }
}
```

#### Smell: external format leaks everywhere

```java
void createOrder(String rawJson) {
    // parse HTTP JSON here
}

void importOrder(String rawJson) {
    // parse the same JSON shape again
}
```

Both methods depend on the same external JSON shape. If the API changes, both change. Put the translation at the boundary:

```java
record CreateOrderRequest(String sku, int quantity) {}

class OrderController {
    void createOrder(CreateOrderRequest request) {
        orderService.create(request.sku(), request.quantity());
    }
}
```

Keep external formats at adapters/controllers/clients. Domain services should speak domain types, not raw JSON, HTTP headers, SQL rows, or vendor SDK objects.

#### Smell: shared mutable global state

```java
class AppState {
    static boolean discountEnabled;
}

class CheckoutService {
    int total(int amount) {
        return AppState.discountEnabled ? amount - 10 : amount;
    }
}
```

Any code can change `discountEnabled`, so behavior becomes hard to reason about and test. Prefer constructor-injected dependencies or immutable configuration.

```java
class CheckoutService {
    private final boolean discountEnabled;

    CheckoutService(boolean discountEnabled) {
        this.discountEnabled = discountEnabled;
    }
}
```

#### Smell: reaching into another class's internals

```java
invoice.items.size();        // public field
user.getAddress().getZip();  // deep chain
```

The caller now depends on how another object stores its data. Encapsulate the behavior:

```java
class Invoice {
    private final List<LineItem> items = new ArrayList<>();

    int itemCount() {
        return items.size();
    }
}
```

Avoid public fields, leaking mutable collections, deep chains like `a.getB().getC().doX()`, and caller-side downcasts.

### Cohesion checklist

A class has high cohesion if:
- All its methods touch most of its fields.
- You can describe what it does without "and."
- Removing any one method makes the class feel incomplete; removing any one field breaks several methods.

You do not need the full cohesion taxonomy either. Remember the practical version: **high cohesion** means the class has one clear reason to exist; **low cohesion** means unrelated responsibilities were grouped together. Vague class names — `*Manager`, `*Service`, `*Utils`, `*Helper`, `*Processor` — often hide low cohesion behind a plausible-sounding label.

Low cohesion is the **Large Class** smell — see below.

---

## Code Smells — named symptoms of bad design

Knowing smell names is how you "name the pain" precisely. *"I have a Shotgun Surgery problem"* is a far more actionable pain statement than *"the code feels messy."* That precision is Step 1 of the Design Thinking Process (see `study_plan/deep_dives/DesignThinkingProcess.md`).

### The catalogue

| Smell | What it signals | Pattern / refactoring that often cures it |
| --- | --- | --- |
| Switch Statements / `instanceof` chains | Polymorphism opportunity | Factory Method, Strategy, State |
| Telescoping Constructors | Parameter management problem | Builder |
| Large Class | SRP violation, low cohesion | Extract Class, split responsibilities |
| Long Method | SLAP violation | Extract Method |
| Feature Envy | Logic is in the wrong class | Move Method |
| Shotgun Surgery | Low cohesion, scattered responsibility | Move to a cohesive class |
| Primitive Obsession | Missing domain types | Replace primitives with Value Objects |
| Data Clumps | Same group of fields/parameters appearing together | Introduce Parameter Object / Value Object |

### The pattern that *exists* to cure each smell

- **Switch on a type → Factory Method or Strategy.** The type system can dispatch for you.
- **Long argument list / telescoping → Builder.** Construct the object by accumulating optional pieces.
- **Repeated grouping of primitives → Value Object.** Wrap the cluster in a class with meaning.
- **Big class doing many things → Extract Class.** Split responsibilities along their natural seams.
- **Method on the wrong class (Feature Envy) → Move Method.** Move the behaviour to where the data lives.

This is the link between code smells and design patterns: the smell *is* the pain; the pattern *is* the cure.

---

## Quick recall

**Q. Cohesion vs coupling — the one-line distinction?**
A. Cohesion looks **inward** (how tightly do my responsibilities hang together?); coupling looks **outward** (how entangled am I with neighbours?). Goal: high cohesion + low coupling.

**Q. What cohesion names do you actually need?**
A. Mostly just **high cohesion** vs **low cohesion**. High cohesion means one clear responsibility; low cohesion means unrelated responsibilities grouped together.

**Q. What coupling names do you actually need?**
A. Mostly just **loose coupling** vs **tight coupling**. Recognize the code shapes: small interfaces are good; globals, boolean flags, raw external formats, deep object chains, and public internals are bad.

**Q. Five signs of tight coupling you can spot in code?**
A. (1) One change ripples through many files. (2) Hard to write isolated tests. (3) Cyclic dependencies between packages. (4) `instanceof` / downcasts in caller code. (5) Long import lists from one specific package.

**Q. Five code smells and what each cures?**
A. Switch statements → Factory Method / Strategy. Telescoping Constructors → Builder. Large Class → Extract Class (SRP). Long Method → Extract Method (SLAP). Feature Envy → Move Method.

**Q. How do you fix a boolean flag like `process(data, true)`?**
A. Default: split into two methods named for what they do. If the variation may grow: Strategy. If the mode is per-instance: set it at construction. Last resort: enum instead of boolean — kills the *boolean trap* even if the branch stays.

**Q. The link between smells and patterns?**
A. The smell *is* the pain; the pattern *is* the cure. Naming the smell is Step 1 of the design-thinking process — you can't fix "messy" but you *can* fix "Shotgun Surgery."

