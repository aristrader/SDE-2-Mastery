# Hashing in Java — what it is and how Java uses it

---

## What hashing is (the general idea)

A **hash function** takes any input and returns a fixed-size number. Properties that make a hash function useful:

- **Deterministic** — the same input always produces the same hash.
- **Fast** — computing the hash should be cheap.
- **Well-distributed** — slightly different inputs produce very different hashes; the output spreads evenly across the range.

Hashes are *not* meant to uniquely identify the input. Two different inputs *can* have the same hash (a **collision**) — the hash is a fixed size (e.g., 32 bits), the input space is unbounded, so collisions are mathematically guaranteed. A good hash function makes them rare for normal inputs.

In Java, every object has a hash via `Object.hashCode()`, which returns an `int`. This is what `HashMap`, `HashSet`, and friends rely on.

---

## The `hashCode` / `equals` contract

The contract has three rules:

1. **If `a.equals(b)` is true, then `a.hashCode() == b.hashCode()`.**
   Equal objects must have equal hashes. Otherwise, hash-based collections silently fail to find the second copy.
2. **The reverse is NOT required.** Two unequal objects *may* have the same hash. That's a collision — handled separately, not a bug.
3. **`hashCode` must be stable** for the lifetime of the object, *as long as the fields it depends on don't change*. If you compute hash from mutable state and then mutate, the object becomes unfindable in any hash-based collection it was inserted into.

### Default implementations from `Object`

- `Object.equals(o)` — identity comparison (`this == o`). Two different instances are never equal even if they look the same.
- `Object.hashCode()` — derived from the JVM-internal identity of the object. Two different instances usually have different hashes (no contract guarantee on what value).

Without overrides, your class has **identity semantics** — wrong for value types (a `Money(100, USD)` should equal another). See `collections/maps/traps/BrokenEqualsHashCodeTrapRun`.

### What a good `hashCode` looks like

For a value class, derive it from the same fields as `equals`. The standard idiom:

```java
@Override
public int hashCode() {
    return Objects.hash(field1, field2, field3);
}
```

`Objects.hash(...)` is convenient but allocates a varargs array. For tight loops, prefer manual computation:

```java
@Override
public int hashCode() {
    int result = 17;
    result = 31 * result + field1.hashCode();
    result = 31 * result + Integer.hashCode(field2);
    result = 31 * result + Boolean.hashCode(field3);
    return result;
}
```

The choice of `31` is the Java tradition: odd prime, decent distribution, and the JVM optimises `31 * x` to `(x << 5) - x`.

---

## How `HashMap` uses hashes — the bucket model

A `HashMap` is internally an **array of buckets** (default size 16):

```
HashMap internal array (default capacity = 16):
┌───┬───┬───┬───┬───┬───┬───┬───┬───┬───┬───┬───┬───┬───┬───┬───┐
│ 0 │ 1 │ 2 │ 3 │ 4 │ 5 │ 6 │ 7 │ 8 │ 9 │10 │11 │12 │13 │14 │15 │
└───┴───┴───┴───┴───┴───┴───┴───┴───┴───┴───┴───┴───┴───┴───┴───┘
```

To `put(key, value)`:

1. Compute `h = key.hashCode() ^ (key.hashCode() >>> 16)` — a small mixing step that XORs the upper 16 bits onto the lower 16 to spread bias.
2. Compute the bucket index: `h & (capacity - 1)`. With capacity 16, that's the last 4 bits of `h`.
3. Store the entry in that bucket.

`get(key)` does the same first two steps to find the bucket, then walks the bucket comparing entries with `equals()` until it finds a match.

Array index access is O(1) and hashing is O(1), so as long as buckets stay small, `get`/`put` are O(1).

### Iteration order is bucket-walk order

When you iterate, the iterator walks the array from index 0, emitting entries from each non-empty bucket. Iteration order is bucket position, not insertion or sort. Two consequences:

