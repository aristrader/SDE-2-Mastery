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


## Solution: basic-hashset-operations - Basic HashSet Operations

```java
public void basicOps() {
    Set<String> set = new HashSet<>();
    set.add("A");
    set.add("B");
    System.out.println(set.contains("A")); // true
    set.remove("A");
    System.out.println(set.size()); // 1
    System.out.println(set.isEmpty()); // false
}
```

## Solution: understanding-add - Understanding add()

```java
public void understandAdd() {
    Set<String> set = new HashSet<>();
    System.out.println(set.add("A")); // true
    System.out.println(set.add("B")); // true
    System.out.println(set.add("A")); // false
    // false is returned when the element is already present
}
```

## Solution: remove-duplicates - Remove Duplicates

```java
public Set<Integer> removeDuplicates(List<Integer> list) {
    return new HashSet<>(list);
}
```

## Solution: preserve-order - Preserve Order

```java
public Set<Integer> preserveOrder(List<Integer> list) {
    return new LinkedHashSet<>(list);
}
```

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

## Solution: sorting - Sorting

```java
public void sortWithTreeSet(List<Integer> list) {
    Set<Integer> set = new TreeSet<>(list);
    System.out.println(set);
}
```

## Solution: null-handling - Null Handling

```java
public void nullHandling() {
    Set<String> hashSet = new HashSet<>();
    hashSet.add(null); // Allowed

    Set<String> linkedHashSet = new LinkedHashSet<>();
    linkedHashSet.add(null); // Allowed

    Set<String> treeSet = new TreeSet<>();
    // treeSet.add(null); // NullPointerException
}
```
`HashSet` and `LinkedHashSet` support one null element. `TreeSet` throws NPE because it cannot compare null to other elements.

## Solution: union - Union

```java
public Set<Integer> union(Set<Integer> a, Set<Integer> b) {
    Set<Integer> result = new HashSet<>(a);
    result.addAll(b); // Union
    return result;
}
```

## Solution: intersection - Intersection

```java
public Set<Integer> intersection(Set<Integer> a, Set<Integer> b) {
    Set<Integer> result = new HashSet<>(a);
    result.retainAll(b); // Intersection
    return result;
}
```

## Solution: difference - Difference

```java
public Set<Integer> difference(Set<Integer> a, Set<Integer> b) {
    Set<Integer> result = new HashSet<>(a);
    result.removeAll(b); // Difference
    return result;
}
```

## Solution: contains-performance - contains() Performance

```java
public void performance() {
    List<Integer> list = new ArrayList<>();
    Set<Integer> set = new HashSet<>();
    for (int i = 0; i < 1_000_000; i++) {
        list.add(i);
        set.add(i);
    }

    long start1 = System.nanoTime();
    list.contains(999_999);
    long end1 = System.nanoTime();

    long start2 = System.nanoTime();
    set.contains(999_999);
    long end2 = System.nanoTime();

    System.out.println("List: " + (end1 - start1) + " ns");
    System.out.println("Set: " + (end2 - start2) + " ns");
}
```
HashSet contains is O(1) whereas ArrayList contains is O(n), so Set is orders of magnitude faster for large collections.

## Solution: unique-visitors - Unique Visitors

```java
public int countUniqueVisitors(List<String> userIdsStream) {
    Set<String> uniqueVisitors = new HashSet<>(userIdsStream);
    return uniqueVisitors.size();
}
```

## Solution: mini-challenge - Mini Challenge

```java
public void miniChallenge(List<String> words) {
    // Remove duplicates & preserve order
    Set<String> linkedSet = new LinkedHashSet<>(words);
    System.out.println("Original Order Unique: " + linkedSet);

    // Alphabetical
    Set<String> treeSet = new TreeSet<>(linkedSet);
    System.out.println("Alphabetical: " + treeSet);

    // Total unique
    System.out.println("Total Unique: " + linkedSet.size());
}
```
