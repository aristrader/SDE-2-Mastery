---
order: 20
---

# Comparator

`Comparator<T>` defines an external order. It stays outside the class and can change per use case.

```java
Comparator<Student> byName = Comparator.comparing(Student::name);
Comparator<Student> byCgpaDesc = Comparator.comparingDouble(Student::cgpa).reversed();

students.sort(byName);
students.sort(byCgpaDesc);
```

Same comparators without method references:

```java
Comparator<Student> byName = Comparator.comparing(student -> student.name());
Comparator<Student> byCgpaDesc = Comparator
    .comparingDouble(student -> student.cgpa())
    .reversed();
```

Verbose anonymous-class form:

```java
Comparator<Student> byCgpaDescVerbose = new Comparator<>() {
    @Override
    public int compare(Student left, Student right) {
        return Double.compare(right.cgpa(), left.cgpa());
    }
};
```

Use `Comparator` when the same class needs multiple valid orderings: by id, by name, by CGPA, by age, or chained business rules.

## Chaining

```java
Comparator<Student> order =
    Comparator.comparingDouble(Student::cgpa).reversed()
        .thenComparing(Student::name)
        .thenComparingInt(Student::id);
```

## Quick recall

- **Many valid orderings?** `Comparator`.
- **Cannot modify the class?** `Comparator`.
- **Tie-breakers?** `thenComparing`.
- **Primitive field helper?** `comparingInt`, `comparingLong`, `comparingDouble`.
