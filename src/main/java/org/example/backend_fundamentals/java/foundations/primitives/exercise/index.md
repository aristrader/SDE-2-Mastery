---
order: 10
search: false
---

# Practice

## Exercise: pass-by-value-primitives - Primitives are Pass-by-Value

### Goal
Prove that passing a primitive to a method copies its value, and mutating it doesn't affect the caller.

### Task
In `main`, declare `int x = 10;`.
Write a method `void changeValue(int x)` that sets `x = 20;`.
Call `changeValue(x)` from `main`, then print `x`.

### Checks
- Does `x` print `10` or `20`? Why?

## Exercise: copied-reference-value - Mutation Is Not Reassignment

### Goal

Distinguish a copied object reference from pass-by-reference.

### Task

Pass a `StringBuilder` containing `"draft"` to a helper. Append `"!"`, then reassign the parameter to a new `StringBuilder("replacement")`. Print the caller's builder after the method returns.

### Checks

- Why is the append visible?
- Why is the replacement not visible?

## Exercise: primitive-vs-object-defaults - Default Values

### Goal
Understand the difference in default values between primitives and their object wrappers.

### Task
Create a class `DefaultTest` with two instance variables:
- `int primitiveInt;`
- `Integer objectInt;`

In a `main` method, instantiate `DefaultTest` and print both fields.

### Checks
- What is the output for each?
- Why can an `Integer` be `null` but an `int` cannot?
- What happens if you assign `objectInt` directly to an `int`?

## Exercise: numeric-conversion-boundary - Predict the Loss

### Goal

Distinguish promotion, implicit compound-assignment narrowing, floating-point precision loss, and checked narrowing.

### Task

Write a small `main` that demonstrates all four cases:

1. Explain why `byte retries = 10; retries = retries + 1;` does not compile, then use `retries += 1`.
2. Assign `16_777_217` to both `int` and `float`, then compare the values after converting the `float` back to `int`.
3. Cast `3_000_000_000L` to `int` and print the result.
4. Call `Math.toIntExact(3_000_000_000L)` and handle the failure deliberately.

### Checks

- Which operation promotes `byte` to `int`?
- Why does the `float` round even though the original `int` is valid?
- Why is the cast not a safe validation step?
- When is failing fast preferable to wrapping or truncating a value?
