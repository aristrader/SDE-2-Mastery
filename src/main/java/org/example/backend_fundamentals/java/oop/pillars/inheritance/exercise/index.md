---
order: 10
search: false
---

# Practice

## Exercise: is-a-relationship - The IS-A relationship

### Goal
Understand how subclassing creates an IS-A relationship and inherits behavior.

### Task
Create a base class `Vehicle` with a method `startEngine()`.
Create a subclass `Car` that `extends Vehicle`.
In `main`, instantiate a `Car` and call `startEngine()`.

### Checks
- Does the `Car` object successfully execute the inherited method?

## Exercise: super-keyword - Using super

### Goal
Use the `super` keyword to invoke parent constructors and methods.

### Task
Add a constructor to `Vehicle` that takes a `String brand`.
Update `Car` to have a constructor that takes `brand` and `int doors`, and uses `super(brand)` to initialize the parent.
Override `startEngine()` in `Car` to print "Car engine starting...", then call `super.startEngine()`.

### Checks
- Does calling `startEngine()` on the `Car` execute both the child's logic and the parent's logic?
