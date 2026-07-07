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
