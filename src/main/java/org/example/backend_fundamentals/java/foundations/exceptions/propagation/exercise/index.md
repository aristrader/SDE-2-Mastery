---
order: 10
search: false
---

# Checked Handling Practice

## Exercise: checked-exception - Checked Exception

### Goal
Observe compiler-enforced handling.

### Task
Write a method that opens a file using `FileReader`.

Fix the compiler error in two ways:

1. using `try-catch`
2. using `throws`

## Exercise: exception-propagation - Exception Propagation

### Goal
See how checked exceptions move up the call stack.

### Task
Create methods `main -> a -> b -> c`.

Throw a checked exception in `c`. Try handling it in `b`, then `a`, then only in `main`.

### Checks
- Explain which method signatures need `throws` in each version.
- Explain where execution resumes after the catch block.
