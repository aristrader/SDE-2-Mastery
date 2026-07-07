---
order: 20
search: false
---

# Solutions

## Solution: set-of-duplicates - Silent Dedupe vs Fail Fast

```java
Set<String> hashSet = new HashSet<>(List.of("A", "A", "B"));
System.out.println(hashSet.size()); // Prints 2 (Silently deduplicated)

// Set<String> immutableSet = Set.of("A", "A", "B"); 
// Throws IllegalArgumentException: duplicate element: A
```
`HashSet` operates normally, finding the existing element in the bucket and overwriting it, resulting in a size of 2. `Set.of()` is designed to fail fast. If you are hardcoding a literal collection, providing duplicates is almost certainly a bug, so the factory rejects it immediately.

## Solution: linked-hash-set - Preserving Insertion Order

```java
Set<Integer> hashSet = new HashSet<>();
hashSet.add(10); hashSet.add(1); hashSet.add(5); hashSet.add(20);
System.out.println(hashSet); // Prints e.g., [1, 20, 5, 10] (Unpredictable)

Set<Integer> linkedSet = new LinkedHashSet<>();
linkedSet.add(10); linkedSet.add(1); linkedSet.add(5); linkedSet.add(20);
System.out.println(linkedSet); // Prints [10, 1, 5, 20] (Insertion order)
```
`HashSet` iterates in bucket-walk order, which looks like random noise to the user and changes whenever the internal array resizes. `LinkedHashSet` pays the overhead of maintaining an internal doubly-linked list through all its entries, precisely so it can guarantee iteration matches insertion order.
