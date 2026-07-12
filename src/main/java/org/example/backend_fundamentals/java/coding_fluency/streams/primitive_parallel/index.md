---
order: 90
---

# Primitive and Parallel Streams

Primitive streams avoid boxing overhead for numeric pipelines. Parallel streams split work across threads, but only help in narrow cases.

## Primitive streams

Use `mapToInt`, `mapToLong`, or `mapToDouble` when the pipeline is numeric.

```java
int totalAge = employees.stream()
    .mapToInt(Employee::age)
    .sum();

double averageSalary = employees.stream()
    .mapToDouble(Employee::salary)
    .average()
    .orElse(0.0);
```

Primitive streams provide numeric operations directly:

- `sum()`
- `average()`
- `min()`
- `max()`
- `summaryStatistics()`

Use boxed streams when you need object APIs or collectors that expect boxed values.

## Parallel streams

`parallelStream()` can help when all of these are true:

- large dataset
- CPU-bound work
- independent elements
- no shared mutable state
- order is not important, or ordering cost is acceptable

```java
long count = numbers.parallelStream()
    .filter(PrimeChecker::isPrime)
    .count();
```

It is usually a bad fit for small lists, I/O-bound work, database calls, HTTP calls, or mutation of shared collections.

```java
List<String> names = new ArrayList<>();

employees.parallelStream()
    .map(Employee::name)
    .forEach(names::add); // unsafe
```

Collect instead:

```java
List<String> names = employees.parallelStream()
    .map(Employee::name)
    .toList();
```

## Ordering and thread pool

Parallel streams use the common ForkJoinPool by default. That means unrelated parallel stream work in the same JVM can compete for the same pool.

`forEach` does not preserve encounter order in parallel. Use `forEachOrdered` only when order matters, and remember that preserving order can reduce the benefit of parallelism.

## Performance rules

- Do not assume parallel is faster. Measure.
- Prefer readable sequential streams unless there is a real bottleneck.
- Prefer `mapToInt`/`mapToDouble` for numeric aggregation.
- Prefer short-circuiting terminal operations like `anyMatch`.
- Avoid mutation in `peek` and `forEach`.

## Quick recall

- **Why primitive streams?** Avoid boxing and get numeric terminal operations.
- **Average salary API?** `mapToDouble(Employee::salary).average()`.
- **Parallel stream sweet spot?** Large CPU-bound independent work.
- **Parallel stream bad for HTTP/DB calls?** Yes, it uses the common pool and can create contention.
- **Shared mutable list inside parallel `forEach`?** Unsafe.
- **Parallel streams always faster?** No. Measure.
