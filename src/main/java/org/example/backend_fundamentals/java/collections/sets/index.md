---
order: 90
---

# Sets

A `Set` models uniqueness. The default implementation is `HashSet`, which is backed by `HashMap`: each element is stored as a map key with a dummy value.

Read `../hashing/` first if `equals`/`hashCode` is still fuzzy.

## Study path

| Order | Page | Use it for |
| --- | --- | --- |
| 1 | this page | `HashSet`, `add`, `contains`, `remove`, set algebra |
| 2 | `ordering` | `LinkedHashSet`, `TreeSet`, `EnumSet`, iteration order |
| 3 | `immutability` | `Set.of`, duplicate/null fail-fast behavior |
| 4 | `hash_contract` | broken `equals`/`hashCode` causing duplicate-looking elements |
| 5 | `concurrency` | `ConcurrentHashMap.newKeySet()` and shared mutable sets |

## Set options

| Type | Ordering | Null | Thread-safe | When to use |
| --- | --- | --- | --- | --- |
| `HashSet` | None | One null allowed | No | Default set. O(1) average add/contains/remove. |
| `LinkedHashSet` | Insertion order | Yes | No | Dedup while preserving encounter order. |
| `TreeSet` | Sorted | No null | No | Sorted unique values. O(log n). |
| `EnumSet` | Enum declaration order | No null | No | Fast compact set of enum constants. |
| `Set.of(...)` | Unspecified | No null | Immutable | Fixed read-only literal set. |
| `ConcurrentHashMap.newKeySet()` | None | No null | Yes | Thread-safe mutable set. |

## Basic operations

```java
Set<String> users = new HashSet<>();

users.add("alice");
users.add("bob");
users.add("alice"); // duplicate ignored

users.contains("alice"); // true
users.remove("bob");
users.size();
users.isEmpty();
```

## The `add` boolean

`Set.add()` tells you whether the set changed:

```java
Set<String> seen = new HashSet<>();

seen.add("A"); // true
seen.add("A"); // false
```

This is the cleanest way to detect duplicates while scanning:

```java
for (String id : ids) {
    if (!seen.add(id)) {
        System.out.println("Duplicate: " + id);
    }
}
```

## Set algebra

```java
Set<Integer> a = new HashSet<>(Set.of(1, 2, 3));
Set<Integer> b = new HashSet<>(Set.of(3, 4));

Set<Integer> union = new HashSet<>(a);
union.addAll(b);       // [1, 2, 3, 4]

Set<Integer> intersection = new HashSet<>(a);
intersection.retainAll(b); // [3]

Set<Integer> difference = new HashSet<>(a);
difference.removeAll(b);   // [1, 2]
```

## Quick recall

- **Default unique collection?** `HashSet`.
- **What does `add` return for a duplicate?** `false`.
- **Union?** Copy one set, then `addAll`.
- **Intersection?** Copy one set, then `retainAll`.
- **Difference?** Copy one set, then `removeAll`.
- **Why does `HashSet` depend on `equals`/`hashCode`?** It is backed by hash buckets, same as `HashMap` keys.
