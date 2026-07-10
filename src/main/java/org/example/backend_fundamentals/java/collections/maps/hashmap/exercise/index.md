---
order: 10
search: false
---

# Practice

## Exercise: mutable-key-trap - The Orphaned Entry

### Goal
Understand why mutating a key after inserting it into a `HashMap` results in silent data loss.

### Task
Create a `Person` class with a `String name`. Override `equals` and `hashCode` using the `name` field. Provide a setter for the name.
In `main`, create a `Person p = new Person("Alice");` and put it in a `HashMap` with a value of `"Engineer"`.
Then, mutate the key: `p.setName("Bob");`.
Finally, try to retrieve the value using `map.get(p);`.

### Checks
- What does `map.get(p)` return?
- Is the entry still in the map (check `map.size()`)? Why can't you find it?

## Exercise: power-of-two - Bucket Index Calculation

### Goal
Manually calculate the bucket index using the bitwise mask technique that `HashMap` uses internally.

### Task
Assume a `HashMap` with the default initial capacity of 16.
You have a key whose `hashCode()` returns `1042`.
Calculate the bucket index using the bitwise mask `(capacity - 1) & hash`.
(You can write a quick Java `main` method to print the result).

### Checks
- What bucket index (0-15) does this key land in?
