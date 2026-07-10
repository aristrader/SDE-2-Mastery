---
order: 20
search: false
---

# Solutions

## Solution: unsorted-custom-object - Try sorting a custom object

Without `Comparable` or a `Comparator`, `Collections.sort(students)` cannot compile because `Student` has no known natural order.

## Solution: comparable-natural-order - Natural ordering with Comparable

```java
record Student(int id, String name, double cgpa) implements Comparable<Student> {
    @Override
    public int compareTo(Student other) {
        return Integer.compare(this.id, other.id);
    }
}

Collections.sort(students);
Set<Student> sortedById = new TreeSet<>(students);
```

Both use `Student.compareTo`, so both order by id. `Integer.compare` avoids overflow.
