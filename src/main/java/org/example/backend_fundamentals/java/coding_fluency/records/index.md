---
order: 60
---

# Records

Records are Java's concise syntax for immutable data carriers. They are a good fit for DTOs, API responses, event payloads, and small value snapshots.

```java
public record UserDto(String id, String email, boolean active) {}
```

The compiler generates:

- a canonical constructor
- accessors named `id()`, `email()`, `active()`
- `equals()`
- `hashCode()`
- `toString()`

Records are not just "classes with getters"; equality and the public API are based on the record components.

## Record components

Record fields are private and final. Accessors are named after the component, not JavaBean-style `getX`.

```java
UserDto user = new UserDto("u1", "a@x.com", true);

String email = user.email(); // not getEmail()
```

This matters with frameworks and libraries that expect JavaBean naming.

## Compact constructors

Use a compact constructor for validation or normalization.

```java
public record Money(BigDecimal amount, String currency) {
    public Money {
        amount = amount.setScale(2, RoundingMode.HALF_UP);
        currency = currency.toUpperCase(Locale.ROOT);

        if (amount.signum() < 0) {
            throw new IllegalArgumentException("amount must be non-negative");
        }
    }
}
```

In a compact constructor, assign normalized values to the parameter name. Do not write `this.currency = ...`; record fields are assigned after the compact constructor body.

## Limits

Records are implicitly `final` and extend `java.lang.Record`, so they cannot extend another class. They can implement interfaces.

```java
public record CustomerId(String value) implements Comparable<CustomerId> {
    @Override
    public int compareTo(CustomerId other) {
        return value.compareTo(other.value);
    }
}
```

Records are shallowly immutable. If a component is a mutable object, copy it.

```java
public record Report(List<String> rows) {
    public Report {
        rows = List.copyOf(rows);
    }
}
```

## Records vs Lombok

Prefer records when:

- the type is a small immutable carrier
- all fields are part of equality
- construction is simple
- Java 16+ is available

Prefer Lombok or a normal class when:

- you need builders or `toBuilder`
- you need inheritance
- not every field belongs in equality
- framework constraints need JavaBean setters or no-arg constructors

## Jackson and Spring

Modern Jackson supports records well. In older stacks, record deserialization may need parameter-name support or explicit annotations. Check the actual Spring Boot/Jackson version before adding annotations.

For Spring Boot 2.7+ and Jackson 2.12+, simple records normally deserialize without extra configuration.

## Quick recall

- **Generated methods?** Canonical constructor, component accessors, `equals`, `hashCode`, `toString`.
- **Accessor style?** `email()`, not `getEmail()`.
- **Compact constructor syntax?** `public RecordName { ... }`.
- **Can records extend classes?** No, they already extend `java.lang.Record`.
- **Deeply immutable?** No. Copy mutable components.
- **Best fit?** Small immutable DTO/value carrier where all components define identity.
