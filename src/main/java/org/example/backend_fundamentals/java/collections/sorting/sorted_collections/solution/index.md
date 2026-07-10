---
order: 20
search: false
---

# Solutions

## Solution: sorted-collections-with-comparator - TreeSet and PriorityQueue ordering

```java
Set<Student> sortedByName = new TreeSet<>(Comparator.comparing(Student::name));
sortedByName.addAll(students);

Queue<Student> queue = new PriorityQueue<>(
    Comparator.comparingDouble(Student::cgpa).reversed()
);
queue.addAll(students);
```

The collection-level comparator overrides any natural order for that collection.

## Solution: treemap-by-student-id - TreeMap ordered by Student id

```java
record Student(int id, String name, double cgpa) implements Comparable<Student> {
    @Override
    public int compareTo(Student other) {
        return Integer.compare(this.id, other.id);
    }
}

Map<Student, Integer> marks = new TreeMap<>();
marks.put(new Student(3, "Chris", 9.0), 88);
marks.put(new Student(1, "Alice", 9.1), 95);
marks.put(new Student(2, "Bob", 8.7), 91);
```

The map iterates keys in id order.

## Solution: treeset-compareto-zero-trap - TreeSet duplicate trap

```java
record Student(int id, String name) implements Comparable<Student> {
    @Override
    public int compareTo(Student other) {
        return Integer.compare(this.id, other.id);
    }
}

Set<Student> set = new TreeSet<>();
set.add(new Student(1, "Alice"));
set.add(new Student(1, "Bob"));
System.out.println(set.size()); // 1
```

`TreeSet` treats comparison result `0` as duplicate. It does not call `equals()` to decide whether both names should remain.
