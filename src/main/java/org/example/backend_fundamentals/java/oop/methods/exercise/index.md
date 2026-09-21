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

## Exercise: reference-copy - Mutation Is Not Reassignment

### Goal
Trace Java's pass-by-value rule for an object reference.

### Task
Create a mutable `Customer` with a `name`. Write `rename(Customer customer)` that first changes the name, then assigns `customer = new Customer("replacement")`.

Call it from `main` and print the original variable's name after the call.

### Checks
- Which line affects the caller's object, and why?
- Why is “Java passes objects by reference” an inaccurate explanation?
