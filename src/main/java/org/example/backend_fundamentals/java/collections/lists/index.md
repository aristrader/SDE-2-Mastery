---
order: 10
---

# Lists in Java — types, choices, and the Array trap

The first family of collections to know. Picking the right list type matters: builders accumulate elements, factories return read-only views, and registries occasionally hand back lists. Companion demos sit alongside this doc in `basics/`, `immutability/`, `performance/`, and `concurrent/`.

---

## List options

| Type | Backed by | Allows null | Thread-safe | When to use |
| --- | --- | --- | --- | --- |
| `ArrayList` | Dynamic array | Yes | No | Default list. Fast random access (`get(i)`), fast add at end, slow insert/delete in the middle. |
| `LinkedList` | Doubly-linked list | Yes | No | Fast insert/delete at both ends. Slow random access. Rarely the right choice in modern Java. |
| `List.of(...)` | Immutable array | No | Yes (immutable) | Read-only lists; cannot add, remove, or set elements. Use when the list is truly fixed. |
| `Collections.unmodifiableList(...)` | Wraps any list | Depends | Depends | Makes an existing list unmodifiable from the outside, but the backing list can still change. |
| `Arrays.asList(...)` | Fixed-size array | Yes | No | Fixed-size but mutable — `set` works, `add`/`remove` throw. A common surprise. |
| `CopyOnWriteArrayList` | Array, copied on write | Yes | Yes | Thread-safe reads; every write copies the entire backing array. Use when reads vastly outnumber writes (listener lists, config caches). |

- [ ] Understand `ArrayList` vs `LinkedList` Big-O comparison — why `ArrayList` wins almost everywhere.
- [ ] Understand `List.of()` vs `Collections.unmodifiableList()` — the subtle difference is that the backing list of `unmodifiableList` can still be mutated externally.
- [ ] Understand `Arrays.asList()` — the fixed-size trap (`add`/`remove` throw `UnsupportedOperationException` even though it looks mutable).
- [ ] Understand `CopyOnWriteArrayList` — every write copies the backing array; iterators see a snapshot at iterator-creation time. Concurrent reads are lock-free.

---

## Array vs. ArrayList

- **Array (`int[]`, `String[]`):** fixed size, holds primitives natively (no boxing), no generics. Use when size is known upfront and avoiding boxing overhead matters.
- **`ArrayList<T>`:** dynamic size, holds objects only (primitives are boxed). Use when size can grow or shrink.
- **Key trap — covariance:** arrays are covariant — `String[]` is an `Object[]`. Generics are invariant — `List<String>` is **not** a `List<Object>`. Storing the wrong type into an array fails at runtime with `ArrayStoreException`; with generics it's caught at compile time.

```java
Object[] arr = new String[3];
arr[0] = 42;                      // compiles, ArrayStoreException at runtime

List<String> list = new ArrayList<>();
List<Object> objList = list;      // won't compile — generics are invariant
```

Compile-time errors beat runtime errors — the strongest single argument for `List<T>` over arrays in modern Java code.

---

## Demo code in this folder

| Demo | Shows |
| --- | --- |
| `basics/ArrayListBasicsRun` | Basic `ArrayList` usage — declare, add, print |
| `basics/LinkedListBasicsRun` | Basic `LinkedList` usage |
| `immutability/ListOfImmutabilityRun` | `List.of()` is fully immutable; `add` throws |
| `immutability/UnmodifiableListViewRun` | `Collections.unmodifiableList` is a read-only view; mutating the backing list affects the view |
| `immutability/ArraysAsListTrapRun` | `Arrays.asList` is fixed-size: `set` works, `add` throws |
| `performance/ArrayListVsLinkedListRandomAccessRun` | O(1) vs O(n) random-access — `ArrayList` wins decisively |
| `concurrent/CopyOnWriteSnapshotIteratorRun` | `CopyOnWriteArrayList` iterator sees a snapshot; modifications during iteration don't throw |
| `concurrent/CopyOnWriteThreadSafetyRun` | `CopyOnWriteArrayList` survives concurrent readers + a writer with no external locking; `ArrayList` doesn't |

---

## Quick recall

- **A list that can grow with frequent random access by index?** `ArrayList` — O(1) `get(i)`, fast append.
- **An immutable list handed back from a factory?** `List.of(...)` — fully read-only, rejects nulls.
- **A read-only view over a mutable backing list?** `Collections.unmodifiableList(...)` — the backing list can still mutate from outside, and the view will see those changes.
- **A list shared across threads where reads vastly outnumber writes?** `CopyOnWriteArrayList` — lock-free reads; every write copies the array.
- **Array vs `ArrayList` for primitives?** Array — no boxing. `ArrayList<Integer>` boxes every element.
- **Why prefer `List<T>` over `String[]`?** Arrays are covariant (`String[]` is an `Object[]`) → `ArrayStoreException` at runtime. Generics are invariant → compile-time safety.
- **What's the `Arrays.asList` trap?** Fixed-size but mutable — `set` works, `add`/`remove` throw `UnsupportedOperationException`.

---

## Related topics

- **Maps** and **Sets** — the other two collection families with their own trade-offs.
- **Stream Collectors** — covers how to produce list types from streams.
- **Encapsulation** — returning a mutable internal collection is a classic encapsulation violation; use `Collections.unmodifiableList(...)` or `List.copyOf(...)` to fix it.

