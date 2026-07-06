---
order: 80
---
# Polymorphism — the details

> One of the four OOP pillars. Does the heaviest lifting in design patterns — virtual dispatch is the mechanism that replaces `if`/`else` chains.

---

## Two kinds — know which one you're using

**Compile-time (static) polymorphism** — resolved by the *declared type* of the variable at compile time.

- Method overloading: `Math.max(int, int)` and `Math.max(double, double)` — same name, different parameter types, compiler picks the right one.
- Generics: `List<String>` vs `List<Integer>` — type is fixed at compile time.

**Runtime (dynamic) polymorphism** — resolved by the *actual class* of the object at runtime. This is the one design patterns exploit.

- Method overriding + virtual dispatch: calling `shape.clone()` on a `Shape` reference dispatches to `Circle.clone()` or `Rectangle.clone()` based on what the object actually is, not what the variable says it is.

| | Static (compile-time) | Dynamic (runtime) |
| --- | --- | --- |
| Resolved by | Declared type of variable | Actual runtime class of object |
| Mechanism | Overloading, generics | Overriding + virtual dispatch |
| Participates in polymorphic dispatch? | No | Yes |
| Example | `Math.max(int, int)` vs `Math.max(double, double)` | `shape.clone()` on a `List<Shape>` |

**Static methods do NOT participate in dynamic dispatch.** You can shadow a static method in a subclass, but the call is resolved by the declared type, not the runtime type. This is why patterns use instance methods for factory hooks, not static ones.

---

## How virtual dispatch replaces `if`/`else`

Without polymorphism — the caller must know and branch:

```java
// Caller is coupled to every concrete type
if (shape instanceof Circle) {
    ((Circle) shape).cloneCircle();
} else if (shape instanceof Rectangle) {
    ((Rectangle) shape).cloneRectangle();
}
```

With polymorphism — the type system dispatches for you:

```java
shape.clone(); // JVM picks Circle.clone() or Rectangle.clone() at runtime
```

Every time you see an `instanceof` chain or a `switch` on a type, that is a polymorphism opportunity — and usually a sign that the responsibility belongs in the class, not in the caller.

---

## Where this appears in this repo

| Call site | What dispatches | Concrete methods called at runtime |
| --- | --- | --- |
| `shape.clone()` in `PolymorphicPrototypeRun` | `Shape.clone()` (abstract) | `Circle.clone()` or `Rectangle.clone()` |
| `hiringProcess.onboard()` in `HR.hireForTeam()` | `HiringProcess.onboard()` (interface) | `DeveloperHiringProcess.onboard()` (final template) |
| `factory.createChair()` in `furnishRoom()` | `FurnitureSetFactory.createChair()` (interface) | `CheapFurnitureFactory` or `LuxuryFurnitureFactory` override |

In every case, the caller holds an abstract type and calls one method. The JVM handles the rest.

---

## Covariant return types

A subclass can override a method and *narrow* the return type:

```java
// Abstract parent
public abstract Shape clone();

// Concrete child — return type Circle is more specific than Shape, and that is allowed
@Override
public Circle clone() { return new Circle(this); }
```

Callers who only know `Shape` still compile and work fine. Callers who know they have a `Circle` get a `Circle` back without casting. This is how `Circle.clone()` works in `prototype/polymorphic/`.

---

## The dispatch mechanism — why static methods aren't polymorphic

Polymorphism *is* runtime dispatch. The mechanism only exists for instance methods:

| Member kind | Dispatch | Wins |
|---|---|---|
| **Instance method** | Dynamic (runtime) | **Actual object's type** ← polymorphism lives here |
| Static method | Static (compile-time) | Declared type |
| Field | Static (compile-time) | Declared type |
| `private` method | Static (compile-time) | Declared type |
| `final` method | `invokevirtual`, but JIT-devirtualizable | Effectively declared type — no override possible |

So `factory.createChair()` only dispatches to the right concrete factory because `createChair` is an instance method. Replace it with a static and the dispatch flips back to declared-type and polymorphism evaporates.

Three runnable demos in this folder make this concrete:
- `WorkingPolymorphism.java` — instance-method dispatch, works as expected.
- `StaticHidingTrap.java` — same shape with `static` methods, declared-type wins.
- `FieldHidingTrap.java` — same trap applied to fields, plus the matching instance getter to show the contrast.

### What the JVM actually does

Instance call:
```
shape.clone()  →  bytecode: invokevirtual Shape.clone()
                  JVM at runtime: "what is shape's actual class? walk its method table → pick the override"
```

Static call:
```
Repo.findAll()         →  invokestatic Repo.findAll()
r.findAll() (r: Repo)  →  invokestatic Repo.findAll()    // r is unused!
```

The reference `r` only exists to tell the *compiler* "look up `findAll` starting from `Repo`." It is never dereferenced at runtime.

### The smoking-gun proof — null doesn't NPE