- For a fixed input, the order is **deterministic** (a function of hash codes and capacity).
- After a resize, every entry rehashes into a different bucket, so the order **changes**. Never persist or rely on `HashMap` iteration order.

Three families avoid this surprise differently:

- `LinkedHashMap` adds a doubly-linked list so iteration follows insertion order.
- `TreeMap` is a red-black tree, so iteration follows key sort order.
- `EnumMap` is an array indexed by `enum.ordinal()`, so iteration follows enum declaration order.

---

## Collisions — the core mechanism

Two distinct keys can land in the same bucket. This happens for two reasons:

1. **True hash collisions** — `a.hashCode() == b.hashCode()` for two different keys. Rare for good hash functions but allowed. Famous Java example: `"Aa".hashCode() == "BB".hashCode() == 2112` (and `"BB"` and `"C#"` and several other 2-character pairs).
2. **Modular collisions** — different hash codes that produce the same bucket index. With capacity 16, the index uses only the last 4 bits of the hash; any two keys sharing those 4 bits collide. Far more common than true collisions.

When a bucket holds multiple entries, `HashMap` stores them in a small **linked list** (or, since Java 8, a balanced **red-black tree** once a single bucket reaches 8 entries — *treeification*).

### How `put` handles a collision

```
put("Aa", 1):  hash → bucket X. Bucket X empty. Store [Aa=1].
put("BB", 2):  hash → bucket X (true collision). Bucket X has [Aa=1].
                Walk it: "Aa".equals("BB") → false. Keep walking.
                End of list. Append. Bucket X is now [Aa=1] -> [BB=2].
put("Aa", 9):  hash → bucket X. Walk it.
                "Aa".equals("Aa") → true. Replace value, not append.
                Bucket X is now [Aa=9] -> [BB=2].
```

### Why both `equals` and `hashCode` matter

- **`hashCode`** decides *which bucket*.
- **`equals`** decides *which entry within the bucket*.
- A broken contract (e.g., `equals` returns true but `hashCode` differs) means lookup visits the wrong bucket entirely, walks it, finds nothing, and returns `null` silently. That's the failure mode in `collections/maps/traps/BrokenEqualsHashCodeTrapRun`.

### Performance impact of collisions

| Scenario | Get/put cost |
| --- | --- |
| Few collisions, buckets stay tiny | O(1) |
| Many collisions, < 8 entries per bucket (linked list) | O(n) per affected bucket |
| Many collisions, ≥ 8 entries per bucket (auto-treeified) | O(log n) per affected bucket |
| Tree shrinks below 6 entries | Tree converts back to linked list |

Treeification thresholds (8 up, 6 down) prevent thrashing if a bucket hovers at the boundary. The tree fallback also protects against collision attacks — adversarial inputs that force O(n²) behaviour.

---

## Resizing — why iteration order can change

When the map gets ~75% full (the default **load factor**), the internal array doubles in size and every entry is **rehashed** into the new larger array. Bucket index is `hash & (capacity - 1)`; capacity changes, the bit mask changes, every key gets a new bucket.

Numbers:
- Default initial capacity: **16**.
- Default load factor: **0.75**.
- Resize threshold: `capacity * load_factor` = 12 entries → triggers resize to capacity 32.

This is why iteration order can vary across runs and why no code should rely on it.

---

## Common pitfalls

1. **Mutating fields used in `hashCode` after the object is in a map.**
   The map looked up the original bucket using the *old* hash; the bucket entry's hash is fixed. Subsequent lookups using the now-mutated key compute a *new* hash, find a *different* bucket, and silently return `null`. Lesson: keys in hash-based collections should be effectively immutable.

2. **Using mutable types as keys.**
   `ArrayList`, `HashMap`, `Date` — anything whose `equals`/`hashCode` depends on contents that can change. Same trap as #1.

