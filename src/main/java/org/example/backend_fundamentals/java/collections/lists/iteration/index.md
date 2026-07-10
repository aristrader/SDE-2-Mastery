---
order: 30
---

# List Iteration

Use the simplest loop that matches the operation. Index loops are useful when the index matters. Enhanced-for loops are best for read-only traversal. `Iterator` is the safe tool for removal during traversal.

## Three traversal styles

```java
for (int i = 0; i < list.size(); i++) {
    System.out.println(list.get(i));
}

for (Integer value : list) {
    System.out.println(value);
}

Iterator<Integer> it = list.iterator();
while (it.hasNext()) {
    System.out.println(it.next());
}
```

## Iterator mental model

Think of the iterator as a cursor between elements:

```text
  |
  v
[1][2][3][4]
```

- `hasNext()` checks ahead; it does not move.
- `next()` moves and returns the element just crossed.
- `remove()` removes the element returned by the most recent `next()`.

Calling `next()` twice in one loop iteration skips every alternate element.

## Safe removal

Unsafe:

```java
for (Integer x : list) {
    if (x % 2 == 0) {
        list.remove(x);
    }
}
```

Safe:

```java
Iterator<Integer> it = list.iterator();
while (it.hasNext()) {
    if (it.next() % 2 == 0) {
        it.remove();
    }
}
```

## Quick recall

- **Read all values?** Enhanced-for loop.
- **Need the index?** Index loop.
- **Need to remove while traversing?** `Iterator.remove()`.
- **What does `hasNext()` do?** Checks only; it does not advance.
