---
order: 10
search: false
---

# Exception Hierarchy Practice

## Exercise: throw-vs-throws - throw vs throws

### Goal
Separate throwing an exception from declaring that it may escape.

### Task
Create `validateAge(int age)`.

If age is negative, throw `IllegalArgumentException`. Call the method with a valid age and an invalid age.

### Checks
- Point to the exact line that uses `throw`.
- Explain why this unchecked exception does not require `throws`.

## Exercise: checked-vs-unchecked - Checked vs Unchecked

### Goal
See which exception types the compiler forces you to handle.

### Task
Throw an `IOException` and an `IllegalArgumentException` in separate small methods.

### Checks
- Identify which one requires `throws` or `try-catch`.
- Explain the inheritance reason.

## Exercise: exception-hierarchy - Exception Hierarchy

### Goal
Classify common exceptions.

### Task
Write a program that throws or references:

- `ArithmeticException`
- `NullPointerException`
- `IOException`

### Checks
- Mark each as checked or unchecked.
- Note where the compiler complains.
