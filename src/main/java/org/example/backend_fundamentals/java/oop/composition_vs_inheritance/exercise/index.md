---
order: 10
search: false
---

# Practice

## Exercise: brittle-hierarchy - The Penguin Problem

### Goal
Experience how inheritance can break the Liskov Substitution Principle (LSP).

### Task
Create a `Bird` class with a `fly()` method.
Create a `Penguin` class that extends `Bird`. Since penguins can't fly, throw an `UnsupportedOperationException` in the overridden `fly()` method.
In `main`, create a `List<Bird>` containing a normal bird and a penguin. Iterate over the list and call `fly()`.

### Checks
- What happens at runtime? Why is this a violation of the "IS-A" relationship contract?

## Exercise: refactor-to-composition - Refactoring to HAS-A

### Goal
Solve brittle inheritance hierarchies using composition.

### Task
Refactor the previous exercise. Create an interface `FlyBehavior` with a `fly()` method, and implement it with `FlyWithWings` and `NoFly`.
Create a base `Bird` class that HAS-A `FlyBehavior`. Pass the behavior in the constructor.
Create `Penguin` and `Eagle` classes that compose the correct behaviors.

### Checks
- Does this solve the LSP violation? Can you safely process a list of birds now?
