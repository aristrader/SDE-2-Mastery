---
order: 20
---

# Set Ordering

Set implementations choose different iteration guarantees. Pick the type whose contract matches the requirement.

| Type | Iteration order | Cost |
| --- | --- | --- |
| `HashSet` | Bucket order, effectively unpredictable | Cheapest |
| `LinkedHashSet` | Insertion order | Extra linked-list pointers |
| `TreeSet` | Sorted order | O(log n) operations |
| `EnumSet` | Enum declaration order | Compact bit vector |

## Dedup while preserving order

```java
List<Integer> numbers = List.of(1, 2, 1, 3, 2);
Set<Integer> uniqueInOrder = new LinkedHashSet<>(numbers); // [1, 2, 3]
```

## Sorted set

```java
Set<Integer> sorted = new TreeSet<>(List.of(30, 10, 20));
System.out.println(sorted); // [10, 20, 30]
```

For custom objects, `TreeSet` needs natural ordering or a `Comparator`. That belongs with `../sorting/`.

## EnumSet

Use `EnumSet` whenever all values are enum constants:

```java
EnumSet<DayOfWeek> weekend = EnumSet.of(SATURDAY, SUNDAY);
```

It is faster and smaller than `HashSet<EnumType>`.

## Demo code

| Demo | Shows |
| --- | --- |
| `../playground/basics/LinkedHashSetBasicsRun` | Insertion order. |
| `../playground/basics/TreeSetBasicsRun` | Sorted iteration and null behavior. |
| `../playground/enum_keyed/EnumSetBasicsRun` | Enum-set factories and declaration-order iteration. |

## Quick recall

- **Need insertion order?** `LinkedHashSet`.
- **Need sorted values?** `TreeSet`.
- **Need enum values?** `EnumSet`.
- **Does `HashSet` preserve insertion order?** No.
