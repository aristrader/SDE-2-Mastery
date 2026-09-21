---
order: 10
search: false
---

# Practice

## Exercise: integer-cache-trap - The Cache Trap

### Goal
Show why `==` is an identity comparison for two wrappers, even when cache reuse makes it look like a value comparison.

### Task
Create two `Integer` variables, `a` and `b`, both set to `50`.
Compare them using `==` and print the result.
Create two `Integer` variables, `x` and `y`, both set to `500`.
Compare them using `==` and print the result.
Finally, compare `x` and `y` using `.equals()`.

### Checks
- Why does `a == b` return `true`?
- Why can cache reuse make `a == b` return `true`?
- Why must you not rely on the result of `x == y`?
- How would the comparison change if either value could be `null`?

## Exercise: unboxing-npe - Hidden NullPointerException

### Goal
Trace the hidden unboxing step that turns an absent map value into a runtime failure.

### Task
Create a `Map<String, Integer> map = new HashMap<>();`.
Attempt to retrieve a missing key and assign it directly to a primitive `int`:
`int count = map.get("missing_key");`.

### Checks
- What exception is thrown?
- Why did the compiler allow the assignment, but the JVM crashed?
- What default or validation policy would make the conversion safe for this API?
