---
order: 70
---

# Sets in Java — types, choices, and the dedup trap

The third of the three collection families. Sets have smaller surface area than Maps but rely on the same hashing machinery — `HashSet` is internally a `HashMap` where elements are keys and a sentinel is the value. Every property of `HashMap` keys (bucket model, `equals`/`hashCode` contract, iteration order) applies directly to `HashSet` elements. Companion demos sit alongside this doc in `basics/`, `immutability/`, `enum_keyed/`, `concurrent/`, and `traps/`.

> **Read alongside:** `java/foundations/hashing/Hashing.md`. Same prerequisites as Maps — `HashSet` is a `HashMap` under the hood.

---

## Set options

| Type | Ordering | Null | Thread-safe | When to use |
| --- | --- | --- | --- | --- |
| `HashSet` | None | One null allowed | No | Default set. O(1) `add`/`contains`/`remove`. |
| `LinkedHashSet` | Insertion order | Yes | No | Predictable iteration order needed. |
| `TreeSet` | Natural / comparator | No null | No | Sorted iteration needed; O(log n) ops. |
| `EnumSet` | Enum declaration order | No null | No | Elements are an enum — bit-vector backed, dramatically faster and smaller than `HashSet<MyEnum>`. |
| `Set.of(...)` | None | No null | Yes (immutable) | Read-only set; up to 10 elements (use varargs overload for more). |
| `ConcurrentHashMap.newKeySet()` | None | No null | Yes | Thread-safe set; backed by `ConcurrentHashMap`. |

There is no class named `ConcurrentHashSet` in the JDK — `ConcurrentHashMap.newKeySet()` is the modern thread-safe set.

- [ ] Understand why `HashSet` has the same `equals`/`hashCode` requirements as `HashMap` keys — they *are* map keys internally.
- [ ] Default to `EnumSet` whenever elements are an enum — bit-vector backing.
- [ ] Default to `ConcurrentHashMap.newKeySet()` for shared mutable sets across threads — never unsynchronized `HashSet`.

---

## Iteration order — same per-type decision as Maps

`HashSet` iteration is bucket-walk order — deterministic for a given input but unrelated to insertion or sort, and it changes after a resize. The other set types choose a deliberate order:

| Type | Iteration order | Cost |
| --- | --- | --- |
| `HashSet` | Bucket-walk (effectively unpredictable) | Cheapest |
| `LinkedHashSet` | Insertion order | One extra doubly-linked-list pointer per element |
| `TreeSet` | Sorted by element | O(log n) ops — backed by a red-black tree |
| `EnumSet` | Enum declaration order | Bit vector indexed by `ordinal()` — no hashing at all |

If you need a predictable order, the contract is in the type. Sorting a `HashSet` afterwards or wrapping iteration with extra logic is the wrong direction.

---

## `Set.of` immutability — three fail-fast checks at construction

`Set.of` is shallowly immutable like `Map.of`. Every mutation method (`add`, `remove`, `clear`) throws `UnsupportedOperationException`. Plus two construction-time fail-fast checks:

- **Null elements** are rejected at construction (`NullPointerException`).
- **Duplicates** are rejected at construction (`IllegalArgumentException`).

The duplicates rule is the surprise. `HashSet` *silently deduplicates* — `new HashSet<>(List.of("a", "a"))` produces `{a}` of size 1 with no warning. `Set.of("a", "a")` throws. In an immutable literal collection, duplicates are almost certainly a bug; failing at construction is more useful than silently shrinking.

```java
new HashSet<>(List.of("a", "a"));   // → {a}, size 1 (silent dedupe)
Set.of("a", "a");                   // → throws IllegalArgumentException
```

The shallow-vs-deep nuance from `Map.of` applies the same way: if elements are mutable types, `Set.of` doesn't deep-freeze them. Wrap each element in its own immutable copy if deep immutability matters.

---

## Concurrent sets — there is no `ConcurrentHashSet`

The JDK does not ship a class literally named `ConcurrentHashSet`. To get a thread-safe set:

```java
Set<Integer> threadSafe = ConcurrentHashMap.newKeySet();
```

This returns a `KeySetView` backed by a `ConcurrentHashMap`, inheriting all its concurrency guarantees: lock-free reads, bucket-level striped locks for writes, atomic compound operations.

`Collections.synchronizedSet(new HashSet<>())` exists but uses a single coarse lock and is bad for read-heavy workloads. Prefer `newKeySet()` in new code.

`HashSet` under concurrent writes shows the same silent-failure mode as `HashMap`: **lost adds with zero exceptions**. The `concurrent/ConcurrentHashSetBasicsRun` demo runs eight threads inserting 10,000 unique ints each (expected size 80,000) — `HashSet` typically delivers something like 70,000 with no stack trace, while `ConcurrentHashMap.newKeySet()` delivers exactly 80,000 every time. Never reach for `HashSet` for shared mutable state, even if "the tests pass" — the symptom is a count that's a bit off.

