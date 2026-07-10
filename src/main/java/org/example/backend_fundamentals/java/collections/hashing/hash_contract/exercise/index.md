---
order: 10
search: false
---

# Practice

## Exercise: broken-hash-contract - The Missing HashCode

### Goal
Experience the most common bug in hash-based collections: overriding `equals` without overriding `hashCode`.

### Task
Create a `Money` class with an `int amount` and `String currency`.
Override `equals` to check both fields, but do not override `hashCode`.
In `main`, create a `HashSet<Money>`. Add `new Money(100, "USD")`.
Check if the set contains `new Money(100, "USD")`.

### Checks
- Does `contains` return `true` or `false`?
- How does the set's internal mechanism fail?

## Exercise: manual-hashcode - Writing a good HashCode

### Goal
Learn the standard Java idiom for manually calculating a hash code.

### Task
Fix the `Money` class by manually overriding `hashCode()`.
Start with `int result = 17;`.
Multiply by `31` and add each field's hash.

### Checks
- Does `HashSet.contains()` now return `true`?