3. **Persisting a `hashCode` value across JVM versions.**
   The contract guarantees stability for the lifetime of *one* JVM run. Different JVM versions may use different hash algorithms (especially `String.hashCode()` is stable across JVMs by spec, but most other types' hashes are not). Never serialise a hash code and expect it to mean anything later.

4. **Forgetting to override `hashCode` after overriding `equals`.**
   The most common contract violation. The IDE-generated default usually does both together; a hand-written `equals` without `hashCode` quietly breaks every hash-based collection that touches your type. Effective Java Item 11.

5. **Identity-based defaults on value types.**
   `class Money { BigDecimal amount; Currency currency; }` without overriding gets `Object`'s identity-based equals/hashCode. Two `Money(100, USD)` instances aren't equal. The fix is overriding both based on the same fields.

---

## Where else hashing shows up in Java

| Type | How it uses hashing |
| --- | --- |
| `HashMap` | Bucket array indexed by `key.hashCode()`. |
| `HashSet` | Backed by `HashMap` — set membership is "is the element a key?" Same hashing concerns apply. |
| `LinkedHashMap` | Same buckets as `HashMap`, plus a doubly-linked list for insertion order. |
| `LinkedHashSet` | Same — backed by `LinkedHashMap`. |
| `ConcurrentHashMap` | Bucket array with **per-bucket locks** (lock striping) so concurrent writes to different buckets don't contend. |
| `Hashtable` | Legacy synchronised version of `HashMap` — global lock, slower than `ConcurrentHashMap`, allows null neither. Avoid in new code. |
| `IdentityHashMap` | Uses `System.identityHashCode(key)` and `==` instead of `equals` — for cases where you want identity-based lookup. |
| `TreeMap` / `TreeSet` | Don't use hashing. Use a red-black tree keyed by `Comparable.compareTo()` or a `Comparator`. Slower (O(log n)) but always sorted. |

---

## Quick recall

**Q. What two conditions together define a hash collision in a `HashMap`?**
A. Two distinct keys produce either the same `hashCode()` (true collision) or different hash codes that mask to the same bucket index via `(n-1) & hash` (modular collision).

**Q. Why does `HashMap` need `equals` even though every key has a `hashCode`?**
A. `hashCode` picks the bucket; `equals` picks the entry within that bucket. Without `equals`, the map can't tell two colliding keys apart.

**Q. If `a.equals(b)` returns true but `a.hashCode() != b.hashCode()`, what's the visible failure mode in a `HashMap`?**
A. `put(a, v)` and `get(b)` visit different buckets, so the lookup silently returns `null`. No exception, just lost data.

**Q. Why does mutating a key's fields after putting it into a `HashMap` make the value unreachable?**
A. The node caches the original hash. After mutation, lookups compute a new hash, land in a different bucket, and miss the orphaned entry sitting in the original bucket.

**Q. What's the trigger condition for `HashMap`'s linked-list-to-tree conversion, and why does it exist?**
A. Bucket chain reaches 8 entries AND `table.length >= 64`. It bounds worst-case `get`/`put` at O(log n) instead of O(n), protecting against adversarial hash-collision attacks.

**Q. Why does `HashMap` iteration order change after the map grows past 12 entries?**
A. At ~75% load, the map resizes (capacity doubles). The bucket index uses different bits of the hash, so every entry rehashes into a different bucket and iteration order shuffles.

**Q. What collision-resolution strategy does Java's `HashMap` use?**
A. Separate chaining — each bucket holds a linked list (or red-black tree after treeification) of colliding entries.

---

## Related topics

- **Maps** — hashing is the mechanism behind `HashMap`, `LinkedHashMap`, `EnumMap`, and `ConcurrentHashMap`.
- **Sets** — `HashSet` and `LinkedHashSet` are backed by `HashMap`; same contract applies to elements.
- **`equals` / `hashCode` contract** — Effective Java Items 10 and 11.
- **`BrokenEqualsHashCodeTrapRun`** in `collections/maps/traps/` — live demo of the contract violation.
