---
order: 50
---

# Abstract Factory

Abstract Factory produces **a coordinated family of related objects** through a single interface. Callers depend on the family-as-a-whole; the concrete factory chosen at wiring time decides which family they receive. The headline guarantee: products from the same factory are guaranteed to belong together &mdash; you cannot accidentally pair a luxury chair with a cheap sofa.

This package walks the canonical example: two furniture families (cheap, luxury), each producing a `Chair` and a `Sofa` that match each other.

## The shape

```
Chair (interface)              Sofa (interface)
   ├── CheapChair                 ├── CheapSofa
   └── LuxuryChair                └── LuxurySofa

FurnitureSetFactory (interface)        ← the abstract factory
   ├── Chair createChair();
   └── Sofa createSofa();

CheapFurnitureFactory          LuxuryFurnitureFactory
   ├── createChair() → CheapChair    ├── createChair() → LuxuryChair
   └── createSofa()  → CheapSofa     └── createSofa()  → LuxurySofa
```

The arrow that matters: every concrete product class hides under an abstract interface that the factory's methods *return*. Callers see only `Chair` and `Sofa`, never the concrete classes.

## The code

### Abstract products

```java
public interface Chair {
  String getMaterial();
  String getColor();
}

public interface Sofa {
  String getMaterial();
  String getColor();
}
```

### Concrete products (one per family)

```java
@ToString
public class CheapChair implements Chair {
  private final String material = "Cheap material";
  private final String color = "Faded color";
  @Override public String getMaterial() { return material; }
  @Override public String getColor() { return color; }
}
// LuxuryChair, CheapSofa, LuxurySofa: same shape, different field values.
```

### Abstract factory

```java
public interface FurnitureSetFactory {
  Chair createChair();
  Sofa createSofa();
}
```

### Concrete factories

```java
public class CheapFurnitureFactory implements FurnitureSetFactory {
  @Override public Chair createChair() { return new CheapChair(); }
  @Override public Sofa createSofa()   { return new CheapSofa(); }
}

public class LuxuryFurnitureFactory implements FurnitureSetFactory {
  @Override public Chair createChair() { return new LuxuryChair(); }
  @Override public Sofa createSofa()   { return new LuxurySofa(); }
}
```

### The client

```java
public static void main(String[] args) {
  furnishRoom(new CheapFurnitureFactory());
  furnishRoom(new LuxuryFurnitureFactory());
}

private static void furnishRoom(FurnitureSetFactory factory) {
  Chair chair = factory.createChair();
  Sofa sofa = factory.createSofa();
  System.out.println("Chair: " + chair);
  System.out.println("Sofa:  " + sofa);
}
```

The `furnishRoom` helper takes the **abstract** factory type. The same helper drives both families &mdash; the concrete factory class is named only once, at the call site that picks the family.

## Discussion learnings — design decisions worth keeping

These are the decisions that came up while building this package. Each one is a load-bearing choice; getting any of them wrong unwinds the pattern.

### A factory must not hold state

Concrete factories must be **stateless**. Their job is to *produce* products on demand, not to *cache* them or *own* them.

A common mistake:

```java
// Wrong:
public class LuxuryFurnitureFactory implements FurnitureSetFactory {
  private LuxuryChair luxuryChair;          // ← factory holding a product as state
  private LuxurySofa luxurySofa;

  @Override
  public void create() {                    // ← void, side-effect-on-fields
    this.luxuryChair = new LuxuryChair();
    this.luxurySofa = new LuxurySofa();
  }
}
```

Three things break:

1. **Single-shot.** The factory can only "produce" once before its fields are populated; subsequent calls overwrite or do nothing.
2. **Identity confusion.** Every caller of `getChair()` would receive *the same instance*. That's flyweight semantics, not factory semantics &mdash; and it leaks shared state across consumers.
3. **DIP violation.** Fields typed against `LuxuryChair` (concrete) instead of `Chair` (abstract) couple the factory to a specific implementation it should be free to swap.

