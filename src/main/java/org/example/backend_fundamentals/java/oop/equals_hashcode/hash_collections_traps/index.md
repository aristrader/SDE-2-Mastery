---
order: 40
---

# Hash Collection Traps

`HashMap` and `HashSet` use `hashCode` first and `equals` second.

```text
hashCode() -> bucket
equals()   -> exact match inside bucket
```

## Interview answer shape

When asked why a lookup fails, answer in this order:

1. Hash collections do not scan every entry.
2. They compute the key's hash and jump to one bucket.
3. Only entries in that bucket are compared with `equals()`.
4. If the hash is wrong or changes after insertion, lookup checks the wrong bucket.

That is why `equals()` and `hashCode()` must agree, and why hash keys should be immutable.

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

## Safe key rules

- Prefer immutable keys: `String`, boxed primitives, enum, record, or immutable value object.
- Use the same stable identity fields in both `equals()` and `hashCode()`.
- Do not include mutable display fields like `name`, `status`, or `lastUpdatedAt`.
- If an entity id is assigned later, do not use the entity as a hash key before the id is stable.
- If equality changes across persistence states, avoid using the object as a long-lived map/set key.

## HashSet duplicate trap

`HashSet` stores elements as `HashMap` keys. Broken equality means deduplication fails.

```java
set.add(new Student("101"));
set.add(new Student("101")); // may be stored as a second element
```

## Quick diagnostic

If `contains`, `get`, or duplicate detection behaves strangely:

1. Check whether `equals()` is overridden.
2. Check whether `hashCode()` is overridden using the same fields.
3. Check whether those fields changed after insertion.
4. Check whether the collection is actually hash-based; `TreeSet` and `TreeMap` use comparison instead.

## Quick recall

- **What chooses the bucket?** `hashCode`.
- **What confirms the exact key?** `equals`.
- **Why does lookup silently fail?** The collection searches the wrong bucket.
- **Safe key type?** Immutable value object, record, `String`, wrapper, enum.
- **Does `HashSet` use the same trap?** Yes. It stores elements as keys in a backing `HashMap`.
