---
order: 10
---

# Stream Foundations

Streams are processing pipelines, not storage.

A collection answers: what data do I have?

A stream answers: how should this data be processed?

```java
List<Employee> employees = ...

List<String> names = employees.stream()
        .filter(employee -> employee.salary() > 100_000)
        .map(Employee::name)
        .toList();
```

The source collection still stores the data. The stream describes the processing path.

## Why streams exist

Before streams, collection processing repeated the same shape:

```java
List<String> result = new ArrayList<>();
for (Employee employee : employees) {
    if (employee.salary() > 100_000) {
        result.add(employee.name());
    }
}
```

Streams let code describe the transformation:

```text
source -> filter -> map -> collect
```

This is closer to SQL: describe what you want, not every loop step.

## Lifecycle

Every stream has:

1. one source
2. zero or more intermediate operations
3. one terminal operation

Intermediate operations build the pipeline:

```java
filter()
map()
sorted()
distinct()
limit()
skip()
peek()
```

Terminal operations trigger execution:

```java
toList()
collect()
count()
reduce()
forEach()
findFirst()
anyMatch()
```

Without a terminal operation, nothing runs.

```java
employees.stream()
        .filter(employee -> {
            System.out.println(employee.name());
            return true;
        });
```

This prints nothing because the pipeline is never consumed.

## Lazy evaluation

Intermediate operations are lazy. They do not eagerly create a new list at every step.

```java
employees.stream()
        .filter(...)
        .map(...)
        .sorted(); // still no execution
```

Execution starts when a terminal operation appears:

```java
employees.stream()
        .filter(...)
        .map(...)
        .sorted()
        .toList();
```

## Pipeline fusion

Streams usually process one element through the whole pipeline before moving to the next element. They do not normally traverse once for `filter`, once for `map`, and once for `count`.

```text
employee 1 -> filter -> map -> terminal
employee 2 -> filter -> map -> terminal
employee 3 -> filter -> map -> terminal
```

This avoids many temporary collections and extra loops.

## Streams are single-use

A stream models one traversal.

```java
Stream<Employee> stream = employees.stream();
stream.count();
stream.count(); // IllegalStateException
```

Need another traversal? Create another stream:

```java
employees.stream().count();
employees.stream().findFirst();
```

This is especially natural for file streams such as `Files.lines(path)`: once the file cursor reaches the end, the same stream cannot restart.

## Source is not modified

Streams do not mutate the source collection just because an operation sounds mutating.

```java
List<Integer> numbers = List.of(5, 2, 1, 4);
numbers.stream().sorted().toList();
```

`numbers` remains unchanged. The sorted result is a new result.

## Quick recall

- **Is a stream a collection?** No. A collection stores; a stream processes.
- **Do intermediate operations run immediately?** No, they are lazy.
- **What starts execution?** A terminal operation.
- **How many times is the source usually traversed?** Usually once, with fused pipeline stages.
- **Can a stream be reused?** No. Create a new stream.
- **Does `sorted()` mutate the source list?** No.
