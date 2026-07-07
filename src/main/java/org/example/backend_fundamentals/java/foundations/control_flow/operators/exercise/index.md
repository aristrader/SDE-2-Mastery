---
order: 10
search: false
---

# Practice

## Exercise: short-circuit-trap - Short-Circuit vs Bitwise

### Goal
Understand the safety implications of short-circuit logical operators compared to non-short-circuit bitwise operators.

### Task
Create a `String str = null;`.
Write an if-statement that checks: `if (str != null && str.length() > 0)`.
Run it. Note what happens.

Now, change the operator from `&&` (logical AND) to `&` (bitwise AND):
`if (str != null & str.length() > 0)`.
Run it again.

### Checks
- What happens in the first case?
- What happens in the second case, and why?
