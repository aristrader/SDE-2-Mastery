---
order: 10
---

# Map Basics

A `Map<K, V>` stores key-value pairs.

- `K` is the key type.
- `V` is the value type.
- Keys are unique.
- Values can repeat.

```java
Map<String, Integer> marks = new HashMap<>();

marks.put("Asha", 90);
marks.put("Ravi", 82);

Integer ashaMarks = marks.get("Asha");      // 90
Integer missing = marks.get("Mina");        // null
int safeMissing = marks.getOrDefault("Mina", 0); // 0
```

## Core operations

| Operation | Meaning |
| --- | --- |
| `put(key, value)` | Insert or replace a mapping |
| `get(key)` | Return value, or `null` if missing |
| `getOrDefault(key, fallback)` | Return value, or fallback if missing |
| `containsKey(key)` | Check if the key exists |
| `containsValue(value)` | Check if any key maps to that value |
| `remove(key)` | Remove mapping by key |
| `replace(key, value)` | Replace only when key already exists |
| `size()` | Number of key-value pairs |
| `isEmpty()` | Whether map has no entries |

## Duplicate keys

Putting the same key again does not create another entry. It replaces the value.

```java
Map<String, Integer> marks = new HashMap<>();

System.out.println(marks.put("Asha", 80)); // null: no old value
System.out.println(marks.put("Asha", 95)); // 80: old value replaced
System.out.println(marks.size());          // 1
System.out.println(marks.get("Asha"));     // 95
```

This is why maps are useful for lookup tables: one key has one current answer.

## Missing keys and null

`get()` returning `null` can mean either:

1. key is absent
2. key exists and maps to `null`

```java
Map<String, Integer> map = new HashMap<>();
map.put("known-null", null);

System.out.println(map.get("missing"));    // null
System.out.println(map.get("known-null")); // null
System.out.println(map.containsKey("known-null")); // true
```

For normal counting and lookup code, avoid storing null values. Use `getOrDefault`, `computeIfAbsent`, or `merge` depending on the job.

## Common update patterns

Counter:

```java
Map<String, Integer> count = new HashMap<>();
count.put("java", count.getOrDefault("java", 0) + 1);
```

Grouping:

```java
Map<String, List<String>> groups = new HashMap<>();
groups.computeIfAbsent("backend", ignored -> new ArrayList<>()).add("Java");
```

Atomic-style value merge:

```java
Map<String, Integer> count = new HashMap<>();
count.merge("java", 1, Integer::sum);
```

## Interface vs implementation

Use the interface for variables when callers only need the map contract:

```java
Map<String, Integer> marks = new HashMap<>();
```

Choose the implementation based on behavior:

| Need | Implementation |
| --- | --- |
| Normal lookup | `HashMap` |
| Insertion order | `LinkedHashMap` |
| Sorted keys | `TreeMap` |
| Enum keys | `EnumMap` |
| Concurrent updates | `ConcurrentHashMap` |

## Quick recall

- **Does duplicate `put` add a second entry?** No, it replaces.
- **Missing key with `get()`?** Returns `null`.
- **Need a fallback for missing key?** `getOrDefault`.
- **Need to initialize a list value?** `computeIfAbsent`.
- **Need a simple counter increment?** `merge(key, 1, Integer::sum)`.
