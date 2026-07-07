---
order: 10
search: false
---

# Records as DTOs Practice

## Domain model

```java
import java.math.BigDecimal;

record TransactionDto(
    String id,
    BigDecimal amount,
    String currency   // ISO-4217, e.g. "USD", "IDR"
) {}

record KycSubjectDto(
    String userId,
    String fullName,
    String nationalId
) {}
```

## Exercise: basic-dto - Basic record DTO + Jackson round-trip

### Goal
Define `TransactionDto` as a record and verify its automatically generated contract.

### Task
1. Declare the record. Create two instances with the same data. Confirm `equals()` returns `true`
   without writing a single method.
2. Call `toString()` on an instance — note the format.
3. Try to assign to a field (`dto.amount = new BigDecimal("200")`) — what does the compiler say?
4. Serialize `TransactionDto` to JSON using Jackson's `ObjectMapper`. Then deserialize it back.
   If deserialization fails, diagnose the error message and apply the minimal fix.

### Gotcha
Jackson 2.11 and earlier cannot map JSON keys to record constructor parameters by name without the `jackson-module-parameter-names` module or a `@JsonCreator`-annotated constructor. Jackson 2.12+ (Spring Boot 2.7+) handles records out of the box — check your version before adding anything.

## Exercise: compact-constructor - Compact constructor validation

### Goal
Add input validation inside the compact constructor.

### Task
Add a compact constructor to `TransactionDto` that:
1. Throws `IllegalArgumentException` if `amount` is null or not positive (≤ 0).
2. Throws `IllegalArgumentException` if `currency` is not exactly 3 uppercase letters.

Write a main method that:
- Creates a valid `TransactionDto` — should succeed.
- Attempts `amount = BigDecimal.ZERO` — should throw.
- Attempts `currency = "usd"` — should throw.

### Gotcha
The compact constructor does NOT redeclare parameters — write `TransactionDto { ... }` with no parameter list. To normalise a value, assign to the *parameter name* directly, for example `currency = currency.toUpperCase();`. Assigning `this.currency = ...` is a compile error. In this exercise you only validate, not normalize.

## Exercise: records-vs-value - Records vs Lombok @Value

### Goal
Build the same DTO twice and compare the two approaches on verbosity, flexibility, and Jackson compatibility.

### Task
Create `KycSubjectDto` as:
- A `record` (one declaration line + compact constructor if you add validation).
- A Lombok `@Value` class with the same three fields.

For each, answer:
- Lines of code (no validation)
- Immutable by language guarantee?
- Supports inheritance (extend a class)?
- Jackson deserialization works out of box?
- Can add mutable state?
- toBuilder() available?

### Gotcha
Records implicitly extend `java.lang.Record` and cannot extend any other class. Lombok
`@Value` classes extend nothing by default. Both can implement interfaces; only Lombok lets you
extend a superclass.

## Exercise: simulating-tobuilder - Simulating toBuilder() on records

### Goal
Understand that records have no built-in copy-with-modification support, and write the manual workaround.

### Task
Records have no `toBuilder()`. Simulate it by writing a `withAmount(BigDecimal newAmount)` instance
method on `TransactionDto` that returns a new `TransactionDto` with the same `id` and `currency` but
the new amount.

Write the method, create a base transaction, derive a modified copy, and confirm:
- The original is unchanged.
- The new object has the updated amount.
- `equals()` returns `false` between the two (different amounts).

Then write a `withCurrency(String newCurrency)` method the same way. Now imagine this record had 10 fields — write out in a comment what the `withX` method for field 7 would look like, and note how unpleasant it is compared to Lombok's `toBuilder()`.

### Gotcha
There is no language shortcut. Each `withX` method must name every other field explicitly.
This verbosity is the main reason to prefer Lombok `@Value` + `@Builder(toBuilder = true)` for DTOs
that need frequent partial-copy patterns.
