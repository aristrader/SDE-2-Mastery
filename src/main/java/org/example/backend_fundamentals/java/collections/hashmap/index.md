---
order: 30
---

# HashMap Internals

---

## Internal structure

A `HashMap<K,V>` is backed by a single array of buckets:

```java
Node<K,V>[] table;  // length is always a power of 2
```

Each slot in `table` is either `null` (empty bucket) or the head of a chain. A `Node` holds four fields:

```
Node<K,V>
  int    hash   — cached hash of the key (avoids recomputing on resize)
  K      key
  V      value
  Node   next   — pointer to the next node in the same bucket (linked list)
```

When a bucket's chain grows long enough (see Collision handling), Java 8 replaces the linked list with a Red-Black tree using `TreeNode<K,V>`, which extends `Node`.

**Key defaults:**

| Parameter | Default value |
|---|---|
| Initial capacity | 16 |
| Load factor | 0.75 |
| Treeify threshold | 8 (chain length at which list becomes tree) |
| Untreeify threshold | 6 (chain length at which tree reverts to list) |

---

## Hash calculation

When you call `map.put(key, value)`, the map first computes a spread hash:

```java
static final int hash(Object key) {
    int h;
    return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
}
```

Then it finds the bucket index:

```java
index = (n - 1) & hash   // n = table.length
```

**Why XOR with the upper 16 bits:** `hashCode()` often has entropy concentrated in the higher bits. When the table is small (e.g., capacity 16), only the lowest 4 bits of the hash determine the bucket — the upper bits are ignored. XOR-ing the upper half down into the lower half folds that entropy in, reducing clustering even with poor `hashCode()` implementations.

**Why `(n - 1) & hash` instead of `hash % n`:** when `n` is a power of 2, `n - 1` is all 1-bits, so the AND is a cheap bitwise mask that extracts exactly the low `log2(n)` bits. This is why capacity must always be a power of 2 — otherwise `(n - 1)` wouldn't be a mask and the trick wouldn't work.

---

## Collision handling

A collision is when two distinct keys map to the same bucket index. Both nodes end up in that bucket's chain.

**Java 7 and earlier:** all buckets are linked lists. With a poor hash function or many collisions, a chain can grow to O(n), making worst-case get/put O(n).

**Java 8+ (JEP 180):** automatic treeification.

```
Bucket chain length:

 1  2  3  4  5  6  7   8 → treeify (only if table.length >= 64)
                         ↓
                   Red-Black tree (O(log n) ops)
                         ↓
 6 → untreeify back to linked list
```

| Phase | Structure | get/put complexity |
|---|---|---|
| Chain length < 8 | Linked list | O(n) worst case |
| Chain length >= 8 AND table.length >= 64 | Red-Black tree | O(log n) worst case |
| Chain length >= 8 AND table.length < 64 | Resize instead of treeify | — |
| Tree shrinks to <= 6 | Reverts to linked list | O(n) worst case |

**Why treeify at 8 and untreeify at 6 (not both at 8):** the gap creates hysteresis. If both thresholds were 8, a bucket hovering at exactly 8 entries would thrash — each put/remove would trigger a treeify/untreeify. The two-unit gap prevents that oscillation.

**The table-size guard (`MIN_TREEIFY_CAPACITY = 64`):** treeifying a tiny table is counterproductive — a small table with a long chain usually means a bad hash function affecting all buckets, so resizing (spreading entries across more buckets) fixes the real problem. Treeification is deferred until the table is large enough that a single pathological chain is the exception.

---

## Resize and rehash

Resize triggers when the total entry count crosses the threshold:

```
threshold = capacity × loadFactor
           = 16 × 0.75 = 12   (with defaults)
```

When `size > threshold`:
1. A new `table` is allocated at `2 × oldCapacity`.
2. Every existing entry is rehashed and placed in the new table — O(n) work.
3. The threshold is recalculated: `newCapacity × loadFactor`.

Because the new capacity is exactly double the old (still a power of 2), Java 8 uses a trick: an entry either stays at the same index or moves to `oldIndex + oldCapacity`. The decision is one bit of the cached hash — no full re-hash needed. This makes Java 8 resize faster than Java 7's full rehash.

