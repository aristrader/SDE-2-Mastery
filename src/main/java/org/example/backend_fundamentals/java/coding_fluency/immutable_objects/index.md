---
order: 70
---

# Immutable Objects

An immutable object has no visible state changes after construction. That makes it safe as a map key, safe to share between threads, and easier to reason about in service code.

Immutability is about observable behavior, not one keyword.

## `final` is not enough

`final` on a class prevents inheritance. It does not freeze fields.

```java
final class Employee {
    int age;
}

Employee employee = new Employee();
employee.age = 50; // still mutable
```

`final` on a field prevents reassigning the reference. It does not freeze the referenced object.

```java
private final List<String> roles;
```

The list can still be mutated unless you copy or wrap it correctly.

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

- make the class `final`, or carefully control subclassing
- make fields `private final`
- initialize all state in the constructor
- expose no setters
- defensively copy mutable inputs and outputs
- do not leak `this` from the constructor

## Defensive copying

Copy mutable inputs on the way in.

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

If you store the caller's list directly, the caller can mutate your object after construction.

For mutable types without immutable factory methods, copy on both input and output.

```java
public final class TokenWindow {
    private final Date expiresAt;

    public TokenWindow(Date expiresAt) {
        this.expiresAt = new Date(expiresAt.getTime());
    }

    public Date expiresAt() {
        return new Date(expiresAt.getTime());
    }
}
```

## Hash keys and concurrency

Hash-based collections depend on stable `equals()` and `hashCode()`. If a key's identity fields change after insertion, lookup can search the wrong bucket.

Immutable objects also reduce concurrency risk because readers cannot observe partial business-state changes after construction.

## Shallow vs deep immutability

Records and `final` fields are shallowly immutable. The reference cannot change, but the object behind it may still be mutable.

```java
record UserSnapshot(List<String> roles) {}
```

This is not deeply immutable unless the constructor copies the list.

```java
record UserSnapshot(List<String> roles) {
    UserSnapshot {
        roles = List.copyOf(roles);
    }
}
```

## Quick recall

- **Does `final class` mean immutable?** No. It only prevents subclassing.
- **Does `final List<T>` mean immutable list?** No. The reference is final, not the list contents.
- **Core immutable shape?** Private final fields, constructor initialization, no setters, defensive copies.
- **Why good as `HashMap` keys?** Equality/hash fields cannot change after insertion.
- **Records deeply immutable?** No. Copy mutable components.
- **Mutable input rule?** Copy it before storing.
