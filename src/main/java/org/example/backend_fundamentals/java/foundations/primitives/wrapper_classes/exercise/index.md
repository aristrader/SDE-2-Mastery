---
order: 10
search: false
---

# Practice

## Exercise: integer-cache-trap - The Cache Trap

### Goal
Experience the inconsistency of using `==` to compare wrapper objects.

### Task
Create two `Integer` variables, `a` and `b`, both set to `50`.
Compare them using `==` and print the result.
Create two `Integer` variables, `x` and `y`, both set to `500`.
Compare them using `==` and print the result.
Finally, compare `x` and `y` using `.equals()`.

### Checks
- Why does `a == b` return `true`?
- Why does `x == y` return `false`?
- What is the only safe way to compare object values?

## Exercise: unboxing-npe - Hidden NullPointerException

### Goal
Observe how autoboxing hides method calls that can throw exceptions.

### Task
Create a `Map<String, Integer> map = new HashMap<>();`.
Attempt to retrieve a missing key and assign it directly to a primitive `int`:
`int count = map.get("missing_key");`.

### Checks
- What exception is thrown?
- Why did the compiler allow the assignment, but the JVM crashed?
