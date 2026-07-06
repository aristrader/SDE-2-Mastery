---
order: 80
---

# TreeMap

---

## Internal structure

TreeMap is backed by a **Red-Black tree** — a self-balancing binary search tree. Every node holds:

- key and value
- references to left child, right child, and parent
- a color: RED or BLACK

**Red-Black tree invariants:**

1. No two consecutive red nodes on any root-to-leaf path.
2. Every path from root to a null leaf passes through the same number of black nodes.

These bound tree height to at most 2 log₂(n + 1), guaranteeing **O(log n) for all structural operations** — `get`, `put`, `remove`, `containsKey`.

```
           (B) 10
          /       \
      (R) 5      (R) 20
      /   \      /   \
   (B)3 (B)7  (B)15  (B)25
```

Compare to HashMap: O(1) average lookups, but no ordering — the moment you need sorted iteration or a range query, HashMap can't help.

---

## Ordering

TreeMap uses key comparison — not `hashCode` — to position every node.

**Natural ordering:** keys implement `Comparable`. The no-arg constructor uses `compareTo()`. Works out of the box for `Integer`, `String`, `LocalDate`, etc.

**Custom ordering:** pass a `Comparator` to the constructor. Keys don't need to implement `Comparable`; the comparator defines the sort order.

```java
// Natural order
TreeMap<String, Integer> byName = new TreeMap<>();

// Custom: reverse order
TreeMap<Integer, String> byDesc = new TreeMap<>(Comparator.reverseOrder());

// Custom: case-insensitive string keys
TreeMap<String, String> caseInsensitive = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
```

**If neither:** the first `put` throws `ClassCastException` when the tree tries to compare the key against an existing node.

**Null keys:** the natural-ordering constructor rejects them — `compareTo(null)` throws `NullPointerException`. If you provide a `Comparator` that explicitly handles `null` (e.g., `Comparator.nullsFirst(...)`), `TreeMap` accepts a null key, because comparison goes through the comparator. Null values are always allowed.

---

## Sorted / navigable operations

This is why you choose TreeMap over HashMap. `TreeMap` implements `NavigableMap`, exposing efficient operations HashMap can't provide at any cost.

| Method | What it returns |
|---|---|
| `firstKey()` / `lastKey()` | Smallest / largest key (throws `NoSuchElementException` if empty) |
| `ceilingKey(k)` | Smallest key >= k (returns `null` if none) |
| `floorKey(k)` | Largest key <= k (returns `null` if none) |
| `higherKey(k)` | Smallest key strictly > k (returns `null` if none) |
| `lowerKey(k)` | Largest key strictly < k (returns `null` if none) |
| `pollFirstEntry()` | Removes and returns the entry with the smallest key |
| `pollLastEntry()` | Removes and returns the entry with the largest key |
| `headMap(toKey)` | View of entries with keys strictly < toKey |
| `tailMap(fromKey)` | View of entries with keys >= fromKey |
| `subMap(from, to)` | View of entries with keys in [from, to) |

All navigation methods run in **O(log n)** — traversal from the matching node.

**Views are live, not copies.** `headMap`, `tailMap`, and `subMap` are windows into the underlying tree. Inserts and removes through the view reflect in the original map, and vice versa. Operations that violate the view's bounds throw `IllegalArgumentException`.

```java
TreeMap<Integer, String> map = new TreeMap<>();
// ... populate ...
SortedMap<Integer, String> first10 = map.headMap(10); // keys < 10
first10.put(5, "x");   // reflected in map
map.put(3, "y");        // reflected in first10
first10.put(15, "z");  // throws IllegalArgumentException — out of range
```

---

## HashMap vs TreeMap

| | HashMap | TreeMap |
|---|---|---|
| Ordering | None (unpredictable) | Sorted by key (natural or comparator) |
| `get` / `put` / `remove` | O(1) average | O(log n) |
| Range queries | Not supported | `headMap`, `tailMap`, `subMap` |
| Navigation | Not supported | `ceilingKey`, `floorKey`, `higherKey`, `lowerKey` |
| Null keys | One null key allowed | Not allowed |
| Memory per entry | Lower (array + linked nodes) | Higher (5 fields per node + color bit) |
| Use when | Fast lookup, ordering irrelevant | Sorted iteration or range/navigation queries needed |

---

## Common interview scenarios

Canonical prompts where TreeMap is the expected answer:

- **"Find all events between time T1 and T2"** — store events keyed by timestamp; `subMap(T1, T2)` returns the window in O(log n + k).
- **"Find the next available slot after time T"** — `ceilingKey(T)` returns the smallest booked slot >= T in O(log n).
- **"Maintain a leaderboard sorted by score"** — score as key; `lastKey()` is always the leader; `descendingMap()` gives top-down order.
- **"Implement a range query cache"** — `headMap` / `tailMap` views scan all keys before or after a threshold without full iteration.
- **"Implement a calendar / interval scheduler"** — `floorKey(start)` finds the last interval starting at or before a candidate; check overlap with one lookup.

---

## Quick recall

**Q. What data structure backs TreeMap?**
A. A Red-Black tree — a self-balancing BST — giving O(log n) for get, put, remove, and all navigation operations.

**Q. Natural ordering vs Comparator — when is each used?**
A. Natural ordering when keys implement `Comparable` (no-arg constructor). Comparator when keys don't implement `Comparable` or you need a different sort order — pass it to the constructor.

**Q. `ceilingKey(k)` vs `higherKey(k)` — what is the difference?**
A. `ceilingKey` returns the smallest key >= k (includes k itself). `higherKey` returns the smallest key strictly > k (excludes k).

**Q. Are `headMap` / `tailMap` / `subMap` views live or copies?**
A. Live. Mutations through the view affect the backing map and vice versa. Out-of-bounds puts throw `IllegalArgumentException`.

**Q. Are null keys allowed in TreeMap?**
A. Only when a `Comparator` is provided that explicitly handles null (e.g., `Comparator.nullsFirst(...)`). The natural-ordering constructor rejects null because `compareTo(null)` throws `NullPointerException`.

**Q. What do `pollFirstEntry()` / `pollLastEntry()` do?**
A. Remove and return the entry with the smallest (or largest) key in O(log n). Useful for priority-queue-style processing of a sorted map.

**Q. When should you choose TreeMap over HashMap?**
A. When you need sorted iteration, range queries (`subMap`, `headMap`, `tailMap`), or nearest-key navigation (`ceilingKey`, `floorKey`). If all you need is fast lookup with no ordering, HashMap's O(1) average beats TreeMap's O(log n).


