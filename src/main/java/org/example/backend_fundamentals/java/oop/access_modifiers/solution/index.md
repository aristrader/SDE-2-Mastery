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
The `final` keyword locks the **reference variable**. It prevents reassignment to another object; it does **not** protect the current object's internal state from mutation.

## Solution: file-structure - Top-Level Modifiers

```java
// In App.java
// private class HiddenApp {} // ERROR: modifier private not allowed here

public class App {
    private class HiddenNestedApp {} // valid
}
```

Java prevents top-level classes from being `private` because `private` means visible only to the enclosing scope. A top-level class has no enclosing class. `private` classes are only valid when nested inside another class.

## Solution: protected-cross-package - Extension Is Not Global Access

```java
// package ledger;
public class Account {
    protected long balance;
}

// package reporting;
class ReportAccount extends Account {
    long ownBalance() {
        return this.balance; // valid
    }

    long otherBalance(Account other) {
        // return other.balance; // compile error across packages
        return 0;
    }
}
```

Outside `ledger`, access occurs from subclass code and only through a qualifying subclass receiver. This prevents a subclass from treating every parent-typed object as an exposed record. A protected method such as `protected long availableBalance()` can preserve invariants better than a mutable field.
