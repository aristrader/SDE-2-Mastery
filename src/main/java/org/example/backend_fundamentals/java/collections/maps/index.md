---
order: 60
---

# Maps in Java — types, choices, and trade-offs

The second of the three collection families. Maps appear everywhere — registries, caches, lookup tables, configuration, in-memory indexes — and picking the wrong variant is one of the most common Java performance mistakes. Companion demos sit alongside this doc in `basics/`, `immutability/`, `enum_keyed/`, `concurrent/`, and `traps/`.

> **Read alongside:** `java/foundations/hashing/Hashing.md`. The bucket model, collisions, and the `equals`/`hashCode` contract live there — assumed knowledge here.

---

## Map options

| Type | Key ordering | Null keys/values | Thread-safe | When to use |
| --- | --- | --- | --- | --- |
| `HashMap` | None | One null key, multiple null values | No | Default map. O(1) average get/put. |
| `LinkedHashMap` | Insertion order | Yes | No | Predictable iteration order needed. |
| `TreeMap` | Natural / comparator order | No null key | No | Entries must be sorted by key. |
| `EnumMap` | Enum declaration order | No null key | No | Keys are an enum — always faster and smaller than `HashMap<MyEnum, V>`. |
| `ConcurrentHashMap` | None | No nulls | Yes | Thread-safe map. Default for shared mutable maps across threads. |
| `Map.of(...)` | None | No nulls | Yes (immutable) | Read-only map with ≤ 10 entries. |
| `Map.ofEntries(...)` | None | No nulls | Yes (immutable) | Read-only map with > 10 entries. |

- [ ] Understand why `HashMap` requires a correct `equals`/`hashCode` on keys — see `traps/BrokenEqualsHashCodeTrapRun` for the silent-null failure mode.
- [ ] Understand `ConcurrentHashMap` vs `Collections.synchronizedMap()` — `ConcurrentHashMap` allows concurrent reads without locking and uses bucket-level striped locks for writes; `synchronizedMap` wraps with a single coarse global lock.
- [ ] Default to `EnumMap` whenever keys are an enum type — bit-vector backing, no hashing.
- [ ] Default to `ConcurrentHashMap` for shared mutable maps across threads — never unsynchronized `HashMap`.

---

## Iteration order — a per-type decision

`HashMap` iteration order is "bucket walk order" — deterministic for a given input but unrelated to insertion or sort, and it changes after a resize. The other map types choose deliberately:

| Type | Iteration order | Cost |
| --- | --- | --- |
| `HashMap` | Bucket-walk (effectively unpredictable) | Cheapest |
| `LinkedHashMap` | Insertion order | One extra doubly-linked-list pointer per entry |
| `TreeMap` | Sorted by key | O(log n) get/put — backed by a red-black tree |
| `EnumMap` | Enum declaration order | Backed by an array indexed by `Enum.ordinal()` — no hashing at all |

If you need a predictable order, the contract is in the type. Don't patch over `HashMap`'s order downstream — switch to whichever map type actually expresses what you want.

---

## Shallow vs deep immutability — `Map.of` is shallow

`Map.of` freezes the *map structure* (which key points to which reference) but not the value objects themselves. If a value is a mutable type, anyone with a reference to it can still mutate its contents through the map.

```java
List<Integer> nums = new ArrayList<>(List.of(1, 2, 3));
Map<String, List<Integer>> shallow = Map.of("nums", nums);

shallow.put("other", new ArrayList<>());  // throws — map structure is frozen
shallow.get("nums").add(99);              // works — list is still mutable
                                           // map now reads {nums=[1, 2, 3, 99]}
```

For deep immutability, wrap each value with `List.copyOf(...)` (or `Set.copyOf`, etc.) before putting it in. That gives both an immutable value AND a defensive snapshot — future changes to the source list are invisible to the map.

