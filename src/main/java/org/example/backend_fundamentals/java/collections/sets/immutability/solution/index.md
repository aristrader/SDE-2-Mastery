---
order: 20
search: false
---

# Solutions

## Solution: set-of-duplicates - Silent Dedupe vs Fail Fast

```java
Set<String> hashSet = new HashSet<>(List.of("A", "A", "B"));
System.out.println(hashSet.size()); // 2

// Set.of("A", "A", "B"); // IllegalArgumentException
```

`HashSet` silently deduplicates. `Set.of` rejects duplicates because literal duplicates are usually a bug.

## Solution: null-handling - Null Handling

```java
Set<String> hashSet = new HashSet<>();
hashSet.add(null); // allowed

Set<String> linkedHashSet = new LinkedHashSet<>();
linkedHashSet.add(null); // allowed

Set<String> treeSet = new TreeSet<>();
// treeSet.add(null); // NullPointerException

// Set.of("A", null); // NullPointerException
```
