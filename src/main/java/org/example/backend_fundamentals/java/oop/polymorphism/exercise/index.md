---
order: 10
search: false
---

# Practice

## Exercise: method-overriding - Runtime polymorphism

### Goal
Understand how method overriding enables dynamic method dispatch at runtime.

### Task
Create an abstract class `Animal` with an abstract method `makeSound()`.
Create two subclasses, `Dog` and `Cat`, that implement `makeSound()`.
In `main`, create a `List<Animal>` containing both a `Dog` and a `Cat`. Iterate through the list and call `makeSound()` on each.

### Checks
- Does the program output the correct sound for each animal type, even though the variable type is `Animal`?

## Exercise: method-overloading - Compile-time polymorphism

### Goal
Understand how method overloading resolves method calls at compile time based on arguments.

### Task
Create a `Calculator` class with two `add` methods: one that takes two `int`s and one that takes three `int`s.
In `main`, call both methods.

### Checks
- Verify that the compiler automatically selects the correct method based on the number of arguments provided.
