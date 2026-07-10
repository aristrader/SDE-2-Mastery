---
order: 20
search: false
---

# Solutions

## Solution: atomic-compute - Atomic updates in ConcurrentHashMap

```java
ConcurrentMap<String, Integer> map = new ConcurrentHashMap<>();

// Atomic increment. If "Alice" is absent, puts 1. 
// If present, applies Integer::sum to the old value and 1.
map.merge("Alice", 1, Integer::sum);

// Alternatively, using compute:
map.compute("Alice", (k, v) -> (v == null) ? 1 : v + 1);
```
Both `merge` and `compute` execute the update atomically under the bucket's internal lock. No other thread can interleave a read or write to that specific key while the lambda is executing. You do not need external synchronization.

## Solution: no-nulls-allowed - Null Keys and Values

```java
Map<String, String> hashMap = new HashMap<>();
hashMap.put(null, null); // Succeeds

Map<String, String> concurrentMap = new ConcurrentHashMap<>();
// concurrentMap.put(null, "Value"); // Throws NullPointerException
// concurrentMap.put("Key", null);   // Throws NullPointerException
```
In a concurrent map, if `get(key)` returns `null`, it's impossible to tell if the key is absent or if it's explicitly mapped to `null`. In a single-threaded map, you can disambiguate using `containsKey()`, but in a concurrent map, another thread might have changed the map between the two calls. Banning nulls removes this ambiguity entirely.
