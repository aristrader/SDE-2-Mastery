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

Each `new Student(...)` call creates a separate object with its own field values.

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
