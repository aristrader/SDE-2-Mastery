---
order: 10
search: false
---

# Practice

## Exercise: pass-by-value - Pass by Value of the Reference

### Goal
Prove that Java copies the reference (pointer) when passing objects to methods, rather than passing the object itself.

### Task
Create a `Dog` class with a `name` field and setter.
In `main`, instantiate a `Dog` named "Rex". 
Write a method `void rename(Dog d)`. Inside it, first call `d.setName("Max")`, then reassign `d = new Dog("Fido")`.
Call `rename(dog)` from `main`, and then print the dog's name.

### Checks
- Does the main method print "Max" or "Fido"? Why?

## Exercise: reference-equality - The `==` operator vs `.equals()`

### Goal
Understand the difference between heap object identity and logical equivalence.

### Task
Create two separate `String` objects using the `new` keyword: 
`String s1 = new String("hello");`
`String s2 = new String("hello");`
Print the result of `s1 == s2`.
Print the result of `s1.equals(s2)`.

### Checks
- Why does `==` return `false` even though the text is identical?

## Exercise: narrowing-cast - ClassCastException at runtime

### Goal
Experience a narrowing cast failure.

### Task
Create `Animal`, `Dog`, and `Cat`.
In `main`: `Animal a = new Dog();`
Try to cast it: `Cat c = (Cat) a;`

### Checks
- Does this compile?
- What happens at runtime? Why?
