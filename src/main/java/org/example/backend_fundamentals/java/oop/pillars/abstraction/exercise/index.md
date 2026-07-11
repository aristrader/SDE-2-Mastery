---
order: 10
search: false
---

# Practice

## Exercise: leaky-abstraction - Identify and fix a leaky abstraction

### Goal
Experience a type leak and fix it by improving the abstraction.

### Task
Create a class `Cache` with a method `public Object get(String key)`. In a `main` method, retrieve a string from the cache and print its length. You will be forced to cast.
Then, refactor the `Cache` class to use Generics (`Cache<T>`) so that `get` returns `T`, eliminating the cast.

### Checks
- Does the caller code still require a cast after refactoring? (It shouldn't).

## Exercise: program-to-interface - Program to an interface

### Goal
Hold the most abstract type that still gives you what you need.

### Task
Write a method `public void processItems(ArrayList<String> items)` that simply iterates and prints the items.
Refactor the method signature so it accepts any kind of list (e.g. `LinkedList`, `ArrayList`) without changing the method body.

### Checks
- Can you pass a `LinkedList<String>` to the refactored method?
