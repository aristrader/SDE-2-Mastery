---
order: 10
search: false
---

# Practice

## Exercise: create-student-class - Create a Student Class

### Objective
Practice creating independent objects and reasoning about aliases.

### Task
Create a `Student` class with `id`, `name`, and `marks`.

Add:

- a constructor for all fields
- a method `boolean hasPassed()`
- a method `void printSummary()`

### Checks
- Can you create two different `Student` objects?
- Does each object keep its own field values?
- If two variables reference the same `Student`, do they represent one object or two? This version is immutable; what would all aliases observe if a mutable version changed `marks`?

## Exercise: constructor-invariant - Constructor Invariant

### Objective
Use a constructor to protect object state.

### Task
Reject invalid marks outside `0..100` by throwing `IllegalArgumentException`.

### Checks
- Does valid input create an object?
- Does invalid input fail immediately?
- Why is this safer than allowing `marks` to be assigned later without validation?

## Exercise: constructor-dispatch-trap - Do Not Call an Override During Construction

### Objective
Predict why an object can observe unfinished subclass state.

### Task
Create a `Parent` constructor that calls an overridable `describe()` method. Create a `Child` override that prints a `String label = "ready"` field. Instantiate `Child` and explain the output.

Rewrite the design so construction does not invoke an overridable method. A private/final helper, a constructor argument, or a factory is acceptable.

### Checks
- Why can `describe()` see `null` even though `label` has an initializer?
- Which initialization steps happen before the `Child` constructor body?
