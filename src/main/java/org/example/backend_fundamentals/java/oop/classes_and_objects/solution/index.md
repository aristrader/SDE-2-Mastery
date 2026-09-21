---
order: 20
search: false
---

# Solutions

## Solution: create-student-class - Create a Student Class

```java
class Student {
    private final String id;
    private final String name;
    private final int marks;

    Student(String id, String name, int marks) {
        this.id = id;
        this.name = name;
        this.marks = marks;
    }

    boolean hasPassed() {
        return marks >= 40;
    }

    void printSummary() {
        System.out.println(id + " " + name + " " + marks);
    }
}
```

Each `new Student(...)` call creates a separate object with its own field values. This implementation is immutable, so its aliases cannot change `marks`; in a mutable version, every alias of one object would observe its changed field, while another `Student` instance would not.

## Solution: constructor-invariant - Constructor Invariant

```java
Student(String id, String name, int marks) {
    if (marks < 0 || marks > 100) {
        throw new IllegalArgumentException("marks must be between 0 and 100");
    }
    this.id = id;
    this.name = name;
    this.marks = marks;
}
```

Constructors are the first place to enforce invariants required for a valid object.

## Solution: constructor-dispatch-trap - Do Not Call an Override During Construction

```java
class Parent {
    Parent() {
        describe();
    }

    void describe() { }
}

class Child extends Parent {
    private String label = "ready";

    @Override
    void describe() {
        System.out.println(label); // null
    }
}
```

`Parent()` runs before `Child` field initializers and the `Child` constructor body. Java still dynamically dispatches `describe()` to `Child`, so the override reads the default `null` value.

```java
class Parent {
    Parent(String label) {
        System.out.println(label);
    }
}

class Child extends Parent {
    Child() {
        super("ready");
    }
}
```

Pass required data to the parent constructor or call only private/final initialization helpers during construction. Run extension hooks only after the whole object is initialized.
