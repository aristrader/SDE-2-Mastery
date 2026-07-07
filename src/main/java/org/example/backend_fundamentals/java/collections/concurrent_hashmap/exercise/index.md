---
order: 10
search: false
---

# Practice

## Exercise: atomic-compute - Atomic updates in ConcurrentHashMap

### Goal
Understand why `get` followed by `put` is a race condition, and how to use `compute` or `merge` instead.

### Task
You have a `ConcurrentHashMap<String, Integer> map`. You want to increment the count for `"Alice"`.
Writing `Integer count = map.get("Alice"); map.put("Alice", count == null ? 1 : count + 1);` is a race condition if two threads do it simultaneously.
Rewrite this update using the `map.merge()` method, which performs the update atomically.

### Checks
- Does `merge` require you to manually synchronize the map?
- What lambda expression did you pass to `merge` to increment the value?

## Exercise: no-nulls-allowed - Null Keys and Values

### Goal
Experience the strict null policy of `ConcurrentHashMap`.

### Task
Create a `HashMap` and put a `null` key and a `null` value into it. (It will succeed).
Create a `ConcurrentHashMap` and try to do the exact same thing.

### Checks
- What exception is thrown by the `ConcurrentHashMap`?
- Why did the designers ban nulls in concurrent maps? (Hint: Ambiguity when `get()` returns null).
