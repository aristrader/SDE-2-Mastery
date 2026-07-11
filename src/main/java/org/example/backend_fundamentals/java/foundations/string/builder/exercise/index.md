---
order: 10
search: false
---

# StringBuilder Practice

## Exercise: stringbuilder-loop - StringBuilder in Loops

### Goal
Avoid intermediate string garbage in loops.

### Task
Write a `for` loop from `0` to `999`.

Use `StringBuilder` to append the numbers into one final string. Do not use `+` inside the loop.

### Checks
- Explain why this is better than repeated `s += i`.
- Call `toString()` once at the end.
