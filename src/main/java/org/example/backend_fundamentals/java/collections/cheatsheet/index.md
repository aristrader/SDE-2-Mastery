---
order: 0
---

# Collections Cheat Sheet

A quick reference guide for the core Java collections we've studied.

---

## Arrays & Lists

| Type | Memory / Backing | Resizable | Stores | Random Access (`get`) | Insert/Delete (Ends) | Insert/Delete (Middle) | Best For |
|---|---|---|---|---|---|---|---|
| **`T[]` (Array)** | Contiguous | No | Primitives & Objects | O(1) | N/A | N/A | Known fixed sizes, maximum primitive performance (no boxing). |
| **`ArrayList`** | Dynamic Array | Yes | Objects only | O(1) | O(1) amortized | O(n) | **99% of use cases**. Read-heavy workloads and standard ordered lists. |
| **`LinkedList`** | Doubly-Linked Nodes | Yes | Objects only | O(n) | O(1) | O(n) to find + O(1) link | Queue/Deque operations, or heavy insertions exactly at the ends. |

### List Gotchas
- **Array Covariance:** `String[]` is an `Object[]` (can cause `ArrayStoreException` at runtime).
- **Generics Invariance:** `List<String>` is **not** a `List<Object>` (compile-time safety).
- **`remove(index)` vs `remove(value)`:** For `List<Integer>`, `list.remove(1)` removes index 1, while `list.remove(Integer.valueOf(1))` removes the value 1.

---

## Sets

| Type | Backing | Ordering Guarantee | `add` / `remove` / `contains` | Null Allowed? | Best For |
|---|---|---|---|---|---|
| **`HashSet`** | `HashMap` | None (Bucket walk) | O(1) avg | Yes (One) | **Default choice** for deduplication and fast lookups. |
| **`LinkedHashSet`** | `HashMap` + Linked List | Insertion Order | O(1) avg | Yes (One) | Removing duplicates while preserving the original sequence. |
| **`TreeSet`** | Red-Black Tree | Sorted | O(log n) | No (Throws NPE) | Automatically keeping elements sorted. |

### Set Gotchas
- **The `add()` Return Value:** `set.add(value)` returns `true` if inserted, and `false` if it was already a duplicate. This is extremely useful for 1-pass duplicate detection without needing a separate `.contains()` check.
- **Silent Dedupe vs Fail Fast:** `new HashSet<>(List.of("A", "A"))` silently dedupes to size 1. `Set.of("A", "A")` throws an `IllegalArgumentException` immediately.
- **Equality Trap:** A `HashSet` relies heavily on `equals()` and `hashCode()`. If you don't override them, you can accidentally add logically identical objects to the set multiple times because their memory identities differ.

---

## Maps

| Type | Backing | Iteration Order | `get` / `put` Time | Null Keys/Values | Thread-Safe | Best For |
|---|---|---|---|---|---|---|
| **`HashMap`** | Array of Buckets | None (Bucket walk) | O(1) avg | 1 Null Key / Any Null Vals | No | **Default choice** for key-value pairs. |
| **`LinkedHashMap`** | `HashMap` + Linked List | Insertion Order | O(1) avg | 1 Null Key / Any Null Vals | No | Caches or when predictable iteration is needed. |
| **`TreeMap`** | Red-Black Tree | Sorted by Key | O(log n) | No Null Keys | No | Keeping keys sorted automatically. |
| **`EnumMap`** | Array | Enum Declaration | O(1) | No Null Keys | No | Any map where keys are an `Enum` (very fast). |
| **`ConcurrentHashMap`** | Thread-safe Buckets | None | O(1) avg | No Nulls Allowed | Yes | Shared mutable state across multiple threads. |
| **`Map.of(...)`** | Immutable Structure | None | O(1) | No Nulls Allowed | Yes (immutable) | Read-only literal maps. |

### Map Gotchas
- **The `equals` / `hashCode` Trap:** If a custom object is used as a key in a `HashMap`, you MUST override both `equals()` and `hashCode()`. Without them, `map.get(new CustomKey("A"))` will return `null` because the default is memory-identity based.
- **Concurrent Mutations are Silent:** A `HashMap` used across threads without synchronization won't usually throw an exception—it will just silently lose updates and corrupt totals. Use `ConcurrentHashMap`.
- **Shallow Immutability:** `Map.of()` freezes the map structure (you can't add or remove keys), but if a value is a mutable object (like a `List`), its internal contents can still be changed! Wrap values in `List.copyOf()` for deep immutability.
