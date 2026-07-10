---
order: 50
---

# Immutable Objects

An immutable object is created once and never changes visible state. This makes it safer as a map key, easier to share across threads, and easier to reason about in service code.

## `final` class is not enough

`final` on a class only prevents inheritance. It does not freeze fields.

```java
final class Employee {
    int age;
}

Employee e = new Employee();
e.age = 50; // still mutable
```

## Standard immutable class shape

```java
public final class EmployeeId {
    private final String value;

    public EmployeeId(String value) {
        this.value = Objects.requireNonNull(value);
    }

    public String value() {
        return value;
    }
}
```

Rules:

- make the class `final`, or carefully prevent unsafe subclassing
- make fields `private final`
- initialize all state in the constructor
- expose no setters
- defensively copy mutable inputs and outputs

## Defensive copying

`final` protects the field reference, not the object behind it.

```java
public final class Report {
    private final List<String> rows;

    public Report(List<String> rows) {
        this.rows = List.copyOf(rows);
    }

    public List<String> rows() {
        return rows;
    }
}
```

Without `List.copyOf`, the caller could mutate the original list after construction and change the supposedly immutable object.

## Why immutability matters for hash keys

Hash-based collections depend on stable `equals()` and `hashCode()`. If a key's identity fields change after insertion, lookup searches the wrong bucket.

Immutable value objects avoid that entire class of bug.

## Records

Records are a concise way to model shallowly immutable value carriers. They generate constructor, accessors, `equals()`, `hashCode()`, and `toString()`. Use the records chapter for compact constructors, validation, and DTO trade-offs.

## Quick recall

**Q. Does `final class` mean immutable?**
A. No. It only prevents subclassing.

**Q. What makes an object immutable?**
A. No visible state changes after construction: private final fields, no setters, and defensive copies for mutable data.

**Q. Why are immutable objects good HashMap keys?**
A. Their hash/equality fields cannot change after insertion.

**Q. Are records deeply immutable?**
A. No. Record fields are final references, but mutable referenced objects can still mutate unless copied.
