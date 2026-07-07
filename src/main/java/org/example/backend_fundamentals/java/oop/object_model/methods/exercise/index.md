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
