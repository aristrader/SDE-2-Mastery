---
order: 10
search: false
---

# Practice

## Exercise: enhanced-for-mutation - ConcurrentModificationException

### Goal
Understand why you cannot modify a collection while iterating over it using an enhanced `for` loop.

### Task
Create a `List<String> names = new ArrayList<>(List.of("Alice", "Bob", "Charlie"));`.
Iterate over the list using an enhanced `for` loop (e.g., `for (String name : names)`).
Inside the loop, if the name is `"Bob"`, try to remove it from the list using `names.remove(name);`.

### Checks
- What exception is thrown at runtime? Why does the enhanced `for` loop forbid this?

## Exercise: modern-switch - Modern Switch Syntax

### Goal
Practice using the modern Java switch syntax (arrow `->`) for cleaner control flow.

### Task
Write a method that takes a `DayOfWeek` (from `java.time`) and prints whether it's a weekday or weekend.
First, write it using a traditional `switch` with `case`, `break`, and `default`.
Then, rewrite it using the modern `switch` syntax (e.g., `case SATURDAY, SUNDAY -> ...`).

### Checks
- Notice how the modern switch eliminates the risk of missing a `break` statement (fall-through bugs).