**Why load factor 0.75:**
- Too low (e.g., 0.5): resize early, many empty buckets, wastes memory.
- Too high (e.g., 1.0): resize late, long chains, more collisions, slower lookups.
- 0.75 is the empirical sweet spot between time (collision rate) and space (wasted capacity), noted explicitly in the Java source.

---

## Null key

`null` is a valid key. `hash(null)` returns 0, so a null key always lands in `table[0]`. Only one null key can exist — subsequent puts replace the value, as with any key.

---

## Complexity summary

| Operation | Average case | Worst case |
|---|---|---|
| `put` / `get` / `remove` | O(1) amortized | O(log n) — tree bin after treeification |
| Resize (rare) | O(n) | O(n) |

"Amortized O(1)" means most operations touch one bucket; resize spreads its cost across all preceding puts. The tree bin worst case of O(log n) applies only to degenerate keys (e.g., many keys with the same `hashCode()`).

---

## Not thread-safe

`HashMap` has no internal synchronization. Concurrent modification can corrupt the map:

- **Lost updates:** two threads writing to the same bucket simultaneously can silently drop one entry.
- **Infinite loop during resize (Java 7):** the Java 7 resize algorithm could form a cycle in a bucket's linked list under concurrent rehash. Reads then spin forever. Java 8 fixed the cycle but the map is still unsynchronized and unsafe to share.
- **Visibility:** without synchronization, one thread may not see writes made by another.

For concurrent access use `ConcurrentHashMap` — segmented locks (Java 7) or CAS + synchronized on individual bins (Java 8+).

---

## Mutable keys

`HashMap` caches the spread hash in each `Node` at insert time (the `int hash` field). That cached value is never updated. If you mutate a key after insertion, `hashCode()` returns a different value — the map looks in the wrong bucket on the next `get`, finds nothing, returns `null`. The entry is still in the original bucket, permanently orphaned.

```java
Person p = new Person("Alice", 30);
map.put(p, "engineer");  // hashCode() → bucket 5, cached there

p.setName("Bob");        // mutation — hashCode() now produces a different int

map.get(p);              // looks in new bucket → null
                         // entry still in bucket 5 — lost, no exception
```

No exception is thrown. The entry isn't removed — just unreachable. The map also leaks memory until the next resize.

**Rule:** only use immutable objects as keys (`String`, `Integer`, your own `record` or `@Value` DTO). If you must use a mutable object, hash only on fields that never change after construction (e.g., a database `id`).

---

## Quick recall

**Q. Why must HashMap capacity always be a power of 2?**
A. So that `(n - 1) & hash` is a cheap bitwise mask equivalent to `hash % n` — works only when `n` is a power of 2.

**Q. What does `(h = key.hashCode()) ^ (h >>> 16)` achieve?**
A. Folds the upper 16 bits of the hash into the lower 16, spreading entropy into the bits that actually determine the bucket index when the table is small.

**Q. What is the treeify threshold, and why is the untreeify threshold lower?**
A. Treeify at chain length 8 (O(log n) tree instead of O(n) list). Untreeify at 6, not 8 — the two-unit gap prevents thrashing when entries hover right at the boundary.

**Q. Does a chain of 8 always trigger treeification?**
A. No. Treeification is skipped when `table.length < 64` (`MIN_TREEIFY_CAPACITY`). Instead, the table is resized — a small table with a long chain usually means a poor hash function affecting all buckets, so spreading entries is the better fix.

**Q. What happens during a resize?**
A. A new table double the size is allocated; all entries are rehashed (O(n)). In Java 8, an entry either stays at the same index or moves to `oldIndex + oldCapacity`, determined by one bit of the cached hash.

**Q. Why is the default load factor 0.75?**
A. Empirical sweet spot: lower means too many empty buckets (wasted space), higher means more collisions (slower lookups).

**Q. Is HashMap thread-safe?**
A. No. Concurrent writes cause lost updates and potential corruption. Java 7 could produce an infinite loop during resize. Use `ConcurrentHashMap` for shared access.

**Q. Where does a null key land?**
A. Always at `table[0]` — `hash(null)` is defined as 0.

**Q. What happens if you mutate a key after inserting it into a HashMap?**
A. The map cached the hash at insert time. After mutation, `hashCode()` returns a different value, the map looks in the wrong bucket, and returns null. The entry is orphaned in the original bucket — silent data loss, no exception.


<ExerciseNav />
