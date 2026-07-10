---
order: 10
search: false
---

# Practice

## Exercise: invalid-overload - The Return Type Trap

### Goal
Understand that return types are not part of a method signature.

### Task
Create a class with two methods:
`public void process(int data)`
`public boolean process(int data)`
Try to compile the code.

### Checks
- What error does the compiler give?
- Why can't the compiler distinguish between the two methods based on the return type?

## Exercise: varargs-rules - The Last Parameter

### Goal
Understand the strict compiler rules for varargs.

### Task
Write a method `public void log(String... messages, int level)`.
Note the compiler error.
Fix the method signature so it compiles, and call it with `log(1, "Error", "Disk full")`.

### Checks
- Why must varargs be the last parameter?