```java
Map<String, List<Integer>> deep = Map.of("nums", List.copyOf(nums));
deep.get("nums").add(99);                 // throws — value list is immutable too
nums.add(99);                              // doesn't affect deep — copyOf already snapshotted
```

Same principle applies to `List.of(...)`, `Set.of(...)`, and `Map.ofEntries(...)` — they're all shallowly immutable. See `immutability/MapOfImmutabilityRun` for the live demonstration.

### Mutation isn't just `put` and `remove`

A common misread of "immutable" is "`put` throws but the higher-level methods are fine." Not so. **Every mutating method on `Map.of` throws `UnsupportedOperationException`**, including:

- `put`, `putIfAbsent`, `putAll`
- `remove`, `clear`
- `replace`, `replaceAll`
- `merge`, `compute`, `computeIfAbsent`, `computeIfPresent`

`merge(key, value, remapper)` *looks* like read-then-maybe-write — for an absent key it puts; for a present key it computes and puts. On `Map.of`, both branches try to write into the frozen structure and throw. There's no API to change a `Map.of` from outside.

---

## Concurrent failures are silent — `HashMap` doesn't throw, it just produces wrong counts

A common misconception: "if `HashMap` is unsafe under contention, surely my tests will catch the misuse." They usually won't.

On modern JVMs (Java 8+), concurrent writes to a `HashMap` typically:

- **Lose updates silently** — multiple threads read the same value, all compute the same new value, all write the same result. Totals are wrong but no exception fires.
- **Rarely throw** — pre-Java 8 had infinite-loop and resize bugs that surfaced visibly; mostly fixed since.

The `concurrent/ConcurrentHashMapBasicsRun` demo proves this: eight threads each call `merge("counter", 1, Integer::sum)` 10,000 times. Expected total: 80,000. `HashMap` typically delivers 15–20% of that with zero exceptions — the bug is in the data, not in stack traces. `ConcurrentHashMap.merge` is atomic and produces the exact 80,000 every time.

The lesson: never reach for `HashMap` for shared mutable state, even if "the tests pass" — the symptom is a total that's a little bit off, often masked by other noise.

---

## The `equals` / `hashCode` contract — the silent-null trap

`HashMap` uses `hashCode` to pick the bucket and `equals` to pick the entry within the bucket. Any custom value-like class used as a key must override **both**, based on the same fields. When you don't, the failure mode is silent — `get` returns `null` when it "should" find the value.

### The trap, concretely

```java
class BadKey {
    final String value;
    BadKey(String value) { this.value = value; }
    // No equals/hashCode override — inherits Object's identity-based defaults
}

Map<BadKey, String> map = new HashMap<>();
BadKey stored = new BadKey("hello");
map.put(stored, "world");

map.get(stored);                  // returns "world"  ← same reference, works
map.get(new BadKey("hello"));     // returns null     ← the trap
```

`new BadKey("hello")` is a *different object* from `stored`, even though their fields match. `Object`'s defaults are identity-based:

- `Object.hashCode()` returns a JVM-assigned identity hash, different for each new instance.
- `Object.equals(other)` is `this == other` — true only for the same reference.

So the second lookup hashes to a *different bucket*, finds nothing, returns `null`. Even if the two identity hashes had landed in the same bucket (a modular collision), the bucket walk would compare with identity equals and still miss.

### How HashMap uses each method

| Step | Method used | `BadKey` | `GoodKey` (overrides both based on `value`) |
| --- | --- | --- | --- |
| Find bucket | `key.hashCode()` | Identity hash → wrong bucket | Field-based hash → right bucket |
| Walk bucket | `entry.equals(searchKey)` | Identity equals → never matches a fresh instance | Field-based equals → matches |

`BadKey` fails at step 1 (wrong bucket), so it never gets to step 2. Even if step 1 succeeded by chance, step 2 would still fail. **Both layers must work for lookup to succeed; both fail when the contract is broken.** Size also grows unexpectedly: calling `put(new BadKey("hello"), ...)` twice creates *two entries*, not one — duplicates the iterator will happily walk past.

