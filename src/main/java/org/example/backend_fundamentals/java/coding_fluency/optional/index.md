---
order: 20
---

# Optional

## Why this matters
Null pointer exceptions are the most common runtime crash in Java services. Optional makes the
absence-of-value case explicit at the type level, eliminating whole classes of NPEs in repository
returns, service lookups, and API payloads. On a KYC platform with missing user data and unresolved
verification records, Optional is a daily tool.

## Quick recall

**Q.** What is the difference between `Optional.of(x)` and `Optional.ofNullable(x)`?  
**A.** `of(x)` throws `NullPointerException` immediately if x is null; `ofNullable(x)` wraps null as an empty Optional.

**Q.** Why is `orElse(expensiveCall())` dangerous?  
**A.** The argument is always evaluated before `orElse` is called — even when the Optional is non-empty. Use `orElseGet(() -> expensiveCall())` for lazy evaluation.

**Q.** When do you need `flatMap` instead of `map`?  
**A.** When the mapping function itself returns an `Optional` — `map` would produce `Optional<Optional<T>>`; `flatMap` flattens it to `Optional<T>`.

**Q.** Why should you not use `Optional` as a method parameter, entity field, or `@RequestParam`?
**A.** `Optional` is not `Serializable` and is not designed for those roles — use plain nullable types. As a method parameter it forces callers to wrap values unnecessarily and signals a design smell.

**Q.** What does `filter()` return when the predicate is false?  
**A.** An empty Optional — it never throws. The downstream `ifPresent` or `orElse` then handles the absent case.

**Q.** What does `ifPresentOrElse` do that `ifPresent` does not?
**A.** It accepts a second `Runnable` for the empty case, so you can handle both branches in one call without a separate `else` block. (Java 9+)

**Q.** What is the difference between `orElseThrow()` and `orElseThrow(supplier)`?
**A.** The no-arg form (Java 10+) throws `NoSuchElementException` with a generic message. The supplier form throws whatever exception you provide — always prefer the supplier form in production so the exception message is actionable.
