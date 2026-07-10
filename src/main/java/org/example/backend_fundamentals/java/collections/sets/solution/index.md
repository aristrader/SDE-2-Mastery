---
order: 20
search: false
---

# Solutions

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
Set<String> set = new HashSet<>();
System.out.println(set.add("A")); // true
System.out.println(set.add("B")); // true
System.out.println(set.add("A")); // false
```

`false` means the element was already present, so the set did not change.

## Solution: remove-duplicates - Remove Duplicates

```java
public Set<Integer> removeDuplicates(List<Integer> list) {
    return new HashSet<>(list);
}
```

## Solution: union - Union

```java
public Set<Integer> union(Set<Integer> a, Set<Integer> b) {
    Set<Integer> result = new HashSet<>(a);
    result.addAll(b);
    return result;
}
```

## Solution: intersection - Intersection

```java
public Set<Integer> intersection(Set<Integer> a, Set<Integer> b) {
    Set<Integer> result = new HashSet<>(a);
    result.retainAll(b);
    return result;
}
```

## Solution: difference - Difference

```java
public Set<Integer> difference(Set<Integer> a, Set<Integer> b) {
    Set<Integer> result = new HashSet<>(a);
    result.removeAll(b);
    return result;
}
```

## Solution: unique-visitors - Unique Visitors

```java
public int countUniqueVisitors(List<String> userIds) {
    return new HashSet<>(userIds).size();
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

    long startList = System.nanoTime();
    list.contains(999_999);
    long listNanos = System.nanoTime() - startList;

    long startSet = System.nanoTime();
    set.contains(999_999);
    long setNanos = System.nanoTime() - startSet;

    System.out.println("List: " + listNanos);
    System.out.println("Set: " + setNanos);
}
```

`ArrayList.contains` scans, so it is O(n). `HashSet.contains` uses hashing, so it is O(1) average.
