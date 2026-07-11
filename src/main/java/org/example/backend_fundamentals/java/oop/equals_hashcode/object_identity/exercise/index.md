---
order: 10
search: false
---

# Object Identity Practice

## Exercise: default-equality - Default Equality

### Goal
Observe default reference-based equality.

### Task
Create a `Student` class with one field: `String id`.

Do not override anything.

Create two different `Student("101")` objects and print:

- `s1 == s2`
- `s1.equals(s2)`
- `s1.hashCode()`
- `s2.hashCode()`

### Checks
- Explain why `==` is false.
- Explain why default `equals()` is false.
- Explain why the two hash codes are identity-based.

## Exercise: comparison-apis - Comparison APIs

### Goal
Choose the right equality API.

### Task
Experiment with:

- `==`
- `.equals()`
- `Objects.equals()`

Include string literals, two different `new String(...)` objects with the same content, and `null` values.

### Checks
- Explain when to use `==`.
- Explain when to use `.equals()`.
- Explain when `Objects.equals()` is safer.
