# Creational Patterns — Quick Reference

One-paragraph mental model per pattern for fast revision. Not a tutorial — read the pattern's own `.md` for the full explanation.

---

## Singleton

**One instance. Full stop.**

The whole application shares one object. The pattern controls construction so you can never accidentally create a second one. Use it for things that genuinely need to be global and shared — a config registry, a connection pool, a logger. The trap: Singleton is a hidden global; it makes testing harder and creates tight coupling across the codebase. Spring beans are singletons by default without the Singleton pattern — prefer DI over hand-rolled singletons in production.

**In one line:** *Everyone gets the same object.*

**JDK:** `Runtime.getRuntime()`, `Collections.EMPTY_LIST`

---

## Factory Method

**The caller wants a product. It doesn't care which one.**

The caller says "give me a developer" and gets back an `Employee`. It never writes `new AndroidDeveloper()` — it just calls `hireForTeam()` on whatever hiring process it was given. You swap the product by swapping the creator subclass. The creator (the hiring process) decides what gets built; the caller (HR) only sees the abstract product. Extend the system by adding a new creator + product pair — no existing class changes.

**In one line:** *Caller gets a product; a subclass decides which one.*

**JDK:** `Calendar.getInstance()`, `NumberFormat.getInstance()`

---

## Abstract Factory

**Factory Method, but for a whole matched family of products.**

You don't just need one product — you need several that belong together. A cheap furniture factory produces a `CheapChair` and a `CheapSofa` that match. A luxury factory produces `LuxuryChair` + `LuxurySofa`. The factory interface guarantees you can never accidentally mix a luxury chair with a cheap sofa. The client gets the whole family from one factory call site and works only against the abstract product interfaces. Add a new family by adding a new concrete factory — no existing client code changes.

**In one line:** *Caller gets a coordinated family of products; the factory guarantees they match.*

**JDK:** JDBC `Connection` → `Statement` → `ResultSet` from the same driver; `DocumentBuilderFactory`

---

## Builder

**Construct complex objects step by step, hand back an immutable result.**

When an object has many fields — some required, many optional — constructors become unreadable (`new Job(title, city, null, null, 0, true, false)`). Builder separates construction from representation: required fields go into the builder's constructor (so you can never forget them), optional fields are fluent setters, and `build()` validates before handing back a fully constructed, immutable product. The Director variant adds pre-packaged recipes so the same steps can build completely different products.

**In one line:** *Construction is complex; Builder makes it readable and validates before handing you the result.*

**JDK:** `StringBuilder`, `HttpRequest.newBuilder()`, `Stream.Builder`; Lombok `@Builder` in any Spring project

---

## Static Factory Methods *(Effective Java Item 1 — not a GoF pattern)*

**Named constructors with superpowers.**

Instead of `new Temperature(37, CELSIUS)` you write `Temperature.celsius(37)`. The name is clearer, you can cache instances (return the same object for the same input), and you can return a subtype the caller doesn't even know about. The two limitations: you can't subclass a class whose only constructor is private, and static factory methods don't stand out in Javadoc the way constructors do.

**In one line:** *Give construction a meaningful name; optionally cache or hide the concrete type.*

**JDK:** `List.of(...)`, `Optional.empty()`, `Integer.valueOf(5)`, `Path.of(...)`

---

## Prototype

**Copy a pre-built object instead of constructing from scratch.**

When construction is expensive (database lookup, heavy computation) and you already have a fully configured instance, cloning it is faster than building a new one. The prototype is a template — you copy it and tweak the copy. Java's `Cloneable`/`clone()` is the canonical mechanism but is widely considered broken (shallow copy by default, no constructor called, awkward exception). Copy constructors are the modern alternative: `new Shape(existingShape)` gives you full control over what gets copied and how deeply.

**In one line:** *Construction is expensive; copy a ready-made template instead.*

**JDK:** `new ArrayList<>(existingList)`, `new HashMap<>(existingMap)` — copy constructors everywhere

---

## Shallow copy vs deep copy (applies to Prototype)

- **Shallow copy:** the new object gets its own primitive fields but shares references to any nested objects with the original. Mutating a nested object in the copy changes the original too.
- **Deep copy:** every nested object is also copied recursively. The original and the copy are completely independent.

`Object.clone()` does a shallow copy. Copy constructors let you choose per field.

---

## Choosing between them — the one-question test

| Question | Pattern |
| --- | --- |
| Do I need exactly one instance globally? | Singleton |
| Do I need an object but want to decide the concrete type elsewhere? | Factory Method |
| Do I need a set of objects that must match each other? | Abstract Factory |
| Does the object have many fields, some optional, and I want immutability? | Builder |
| Do I want a named constructor, caching, or to hide the concrete type? | Static Factory Method |
| Is construction expensive and I already have a good template? | Prototype |

---

## The trap each one hides

| Pattern | Common mistake |
| --- | --- |
| Singleton | Using it everywhere — it's a hidden global that breaks testability |
| Factory Method | Adding it when a plain constructor would do; not every `new` needs a factory |
| Abstract Factory | Stuffing behaviour (e.g., `furnishRoom`) into the factory — consumption belongs to the caller |
| Builder | Skipping required-field enforcement — if a field is mandatory, it goes in the builder's constructor, not as a setter |
| Static Factory | Calling `JobOffer.builder()` a "static factory method" — it returns a `Builder`, not a `JobOffer`; that's not the EJ definition |
| Prototype | Using `clone()` and assuming deep copy — it's shallow unless you override everything |
