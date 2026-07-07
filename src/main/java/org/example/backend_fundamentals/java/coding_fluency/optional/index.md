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


## Domain model

```java
record Address(String street, String city, String country) {}

record User(String id, String email, boolean active, Address address) {}

// Simulated repo / service methods you will call in exercises
Optional<User> findUserById(String id);          // may return empty
Optional<Address> getShippingAddress(User user); // may return empty
```


## Common Gotchas

- Every `.map()` receives the unwrapped value from the previous step. A chain like `Optional.ofNullable(user).map(User::address).map(Address::city).orElse("Unknown")` stays flat because each mapper returns a plain value.
- The argument to `orElse()` is evaluated before `orElse` is called. `orElseGet()` runs its supplier only when the Optional is empty, which matters when the fallback does I/O or logs a DB call.
- `.filter()` returns an empty Optional if the predicate is false — it does not throw. A present-but-inactive user simply skips the downstream `ifPresent` block.
- `.map(f)` wraps `f`'s return value in another Optional. If `f` already returns `Optional<Address>`, use `.flatMap(f)` or you get `Optional<Optional<Address>>`.
- `ifPresent()` is present-case only. Use `ifPresentOrElse(...)` when the absent case should log or print `"User not found"`.
- `ifPresentOrElse` takes a `Consumer<T>` for the present case and a `Runnable` for the absent case — not a `Supplier`. The absent branch is side-effect only. If you need a value back, use `map(...).orElse(...)` instead.
- No-arg `orElseThrow()` (Java 10+) throws `NoSuchElementException` with a generic message — useless in a log file. Always supply a meaningful exception supplier in production.