The right shape: each call to `createChair()` does `return new LuxuryChair();` &mdash; fresh product, no fields, return type is the abstract interface.

> **Rule:** factories produce, they don't possess. If a class needs to hold the products it created, that's a different class (a *registry*, *cache*, or *holder*), not a factory.

### `furnishRoom` is consumption, not production &mdash; it belongs to the caller

The temptation: "both `CheapFurnitureFactory` and `LuxuryFurnitureFactory` need a way to be 'used' &mdash; put `furnishRoom` on the factory interface (or as a `default` method) so both inherit it."

This is technically possible:

```java
// Tempting, but wrong:
public interface FurnitureSetFactory {
  Chair createChair();
  Sofa createSofa();

  default void furnishRoom() {
    System.out.println("Chair: " + createChair());
    System.out.println("Sofa:  " + createSofa());
  }
}
```

It compiles. Both factories inherit `furnishRoom` for free. So why is it wrong?

| Concern | Why it bites |
| --- | --- |
| **Single Responsibility** | The factory now has two reasons to change. Tomorrow you change the print format, switch to a logger, ship to a database, render to HTML &mdash; you're editing the factory interface even though *production* hasn't changed. |
| **Forced support** | Every factory must inherit `furnishRoom`, even ones where the concept doesn't apply (a museum-piece factory that never "furnishes a room", a serialiser that just emits JSON). |
| **Hides the lesson** | The pattern's wow moment is "same client code, any factory". With `furnishRoom` on the interface, the "client code" disappears into the factory implementation &mdash; the polymorphism is no longer at the call site. |

The right home for `furnishRoom` is the **client side** &mdash; a static helper on the runner, or a separate consumer class. It *takes* a `FurnitureSetFactory` as a parameter. The factory provides the *ability* to produce; the caller decides what to *do* with that ability.

> **Rule:** if the variance lives at the call site (different consumers want different things), the logic belongs at the call site. If the variance lives at the factory level (different families produce different things), the logic belongs in the factory's products. The factory itself stays focused on *what to produce*.

### Family-specific behaviour grows by *adding products*, not by adding factory methods

What if a family genuinely needs more behaviour? Say *the luxury family includes free cleaning every year for five years and a twice-yearly movement service*; the cheap family includes neither.

The wrong move: pile new methods onto the factory interface.

```java
// Wrong:
public interface FurnitureSetFactory {
  Chair createChair();
  Sofa createSofa();
  void scheduleCleaning();    // — only luxury has this; cheap is forced into a no-op
  void scheduleMovement();    // — same
}
```

Forcing `CheapFurnitureFactory` to implement `scheduleCleaning` as a no-op is exactly the kind of "Tell, Don't Ask" violation Liskov warns about &mdash; the cheap factory now lies about its capabilities.

The right move: **make the family-specific service its own product**, and have the factory produce it alongside `Chair` and `Sofa`.

```java
public interface FurnitureSetFactory {
  Chair createChair();
  Sofa createSofa();
  AfterSalesService createAfterSalesService();   // ← new product in the family
}

public interface AfterSalesService {
  void scheduleCleaning();
  void scheduleMovement();
  void describePlan();
}

public class LuxuryAfterSalesService implements AfterSalesService {
  // Free cleaning every year for 5 years, twice-yearly movement, ...
}

public class CheapAfterSalesService implements AfterSalesService {
  // Minimal or "no service included" — no lying, just a different family member.
}
```

Now:

- The luxury cleaning logic lives in `LuxuryAfterSalesService`, not in `LuxuryFurnitureFactory`.
- Adding a third family (`StudentFurnitureFactory`) just produces a `StudentAfterSalesService` &mdash; no method is forced anywhere.
- The factory interface stays narrow: each method produces a member of the family, full stop.

> **Rule:** Abstract Factory grows by *widening the family* (more products) rather than *fattening the factory* (more methods on the same products). Each family member carries its own per-family variation.

### Why the factory is an *interface*, not an *abstract class*

