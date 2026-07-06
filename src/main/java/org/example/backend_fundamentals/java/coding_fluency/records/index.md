---
order: 30
---

# Records as DTOs — Coding Exercises

## Why this matters
Records (Java 16+) are the language-native immutable data carrier. They eliminate the boilerplate
of a Lombok `@Value` class with zero annotation-processing dependencies. On a KYC platform, API
response bodies and event payloads are natural candidates — created once, read many times, never
mutated. Knowing where records stop and where Lombok picks up (toBuilder, Jackson quirks,
inheritance limits) determines when to reach for which tool.

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

## Exercise 1: Basic record DTO + Jackson round-trip (~10 min)

**Goal:** Define `TransactionDto` as a record and verify its automatically generated contract.

**Task:**  
1. Declare the record. Create two instances with the same data. Confirm `equals()` returns `true`
   without writing a single method.
2. Call `toString()` on an instance — note the format.
3. Try to assign to a field (`dto.amount = new BigDecimal("200")`) — what does the compiler say?
4. Serialize `TransactionDto` to JSON using Jackson's `ObjectMapper`. Then deserialize it back.
   If deserialization fails, diagnose the error message and apply the minimal fix.

**Gotcha:** Jackson 2.11 and earlier cannot map JSON keys to record constructor parameters by name without the `jackson-module-parameter-names` module or a `@JsonCreator`-annotated constructor. Jackson 2.12+ (Spring Boot 2.7+) handles records out of the box — check your version before adding anything.

---

## Exercise 2: Compact constructor validation (~10 min)

**Goal:** Add input validation inside the compact constructor — the idiomatic record replacement
for a factory method or builder validation step.

**Task:**  
Add a compact constructor to `TransactionDto` that:
1. Throws `IllegalArgumentException` if `amount` is null or not positive (≤ 0).
2. Throws `IllegalArgumentException` if `currency` is not exactly 3 uppercase letters
   (hint: `currency.matches("[A-Z]{3}"`)).

Write a main method that:
- Creates a valid `TransactionDto` — should succeed.
- Attempts `amount = BigDecimal.ZERO` — should throw.
- Attempts `currency = "usd"` — should throw.

**Gotcha:** The compact constructor does NOT redeclare parameters — write `TransactionDto { ... }`
with no parameter list. To normalise a value (e.g., strip whitespace), assign to the *parameter name*
directly, NOT to `this.field`:

```java
TransactionDto {
    currency = currency.toUpperCase(); // assigns to the parameter; this.currency is set implicitly after the body
}
```

`this.currency = ...` inside a compact constructor is a compile error. In this exercise you only validate, not normalize.

---

## Exercise 3: Records vs Lombok @Value — side-by-side (~15 min)

**Goal:** Build the same DTO twice and compare the two approaches on verbosity, flexibility, and
Jackson compatibility.

**Task:**  
Create `KycSubjectDto` as:
- A `record` (one declaration line + compact constructor if you add validation).
- A Lombok `@Value` class with the same three fields.

For each, answer:

| Question                                  | record | @Value |
|-------------------------------------------|--------|--------|
| Lines of code (no validation)             |        |        |
| Immutable by language guarantee?          |        |        |
| Supports inheritance (extend a class)?    |        |        |
| Jackson deserialization works out of box? |        |        |
| Can add mutable state?                    |        |        |
| toBuilder() available?                    |        |        |

**Gotcha:** Records implicitly extend `java.lang.Record` and cannot extend any other class. Lombok
`@Value` classes extend nothing by default. Both can implement interfaces; only Lombok lets you
extend a superclass.

---

## Exercise 4: Simulating toBuilder() on records (~10 min)

**Goal:** Understand that records have no built-in copy-with-modification support, and write the
manual workaround.

**Task:**  
Records have no `toBuilder()`. Simulate it by writing a `withAmount(BigDecimal newAmount)` instance
method on `TransactionDto` that returns a new `TransactionDto` with the same `id` and `currency` but
the new amount.

Write the method, create a base transaction, derive a modified copy, and confirm:
- The original is unchanged.
- The new object has the updated amount.
- `equals()` returns `false` between the two (different amounts).

Then write a `withCurrency(String newCurrency)` method the same way. Now imagine this record had
10 fields — write out in a comment what the `withX` method for field 7 would look like, and note how
unpleasant it is compared to Lombok's `toBuilder()`.

**Gotcha:** There is no language shortcut. Each `withX` method must name every other field explicitly.
This verbosity is the main reason to prefer Lombok `@Value` + `@Builder(toBuilder = true)` for DTOs
that need frequent partial-copy patterns (e.g., updating status fields in a pipeline).

---

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

