---
order: 40
---

# Records as DTOs

## Why this matters
Records (Java 16+) are the language-native immutable data carrier. They eliminate the boilerplate
of a Lombok `@Value` class with zero annotation-processing dependencies. On a KYC platform, API
response bodies and event payloads are natural candidates — created once, read many times, never
mutated. Knowing where records stop and where Lombok picks up (toBuilder, Jackson quirks,
inheritance limits) determines when to reach for which tool.

## Quick recall

**Q.** What methods does a record auto-generate?  
**A.** A public all-args canonical constructor, a getter for each component (named after the field, not `getX`), `equals()`, `hashCode()`, and `toString()`.

**Q.** What is a compact constructor and where does it live?  
**A.** A constructor body written as `RecordName { ... }` with no parameter list — it runs before the canonical constructor assigns fields, used for validation or normalization.

**Q.** Are records final? Can they be subclassed?  
**A.** Yes, records are implicitly `final`. They cannot be extended by any class.

**Q.** Why can a record not extend another class?  
**A.** Records implicitly extend `java.lang.Record`, and Java does not support multiple inheritance of classes. They can, however, implement any number of interfaces.

**Q.** What is the minimal Jackson fix when record deserialization fails?  
**A.** On Spring Boot 2.7+ / Jackson 2.12+ no fix is needed — built-in record support is included. On older versions, add `jackson-module-parameter-names` with `-parameters` compiler flag, or annotate the canonical constructor with `@JsonCreator` + `@JsonProperty`.

**Q.** When should you prefer Lombok `@Value` + `@Builder` over a record?  
**A.** When you need frequent partial-copy patterns (`toBuilder()`), Jackson deserialization without extra config, or you are on Java < 16. Prefer records for simplicity when the DTO is small, immutable, and Jackson config is already in place.


## Domain model

```java
import java.math.BigDecimal;

// You will define this as a record and experiment with its constraints.
// Records are implicitly final — they cannot be subclassed.
record TransactionDto(
    String id,
    BigDecimal amount,
    String currency   // ISO-4217, e.g. "USD", "IDR"
) {}

// A user identity snapshot used in KYC checks
record KycSubjectDto(
    String userId,
    String fullName,
    String nationalId
) {}
```


## Common Gotchas

- Jackson 2.11 and earlier cannot map JSON keys to record constructor parameters by name without the `jackson-module-parameter-names` module or a `@JsonCreator`-annotated constructor. Jackson 2.12+ (Spring Boot 2.7+) handles records out of the box — check your version before adding anything.
- The compact constructor does NOT redeclare parameters — write `TransactionDto { ... }`, not `TransactionDto(String id, ...) { ... }`.
- To normalize a record component in a compact constructor, assign to the parameter name, for example `currency = currency.toUpperCase();`. Writing `this.currency = ...` is a compile error because record fields are final and assigned after the compact constructor body.
- Records implicitly extend `java.lang.Record` and cannot extend any other class. Use records for small immutable DTOs, not for hierarchies.
- There is no language shortcut for partial copies. Each `withX` method must name every other field explicitly; this becomes unpleasant on large records and is where Lombok `toBuilder()` may be more practical.
