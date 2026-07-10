---
order: 0
---

# Collections Cheat Sheet

A quick reference for choosing and using the Java collections covered in this module.

## Arrays & Lists

| Type | Backing | Random access | Insert/remove middle | Best for |
| --- | --- | ---: | ---: | --- |
| `T[]` | Fixed array | O(1) | Manual shifting | Fixed size, primitives, lowest overhead. |
| `ArrayList` | Dynamic array | O(1) | O(n) | Default list. Read-heavy ordered data. |
| `LinkedList` | Doubly linked nodes | O(n) | O(n) to find | Rare as a list; prefer `ArrayDeque` for queue/deque work. |
| `List.of(...)` | Immutable list | O(1) | Not allowed | Small read-only literals. |

List traps:

- `list.remove(1)` removes index 1; `list.remove(Integer.valueOf(1))` removes value 1.
- `Arrays.asList(array)` is fixed-size; `set()` works, `add()` fails.
- `List.of()` rejects `null` and is structurally immutable.
- `List<String>` is not a `List<Object>`; generics are invariant.

## Sets

| Type | Backing | Ordering Guarantee | `add` / `remove` / `contains` | Null Allowed? | Best For |
| --- | --- | --- | ---: | --- | --- |
| `HashSet` | `HashMap` | None | O(1) avg | One | Default dedupe and fast membership checks. |
| `LinkedHashSet` | `HashMap` + linked order | Insertion order | O(1) avg | One | Dedupe while preserving input order. |
| `TreeSet` | Red-black tree | Sorted | O(log n) | No | Always-sorted unique values. |
| `Set.of(...)` | Immutable set | Unspecified | O(1) avg | No | Small read-only unique literals. |

Set traps:

- `set.add(value)` returns `false` when the value already exists.
- `new HashSet<>(list)` silently dedupes; `Set.of("A", "A")` throws.
- `HashSet` depends on correct `equals()` and `hashCode()`.
- `TreeSet` uniqueness comes from comparison: if comparator returns `0`, values are treated as duplicates.

## Maps

| Type | Backing | Iteration Order | `get` / `put` Time | Null Keys/Values | Thread-Safe | Best For |
| --- | --- | --- | ---: | --- | --- | --- |
| `HashMap` | Buckets + nodes/tree bins | None | O(1) avg | One null key, null values | No | Default key-value store. |
| `LinkedHashMap` | `HashMap` + linked order | Insertion/access order | O(1) avg | Same as `HashMap` | No | Predictable iteration, simple LRU cache. |
| `TreeMap` | Red-black tree | Sorted by key | O(log n) | No null keys | No | Range queries and sorted keys. |
| `ConcurrentHashMap` | Concurrent buckets | None | O(1) avg | No nulls | Yes | Shared mutable maps. |
| `Map.of(...)` | Immutable map | Unspecified | O(1) avg | No nulls | Immutable | Small read-only literals. |

Map patterns:

```java
Map<String, Integer> freq = new HashMap<>();

for (String word : words) {
    freq.put(word, freq.getOrDefault(word, 0) + 1);
}
```

- Use `containsKey()` when `null` values make `get()` ambiguous.
- Use `computeIfAbsent()` for grouping.
- `Map.of()` is shallowly immutable; mutable values can still mutate.
- Never use mutable fields inside a `HashMap` key's `equals()` / `hashCode()`.

## Queues, Deques, Priority Queues

| Type | Ordering | Core ops | Best for |
| --- | --- | --- | --- |
| `Queue` + `ArrayDeque` | FIFO | `offer`, `poll`, `peek` | Normal queue work. |
| `Deque` + `ArrayDeque` | Both ends | `offerFirst/Last`, `pollFirst/Last` | Stack replacement, browser history, palindrome checks. |
| `PriorityQueue` | Natural/comparator priority | `offer`, `poll`, `peek` | Top K, scheduling, merge K sorted lists, Dijkstra-style problems. |

Queue traps:

- Prefer `offer` / `poll` / `peek`; they return special values instead of throwing.
- `ArrayDeque` is the boring default for queues and stacks; avoid legacy `Stack`.
- `PriorityQueue` iteration is not sorted. Poll repeatedly for priority order.
- `PriorityQueue` default is min-heap; use `Comparator.reverseOrder()` for max-heap.
- Mutating an object's priority after insertion does not reorder the heap.

PriorityQueue costs:

| Operation | Cost |
| --- | ---: |
| `peek()` | O(1) |
| `offer()` | O(log n) |
| `poll()` | O(log n) |
| `contains()` / `remove(Object)` | O(n) |
| Build from collection | O(n) |

## Hashing

Hash-based collections need the same contract:

- If `a.equals(b)` is `true`, `a.hashCode() == b.hashCode()` must also be true.
- Unequal objects may share a hash code; collisions are allowed.
- Hash buckets give average O(1), not guaranteed O(1) for every operation.
- Bad hash distribution causes more collisions and slower lookup.

Use stable key fields. If a key changes after insertion, the object may be in the wrong bucket and become hard to find.

## Sorting

| Tool | Use when |
| --- | --- |
| `Comparable<T>` | The class has one obvious natural order. |
| `Comparator<T>` | The ordering is use-case-specific or there are multiple useful orders. |
| `thenComparing(...)` | Need deterministic tie-breakers. |
| `reversed()` | Need descending order; place it carefully. |

Comparator examples:

```java
Comparator<Student> byName = Comparator.comparing(Student::name);

Comparator<Student> byCgpaDesc =
        Comparator.comparingDouble(Student::cgpa).reversed();

Comparator<Student> byCgpaNameId =
        Comparator.comparingDouble(Student::cgpa).reversed()
                  .thenComparing(Student::name)
                  .thenComparingInt(Student::id);
```

Without method references:

```java
Comparator<Student> byName =
        Comparator.comparing(student -> student.name());

Comparator<Student> byCgpaDesc =
        Comparator.comparingDouble(student -> student.cgpa()).reversed();
```

Sorting traps:

- Do not implement comparators with subtraction; it can overflow.
- `reversed()` at the end reverses the whole chain.
- `TreeSet` and `TreeMap` use comparison for uniqueness/order, not only `equals()`.

## Pick Fast

| Need | Use |
| --- | --- |
| Ordered resizable list | `ArrayList` |
| Unique values, fast lookup | `HashSet` |
| Unique values, preserve insertion order | `LinkedHashSet` |
| Unique sorted values | `TreeSet` |
| Key-value lookup | `HashMap` |
| Key-value lookup with predictable iteration | `LinkedHashMap` |
| Sorted keys / range queries | `TreeMap` |
| Shared mutable map | `ConcurrentHashMap` |
| FIFO queue | `ArrayDeque` as `Queue` |
| Stack behavior | `ArrayDeque` as `Deque` |
| Priority-based removal | `PriorityQueue` |

## Quick recall

- **Default list?** `ArrayList`.
- **Default set?** `HashSet`.
- **Default map?** `HashMap`.
- **Default queue/deque?** `ArrayDeque`.
- **Priority removal?** `PriorityQueue`.
- **Sorted unique values?** `TreeSet`.
- **Sorted keys?** `TreeMap`.
- **Comparator or Comparable?** `Comparator` for use-case ordering, `Comparable` for natural order.