```java
Parent p = null;
p.greet();    // prints "Parent" — static: ref is unused, no NPE
p.toString(); // NullPointerException — instance: ref must be deref'd to find the vtable
```

If a method can be called on a `null` reference without exception, that method is statically dispatched. End of test.

### `@Override` is the compiler's tell

```java
class Child extends Parent {
    @Override static void greet() { ... }   // does NOT compile
    @Override void greet() { ... }          // compiles
}
```

`@Override` is enforced — the compiler insists the method actually overrides something. It refuses on a static because there *is* no override relationship; the parent's static and the child's static are unrelated methods that share text.

### Override vs Hide — precise terminology

| | Override | Hide |
|---|---|---|
| Applies to | Instance methods | Static methods, fields |
| Dispatch | Dynamic — actual type | Static — declared type |
| `@Override` | Compiles | Does not compile |
| Mental model | One method, two implementations, JVM picks at runtime | Two unrelated members; declared type picks |

### Fields are hidden too — same rule, applied to data

```java
class Parent { String name = "parent"; }
class Child  extends Parent { String name = "child"; }

Parent p = new Child();
p.name;            // "parent" — declared type wins, just like static methods
((Child) p).name;  // "child"
```

A `Child` instance literally carries *both* `name` fields in memory; the reference type at the call site decides which one you read. This is the strongest reason fields should be `private` and read through (polymorphic) getters.

### The factory walks because instance methods dispatch

```java
public interface FurnitureSetFactory {
    Chair createChair();   // instance method on an interface
}

static void furnishRoom(FurnitureSetFactory factory) {
    Chair chair = factory.createChair();   // invokeinterface
}

furnishRoom(new CheapFurnitureSetFactory());   // → CheapChair
furnishRoom(new LuxuryFurnitureSetFactory());  // → LuxuryChair
```

`factory` is declared `FurnitureSetFactory` but the JVM ignores that at runtime — it walks the actual object's vtable and runs the matching override. Polymorphism *because* it's an instance method. The Abstract Factory pattern is structurally impossible with static methods: an interface can't declare instance behaviour through statics, and the call site couldn't dispatch even if it could.

### The real-world trap — "I'll override the static for tests"

A common bad design:

```java
class Repo {
    static List<User> findAll() { return DB.query("..."); }
}
class TestRepo extends Repo {
    static List<User> findAll() { return List.of(new User("test")); }
}
```

Production code calls `Repo.findAll()` (or `repo.findAll()` on a `Repo` reference) — both compile to `invokestatic Repo.findAll()`. The `TestRepo.findAll()` "stub" is unreachable; tests still hit the database. There is *no swap point* — the bytecode hard-codes the class name.

The fix is to make it an instance method and inject:

```java
class Repo {
    List<User> findAll() { return DB.query("..."); }
}
class TestRepo extends Repo {
    @Override List<User> findAll() { return List.of(new User("test")); }
}

class UserService {
    private final Repo repo;
    UserService(Repo repo) { this.repo = repo; }   // DI seam
    void notifyAll() { for (User u : repo.findAll()) { ... } }
}

new UserService(new Repo());      // production
new UserService(new TestRepo());  // tests
```

Now `repo.findAll()` is `invokevirtual` → runtime type wins → the test stub runs in tests. This is *exactly why DI exists*: it gives you the polymorphism-aware seam that statics structurally deny.

### One-line summary

Polymorphism (runtime-type dispatch) only happens for instance methods. Fields, statics, and `private`/`final` follow the declared type. Every "look up the right thing at runtime" pattern (Strategy, Template Method, Abstract Factory, Visitor) is built on instance methods specifically — and that's not coincidence; it's the only way the language gives you the seam.

---

## Quick recall

**Q. One-sentence rule for what dispatches polymorphically?**
A. Polymorphism (runtime-type dispatch) only happens for instance methods. Fields, statics, and `private`/`final` follow the declared type.

**Q. Why does Abstract Factory work but a "TestRepo extends Repo" stub of static methods doesn't?**
A. `createChair()` is an instance method → `invokevirtual` → JVM picks the override at runtime. A static `findAll()` becomes `invokestatic Repo.findAll()` at compile time; the reference is never read, so a subclass's static is unreachable.

**Q. Smoking-gun proof that a method is statically dispatched?**
A. You can call it on a `null` reference without an NPE — the reference is never dereferenced.

**Q. Override vs Hide — one-line difference?**
A. Override = same call site runs different code based on the actual object (instance methods, dynamic). Hide = same name in two unrelated places, picked by declared type (statics, fields).

**Q. The fix when you really want a swap-in stub?**
A. Make the method an instance method and inject the implementation (DI). The call becomes virtual and runtime-type wins.

---

## Related topics

- **Inheritance** — the mechanism that creates the chain virtual dispatch walks.
- **Abstract Class vs Interface** — both can host abstract methods that get polymorphically dispatched.
- **Access Modifiers Deep Dive** — `private` methods are not virtual; `protected` and `public` instance methods are.

