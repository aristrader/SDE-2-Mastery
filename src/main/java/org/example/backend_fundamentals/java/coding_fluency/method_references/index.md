---
order: 10
---

# Method References

## Why this matters
Method references are the idiomatic replacement for single-method lambdas — less noise, more explicit intent.
Interviewers ask you to rewrite a lambda on the spot; fumbling bound vs unbound signals you learned the syntax but not the semantics.
Constructor references come up in stream-to-collection pipelines and DTO mapping — daily patterns in service code.

## Quick recall

**Q.** What is the difference between a bound and an unbound instance method reference?
**A.** Bound: a specific object instance is captured (`instance::method`) — the reference acts as a zero-arg (or n-arg) function where the receiver is fixed. Unbound: the receiver is the first argument supplied at invocation (`ClassName::method`) — used in `.map()` where the stream element is the receiver.

**Q.** When can you NOT replace a lambda with a method reference?
**A.** When the lambda does more than delegate to a single method call — e.g., two chained calls (`o.status().name()`), arithmetic, conditional logic, or multiple arguments beyond what the method expects.

**Q.** What functional interface does `ClassName::new` satisfy?
**A.** Whichever interface matches the constructor's parameter list. A no-arg constructor satisfies `Supplier<T>`; a single-arg constructor satisfies `Function<A, T>` or `UnaryOperator<T>`; a two-arg constructor satisfies `BiFunction<A, B, T>`.

**Q.** Why is `System.out::println` a bound reference and not a static one?
**A.** `println` is an instance method on `PrintStream`; `System.out` is the specific `PrintStream` instance that gets captured. Static references point to methods that don't require an instance at all (e.g., `Integer::parseInt`).

**Q.** What happens if you write `OrderDto::new` but `OrderDto` has no constructor matching the stream element type?
**A.** Compile error — the constructor reference cannot be resolved to a matching functional interface. The error points to the reference site, not inside `OrderDto`.

**Q.** How does `Comparator.comparing(Order::total)` work and which method reference form is it?
**A.** It uses an unbound instance reference: `Order::total` is the key extractor — Java calls `total()` on whichever `Order` it receives. Chain `.reversed()` for descending, `.thenComparing(...)` for secondary sort keys.
