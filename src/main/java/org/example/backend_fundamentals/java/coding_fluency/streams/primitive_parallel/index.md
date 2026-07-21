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

`parallelStream()` means: Java splits the stream into chunks, runs those chunks on multiple worker threads, and then combines the partial results. It is not a magic faster stream; it is useful only when the cost of splitting, scheduling, thread coordination, and merging is smaller than the work being parallelized.

It can help when all of these are true:

- large dataset
- CPU-bound work
- each element can be processed independently
- no shared mutable state
- the result can be combined safely
- order is not important, or preserving order is worth the cost

```java
long count = numbers.parallelStream()
    .filter(PrimeChecker::isPrime)
    .count();
```

This example is a reasonable candidate because primality checking is CPU-heavy and each number can be checked independently.

It is usually a bad fit for small lists, cheap operations, I/O-bound work, database calls, HTTP calls, or mutation of shared collections. For those cases, parallelism often adds overhead, hides latency problems, or overloads shared resources.

Bad mental model:

```java
// "Use all cores, so it must be faster."
employees.parallelStream()
    .map(this::callRemoteService)
    .toList();
```

Better mental model:

```java
// Parallel stream is for CPU work, not for blocking remote calls.
employees.stream()
    .map(this::callRemoteService)
    .toList();
```

For HTTP/database concurrency, use a deliberately chosen async/concurrency model with limits and timeouts. Do not hide it inside `parallelStream()`.

## Shared state trap

The most common parallel stream bug is mutating shared state from `forEach`.

```java
List<String> names = new ArrayList<>();

employees.parallelStream()
    .map(Employee::name)
    .forEach(names::add); // unsafe
```

This is unsafe because `parallelStream()` may run the lambda on multiple threads at the same time. All those threads are calling `names.add(...)` on the same `ArrayList`.

`ArrayList` is not thread-safe. Internally, adding an element roughly means:

1. Read the current size.
2. Put the new element at that index.
3. Increase the size.
4. Grow/copy the internal array if capacity is full.

If two threads do that together, they can interfere with each other:

- both threads may read the same old size and write to the same slot
- one write may overwrite another write
- `size` may be updated incorrectly
- resizing may happen while another thread is writing
- the final list may have missing names, wrong order, or occasional exceptions

The dangerous part is that it may appear to work during a small local run. Race conditions depend on timing, so the same code can pass 100 times and fail under load.

This is still not the right fix:

```java
List<String> names = Collections.synchronizedList(new ArrayList<>());

employees.parallelStream()
    .map(Employee::name)
    .forEach(names::add);
```

It avoids some corruption, but it keeps the wrong shape: many worker threads now fight over one lock, ordering is still not the normal stream encounter order, and the code is harder to reason about.

Collect instead:

```java
List<String> names = employees.parallelStream()
    .map(Employee::name)
    .toList();
```

This is safe because the stream framework owns the accumulation. Conceptually, each worker can build its own partial result, then the framework merges those partial results at the end. Your lambda stays pure: it maps one employee to one name and does not mutate shared state.

Same rule for counters:

```java
int[] count = {0};

employees.parallelStream()
    .filter(employee -> employee.salary() > 100_000)
    .forEach(employee -> count[0]++); // unsafe
```

Use a terminal operation instead:

```java
long highEarners = employees.parallelStream()
    .filter(employee -> employee.salary() > 100_000)
    .count();
```

Interview rule: in a parallel stream, avoid shared mutable variables outside the stream pipeline. Prefer `map`, `filter`, `reduce`, `count`, `toList`, and collectors.

## Ordering and thread pool

Parallel streams use the common ForkJoinPool by default. That means unrelated parallel stream work in the same JVM can compete for the same pool.

`forEach` does not preserve encounter order in parallel:

```java
employees.parallelStream()
    .map(Employee::name)
    .forEach(System.out::println); // order is not guaranteed
```

Use `forEachOrdered` only when order matters, and remember that preserving order can reduce the benefit of parallelism:

```java
employees.parallelStream()
    .map(Employee::name)
    .forEachOrdered(System.out::println);
```

If you need an ordered result, prefer collecting:

```java
List<String> names = employees.parallelStream()
    .map(Employee::name)
    .toList(); // keeps encounter order for ordered sources like List
```

## Performance rules

- Do not assume parallel is faster. Measure.
- Prefer readable sequential streams unless there is a real bottleneck.
- Prefer `mapToInt`/`mapToDouble` for numeric aggregation.
- Prefer reductions and collectors over shared mutation.
- Avoid mutation in `peek` and `forEach`.
- Avoid `parallelStream()` inside request handling unless you understand common-pool contention.
- Do not use it to parallelize blocking HTTP or database calls.

## Interview answer shape

If asked whether to use `parallelStream()`, answer with the decision conditions:

1. What is the dataset size?
2. Is the work CPU-bound or blocking?
3. Are elements independent?
4. Is there shared mutable state?
5. Does order matter?
6. Has it been measured?

The senior answer is usually not "parallel streams are bad." It is "they are narrow: useful for large independent CPU-bound transformations, risky for shared state and blocking work."

## Quick recall

- **Why primitive streams?** Avoid boxing and get numeric terminal operations.
- **Average salary API?** `mapToDouble(Employee::salary).average()`.
- **Parallel stream sweet spot?** Large independent CPU-bound work where combine cost is low.
- **Parallel stream bad for HTTP/DB calls?** Usually yes; it uses the common pool and can create contention.
- **Shared mutable list inside parallel `forEach`?** Unsafe.
- **Safe replacement for shared list mutation?** `map(...).toList()` or a collector.
- **Does parallel `forEach` preserve order?** No. Use `forEachOrdered` only when ordering is required.
- **Parallel streams always faster?** No. Measure.
