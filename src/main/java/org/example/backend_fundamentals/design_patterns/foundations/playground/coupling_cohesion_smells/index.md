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

### Coupling spectrum (loosest to tightest)

| Type | Example | Notes |
| --- | --- | --- |
| **Message coupling** | Modules communicate only through public interfaces | Loosest. `notifier.send(msg)` where `notifier` is an interface — caller sees nothing inside. |
| **Data coupling** | Method takes primitive parameters | Fine. Just data, no internals. |
| **Stamp coupling** | Method takes a struct/object but only uses one field | Slightly worse — receiver knows too much. |
| **Control coupling** | Method takes a flag that controls its branches | Smell. Default fix: split into two methods named for what they do. Bigger variation that may grow: Strategy. Per-instance mode: set at construction. Last resort: enum (kills the *boolean trap* — `process(data, true)` doesn't read at the call site even if the declaration is clear). |
| **External coupling** | Many modules depend on the same external format / protocol / device | Switching the format breaks them all at once. |
| **Common coupling** | Two modules share global mutable state | Bad — changes ripple unpredictably. |
| **Content coupling** | One module reaches into another's internals | Worst — breaks encapsulation entirely. |

Pattern work usually moves you up this list (toward looser coupling).

### Cohesion checklist

A class has high cohesion if:
- All its methods touch most of its fields.
- You can describe what it does without "and."
- Removing any one method makes the class feel incomplete; removing any one field breaks several methods.

The classical spectrum runs from **functional cohesion** (best — every element contributes to one task) through procedural/temporal/logical groupings down to **coincidental cohesion** (worst — methods grouped for no real reason). Vague class names — `*Manager`, `*Service`, `*Utils`, `*Helper`, `*Processor` — often hide low cohesion behind a plausible-sounding label.

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

**Q. Cohesion spectrum — top and bottom?**
A. **Functional cohesion** (best — every element contributes to one task) → **coincidental cohesion** (worst — grouped for no real reason). Vague class names (`*Manager`, `*Service`, `*Utils`) often hide low cohesion.

**Q. Coupling spectrum — top and bottom?**
A. **Message coupling** (best — modules talk only through interfaces) → **content coupling** (worst — one module reaches into another's internals).

**Q. Five signs of tight coupling you can spot in code?**
A. (1) One change ripples through many files. (2) Hard to write isolated tests. (3) Cyclic dependencies between packages. (4) `instanceof` / downcasts in caller code. (5) Long import lists from one specific package.

**Q. Five code smells and what each cures?**
A. Switch statements → Factory Method / Strategy. Telescoping Constructors → Builder. Large Class → Extract Class (SRP). Long Method → Extract Method (SLAP). Feature Envy → Move Method.

**Q. How do you fix Control Coupling (`process(data, true)`)?**
A. Default: split into two methods named for what they do. If the variation may grow: Strategy. If the mode is per-instance: set it at construction. Last resort: enum instead of boolean — kills the *boolean trap* even if the branch stays.

**Q. The link between smells and patterns?**
A. The smell *is* the pain; the pattern *is* the cure. Naming the smell is Step 1 of the design-thinking process — you can't fix "messy" but you *can* fix "Shotgun Surgery."

---

## Related topics

- **Design Thinking Process** (`study_plan/deep_dives/DesignThinkingProcess.md`) — "name the pain" relies on having the smell vocabulary loaded.
- **SOLID — SRP** — Large Class is the canonical SRP violation.
