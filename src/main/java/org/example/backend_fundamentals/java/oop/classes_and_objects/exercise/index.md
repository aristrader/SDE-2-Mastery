---
order: 10
search: false
---

# Practice

## Exercise: create-student-class - Create a Student Class

### Objective
Practice fields, constructor, and instance methods.

### Task
Create a `Student` class with `id`, `name`, and `marks`.

Add:

- a constructor for all fields
- a method `boolean hasPassed()`
- a method `void printSummary()`

### Checks
- Can you create two different `Student` objects?
- Does each object keep its own field values?

## Exercise: constructor-invariant - Constructor Invariant

### Objective
Use a constructor to protect object state.

### Task
Reject invalid marks outside `0..100` by throwing `IllegalArgumentException`.

### Checks
- Does valid input create an object?
- Does invalid input fail immediately?