---

## The `equals` / `hashCode` contract — the silent-duplicates trap

`HashSet` uses the same machinery as `HashMap` keys, but the visible failure mode for a broken contract is different:

- For `HashMap`: `get` returns `null` even when the entry is in the map.
- For `HashSet`: the set holds *duplicates* even though dedup is its primary purpose.

### The trap, concretely

```java
class BadKey {
    final String value;
    BadKey(String value) { this.value = value; }
    // No equals/hashCode override — inherits Object's identity-based defaults
}

Set<BadKey> set = new HashSet<>();
set.add(new BadKey("hello"));
set.add(new BadKey("hello"));                                       // should dedupe — doesn't
set.add(new BadKey("hello"));                                       // ditto

System.out.println(set.size());                                     // 3
System.out.println(set.contains(new BadKey("hello")));              // false
```

Three distinct `new` instances → three distinct identity hashes → three different buckets → the set stores all three. They all *look* like `BadKey(hello)` when printed, but as far as the set is concerned, they are three different elements. `contains` for a freshly constructed `BadKey("hello")` returns `false` for the same reason — different identity hash → wrong bucket.

### How HashSet uses each method

| Step | Method used | With `BadKey` | With `GoodKey` (overrides both based on `value`) |
| --- | --- | --- | --- |
| Find bucket | `element.hashCode()` | Identity hash → wrong bucket | Field-based hash → right bucket |
| Walk bucket | `existing.equals(newElement)` | Identity equals → never matches | Field-based equals → matches → dedup wins |

For `BadKey`, both layers fail — every `add` succeeds and the set grows. For `GoodKey`, both layers work, so the second and third adds return `false` and the size stays at 1.

### The fix — same three options as for Maps

1. **Records (Java 14+).** `record GoodKey(String value) {}` — `equals`, `hashCode`, and `toString` auto-generated from the components.
2. **Lombok.** `@EqualsAndHashCode` or `@Data`.
3. **IDE generator.** Always emit both at once based on the same fields.

If you write `equals` by hand, the two methods MUST stay in sync. Adding a field to `equals` without adding it to `hashCode` silently breaks every existing element in every `HashSet` of your type.

See `traps/BrokenEqualsHashCodeSetTrapRun` for the live demonstration.

---

## Demo code in this folder

| Demo | Shows |
| --- | --- |
| `basics/HashSetBasicsRun` | First `add` returns `true`, duplicate returns `false`; null allowed (one); iteration in bucket order |
| `basics/LinkedHashSetBasicsRun` | LinkedHashSet preserves insertion order including null; HashSet doesn't (same data, two visible orders) |
| `basics/TreeSetBasicsRun` | Sorted iteration; null throws (the tree can't compare null) |
| `immutability/SetOfImmutabilityRun` | `Set.of` rejects all mutation, plus null AND duplicates at construction (the latter unique vs HashSet's silent dedupe) |
| `enum_keyed/EnumSetBasicsRun` | Four factory methods (`of`, `allOf`, `noneOf`, `range`); iteration in declaration order regardless of insert order; null throws |
| `concurrent/ConcurrentHashSetBasicsRun` | HashSet loses adds under contention with zero exceptions; `ConcurrentHashMap.newKeySet()` returns the exact size |
| `traps/BrokenEqualsHashCodeSetTrapRun` | Broken contract → set holds three "logically equal" elements (silent duplicates); `contains` returns false for a fresh equal element |

---

## Quick recall

- **A set of unique strings, no ordering need?** `HashSet`.
- **A set that iterates in insertion order?** `LinkedHashSet`.
- **A set of enum constants used frequently in checks?** `EnumSet` — bit-vector backing.
- **A sorted set of comparable elements?** `TreeSet` — O(log n) ops.
- **A read-only set from a factory?** `Set.of(...)`.
- **A thread-safe set for shared mutable state?** `ConcurrentHashMap.newKeySet()` — there is no `ConcurrentHashSet` class in the JDK.
- **Why might adding `new MyKey("hello")` three times to a `HashSet` produce size 3?** No `equals`/`hashCode` override → identity hashes differ → three buckets → no dedupe.
- **Why does `new HashSet<>(List.of("a", "a"))` succeed but `Set.of("a", "a")` throw?** `HashSet` silently dedupes; `Set.of` treats duplicates in an immutable literal as a bug and throws `IllegalArgumentException`.
- **Why does `HashSet` under concurrent adds silently lose elements?** Same silent-failure model as `HashMap` — lost writes, no exception.

---

## Related topics

- **Hashing** — the prerequisite. Same bucket model as `HashMap`; `HashSet` *is* a `HashMap` internally.
- **Maps** (previous family) and **Lists** — the other two collection families.
- **Stream Collectors** — `toSet`, `toUnmodifiableSet` produce sets from streams.
- **`equals` / `hashCode` contract** — Effective Java Items 10 and 11.


<ExerciseNav />
