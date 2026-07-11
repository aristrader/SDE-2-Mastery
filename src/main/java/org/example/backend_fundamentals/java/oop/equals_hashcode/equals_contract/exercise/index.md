---
order: 10
search: false
---

# equals Contract Practice

## Exercise: equals-override - Override equals

### Goal
Implement a correct, five-rule `equals` method.

### Task
Create a `Person` class with fields `String name` and `int age`.

Override `equals` using this shape:

1. same reference shortcut
2. null-safe type check
3. field comparison

### Checks
- `p1.equals(p1)` is true.
- `p1.equals(p2)` is true when both fields match.
- `p1.equals(p3)` is false when a field differs.
- `p1.equals(null)` is false.
- `p1.equals("Alice")` is false.