This came up in passing. The choice rests on the questions in `todo/FoundationsToRead.md` &raquo; "Abstract class vs Interface":

- **Does the factory need state?** No &mdash; factories are stateless.
- **Does it need a constructor?** No &mdash; nothing to initialise.
- **Does it need a template method calling subclass hooks?** No &mdash; each factory's logic is one line per product.

All three answers point to interface. If any of them flipped (e.g., a shared template method that orchestrates `createChair() + createSofa() + log()` with a `final` algorithm), promoting to an abstract class would be justified. None of those needs exist here.

### Abstract Factory vs Factory Method &mdash; the dial

Both patterns deal with polymorphic creation, but operate at different scales:

| | Factory Method | Abstract Factory |
| --- | --- | --- |
| **Produces** | One product per creator | A family of related products per creator |
| **Creator shape** | Often an abstract class with a template method calling the factory hook | Usually a stateless interface with one method per product type |
| **Polymorphism on** | Which concrete product the hook returns | Which family of concrete products the factory returns |
| **Used when** | Different subclasses do *the same algorithm* with *one varying thing* | Different sets of objects need to be *used together* |

Spotting the difference: if the question is "*which* one of these do I want?", reach for Factory Method. If it's "*which set* of these do I want, and they all need to match?", reach for Abstract Factory.

## When this pattern earns its keep

- Your codebase has **multiple objects that are meaningless apart** &mdash; a `Connection`, `Statement`, and `ResultSet` from the same JDBC driver; a `Button`, `TextField`, and `Checkbox` for one OS theme; a `Chair` and `Sofa` from the same furniture family.
- You want callers to depend on the **family-as-a-whole**, not on individual products.
- You expect the family axis to **vary at runtime** &mdash; different deployments, different config, different user choices.

## When to skip

- You have one product type, not a family. Factory Method is enough.
- The "family" doesn't actually need to match (a `Chair` plus an unrelated `String username`). There's nothing to coordinate.
- The family is fixed and tiny and the call sites are few. A switch statement and a couple of constructors are simpler.

## Demo

Running `RunAbstractFactory.main` produces:

```
=== Cheap family ===
Chair: CheapChair(material=Cheap material, color=Faded color)
Sofa:  CheapSofa(material=Cheap material, color=Faded color)
=== Luxury family ===
Chair: LuxuryChair(material=Luxury material, color=Shinny color)
Sofa:  LuxurySofa(material=Luxury material, color=Shinny color)
```

The same `furnishRoom` call drove both families. The helper has no idea whether the factory it received is `CheapFurnitureFactory` or `LuxuryFurnitureFactory` &mdash; it just sees `FurnitureSetFactory`. Family polymorphism delivered at the call site.

Run with:

```bash
mvn -q exec:java -Dexec.mainClass="org.example.design_patterns.creational.abstract_factory.RunAbstractFactory"
```

## Where this pattern shows up in the JDK and ecosystem

- **JDBC.** A `Driver` is an abstract factory: `getConnection()` returns a `Connection`, which produces `Statement`s, which produce `ResultSet`s &mdash; all from the same vendor's family.
- **XML APIs.** `DocumentBuilderFactory` and `SAXParserFactory` produce coordinated parser objects.
- **Cross-platform UI.** Look-and-feel toolkits (Swing's `LookAndFeel`, web framework component libraries) where one factory provides matching widgets.

## Related

- `creational/CreationalPatternsRoadmap.md` &mdash; this is pattern #3 in the creational sequence.
- `creational/factory/Factory.md` &mdash; Factory Method, the single-product cousin.
- `creational/builder/Builder.md` &mdash; Builder configures one product; Abstract Factory chooses among coordinated families. Different jobs.
- `todo/FoundationsToRead.md` &raquo; "Abstract class vs Interface" &mdash; covers the questions behind the "factory as interface, not abstract class" decision, with practice exercises queued.
- `todo/FoundationsToRead.md` &raquo; "Dependency Inversion (DIP) vs Dependency Injection (DI)" &mdash; the abstract-product return type is a textbook DIP application.

