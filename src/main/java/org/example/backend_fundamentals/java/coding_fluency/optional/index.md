---
order: 40
---

# Optional

`Optional<T>` represents a value that may be absent. Use it mainly as a return type when absence is a normal outcome, not as a replacement for every nullable reference in Java.

```java
Optional<User> user = userRepository.findById(id);
```

This forces the caller to choose how absence is handled instead of discovering it later through a `NullPointerException`.

## Creating optionals

Use `of` when null is a bug. Use `ofNullable` when null is allowed input.

```java
Optional<String> required = Optional.of(email);        // throws if null
Optional<String> optional = Optional.ofNullable(email); // empty if null
```

Do not write `Optional.ofNullable(x).get()`. That only moves the crash one line later.

## Transforming values

`map` transforms the present value. Empty stays empty.

```java
String city = Optional.ofNullable(user)
    .map(User::address)
    .map(Address::city)
    .orElse("Unknown");
```

Use `flatMap` when the mapper already returns an `Optional`.

```java
Optional<Address> address = findUser(id)
    .flatMap(this::findShippingAddress);
```

Using `map` here would produce `Optional<Optional<Address>>`.

## Filtering and consuming

`filter` keeps a present value only if the predicate is true.

```java
Optional<User> activeUser = findUser(id)
    .filter(User::active);
```

Use `ifPresent` for present-case side effects and `ifPresentOrElse` when both branches are side effects.

```java
findUser(id).ifPresentOrElse(
    user -> auditLogin(user.id()),
    () -> auditMissingUser(id));
```

If you need a value back, use `map`, `orElseGet`, or `orElseThrow`, not `ifPresent`.

## Fallbacks and exceptions

`orElse` evaluates its argument eagerly. `orElseGet` evaluates lazily only when empty.

```java
User user = findUser(id).orElseGet(() -> createGuestUser(id));
```

For required values, prefer an exception with context.

```java
User user = findUser(id)
    .orElseThrow(() -> new IllegalArgumentException("Unknown user: " + id));
```

The no-arg `orElseThrow()` throws `NoSuchElementException` with little context.

## Where not to use Optional

Avoid `Optional` in fields, DTO properties, JPA entities, and method parameters. These cases usually create framework friction and force callers to wrap values. Use nullable fields plus validation/serialization rules there.

Good fit:

```java
Optional<User> findUserById(String id)
```

Poor fit:

```java
void updateEmail(Optional<String> email)
```

Prefer overloads or a nullable argument with clear method documentation when a parameter may be absent.

## Common traps

- `Optional` does not eliminate null everywhere; it makes absence explicit at selected API boundaries.
- `orElse(expensiveCall())` runs the expensive call even when the optional is present.
- `map` wraps mapper results; use `flatMap` for mappers returning `Optional`.
- `Optional.get()` is almost always the wrong API in application code.
- `Optional` is not a collection. Do not use it for multiple values.

## Quick recall

- **`of` vs `ofNullable`?** `of` rejects null immediately; `ofNullable` converts null to empty.
- **Lazy fallback?** `orElseGet`.
- **Mapper returns Optional?** Use `flatMap`.
- **Predicate false in `filter`?** The result becomes empty.
- **Production exception?** Prefer `orElseThrow(() -> new MeaningfulException(...))`.
- **Optional as field/parameter?** Usually avoid.
