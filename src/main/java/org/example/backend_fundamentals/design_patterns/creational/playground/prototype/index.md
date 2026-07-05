---
order: 40
---

# Prototype

Prototype is a creational pattern that lets you create new objects by **copying an existing instance** rather than constructing from scratch. The existing instance is the "prototype" — a ready-made template you clone and tweak.

This package has two sub-packages that each teach a distinct aspect of the pattern:

- `simple/` — copy constructor mechanics: shallow copy vs deep copy, and why the difference matters.
- `polymorphic/` — GoF polymorphic cloning: abstract `clone()` method, concrete subclasses, caller works against the abstract type and never knows the concrete class.

---

## The shape

### `simple/` — copy constructor, shallow vs deep

```
ShapeShallow                       ShapeDeep
  + ShapeShallow(name, points)       + ShapeDeep(name, points)
  + ShapeShallow(ShapeShallow)  ←←   + ShapeDeep(ShapeDeep)       ← copy constructors
    points = shape.points              points = stream().map(new Point)...
    (shared reference)                 (brand new Point objects)

Point
  + int x, y   (mutable — the trap lives here)
```

### `polymorphic/` — abstract clone, polymorphic dispatch

```
Shape (abstract)
  + x, y, color
  # Shape(Shape source)          ← shared copy constructor, called via super(source)
  + clone(): Shape (abstract)

    ├── Circle
    │     + radius
    │     - Circle(Circle source)    ← private copy constructor
    │     + clone(): Circle          ← covariant return
    │
    └── Rectangle
          + width, height
          - Rectangle(Rectangle source)
          + clone(): Rectangle
```

The caller holds `List<Shape>` and calls `shape.clone()`. The JVM dispatches to `Circle.clone()` or `Rectangle.clone()` at runtime — the caller never checks `instanceof` or casts.

---

## The code

### `simple/` — copy constructor variants

```java
// Shallow copy — points list reference is shared
public ShapeShallow(ShapeShallow shape) {
    this.name = shape.name;
    this.points = shape.points;        // same list, same Point objects
}

// Deep copy — each Point reconstructed independently
public ShapeDeep(ShapeDeep shape) {
    this.name = shape.name;
    this.points = shape.getPoints().stream()
        .map(point -> new Point(point.getX(), point.getY()))
        .collect(Collectors.toList());
}
```

### `polymorphic/` — abstract Shape with copy constructor chain

```java
// Abstract base — copy constructor copies shared fields
protected Shape(Shape source) {
    this.x = source.x;
    this.y = source.y;
    this.color = source.color;
}

public abstract Shape clone();

// Concrete — private copy constructor, called only by clone()
private Circle(Circle source) {
    super(source);           // delegates shared fields to Shape's copy constructor
    this.radius = source.radius;
}

@Override
public Circle clone() {
    return new Circle(this); // covariant return: Circle, not Shape
}
```

### The polymorphic client

```java
List<Shape> originals = new ArrayList<>();
originals.add(new Circle(10, 10, "red", 20));
originals.add(new Rectangle(5, 5, "blue", 30, 15));

List<Shape> clones = new ArrayList<>();
for (Shape shape : originals) {
    clones.add(shape.clone());   // no instanceof, no cast — polymorphism does the work
}
```

---

## Design decisions worth keeping

### Shallow copy shares mutable state — and that is always a bug in production

When the copy constructor does `this.points = shape.points`, both the original and the copy hold a reference to the same `List<Point>`. Mutating a `Point` in the copy (via `setX` / `setY`) is seen by the original — they share the same objects.

```
Original:  Shape [ points → [ P1, P2, P3 ] ]
                                ↑   ↑   ↑
Shallow:   Shape [ points → [ P1, P2, P3 ] ]   ← same Point instances
```

`ShapeShallow` demonstrates this intentionally. In production, a shallow copy of a mutable nested object is a hidden aliasing bug.

### `new ArrayList<>(shape.points)` is not a deep copy

This is the most common intermediate mistake:

```java
// Looks like deep copy — is not
this.points = new ArrayList<>(shape.points);
```

`new ArrayList<>(...)` creates a new list wrapper but copies the same `Point` references into it. The lists are independent (you can add/remove from one without affecting the other), but the `Point` objects inside are still shared. `setX` on a point in the copy still mutates the original's point.

The fix: reconstruct each nested object:

```java
this.points = shape.getPoints().stream()
    .map(point -> new Point(point.getX(), point.getY()))
    .collect(Collectors.toList());
```

### The private copy constructor

In `Circle` and `Rectangle`, the copy constructor is `private`. Nobody outside the class should construct a `Circle` by handing in another `Circle` — they go through `clone()`. The `clone()` method is the public contract; the copy constructor is its implementation detail.

```java
private Circle(Circle source) { ... }   // implementation — private

public Circle clone() {                  // contract — public
    return new Circle(this);
}
```

### Covariant return type

`Circle.clone()` is declared as returning `Circle`, not `Shape`. This is Java's *covariant return type*: a subclass can override a method and narrow the return type, as long as the narrower type is still a subtype.

```java
// Parent declares
public abstract Shape clone();

// Child narrows — this is valid
@Override
public Circle clone() { return new Circle(this); }
```

Callers who only know `Shape` still compile and run fine — they get a `Shape` back. Callers who know they have a `Circle` get a `Circle` without casting.

