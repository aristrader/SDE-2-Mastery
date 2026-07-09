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

## 1. List Mental Model

`List` is an ordered collection.

Properties:

* Maintains insertion order
* Allows duplicates
* Supports index-based access
* Common implementations:

    * `ArrayList`
    * `LinkedList`

Preferred declaration:

```java
List<Integer> nums = new ArrayList<>();
```

Why?

Code depends on the `List` interface, not the concrete implementation.

---

## 2. ArrayList

`ArrayList` is backed by a dynamic array.

Good for:

* Random access
* Iteration
* Adding at the end

Weak for:

* Insert/delete in middle
* Insert/delete at beginning

Time complexity:

```text
get(index)       O(1)
add(end)         amortized O(1)
add(index, val)  O(n)
remove(index)    O(n)
contains(value)  O(n)
```

Default choice in most backend/interview code.

---

## 3. LinkedList

`LinkedList` is backed by a doubly linked list.

Each internal node has:

```text
prev, value, next
```

But as Java users, we do not manipulate `prev` and `next` directly.

Good for:

* Adding/removing from ends
* Queue/deque style operations

Weak for:

* Random access
* Cache locality
* Most normal list use cases

Time complexity:

```text
get(index)       O(n)
add(end)         O(1)
remove(end)      O(1)
add(index, val)  O(n) to reach position
remove(index)    O(n) to reach position
```

In practice, `ArrayList` is usually preferred.

---

## 4. Common List Operations

```java
List<String> names = new ArrayList<>();

names.add("A");
names.add("B");
names.add("C");

names.get(0);
names.set(1, "X");
names.remove(0);
names.remove("C");
names.contains("X");
names.size();
names.isEmpty();
```

---

## 5. Important Gotcha: remove(index) vs remove(value)

For `List<Integer>`:

```java
List<Integer> nums = new ArrayList<>();
nums.add(10);
nums.add(20);
nums.add(30);

nums.remove(1); // removes index 1 => 20
nums.remove(Integer.valueOf(10)); // removes value 10
```

This is a common interview/code bug.

---

## 6. Iteration

Index-based loop:

```java
for (int i = 0; i < list.size(); i++) {
    System.out.println(list.get(i));
}
```

Enhanced for loop:

```java
for (Integer x : list) {
    System.out.println(x);
}
```

Iterator:

```java
Iterator<Integer> it = list.iterator();

while (it.hasNext()) {
    Integer x = it.next();
}
```

---

## 7. Safe Removal During Iteration

Unsafe:

```java
for (Integer x : list) {
    if (x % 2 == 0) {
        list.remove(x);
    }
}
```

This can cause `ConcurrentModificationException`.

Safe:

```java
Iterator<Integer> it = list.iterator();

while (it.hasNext()) {
    if (it.next() % 2 == 0) {
        it.remove();
    }
}
```

Use `Iterator.remove()` when removing while iterating.

---

## 8. Sorting and Reversing

Sorting:

```java
Collections.sort(list);
```

or:

```java
list.sort(null);
```

Reverse:

```java
Collections.reverse(list);
```

Example:

```java
List<Integer> nums = new ArrayList<>(List.of(3, 1, 2));
Collections.sort(nums);     // [1, 2, 3]
Collections.reverse(nums);  // [3, 2, 1]
```

Custom sorting will be covered later with `Comparator`.

---

## 9. subList()

```java
List<Integer> sub = list.subList(1, 3);
```

Range:

```text
start index inclusive
end index exclusive
```

Important:

`subList()` returns a view backed by the original list.

Changes to the sublist can affect the original list.

---

## 10. ArrayList vs LinkedList Interview Rule

Use `ArrayList` by default.

Use `LinkedList` only when:

* You specifically need linked-list behavior
* You need frequent operations at ends
* You are using it as a queue/deque, though `ArrayDeque` is usually better

SDE-2 answer:

```text
ArrayList is usually preferred because random access and iteration are fast, memory locality is better, and most real-world list operations are read/iterate-heavy.
```

---

## Definition of Done

You are done with this checkpoint only when:

* You can create and use `ArrayList` from memory.
* You can create and use `LinkedList` from memory.
* You know when to prefer `ArrayList`.
* You know the time complexities.
* You can explain `remove(index)` vs `remove(value)`.
* You can safely remove elements using `Iterator`.
* You can sort and reverse a list.
* You can explain why `subList()` is risky.


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

