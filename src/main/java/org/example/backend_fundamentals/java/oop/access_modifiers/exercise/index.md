---
order: 10
search: false
---

# Practice

## Exercise: variable-scope - Variable Scope and Defaults

### Goal
Understand the difference between local, instance, and static variables, especially regarding default values.

### Task
Create a `ScopeTest` class. 
1. Declare an instance variable `int instanceVar` and a static variable `static int staticVar`.
2. In a `printVars()` method, declare a local variable `int localVar`.
3. Try to print all three variables without explicitly initializing them.

### Checks
- Which variable causes a compilation error? Why?

## Exercise: final-references - Final References vs Mutation

### Goal
Understand that `final` on an object reference prevents reassignment, but does not make the object immutable.

### Task
Create a `Person` class with an `age` field. 
In `main`, declare a `final Person p = new Person();`.
Try to reassign `p = new Person();`. 
Try to mutate the object: `p.age = 30;`.

### Checks
- Which operation fails to compile? 
- What does this teach you about `final` vs immutability?

## Exercise: file-structure - Top-Level Modifiers

### Goal
Understand what modifiers are legally allowed at the top level of a file.

### Task
Create a file named `App.java`.
Inside, try to declare:
`private class HiddenApp {}`
Note the compiler error.

### Checks
- Why does Java prevent top-level classes from being `private`?
- Where can you use `private class`?
