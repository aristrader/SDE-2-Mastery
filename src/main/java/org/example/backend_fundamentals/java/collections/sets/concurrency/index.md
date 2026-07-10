---
order: 50
---

# Concurrent Sets

The JDK has no `ConcurrentHashSet` class. Use a key set backed by `ConcurrentHashMap`:

```java
Set<Integer> threadSafe = ConcurrentHashMap.newKeySet();
```

`Collections.synchronizedSet(new HashSet<>())` exists, but it uses one coarse lock. Prefer `ConcurrentHashMap.newKeySet()` for new shared mutable sets.

## Why normal HashSet fails

`HashSet` under concurrent writes can lose additions without throwing. The failure is the same family as unsynchronized `HashMap`: shared mutation corrupts the intended state.

## Demo code

| Demo | Shows |
| --- | --- |
| `../playground/concurrent/ConcurrentHashSetBasicsRun` | `HashSet` loses adds under contention; `ConcurrentHashMap.newKeySet()` reaches the expected size. |

## Quick recall

- **Thread-safe mutable set?** `ConcurrentHashMap.newKeySet()`.
- **Class named `ConcurrentHashSet` in the JDK?** No.
- **Use raw `HashSet` across threads?** No.
