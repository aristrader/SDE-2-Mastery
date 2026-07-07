---
order: 10
search: false
---

# Optional Practice

## Domain model

```java
record Address(String street, String city, String country) {}
record User(String id, String email, boolean active, Address address) {}

Optional<User> findUserById(String id);
Optional<Address> getShippingAddress(User user);
```

## Exercise: replace-null-check - Replace a null-check chain

### Goal
Turn a nested null-guard tower into a single Optional chain.

### Task
Start with this imperative block:
```java
String city = "Unknown";
User user = findUserById(id).orElse(null);
if (user != null) {
    Address address = user.address();
    if (address != null && address.city() != null) {
        city = address.city();
    }
}
```
Rewrite it as a single expression using `Optional.ofNullable()`, two `.map()` calls, and `.orElse("Unknown")`.
Then make the same call return `"CITY UNKNOWN"` in upper-case — add a `.map(String::toUpperCase)` in the chain.

### Gotcha
Every `.map()` receives the unwrapped value from the previous step. The mapper should take `Address`, not `Optional<Address>`; mixing that up creates `Optional<Optional<Address>>`.

## Exercise: orelse-vs-orelseget - orElse vs orElseGet eager vs lazy

### Goal
See firsthand that `orElse()` always evaluates its argument; `orElseGet()` does not.

### Task
Write a method `expensiveDefault()` that prints `"[DB CALL]"` and returns a fallback `User`.
Then write two calls using `.orElse(expensiveDefault())` and `.orElseGet(() -> expensiveDefault())`.
Pass `"existing-id"` mapped to a real user in a local `Map<String, User>`. Run both. Observe that the `orElse(...)` call prints `"[DB CALL]"` even though the user was found; `orElseGet(...)` does not.

### Gotcha
The argument to `orElse()` is evaluated before `orElse` is called. The lambda inside `orElseGet()` runs only when the Optional is empty.

## Exercise: filter-ifpresent - filter + ifPresent

### Goal
Chain filtering and conditional action without unpacking the Optional manually.

### Task
Given an `Optional<User>` returned by `findUserById()`, write a chain that filters to only active users and, if still present, prints `"Sending email to: " + user.email()`.
Do not use `isPresent()` + `get()`. Use `.filter()` + `.ifPresent()` only.
Then extend it: use `.map(User::email).ifPresent(email -> sendVerificationEmail(email))` — same outcome, different shape. Decide which reads more clearly here.

### Gotcha
`.filter()` returns an empty Optional if the predicate is false — it does not throw. A present-but-inactive user simply skips the `ifPresent` block.

## Exercise: flatmap-chained - flatMap for chained Optionals

### Goal
Avoid `Optional<Optional<Address>>` by using `flatMap` when the mapping function itself returns an Optional.

### Task
Attempt 1 — use `.map()` for both `findUserById` and `getShippingAddress` steps. Note the return type you get.
Attempt 2 — replace the second `.map()` with `.flatMap()`. Note the return type now.
Finally, add `.map(Address::city).orElse("No city on file")`.

### Gotcha
`.map(f)` wraps `f`'s return value in another Optional. `flatMap` unwraps that one level.

## Exercise: ifpresentorelse - ifPresentOrElse branch without unpacking

### Goal
Handle present and absent cases in a single call.

### Task
Using `findUserById()`, first write the imperative `isPresent()` + `get()` version. Then rewrite it as `.ifPresentOrElse(...)`: print `"Found: " + user.email()` if present, or `"User not found"` if absent.

### Gotcha
`ifPresentOrElse` takes a `Consumer<T>` for the present case and a `Runnable` for the absent case — not a `Supplier`.

## Exercise: orelsethrow-mandatory - orElseThrow for mandatory lookups

### Goal
Replace a manual null-check-and-throw pattern with `orElseThrow`.

### Task
Rewrite the manual pattern using `.orElseThrow(() -> new UserNotFoundException("User not found: " + id))`.
Then try `orElseThrow()` with no argument — what does it throw?

### Gotcha
No-arg `orElseThrow()` throws `NoSuchElementException` with a generic message — useless in a log file. Always supply a meaningful exception supplier.
