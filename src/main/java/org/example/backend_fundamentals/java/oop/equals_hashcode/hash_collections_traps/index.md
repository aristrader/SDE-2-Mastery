---
order: 40
---

# Hash Collection Traps

`HashMap` and `HashSet` use `hashCode` first and `equals` second.

```text
hashCode() -> bucket
equals()   -> exact match inside bucket
```

## Which method gets used

| Collection/API | Matching rule |
| --- | --- |
| `HashMap`, `HashSet` | `hashCode` chooses the bucket, then `equals` confirms the match |
| `TreeMap`, `TreeSet` | `compareTo` or `Comparator`; `equals` is not used for duplicate detection |
| `ArrayList.contains`, `ArrayList.indexOf` | `equals` only |

## Missing `hashCode`

If you override `equals` but leave default `hashCode`, equal objects can land in different buckets.

```java
map.put(new Student("101"), "Asha");
map.get(new Student("101")); // may return null
```

The lookup searches the bucket for the new object's identity hash, not the bucket where the original object was stored.

## Mutable keys

Never mutate fields used by `equals`/`hashCode` after insertion into a hash collection.

```java
Student key = new Student("101");
map.put(key, "Asha");

key.setId("202");
map.get(key); // searches the wrong bucket
```

The entry is still inside the map, but normal lookup cannot find it.

## HashSet duplicate trap

`HashSet` stores elements as `HashMap` keys. Broken equality means deduplication fails.

```java
set.add(new Student("101"));
set.add(new Student("101")); // may be stored as a second element
```

## Quick recall

- **What chooses the bucket?** `hashCode`.
- **What confirms the exact key?** `equals`.
- **Why does lookup silently fail?** The collection searches the wrong bucket.
- **Safe key type?** Immutable value object, record, `String`, wrapper, enum.
