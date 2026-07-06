---
order: 20
---

# Optional — Coding Exercises

## Why this matters
Null pointer exceptions are the most common runtime crash in Java services. Optional makes the
absence-of-value case explicit at the type level, eliminating whole classes of NPEs in repository
returns, service lookups, and API payloads. On a KYC platform with missing user data and unresolved
verification records, Optional is a daily tool.

## Domain model

```java
record Address(String street, String city, String country) {}

record User(String id, String email, boolean active, Address address) {}

// Simulated repo / service methods you will call in exercises
Optional<User> findUserById(String id);          // may return empty
Optional<Address> getShippingAddress(User user); // may return empty
```

## Exercise 1: Replace a null-check chain (~10 min)

**Goal:** Turn a nested null-guard tower into a single Optional chain.

**Task:**  
Start with this imperative block:

```java
String city = "Unknown";
User user = findUserById(id);
if (user != null) {
    Address address = user.address();
    if (address != null) {
        if (address.city() != null) {
            city = address.city();
        }
    }
}
```

Rewrite it as a single expression using `Optional.ofNullable()`, two `.map()` calls, and `.orElse("Unknown")`.  
Then make the same call return `"CITY UNKNOWN"` in upper-case — add a `.map(String::toUpperCase)` in the chain.

**Gotcha:** Every `.map()` receives the unwrapped value from the previous step — the second `.map()`
takes `Address`, not `Optional<Address>`. Mix that up and you get `Optional<Optional<Address>>`.

---

## Exercise 2: orElse vs orElseGet — eager vs lazy (~10 min)

**Goal:** See firsthand that `orElse()` always evaluates its argument; `orElseGet()` does not.

**Task:**  
Write a method `expensiveDefault()` that prints `"[DB CALL]"` and returns a fallback `User`.  
Then write two calls:

```java
// Call A
User a = findUserById("existing-id").orElse(expensiveDefault());

// Call B
User b = findUserById("existing-id").orElseGet(() -> expensiveDefault());
```

Pass `"existing-id"` mapped to a real user in a local `Map<String, User>`. Run both. Observe that
Call A prints `"[DB CALL]"` even though the user was found; Call B does not.

**Gotcha:** The argument to `orElse()` is evaluated before `orElse` is called. The lambda inside
`orElseGet()` runs only when the Optional is empty. This matters when the fallback does I/O, sends
metrics, or has any side effect.

---

## Exercise 3: filter + ifPresent (~10 min)

**Goal:** Chain filtering and conditional action without unpacking the Optional manually.

**Task:**  
Given an `Optional<User>` returned by `findUserById()`, write a chain that:
1. Filters to only active users (`user.active() == true`).
2. If still present, prints `"Sending email to: " + user.email()`.

Do not use `isPresent()` + `get()`. Use `.filter()` + `.ifPresent()` only.

Then extend it: use `.map(User::email).ifPresent(email -> sendVerificationEmail(email))` — same outcome,
different shape. Decide which reads more clearly here.

**Gotcha:** `.filter()` returns an empty Optional if the predicate is false — it does not throw. A
present-but-inactive user simply skips the `ifPresent` block.

---

## Exercise 4: flatMap for chained Optionals (~15 min)

**Goal:** Avoid `Optional<Optional<Address>>` by using `flatMap` when the mapping function itself returns
an Optional.

**Task:**  
You have:
```java
Optional<User>    findUserById(String id);
Optional<Address> getShippingAddress(User user);
```

Attempt 1 — use `.map()` for both steps. Note the return type you get.  
Attempt 2 — replace the second `.map()` with `.flatMap()`. Note the return type now.  
Finally, add `.map(Address::city).orElse("No city on file")` to extract the city string.

**Gotcha:** `.map(f)` wraps `f`'s return value in another Optional. If `f` already returns
`Optional<Address>`, you get `Optional<Optional<Address>>`. `flatMap` unwraps that one level.

---

## Exercise 5: ifPresentOrElse — Java 9+ branch without unpacking (~5 min)

**Goal:** Handle present and absent cases in a single call instead of separate `ifPresent` + else block.

**Task:**  
Using `findUserById()`, write a chain that:
- If the user is present: prints `"Found: " + user.email()`.
- If the user is absent: prints `"User not found"`.

Write it first with `isPresent()` + `get()` (the imperative way), then rewrite as a single `.ifPresentOrElse(consumer, runnable)` call.

**Gotcha:** `ifPresentOrElse` takes a `Consumer<T>` for the present case and a `Runnable` for the absent case — not a `Supplier`. The absent branch is side-effect only. If you need a value back, use `map(...).orElse(...)` instead.

---

## Exercise 6: orElseThrow for mandatory lookups (~5 min)

**Goal:** Replace a manual null-check-and-throw pattern with `orElseThrow`.

**Task:**  
Start with:
```java
User user = findUserById(id);
if (user == null) {
    throw new UserNotFoundException("User not found: " + id);
}
```
(Note: `findUserById` now returns `Optional<User>`.)

Rewrite as a one-liner using `.orElseThrow()`. Use a supplier lambda so the exception message includes
the id: `orElseThrow(() -> new UserNotFoundException("User not found: " + id))`.

Then try `orElseThrow()` with no argument — what does it throw? When is that acceptable vs when do you
need the supplier form?

**Gotcha:** No-arg `orElseThrow()` (Java 10+) throws `NoSuchElementException` with a generic message — useless in a log file. Always supply a meaningful exception supplier in production.

---

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

