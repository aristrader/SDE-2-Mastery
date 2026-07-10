---
order: 20
search: false
---

# Solutions

## Solution: variable-scope - Variable Scope and Defaults

```java
public class ScopeTest {
    int instanceVar; // Defaults to 0
    static int staticVar; // Defaults to 0

    public void printVars() {
        int localVar; // No default value!
        
        System.out.println(instanceVar);
        System.out.println(staticVar);
        // System.out.println(localVar); // COMPILE ERROR: variable localVar might not have been initialized
    }
}
```
Local variables (method scope) do not receive default values in Java and must be explicitly initialized before use. Instance and static variables receive defaults (e.g., `0` for numeric, `null` for references).

## Solution: final-references - Final References vs Mutation

```java
class Person {
    int age;
}

public class Main {
    public static void main(String[] args) {
        final Person p = new Person();
        
        // p = new Person(); // COMPILE ERROR: cannot assign a value to final variable p
        
        p.age = 30; // Works fine! The object's internal state mutated.
        System.out.println(p.age);
    }
}
```
The `final` keyword only locks the **reference** (the pointer). It prevents you from pointing the variable to a new location in memory. It does **not** protect the object's internal state from mutation.

## Solution: file-structure - Top-Level Modifiers

```java
// In App.java
// private class HiddenApp {} // ERROR: modifier private not allowed here

public class App {
    private class HiddenNestedApp {} // valid
}
```

Java prevents top-level classes from being `private` because `private` means visible only to the enclosing scope. A top-level class has no enclosing class. `private` classes are only valid when nested inside another class.
