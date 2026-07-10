---
order: 10
---

# Comparable

`Comparable<T>` defines the class's natural order. The comparison lives inside the class.

```java
final class Student implements Comparable<Student> {
    private final int id;

    @Override
    public int compareTo(Student other) {
        return Integer.compare(this.id, other.id);
    }
}
```

After that, `Collections.sort(students)`, `students.sort(null)`, `TreeSet<Student>`, and `TreeMap<Student, ...>` can use that natural order.

Use `Comparable` only when one ordering is clearly the default identity of the type.

## Return contract

| Return value | Meaning |
| --- | --- |
| negative | left value comes before right value |
| zero | values are equal for sorting |
| positive | left value comes after right value |

Never subtract to compare numbers:

```java
return this.id - other.id; // can overflow
```

Prefer:

```java
return Integer.compare(this.id, other.id);
```

## Quick recall

- **Natural order lives where?** Inside the class.
- **How many natural orders can one class have?** One.
- **Safer numeric comparison?** `Integer.compare`, `Long.compare`, `Double.compare`.
