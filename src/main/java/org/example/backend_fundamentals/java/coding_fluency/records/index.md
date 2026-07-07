---
order: 30
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
