---
order: 10
---
# Encapsulation

> First of the four OOP pillars. Builds on access modifiers, paired with Abstraction.

---

## Beyond "private fields, public methods"

Encapsulation is *information hiding* — a deliberate decision about what to expose and what to protect. Access modifiers are the tool; the principle is bigger.

**Three layers** (each demonstrated by a class in this folder):

1. **Field-level** — `private` fields, accessed only through methods. Any validation or invariant enforcement lives in those methods, not scattered across callers. See `FieldLevel.java`.
2. **Package-level** — no access modifier (package-private) means visible within the package, hidden from outside. Used in the factory packages: `AndroidDeveloper` is package-private so external code cannot `new AndroidDeveloper()` — it must go through `AndroidHiringProcess`. See `PackageLevel.java` and the cross-package counter-demo in `other/CrossPackageAccessRun.java`.
3. **Construction-level** — `final` fields + a validating constructor and no setter (or private constructors + factory methods). The class controls *how* it's created and stays valid for its whole lifetime. This is also what `BillPughSingleton`, `JobOffer.Builder`, and `Temperature.celsius()` all exploit. See `ConstructorLevel.java`.

`PackageLevel` is deliberately mixed: a `public` field/method (visible everywhere) sits next to a package-private field/method (visible only to same-package code). The cross-package runner reads the `public` ones successfully and keeps the package-private accesses as commented-out lines — uncomment one and `mvn -q compile` rejects it. That contrast is the clearest way to feel where the package boundary actually sits.

---

## Immutability as the strongest encapsulation

`final` fields + no setters = nothing outside the object can corrupt its state after construction. Callers can read; they can never write.

```java
// No setter, final field — once set in the constructor, salary is sealed
private final int salary;
```

This is what the EJ Builder pattern achieves for `JobOffer`: the object is fully validated in `build()` and then frozen. Any future caller who holds the `JobOffer` reference cannot put it into an invalid state.

---

## Encapsulation violations to watch for

**Returning a mutable field reference directly:**

```java
// Bad — caller can mutate internal state
public List<Point> getPoints() { return points; }

// Better — caller gets a read-only view
public List<Point> getPoints() { return Collections.unmodifiableList(points); }
```

`ShapeShallow` does this intentionally so the shallow-copy trap is visible. In production code, this would be a bug.

**Public fields:**

```java
public int salary; // any caller can set it to -1 with no validation
```

**Setter on an "immutable" class:** adding `setSalary(int)` to `JobOffer` after building it via the Builder defeats the entire point — the object is no longer immutable.

---

## Encapsulation and testability

A well-encapsulated class is easy to test in isolation because:

- Its dependencies arrive via the constructor (or a seam) rather than being hardcoded inside.
- Its state is changed only through its own methods, so the test can set up known state and trust it stays that way.

A class that `new`s its own dependencies internally (hardcoded `new EmailService()`) is poorly encapsulated at the dependency boundary — there's no seam to inject a test double.

---

## Quick recall

**Q. Name three encapsulation violations.**
A. Returning a mutable field reference; public fields; providing a setter on a class meant to be immutable.

**Q. Same package vs different package — what changes?**
A. Same package sees both `public` and package-private members. A different package only sees `public`; package-private members don't compile.

**Q. Two practical fixes for "returning a mutable field"?**
A. Return `Collections.unmodifiableList(list)` (read-only view) or `List.copyOf(list)` (defensive copy).

**Q. Why is constructor-level encapsulation stronger than a validating setter?**
A. The object is born valid (constructor checks the invariant) and a `final` field can never become invalid afterward — there's no setter to misuse later.
