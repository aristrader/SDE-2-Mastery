---
order: 10
search: false
---

# Primitive and Parallel Streams Practice

## Exercise: primitive-and-parallel - Numeric and Parallel Stream Choices

### Goal
Use primitive streams for numeric work and identify unsafe parallel stream patterns.

### Task
Using `Employee`:

1. Calculate average salary with a primitive stream.
2. Calculate total age with a primitive stream.
3. Explain why this is unsafe:

```java
employees.parallelStream()
         .forEach(list::add);
```

4. Rewrite it safely.
5. Decide whether `parallelStream()` is appropriate for HTTP calls per employee.

### Checks
- Use `mapToDouble` or `mapToInt`.
- Do not mutate shared collections from parallel streams.
- State that parallel streams are not a default performance upgrade.
