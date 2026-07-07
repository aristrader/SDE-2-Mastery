---
order: 10
search: false
---

# Practice

## Exercise: varargs-rules - The Last Parameter

### Goal
Understand the strict compiler rules for varargs.

### Task
Write a method `public void log(String... messages, int level)`.
Note the compiler error.
Fix the method signature so it compiles, and call it with `log(1, "Error", "Disk full")`.

### Checks
- Why must varargs be the last parameter?
