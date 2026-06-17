# equals / hashCode / Comparable / Comparator

---

## equals

By default `equals` from `Object` checks reference equality (`==`). Override it when you want value equality — two different objects that hold the same data should be considered equal.

**The five-rule contract:**

| Rule | What it means |
|---|---|
| Reflexive | `x.equals(x)` is always `true` |
| Symmetric | if `x.equals(y)` then `y.equals(x)` |
| Transitive | if `x.equals(y)` and `y.equals(z)` then `x.equals(z)` |
| Consistent | multiple calls return same result if nothing changed — keep equals pure, no external state |
| Null-safe | `x.equals(null)` returns `false`, never throws |

**Standard shape — every correct implementation follows this:**

```java
@Override
public boolean equals(Object o) {
    if (this == o) return true;                    // same reference shortcut
    if (!(o instanceof MyClass other)) return false; // null-safe + type check + cast (Java 16+)
    return Objects.equals(field1, other.field1)    // compare fields
        && field2 == other.field2;
}
```

Use `Objects.equals` for object fields (handles null), `==` for primitives. Never use `==` on Strings.

**Symmetry trap — easy to break with inheritance or mixed types:**

```java
// money.equals("$5") → true, but "$5".equals(money) → false
// String.equals doesn't know about Money
```

**Transitivity trap — adding a field in a subclass:**

If `ColorPoint extends Point` and `ColorPoint.equals` ignores color when comparing against a `Point`, transitivity breaks. Standard fix: composition over inheritance — hold a `Point` as a field instead of extending it.

---

## hashCode

**One rule:** if `x.equals(y)` is `true`, then `x.hashCode()` must equal `y.hashCode()`.

The inverse is not required — equal hashCodes don't mean equal objects (that's a collision).

**Why it matters — HashMap/HashSet lookup is two steps:**

1. `hashCode()` → which bucket?
2. `equals()` → is this the right object in the bucket?

If you override `equals` but not `hashCode`, two equal objects land in different buckets. The map never finds the second one — silent data loss, no exception.

```java
map.put(p1, "engineer");
map.get(p2);  // null — p1 and p2 equal by equals, but different hashCode → different bucket
```

**Implementation — include the same fields as equals:**

```java
@Override
public int hashCode() {
    return Objects.hash(field1, field2);  // combines fields into one int
}
```

---

## Comparable

`Comparable<T>` defines the **natural order** of a class. Once implemented, `Collections.sort`, `TreeSet`, `TreeMap`, and `List.sort(null)` all work without a comparator.

```java
public class Person implements Comparable<Person> {
    @Override
    public int compareTo(Person other) {
        return Integer.compare(this.age, other.age);  // ascending by age
    }
}
```

**Return convention:** negative = this comes first, zero = equal, positive = other comes first.

**Never subtract** (`this.age - other.age`) — integer overflow makes it wrong for large values. Always use `Integer.compare`, `Double.compare`, `String.compareTo`.

**Contract — three rules:**
1. Antisymmetric — `x.compareTo(y) > 0` implies `y.compareTo(x) < 0`
2. Transitive — if `x > y` and `y > z` then `x > z`
3. Consistent with equals *(strongly recommended)* — `x.compareTo(y) == 0` should mean `x.equals(y)`

**Consistent with equals — why it matters for TreeSet/TreeMap:**

`TreeSet` uses `compareTo` for everything including deduplication — `equals` is never called. If `compareTo` returns 0 for two objects `equals` considers different, `TreeSet` silently drops one.

```java
// compareTo uses age only → Zlice(30), Alice(30), Blice(30) all return 0
// TreeSet keeps only one of the three — silent data loss
TreeSet<Person> set = new TreeSet<>(people);
```

Fix: make `compareTo` include all fields that `equals` includes.

**Multi-field compareTo — chain Comparator to avoid if/else:**

```java
@Override
public int compareTo(Person other) {
    return Comparator.comparingInt((Person p) -> p.age)
        .thenComparing(p -> p.name)
        .compare(this, other);
}
```

---

## Comparator

`Comparator` is external ordering — defined at the call site, not on the class. Any number of orderings per class.

```java
// sort by name — without touching Person
list.sort(Comparator.comparing(p -> p.name));

// age descending
list.sort(Comparator.comparingInt((Person p) -> p.age).reversed());

// age ascending, name as tiebreaker
list.sort(Comparator.comparingInt((Person p) -> p.age).thenComparing(p -> p.name));
```

`thenComparing` only kicks in when everything before it returned 0.

---

## Which method each collection uses

| Collection | Equality / ordering method |
|---|---|
| `HashMap`, `HashSet` | `hashCode` (bucket) + `equals` (confirm) |
| `TreeMap`, `TreeSet` | `compareTo` or `Comparator` — `equals` never called |
| `ArrayList.contains`, `indexOf` | `equals` only |

---

## Quick recall

**Q. Override equals but not hashCode — what breaks?**
A. HashMap/HashSet — two equal objects land in different buckets, lookups return null.

**Q. Why does TreeSet silently drop elements?**
A. It uses `compareTo` for deduplication, not `equals`. If `compareTo` returns 0, the element is treated as a duplicate regardless of what `equals` says.

**Q. When to use Comparable vs Comparator?**
A. `Comparable` for the one natural order that belongs on the class. `Comparator` for any additional or context-specific orderings.

**Q. Why not subtract in compareTo?**
A. Integer overflow — `Integer.MIN_VALUE - 1` wraps to positive, flipping the order silently.

**Q. Comparator for multiple fields without if/else?**
A. `Comparator.comparingInt(...).thenComparing(...).thenComparing(...)` — chain tiebreakers.
