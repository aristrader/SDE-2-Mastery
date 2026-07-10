---
order: 20
search: false
---

# Solutions

## Solution: preserve-order - Preserve Order

```java
public Set<Integer> preserveOrder(List<Integer> list) {
    return new LinkedHashSet<>(list);
}
```

## Solution: linked-hash-set - Preserving Insertion Order

```java
Set<Integer> hashSet = new HashSet<>();
hashSet.add(10);
hashSet.add(1);
hashSet.add(5);
hashSet.add(20);

Set<Integer> linkedSet = new LinkedHashSet<>();
linkedSet.add(10);
linkedSet.add(1);
linkedSet.add(5);
linkedSet.add(20);

System.out.println(hashSet);
System.out.println(linkedSet); // [10, 1, 5, 20]
```

## Solution: sorting - Sorting

```java
public Set<Integer> sortWithTreeSet(List<Integer> values) {
    return new TreeSet<>(values);
}
```

## Solution: mini-challenge - Mini Challenge

```java
public void miniChallenge(List<String> words) {
    Set<String> insertionOrder = new LinkedHashSet<>(words);
    Set<String> alphabetical = new TreeSet<>(insertionOrder);

    System.out.println(insertionOrder);
    System.out.println(alphabetical);
    System.out.println(insertionOrder.size());
}
```
