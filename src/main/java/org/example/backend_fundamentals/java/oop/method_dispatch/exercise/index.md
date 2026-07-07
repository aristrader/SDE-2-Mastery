---
order: 10
search: false
---

# Practice

## Exercise: dynamic-dispatch - Dynamic Method Dispatch

### Goal
Observe how instance methods are resolved at runtime (Dynamic Dispatch).

### Task
Create an `Animal` class with a method `public void sound() { System.out.println("Animal"); }`.
Create a `Dog` subclass that overrides `sound()` to print `"Dog"`.
In `main`, write: `Animal a = new Dog(); a.sound();`

### Checks
- Does it print "Animal" or "Dog"? Why?

## Exercise: static-shadowing - Static Method Shadowing

### Goal
Understand that `static` methods are NOT polymorphic and are resolved at compile time.

### Task
Add a `public static void identify()` method to both `Animal` (prints "Static Animal") and `Dog` (prints "Static Dog").
In `main`, write: `Animal a = new Dog(); a.identify();`

### Checks
- Does it print "Static Animal" or "Static Dog"? 
- Why is hiding a static method in a subclass not considered true overriding?

## Exercise: field-shadowing - Fields are not polymorphic

### Goal
Understand that field access is resolved at compile time, unlike methods.

### Task
Add a `public String name = "Animal";` field to `Animal`.
Add a `public String name = "Dog";` field to `Dog`.
In `main`, write: `Animal a = new Dog(); System.out.println(a.name);`

### Checks
- What gets printed? 