### A worse failure — silent duplicates

```java
map.put(new BadKey("hello"), "first");
map.put(new BadKey("hello"), "second");
System.out.println(map.size());   // prints 2, not 1
```

For `BadKey`, the two `new` instances are distinct keys (identity differs), so both entries persist. For `GoodKey`, the second `put` would replace the first because `equals` says they're the same key.

### The fix — three ways to avoid the bug

1. **Records (Java 14+).** `record GoodKey(String value) {}` — `equals`, `hashCode`, and `toString` are auto-generated from the components.
2. **Lombok.** `@EqualsAndHashCode` or `@Data`. Generated at compile time from chosen fields.
3. **IDE generator.** Use the IDE's "equals() and hashCode()" command — it always emits both at once based on the same fields.

If you write `equals` by hand, the two methods must stay in sync. Adding a field to `equals` without adding it to `hashCode` silently breaks every existing key in every `HashMap` that uses your type.

See `traps/BrokenEqualsHashCodeTrapRun` for the live failure and the side-by-side fix.

---

## Demo code in this folder

| Demo | Shows |
| --- | --- |
| `basics/HashMapBasicsRun` | One null key + multiple null values; iteration order is hash-bucket order |
| `basics/LinkedHashMapBasicsRun` | LinkedHashMap preserves insertion order; HashMap doesn't (same data, two visible orders) |
| `basics/TreeMapBasicsRun` | Sorted iteration by key; null key throws (tree can't compare null) |
| `immutability/MapOfImmutabilityRun` | `Map.of` rejects all mutating ops including `merge`; rejects null keys/values at construction; shallow-vs-deep immutability shown via a `List` value |
| `enum_keyed/EnumMapBasicsRun` | EnumMap iterates in enum declaration order; null key throws |
| `concurrent/ConcurrentHashMapBasicsRun` | HashMap loses updates under concurrent merges; ConcurrentHashMap returns the exact count without external locking |
| `traps/BrokenEqualsHashCodeTrapRun` | Custom keys without `equals`/`hashCode` — lookup by a logically-equal but distinct instance returns `null` silently |

---

## Quick recall

- **A map keyed by an enum?** `EnumMap` — array indexed by `ordinal()`, no hashing.
- **A map that preserves insertion order?** `LinkedHashMap`.
- **A thread-safe map shared across threads?** `ConcurrentHashMap` — bucket-striped locks, lock-free reads.
- **A read-only map from a factory?** `Map.of(...)` (≤10 entries) or `Map.ofEntries(...)` for more.
- **Entries sorted by key?** `TreeMap` — O(log n), red-black tree.
- **Why does `get(new MyKey("hello"))` return `null` after `put(new MyKey("hello"), ...)`?** No `equals`/`hashCode` override → identity hash → different bucket → miss.
- **What does "shallow immutability" mean for `Map.of`?** The structure is frozen, but mutable value objects can still be mutated through the map. Fix: wrap each value in `List.copyOf(...)` / `Set.copyOf(...)` etc.
- **Why `ConcurrentHashMap` over `Collections.synchronizedMap`?** Bucket-level striped locks + lock-free reads vs a single coarse global lock.
- **Why does `HashMap` iteration order change after the map grows past 12 entries?** Default capacity 16, load factor 0.75 → resize at 12. Resize re-buckets every entry, so the walk order changes.
- **Why is `HashMap` under concurrent writes dangerous even if tests pass?** Lost updates are silent — no exceptions, just wrong totals.

---

## Related topics

- **Hashing** — the prerequisite. Bucket model, collisions, the `equals`/`hashCode` contract.
- **Lists** (previous family) and **Sets** (next family) — the other two collection families with their own trade-offs.
- **Stream Collectors** — `toMap`, `groupingBy`, `counting` produce maps from streams.
- **`equals` / `hashCode` contract** — Effective Java Items 10 and 11.


