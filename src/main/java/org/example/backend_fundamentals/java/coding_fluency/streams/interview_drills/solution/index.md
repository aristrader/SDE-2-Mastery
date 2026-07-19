---
order: 20
search: false
---

# Stream Interview Drills Solutions

## Solution: output-prediction - Predict Stream Output

| Case | Output |
| --- | --- |
| 1 | `[20, 40]` |
| 2 | `[a, b, c]` |
| 3 | `[3, 4]` |
| 4 | `[1, 2, 3, 4]` |
| 5 | `true` |
| 6 | `false` |

## Solution: find-stream-bugs - Find the Bug

1. Two-argument `toMap` throws `IllegalStateException` on duplicate IDs. Add a merge function.
2. A stream is single-use. Create a new stream for the second traversal.
3. `findFirst()` returns `Optional`; `.get()` can throw. Use `orElseThrow`, `orElse`, or handle absence.
4. `parallelStream().forEach(list::add)` mutates shared state from multiple threads. Collect the result instead.
5. `peek` should not perform business mutation. Use `map` for transformation or a loop for explicit mutation.

## Solution: integrated-review - Integrated Stream Review

```java
Map<String, List<String>> topActiveNamesByDepartment = employees.stream()
    .filter(Employee::active)
    .collect(Collectors.groupingBy(
        Employee::department,
        Collectors.collectingAndThen(
            Collectors.toList(),
            list -> list.stream()
                .sorted(Comparator.comparingDouble(Employee::salary).reversed())
                .limit(3)
                .map(Employee::name)
                .toList())));

Map<String, Set<String>> uniqueSkillsByDepartment = employees.stream()
    .collect(Collectors.groupingBy(
        Employee::department,
        Collectors.flatMapping(e -> e.skills().stream(), Collectors.toSet())));

List<String> topWords = sentences.stream()
    .flatMap(sentence -> Arrays.stream(sentence.toLowerCase().split("\\W+")))
    .filter(word -> !word.isBlank())
    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
    .entrySet().stream()
    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
    .limit(3)
    .map(Map.Entry::getKey)
    .toList();

List<Employee> javaAndSpring = employees.stream()
    .filter(e -> e.skills().contains("Java") && e.skills().contains("Spring"))
    .toList();
```
