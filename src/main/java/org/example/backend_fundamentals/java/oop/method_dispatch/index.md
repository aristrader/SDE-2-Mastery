---
order: 90
---
# Method Overloading vs Overriding & Dynamic Dispatch

## Method Overloading (Static Polymorphism)
- Same method name, different parameters (number, type, or order).
- Compiler decides which method to call.
- **Interview Trap:** Changing ONLY the return type is illegal and does not overload the method.

## Method Overriding (Runtime Polymorphism)
- Child replaces parent functionality. Method signature must match exactly.
- **Rules:**
  - Cannot reduce visibility (e.g., overriding a `public` method with `private` is illegal).
  - Covariant return types are allowed (returning a subclass of the original return type).

## Dynamic Method Dispatch
When a parent reference points to a child object (`Animal a = new Dog()`), Java uses Dynamic Method Dispatch to call the child's overridden method at runtime.
- **Compiler checks:** Does `Animal` have `sound()`?
- **Runtime executes:** `Dog.sound()`.

## Interview Trap: Fields vs Methods
- **Methods are polymorphic.** They are dynamically dispatched at runtime.
- **Fields are NOT polymorphic.** Field access is resolved at compile time using the reference type.
```java
Animal a = new Dog();
a.sound(); // Prints "Dog" (Polymorphic)
System.out.println(a.name); // Prints Animal's name field (Not Polymorphic)
```


<ExerciseNav />
