---
order: 20
search: false
---

# PriorityQueue Comparator Solutions

## Solution: string-length-priority - String Length

```java
PriorityQueue<String> pq = new PriorityQueue<>(
    Comparator.comparingInt(String::length).thenComparing(Comparator.naturalOrder()));
```

## Solution: student-by-cgpa - Student by CGPA

```java
record Student(int id, String name, double cgpa) {}

PriorityQueue<Student> pq = new PriorityQueue<>(
    Comparator.comparingDouble(Student::cgpa).reversed());
```

## Solution: comparator-tie-breaking - Comparator Tie-Breaking

```java
PriorityQueue<Student> pq = new PriorityQueue<>(
    Comparator.comparingDouble(Student::cgpa).reversed()
        .thenComparing(Student::name)
        .thenComparingInt(Student::id));
```

Put `reversed()` before tie-breakers so only CGPA is descending.

## Solution: task-scheduler-simulation - Task Scheduler Simulation

```java
record Task(int id, String name, int priority, long createdAt) {}

PriorityQueue<Task> pq = new PriorityQueue<>(
    Comparator.comparingInt(Task::priority).reversed()
        .thenComparingLong(Task::createdAt)
        .thenComparingInt(Task::id));
```