### Why `Object.clone()` + `Cloneable` is broken — and why we avoided it

Java's built-in `Cloneable` + `clone()` mechanism has several well-known problems:

| Problem | What breaks |
| --- | --- |
| `Cloneable` is a marker interface with no methods | You can't tell from the type whether an object supports cloning |
| `Object.clone()` does a shallow copy by default | Every mutable nested field must be manually deep-copied; forgetting one is a silent bug |
| `clone()` bypasses constructors | Invariants enforced in your constructor are silently skipped during cloning |
| `CloneNotSupportedException` is checked | Must be caught or declared, even though it can never be thrown once `Cloneable` is implemented |
| No `super.clone()` chain enforcement | If a subclass forgets to call `super.clone()`, it gets a wrong type back |

*Effective Java* Item 13 calls `clone()` "a highly atypical use of interfaces" and recommends against implementing it. The copy constructor approach used in this package avoids every one of these problems.

### The copy constructor chain (`super(source)`)

In `polymorphic/`, the shared fields (`x`, `y`, `color`) are copied by `Shape`'s protected copy constructor. Each concrete subclass calls `super(source)` to delegate the shared work, then copies its own fields.

This is the correct pattern because: shared copy logic lives in one place, concrete subclasses cannot forget to copy the parent fields (they must call `super`), and adding a new field to `Shape` only requires updating `Shape`'s copy constructor — not every subclass.

---

## When this pattern earns its keep

- **Construction is expensive.** Database lookups, heavy computation, external calls — if you already have a valid fully-configured instance, copying it is faster than rebuilding from scratch.
- **Many near-identical instances.** Game engine: 100 enemies from the same base config with minor stat tweaks. Document editor: duplicate a paragraph style or a slide template.
- **Configuration templates.** A "default config" object is built once and cloned per environment, per user, or per request; callers tweak only what differs.
- **Caller doesn't know the concrete type.** `PolymorphicPrototypeRun` demonstrates this: the client clones a whole `List<Shape>` without knowing it contains a `Circle` and a `Rectangle`. This is the GoF headline use case.

## When to skip

- Construction is cheap. `new Circle(...)` is trivial — there's nothing to gain from cloning.
- The object has no mutable nested state. No shared references = shallow copy is safe = just use the regular constructor.
- The object graph has circular references. Cloning becomes recursive and must be carefully cycle-broken; usually a sign to rethink the design.

---

## Demo output

### `simple/` — ShapeRun

```
SHALLOW
ShapeShallow(name=Rectangle, points=[Point(x=1, y=1), Point(x=2, y=2), Point(x=5, y=5), Point(x=4, y=4)])
ShapeShallow(name=Rectangle, points=[Point(x=1, y=1), Point(x=2, y=2), Point(x=5, y=5), Point(x=4, y=4)])
DEEP
ShapeDeep(name=Rectangle, points=[Point(x=1, y=1), Point(x=2, y=2), Point(x=3, y=3), Point(x=4, y=4)])
ShapeDeep(name=Rectangle, points=[Point(x=1, y=1), Point(x=2, y=2), Point(x=5, y=5), Point(x=4, y=4)])
```

In the shallow section, both lines show `Point(x=5, y=5)` at index 2 — the mutation on the copy was seen by the original. In the deep section, only the second line (the copy) shows the mutation; the original stays at `Point(x=3, y=3)`.

### `polymorphic/` — PolymorphicPrototypeRun

```
=== Originals ===
Circle(super=Shape(x=10, y=10, color=red), radius=20)
Rectangle(super=Shape(x=5, y=5, color=blue), width=30, height=15)

=== Clones ===
Circle(super=Shape(x=10, y=10, color=red), radius=20)
Rectangle(super=Shape(x=5, y=5, color=blue), width=30, height=15)

=== Same object reference? ===
Circle: DIFFERENT — correct
Rectangle: DIFFERENT — correct
```

Same values, different objects — independent copies. Clones printed correctly as `Circle` and `Rectangle` without the client ever naming those classes.

Run with:

```bash
mvn -q exec:java -Dexec.mainClass="org.example.design_patterns.creational.prototype.simple.ShapeRun"
mvn -q exec:java -Dexec.mainClass="org.example.design_patterns.creational.prototype.polymorphic.PolymorphicPrototypeRun"
```

---

## Where this pattern appears in the JDK

- `new ArrayList<>(existingList)` — copy constructor; independent list with the same elements (shallow).
- `new HashMap<>(existingMap)` — same.
- `String` is effectively a prototype: every `substring`, `toUpperCase`, etc. produces a new independent string.
- Jackson's `objectMapper.convertValue(obj, SameClass.class)` — deep copy via serialization round-trip; the modern way when copy constructors aren't available.
- Spring's `BeanUtils.copyProperties(source, target)` — shallow property copy by reflection.

---

## Related

- `creational/CreationalPatternsRoadmap.md` — this is the last pattern in the creational sequence.
- `creational/CreationalPatternsQuickRef.md` — one-paragraph mental model for fast revision.
- `todo/FoundationsToRead.md` → "Polymorphism — the details" — the virtual dispatch mechanism that makes `PolymorphicPrototypeRun` work.
- `todo/FoundationsToRead.md` → "Java Collections" — ArrayList copy constructor, `List.of` immutability, and why `new ArrayList<>(list)` is a shallow copy.
